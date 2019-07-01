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
import org.apache.uima.cas.CAS;
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

import static de.julielab.xml.XmiSplitUtilities.isPrimitive;

public class BinaryJeDISNodeEncoder {
    private final static Logger log = LoggerFactory.getLogger(BinaryJeDISNodeEncoder.class);
    private final ByteBuffer bb8;
    private final VTDGen vg;

    public BinaryJeDISNodeEncoder() {
        vg = new VTDGen();
        bb8 = ByteBuffer.allocate(Math.max(Long.SIZE, Double.SIZE));
    }

    public BinaryStorageAnalysisResult findMissingItemsForMapping(Collection<JeDISVTDGraphNode> nodesWithLabel, TypeSystem ts, Map<String, Integer> existingMapping) {

        String currentXmiElementForLogging = null;
        try {
            Map<String, Set<String>> featureValues = new HashMap<>();
            // The names of the types and other XMI-XML elements, e.g. 'types:Token' or 'synonyms'.
            Set<String> xmiTagNames = new HashSet<>();
            Set<String> featureAttributeNames = new HashSet<>();
            Multiset<String> featureOccurrences = HashMultiset.create();
            for (JeDISVTDGraphNode n : nodesWithLabel) {
                currentXmiElementForLogging = n.getModuleXmlData();
                vg.setDoc_BR(n.getModuleXmlData().getBytes(StandardCharsets.UTF_8));
                vg.parse(false);
                final VTDNav vn = vg.getNav();
                int index = 0;
                String attrName = null;
                while (index < vn.getTokenCount()) {
                    if (vn.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG)
                        xmiTagNames.add(vn.toString(index));
                    if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                        attrName = vn.toString(index);
                        featureAttributeNames.add(attrName);
                    }
                    if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_VAL) {
                        final Type nodeType = ts.getType(n.getTypeName());
                        // the uima.cas.NULL does not have an actual Type
                        if (nodeType != null) {
                            final Feature feature = nodeType.getFeatureByBaseName(attrName);
                            if (feature != null && feature.getRange().getName().equals("uima.cas.String")) {
                                final String featureName = feature.getName();
                                final Set<String> values = featureValues.compute(featureName, (k, v) -> v != null ? v : new HashSet<>());
                                final String value = vn.toString(index);
                                values.add(value);
                                featureOccurrences.add(featureName);
                            }
                        }
                    }
                    ++index;
                }

            }

            Set<String> featuresToMap = new HashSet<>();
            for (String featureName : featureValues.keySet()) {
                final Set<String> values = featureValues.get(featureName);
                int numValues = values.size();
                final int numOccurrences = featureOccurrences.count(featureName);
                if (numValues / (double) numOccurrences < .5)
                    featuresToMap.add(featureName);

            }

            final Stream<String> tagNames = xmiTagNames.stream();
            final Stream<String> featureValuesToMap = featuresToMap.stream().map(featureValues::get).flatMap(Collection::stream);
            Stream<String> itemsForMapping = Stream.concat(tagNames, featureAttributeNames.stream());
            itemsForMapping = Stream.concat(itemsForMapping, featureValuesToMap);
            // Filter for items that are not yet contained in the mapping
            itemsForMapping = itemsForMapping.filter(item -> !existingMapping.containsKey(item));
            final List<String> itemsForMappingList = itemsForMapping.collect(Collectors.toList());
            return new BinaryStorageAnalysisResult(itemsForMappingList, featuresToMap);
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

    public Map<String, ByteArrayOutputStream> encode(Collection<JeDISVTDGraphNode> nodesWithLabel, TypeSystem ts, Map<String, Integer> mapping, Set<String> mappedFeatures) {
        String currentXmiElementForLogging = null;
        final Map<String, List<JeDISVTDGraphNode>> nodesByLabel = nodesWithLabel.stream().flatMap(n -> n.getAnnotationModuleLabels().stream().map(l -> new ImmutablePair<>(n, l))).collect(Collectors.groupingBy(Pair::getRight, Collectors.mapping(Pair::getLeft, Collectors.toList())));
        try {
            Map<String, ByteArrayOutputStream> binaryAnnotationModuleData = new HashMap<>();
            for (String label : nodesByLabel.keySet()) {
                ByteArrayOutputStream moduleData = new ByteArrayOutputStream();
                final List<JeDISVTDGraphNode> nodesForCurrentLabel = nodesByLabel.get(label);
                for (JeDISVTDGraphNode n : nodesForCurrentLabel) {
                    currentXmiElementForLogging = n.getModuleXmlData();
                    vg.setDoc_BR(n.getModuleXmlData().getBytes(StandardCharsets.UTF_8));
                    vg.parse(false);
                    final VTDNav vn = vg.getNav();
                    int index = 0;
                    String attrName = null;
                    final ByteArrayOutputStream nodeData = new ByteArrayOutputStream();
                    while (index < vn.getTokenCount()) {
                        if (vn.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG && attrName == null) {
                            writeInt(mapping.get(vn.toString(index)), nodeData);
                            nodeData.write(vn.getAttrCount());
                        }
                        if (vn.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG && attrName != null) {
                            // Indicate that the element is not yet finished
                            nodeData.write(0);
                            // The string array values are the last thing we want to encode, thus this will be the
                            // last action for the current annotation node
                            index = encodeEmbeddedStringArrays(vn, index, mapping, nodeData);
                        }
                        if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                            attrName = vn.toString(index);
                            writeInt(mapping.get(attrName), nodeData);
                        }
                        if (vn.getTokenType(index) == VTDNav.TOKEN_ATTR_VAL) {
                            encodeAttributeValue(index, attrName, n, ts, mapping, mappedFeatures, nodeData, vn);
                        }
                        ++index;
                    }
                    // Indicate that this node data is finished
                    nodeData.write(1);
                    final byte[] nodeDataBytes = nodeData.toByteArray();
                    moduleData.writeBytes(nodeDataBytes);
                }
                binaryAnnotationModuleData.put(label, moduleData);

            }

            return binaryAnnotationModuleData;

        } catch (ParseException | NavException e) {
            log.error("Could not parse XMI element {}", currentXmiElementForLogging, e);
            throw new IllegalArgumentException(e);
        }
    }

    private int encodeEmbeddedStringArrays(VTDNav vn, int index, Map<String, Integer> mapping, ByteArrayOutputStream baos) throws NavException {
        // First collect the values. Then write them in a compact fashion.
        Multimap<String, String> valuesByFeature = HashMultimap.create();
        while (index < vn.getTokenCount()) {
            if (vn.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG) {
                String feature = vn.toString(index);
                ++index;
                String value = vn.toString(index);
                valuesByFeature.put(feature, value);
            }
            ++index;
        }
        // Now write the values in a compact way: Write the encoded feature name once and how many values to expect, the write those values.
        // Write the numbers of string array features that have values
        writeInt(valuesByFeature.keySet().size(), baos);
        for (String feature : valuesByFeature.keySet()) {
            final Collection<String> values = valuesByFeature.get(feature);
            writeInt(mapping.get(feature), baos);
            writeInt(values.size(), baos);
            for (String value : values) {
                writeInt(value.length(), baos);
                baos.writeBytes(value.getBytes(StandardCharsets.UTF_8));
            }
        }
        return index;
    }

    private void encodeAttributeValue(int index, String attrName, JeDISVTDGraphNode n, TypeSystem ts, Map<String, Integer> mapping, Set<String> mappedFeatures, ByteArrayOutputStream baos, VTDNav vn) throws NavException {
        String typeName = XmiSplitUtilities.resolveListSubtypes(n.getTypeName());
        final Type nodeType = ts.getType(n.getTypeName());
        // the uima.cas.NULL element does not have an actual type
        final Feature feature = nodeType != null ? nodeType.getFeatureByBaseName(attrName) : null;
        final String attributeValue = vn.toString(index);
        if (attrName.equals("xmi:id") || attrName.equals("sofa") || XmiSplitUtilities.isReferenceAttribute(nodeType, attrName, ts)) {
            handleReferenceAttributes(attrName, attributeValue, baos);
        } else if (XmiSplitUtilities.isListTypeName(typeName)) {
            handleListTypes(attrName, attributeValue, typeName, mapping, mappedFeatures, baos);
        } else if (XmiSplitUtilities.isMultiValuedFeatureAttribute(nodeType, attrName) || feature.getRange().isArray() || XmiSplitUtilities.isListTypeName(feature.getRange().getName())) {
            handleArrayElementFeature(attrName, attributeValue, typeName, nodeType, feature, baos);
        } else if (feature.getRange().isPrimitive()) {
            handlePrimitiveFeatures(attrName, attributeValue, feature, mapping, mappedFeatures, baos);
        } else
            throw new IllegalArgumentException("Unhandled feature '" + attrName + "' of type '" + n.getTypeName() + "'");
    }

    private void handlePrimitiveFeatures(String attrName, String attributeValue, Feature feature, Map<String, Integer> mapping, Set<String> mappedFeatures, ByteArrayOutputStream baos) {
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_STRING)) {
            writeStringWithMapping(attributeValue, attrName, mappedFeatures, mapping, baos);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_FLOAT)) {
            writeDouble(Double.valueOf(attributeValue), baos);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_DOUBLE)) {
            writeDouble(Double.valueOf(attributeValue), baos);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_SHORT)) {
            writeShort(Short.valueOf(attributeValue), baos);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_BYTE)) {
            baos.write(Byte.valueOf(attributeValue));
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_INTEGER)) {
            writeInt(Integer.valueOf(attributeValue), baos);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_LONG)) {
            writeLong(Long.valueOf(attributeValue), baos);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_BOOLEAN)) {
            baos.write(Boolean.valueOf(attributeValue) ? 1 : 0);
        }
    }

    private void handleArrayElementFeature(String attrName, String attributeValue, String typeName, Type nodeType, Feature feature, ByteArrayOutputStream baos) {
        // This branch is entered if we have either an Array type and its elements attribute or
        // some other type with a feature that is multi valued but does not contain references
        // to other types (since the references are handled by the first if)
        final String[] valueSplit = attributeValue.split(" ");
        writeInt(valueSplit.length, baos);
        Stream<String> arrayValues = Stream.of(valueSplit);
        // The list subtype names have already been resolved at the calling method
        String arrayTypeName = XmiSplitUtilities.isMultiValuedFeatureAttribute(nodeType, attrName) ? typeName : feature.getRange().getName();
        if (arrayTypeName.equals(CAS.TYPE_NAME_DOUBLE_ARRAY) || arrayTypeName.equals(CAS.TYPE_NAME_FLOAT_LIST)) {
            arrayValues.mapToDouble(Double::valueOf).forEach(d -> writeDouble(d, baos));
        } else if (arrayTypeName.equals(CAS.TYPE_NAME_SHORT_ARRAY)) {
            arrayValues.mapToInt(Integer::valueOf).forEach(i -> writeShort((short) i, baos));
        } else if (arrayTypeName.equals(CAS.TYPE_NAME_BYTE_ARRAY)) {
            arrayValues.mapToInt(Integer::valueOf).forEach(baos::write);
        } else if (arrayTypeName.equals(CAS.TYPE_NAME_BOOLEAN_ARRAY)) {
            arrayValues.map(s -> Boolean.valueOf(s) ? 1 : 0).forEach(baos::write);
        } else if (arrayTypeName.equals(CAS.TYPE_NAME_INTEGER_ARRAY) || arrayTypeName.equals(CAS.TYPE_NAME_INTEGER_LIST)) {
            arrayValues.mapToInt(Integer::valueOf).forEach(i -> writeInt(i, baos));
        } else if (arrayTypeName.equals(CAS.TYPE_NAME_LONG_ARRAY)) {
            arrayValues.mapToLong(Long::valueOf).forEach(l -> writeLong(l, baos));
        } else throw new IllegalArgumentException("Unhandled feature '" + attrName + "' of type '" + typeName + "'.");
    }

    private void handleListTypes(String attrName, String attributeValue, String typeName, Map<String, Integer> mapping, Set<String> mappedFeatures, ByteArrayOutputStream baos) {
        // Handle the list node elements themselves. Their features are "head" and "tail", head being
        // the value of the linked list node, tail being a reference to the next node, if it exists.
        // The tail is a xmi:id reference to the next list node
        if (attrName.equals(CAS.FEATURE_BASE_NAME_TAIL)) {
            writeInt(Integer.valueOf(attributeValue), baos);
        } else if (attrName.equals(CAS.FEATURE_BASE_NAME_HEAD)) {
            // The head contains the actual value
            if (typeName.equals(CAS.TYPE_NAME_FLOAT_LIST)) {
                writeDouble(Double.valueOf(attributeValue), baos);
            }
            if (typeName.equals(CAS.TYPE_NAME_FS_LIST)) {
                writeInt(Integer.valueOf(attributeValue), baos);
            }
            if (typeName.equals(CAS.TYPE_NAME_INTEGER_LIST)) {
                writeInt(Integer.valueOf(attributeValue), baos);
            }
            if (typeName.equals(CAS.TYPE_NAME_STRING_LIST)) {
                writeStringWithMapping(attributeValue, attrName, mappedFeatures, mapping, baos);
            }
        }
    }

    private void handleReferenceAttributes(String attrName, String attributeValue, ByteArrayOutputStream baos) {
        if (attrName.equals("xmi:id"))
            writeInt(Integer.valueOf(attributeValue), baos);
        else if (attrName.equals("sofa"))
            baos.write(Byte.valueOf(attributeValue));
        else { // is reference attribute
            final String[] referencedIds = attributeValue.split(" ");
            writeInt(referencedIds.length, baos);
            // -1 means "null"
            Stream.of(referencedIds).map(id -> id.equals("null") ? -1 : Integer.valueOf(id)).forEach(i -> writeInt(i, baos));
        }
    }

    private void writeStringWithMapping(String attributeValue, String attrName, Set<String> mappedFeatures, Map<String, Integer> mapping, ByteArrayOutputStream baos) {
        if (mappedFeatures.contains(attrName)) {
            writeInt(mapping.get(attributeValue), baos);
        } else {
            writeInt(attributeValue.length(), baos);
            writeString(attributeValue, baos);
        }
    }

    private void writeInt(int i, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putInt(i);
        baos.write(bb8.array(), 0, Integer.BYTES);
    }

    private void writeDouble(double d, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putDouble(d);
        baos.write(bb8.array(), 0, Double.BYTES);
    }

    private void writeShort(short s, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putShort(s);
        baos.write(bb8.array(), 0, Short.BYTES);
    }

    private void writeFloat(short f, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putFloat(f);
        baos.write(bb8.array(), 0, Float.BYTES);
    }

    private void writeLong(long l, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putLong(l);
        baos.write(bb8.array(), 0, Long.BYTES);
    }

    private void writeString(String s, ByteArrayOutputStream baos) {
        baos.writeBytes(s.getBytes(StandardCharsets.UTF_8));
    }


}