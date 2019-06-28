package de.julielab.xml;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.isFSArray;
import static de.julielab.xml.XmiSplitUtilities.isPrimitive;

public class BinaryJeDISNodeEncoder {
    private final static Logger log = LoggerFactory.getLogger(BinaryJeDISNodeEncoder.class);
    private final ByteBuffer bb8;
    private final VTDGen vg;

    public BinaryJeDISNodeEncoder() {
        vg = new VTDGen();
        bb8 = ByteBuffer.allocate(Math.max(Long.SIZE, Double.SIZE));
    }

    public List<String> findMissingItemsForMapping(Collection<JeDISVTDGraphNode> nodesWithLabel, TypeSystem ts, Map<String, Integer> existingMapping) {

        String currentXmiElementForLogging = null;
        try {
            Map<String, Set<String>> featureValues = new HashMap<>();
            // The names of the types and other XMI-XML elements, e.g. 'types:Token' or 'synonyms'.
            Set<String> xmiTagNames = new HashSet<>();
            Multiset<String> featureOccurrences = HashMultiset.create();
            for (JeDISVTDGraphNode n : nodesWithLabel) {
                currentXmiElementForLogging = n.getModuleXmlData();
                System.out.println(currentXmiElementForLogging);
                vg.setDoc_BR(n.getModuleXmlData().getBytes(StandardCharsets.UTF_8));
                vg.parse(false);
                final VTDNav vn = vg.getNav();
                int index = 0;
                String attrName = null;
                while (index < vn.getTokenCount()) {
                    if (vn.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG)
                        xmiTagNames.add(vn.toString(index));
                    if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME)
                        attrName = vn.toString(index);
                    if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_VAL) {
                        final Feature feature = ts.getType(n.getTypeName()).getFeatureByBaseName(attrName);
                        if (feature != null && feature.getRange().getName().equals("uima.cas.String")) {
                            final String featureName = feature.getName();
                            final Set<String> values = featureValues.compute(featureName, (k, v) -> v != null ? v : new HashSet<>());
                            final String value = vn.toString(index);
                            values.add(value);
                            featureOccurrences.add(featureName);
                        }
                    }
                    ++index;
                }

            }

            List<String> featuresToMap = new ArrayList<>();
            for (String featureName : featureValues.keySet()) {
                final Set<String> values = featureValues.get(featureName);
                int numValues = values.size();
                final int numOccurrences = featureOccurrences.count(featureName);
                if (numValues / (double) numOccurrences < .5)
                    featuresToMap.add(featureName);

            }

            final Stream<String> tagNames = xmiTagNames.stream();
            final Stream<String> featureAttributeNames = featuresToMap.stream().map(ts::getFeatureByFullName).map(Feature::getShortName);
            final Stream<String> featureValuesToMap = featuresToMap.stream().map(featureValues::get).flatMap(Collection::stream);
            Stream<String> itemsForMapping = Stream.concat(tagNames, featureAttributeNames);
            itemsForMapping = Stream.concat(itemsForMapping, featureValuesToMap);
            itemsForMapping = Stream.concat(itemsForMapping, Stream.of("sofa", "xmi:id"));
            // Filter for items that are not yet contained in the mapping
            itemsForMapping = itemsForMapping.filter(item -> !existingMapping.containsKey(item));
            final List<String> itemsForMappingList = itemsForMapping.collect(Collectors.toList());
            return itemsForMappingList;
            //if (!itemsForMappingList.isEmpty()) {
            // lock table
            // update mapping
            // filter again for items in the new mapping
            // add new items
            // unlock table
            //  }

        } catch (ParseException | NavException e) {
            log.error("Could not parse XMI element {}", currentXmiElementForLogging, e);
            throw new IllegalArgumentException(e);
        }
    }

    public Map<String, ByteArrayOutputStream> encode(Collection<JeDISVTDGraphNode> nodesWithLabel, TypeSystem ts, Map<String, Integer> mapping) {
        String currentXmiElementForLogging = null;
        final Map<String, List<JeDISVTDGraphNode>> nodesByLabel = nodesWithLabel.stream().flatMap(n -> n.getAnnotationModuleLabels().stream().map(l -> new ImmutablePair<>(n, l))).collect(Collectors.groupingBy(Pair::getRight, Collectors.mapping(Pair::getLeft, Collectors.toList())));
        try {
            Map<String, ByteArrayOutputStream> binaryAnnotationModuleData = new HashMap<>();
            for (String label : nodesByLabel.keySet()) {
                final List<JeDISVTDGraphNode> nodesForCurrentLabel = nodesByLabel.get(label);
                for (JeDISVTDGraphNode n : nodesForCurrentLabel) {
                    currentXmiElementForLogging = n.getModuleXmlData();

                    vg.setDoc_BR(n.getModuleXmlData().getBytes(StandardCharsets.UTF_8));
                    vg.parse(false);
                    final VTDNav vn = vg.getNav();
                    int index = 0;
                    String attrName = null;
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    binaryAnnotationModuleData.put(label, baos);
                    while (index < vn.getTokenCount()) {
                        if (vn.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG && attrName == null)
                            writeInt(mapping.get(vn.toString(index)), baos);
                        if (vn.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG && attrName != null)
                            // The string array values are the last thing we want to encode, thus this will be the
                            // last action for the current annotation node
                            encodeEmbeddedStringArrays(vn,index,  mapping, baos);
                        if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                            attrName = vn.toString(index);
                            writeInt(mapping.get(attrName), baos);
                        }
                        if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_VAL) {
                            encodeAttributeValue(index, attrName, n, ts, mapping, baos, vn);
                        }
                        ++index;
                    }
                }

            }

            return binaryAnnotationModuleData;

        } catch (ParseException | NavException e) {
            log.error("Could not parse XMI element {}", currentXmiElementForLogging, e);
            throw new IllegalArgumentException(e);
        }
    }

    private void encodeEmbeddedStringArrays(VTDNav vn, int index, Map<String, Integer> mapping, ByteArrayOutputStream baos) throws NavException {
        // First collect the values. Then write them in a compact fashion.
        Multimap<String, String> valuesByFeature = HashMultimap.create();
        while (index < vn.getTokenCount()) {
            if (vn.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG) {
               String feature = vn.toString(index);
                ++index;
                String value = vn.toString();
                valuesByFeature.put(feature, value);
            }
            ++index;
        }
        // Now write the values in a compact way: Write the encoded feature name once and how many values to expect, the write those values.
        for (String feature : valuesByFeature.keySet()) {
            final Collection<String> values = valuesByFeature.get(feature);
            writeInt(mapping.get(feature), baos);
            writeInt(values.size(), baos);
            for (String value : values) {
                writeInt(value.length(), baos);
                baos.writeBytes(value.getBytes(StandardCharsets.UTF_8));
            }
        }

    }

    private void encodeAttributeValue(int index, String attrName, JeDISVTDGraphNode n, TypeSystem ts, Map<String, Integer> mapping, ByteArrayOutputStream baos, VTDNav vn) throws NavException {
        final Feature feature = ts.getType(n.getTypeName()).getFeatureByBaseName(attrName);
        final String attributeValue = vn.toString(index);
        if (feature != null && feature.getRange().getName().equals("uima.cas.String")) {
            final Integer value = mapping.get(attributeValue);
            // Not all values are mapping, thus the check
            if (value != null)
                writeInt(value, baos);
        } else {
            if (attrName.equals("xmi:id") || attrName.equals("sofa") || isReferenceAttribute(ts.getType(n.getTypeName()), attrName)) {
                writeInt(mapping.get(attrName), baos);
                if (attrName.equals("xmi:id"))
                    writeInt(Integer.valueOf(attributeValue), baos);
                else if (attrName.equals("sofa"))
                    baos.write(Byte.valueOf(attributeValue));
                else { // is reference attribute
                    final String[] referencedIds = attributeValue.split(" ");
                    writeInt(referencedIds.length, baos);
                    Stream.of(referencedIds).map(Integer::valueOf).forEach(i -> writeInt(i, baos));
                }
            } else if (feature.getRange().isArray()) {
                Stream<String> arrayValues = Stream.of(attributeValue.split(" "));
                if (feature.getRange().getComponentType().getName().equals("uima.cas.Double")) {
                    arrayValues.mapToDouble(Double::valueOf).forEach(d -> writeDouble(d, baos));
                }
                if (feature.getRange().getComponentType().getName().equals("uima.cas.Short")) {
                    arrayValues.mapToInt(Integer::valueOf).forEach(i -> writeShort((short) i, baos));
                }
                if (feature.getRange().getComponentType().getName().equals("uima.cas.Byte")) {
                    arrayValues.mapToInt(Integer::valueOf).forEach(baos::write);
                }
                if (feature.getRange().getComponentType().getName().equals("uima.cas.Integer")) {
                    arrayValues.mapToDouble(Double::valueOf).forEach(i -> writeDouble(i, baos));
                }
                if (feature.getRange().getComponentType().getName().equals("uima.cas.Long")) {
                    arrayValues.mapToLong(Long::valueOf).forEach(l -> writeLong(l, baos));
                }
            } else if (feature.getRange().isPrimitive()) {
                if (feature.getRange().getName().equals("uima.cas.Double")) {
                    writeDouble(Double.valueOf(attributeValue), baos);
                }
                if (feature.getRange().getName().equals("uima.cas.Short")) {
                    writeShort(Short.valueOf(attributeValue), baos);
                }
                if (feature.getRange().getName().equals("uima.cas.Byte")) {
                    baos.write(Byte.valueOf(attributeValue));
                }
                if (feature.getRange().getName().equals("uima.cas.Integer")) {
                    writeDouble(Integer.valueOf(attributeValue), baos);
                }
                if (feature.getRange().getName().equals("uima.cas.Long")) {
                    writeLong(Long.valueOf(attributeValue), baos);
                }
            }
        }
    }

    private void writeInt(int i, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putInt(i);
        baos.write(bb8.array(), 0, Integer.SIZE);
    }

    private void writeDouble(double d, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putDouble(d);
        baos.write(bb8.array(), 0, Double.SIZE);
    }

    private void writeShort(short s, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putShort(s);
        baos.write(bb8.array(), 0, Short.SIZE);
    }

    private void writeFloat(short f, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putFloat(f);
        baos.write(bb8.array(), 0, Float.SIZE);
    }

    private void writeLong(long l, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putLong(l);
        baos.write(bb8.array(), 0, Long.SIZE);
    }

    private boolean isReferenceAttribute(Type annotationType, String attributeName) {
        if (isFSArray(annotationType)) {
            return attributeName.equals("elements");
        } else {
            Type featureType = annotationType.getFeatureByBaseName(attributeName).getRange();
            if ((featureType.isArray() || !isPrimitive(featureType)) && (featureType.getComponentType() == null || !featureType.getComponentType().isPrimitive())) {
                return true;
            }
        }
        return false;
    }
}