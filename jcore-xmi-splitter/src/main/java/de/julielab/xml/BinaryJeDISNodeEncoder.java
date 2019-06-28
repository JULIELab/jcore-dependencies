package de.julielab.xml;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.isFSArray;
import static de.julielab.xml.XmiSplitUtilities.isPrimitive;

public class BinaryJeDISNodeEncoder {
    private final static Logger log = LoggerFactory.getLogger(BinaryJeDISNodeEncoder.class);
    private final ByteBuffer bb8;
    private final VTDGen vg;

    public BinaryJeDISNodeEncoder() {
        vg = new VTDGen();
        bb8 = ByteBuffer.allocate(8);
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
        try {
            for (JeDISVTDGraphNode n : nodesWithLabel) {
                currentXmiElementForLogging = n.getModuleXmlData();

                vg.setDoc_BR(n.getModuleXmlData().getBytes(StandardCharsets.UTF_8));
                vg.parse(false);
                final VTDNav vn = vg.getNav();
                int index = 0;
                String attrName = null;
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String lastAttributeName = null;
                while (index < vn.getTokenCount()) {
                    if (vn.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG)
                        writeInt(mapping.get(vn.toString(index)), baos);
                    if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                        lastAttributeName = vn.toString(index);
                        writeInt(mapping.get(lastAttributeName), baos);
                    }
                    if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_VAL) {
                        final Feature feature = ts.getType(n.getTypeName()).getFeatureByBaseName(attrName);
                        final String attributeValue = vn.toString(index);
                        if (feature != null && feature.getRange().getName().equals("uima.cas.String")) {
                            final Integer value = mapping.get(attributeValue);
                            // Not all values are mapping, thus the check
                            if (value != null)
                                writeInt(value, baos);
                        } else {
                            if (lastAttributeName.equals("xmi:id") || lastAttributeName.equals("sofa") || isReferenceAttribute(ts.getType(n.getTypeName()), lastAttributeName)) {
                                //writeInt(lastAttributeName.length(), bao);
                                //unchangedAttributesStreamBuilder.accept(lastAttributeName);
                                //unchangedAttributesStreamBuilder.accept(attributeValue);
                            }
                            // arrays
                            // non-string primitive features
                        }
                    }
                    ++index;
                }

            }

            return null;

        } catch (ParseException | NavException e) {
            log.error("Could not parse XMI element {}", currentXmiElementForLogging, e);
            throw new IllegalArgumentException(e);
        }
    }

    private void writeInt(int i, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putInt(i);
        baos.write(bb8.array(), 0, 4);
    }

    private void writeDouble(double d, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putDouble(d);
        baos.writeBytes(bb8.array());
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