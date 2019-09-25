package de.julielab.xml.binary;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.julielab.xml.JeDISVTDGraphNode;
import de.julielab.xml.XmiSplitUtilities;
import de.julielab.xml.util.MissingBinaryMappingException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BinaryJeDISNodeEncoder {
    public static final int JEDIS_BINARY_MAGIC = 0x6195;
    private final static Logger log = LoggerFactory.getLogger(BinaryJeDISNodeEncoder.class);
    private final ByteBuffer bb8;
    private XMLInputFactory inputFactory;
    private byte[] currentNodeData;

    public BinaryJeDISNodeEncoder() {
        // Explicitly use Woodstox because it allows to disable namespace awareness which Aalto does not
        inputFactory = new WstxInputFactory();
        inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        // Effectively remove the size restriction of attribute values
        inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, Integer.MAX_VALUE);
        bb8 = ByteBuffer.allocate(Math.max(Long.SIZE, Double.SIZE));
    }

    public BinaryStorageAnalysisResult findMissingItemsForMapping(Collection<JeDISVTDGraphNode> nodesWithLabel, TypeSystem ts, Map<String, Integer> existingMapping, Map<String, Boolean> existingFeaturesToMap) {
        return findMissingItemsForMapping(nodesWithLabel, ts, existingMapping, existingFeaturesToMap, false);
    }

    public BinaryStorageAnalysisResult findMissingItemsForMapping(Collection<JeDISVTDGraphNode> nodesWithLabel, TypeSystem ts, Map<String, Integer> existingMapping, Map<String, Boolean> existingFeaturesToMap, boolean logMappedFeatures) {

        String currentXmiElementForLogging = null;
        try {
            Map<String, Set<String>> featureValues = new HashMap<>();
            // The names of the types and other XMI-XML elements, e.g. 'types:Token' or 'synonyms'.
            Set<String> xmiTagNames = new HashSet<>();
            Set<String> featureAttributeNames = new HashSet<>();
            Multiset<String> featureOccurrences = HashMultiset.create();
            for (JeDISVTDGraphNode n : nodesWithLabel) {
                currentXmiElementForLogging = n.getModuleXmlData();
                final XMLStreamReader reader = inputFactory.createXMLStreamReader(new ByteArrayInputStream(n.getModuleXmlData().getBytes(UTF_8)), "UTF-8");
                while (reader.hasNext()) {
                    if (reader.next() == XMLStreamConstants.START_ELEMENT) {

                        final QName elementName = reader.getName();
                        xmiTagNames.add(elementName.getLocalPart());

                        for (int attrIndex = 0; attrIndex < reader.getAttributeCount(); attrIndex++) {
                            final String attrName = reader.getAttributeLocalName(attrIndex);
                            featureAttributeNames.add(attrName);

                            final Type nodeType = ts.getType(n.getTypeName());
                            // the uima.cas.NULL does not have an actual Type
                            if (nodeType != null) {
                                final Feature feature = nodeType.getFeatureByBaseName(attrName);
                                // Note that we currently do not handle arrays or lists of strings
                                if (feature != null && ts.subsumes(ts.getType(CAS.TYPE_NAME_STRING), feature.getRange())) {
                                    final String featureName = feature.getName();
                                    // Only record features and feature values if they are either not yet known or already set to be mapped.
                                    // This is important because the expected output of this method are only items that
                                    // are missing from the existing input mapping.
                                    if (!existingFeaturesToMap.containsKey(featureName) || existingFeaturesToMap.get(featureName)) {
                                        final Set<String> values = featureValues.compute(featureName, (k, v) -> v != null ? v : new HashSet<>());
                                        final String value = reader.getAttributeValue(attrIndex);
                                        values.add(value);
                                        featureOccurrences.add(featureName);
                                    }
                                }
                            }
                        }

                    }
                }
            }

            Map<String, Boolean> featuresToMap = new HashMap<>();
            for (String featureName : featureValues.keySet()) {
                if (!existingFeaturesToMap.containsKey(featureName)) {
                    final Set<String> values = featureValues.get(featureName);
                    int numValues = values.size();
                    final int numOccurrences = featureOccurrences.count(featureName);
                    // Note that the value is actually a boolean, saying whether or not to map the feature.
                    featuresToMap.put(featureName, numValues / (double) numOccurrences <= .5);
                }
            }

            if (logMappedFeatures && log.isInfoEnabled()) {
                final List<String> actuallyMappedFeatures = featuresToMap.keySet().stream().filter(featuresToMap::get).collect(Collectors.toList());
                String none = featuresToMap.isEmpty() ? " none" : "";
                log.info("Determined features to map:{}", none);
                for (String featureName : actuallyMappedFeatures) {
                    final Set<String> values = featureValues.get(featureName);
                    int numValues = values.size();
                    log.info(featureName);
                    log.info("    feature occurrences: {}", featureOccurrences.count(featureName));
                    log.info("    number different values: {}", numValues);
                    log.info("    values: {}", values);
                }
            }
            final Stream<String> tagNames = xmiTagNames.stream();
            // Only map those values where the featuresToMap says "true".
            // It is important to get the values of those features that were already before set for mapping
            // and those that have been added in this very run.
            final Stream<String> featureValuesToMap = Stream.concat(featuresToMap.keySet().stream().filter(featuresToMap::get), existingFeaturesToMap.keySet().stream().filter(existingFeaturesToMap::get)).filter(featureValues::containsKey).map(featureValues::get).flatMap(Collection::stream);
            Stream<String> itemsForMapping = Stream.concat(tagNames, featureAttributeNames.stream());
            itemsForMapping = Stream.concat(itemsForMapping, featureValuesToMap);
            // Filter for items that are not yet contained in the mapping
            itemsForMapping = itemsForMapping.filter(item -> !existingMapping.containsKey(item));
            final Set<String> itemsForMappingList = itemsForMapping.collect(Collectors.toSet());
            return new BinaryStorageAnalysisResult(itemsForMappingList, featuresToMap, existingMapping.size());
        } catch (XMLStreamException e) {
            log.error("Could not parse XMI element {}", currentXmiElementForLogging, e);
            throw new IllegalArgumentException(e);
        }
    }

    public Map<String, ByteArrayOutputStream> encode(Collection<JeDISVTDGraphNode> nodesWithLabel, TypeSystem ts, Map<String, Integer> mapping, Map<String, Boolean> mappedFeatures) throws MissingBinaryMappingException {
        String currentXmiElementForLogging = null;
        final Map<String, List<JeDISVTDGraphNode>> nodesByLabel = nodesWithLabel.stream().flatMap(n -> n.getAnnotationModuleLabels().stream().map(l -> new ImmutablePair<>(n, l))).collect(Collectors.groupingBy(Pair::getRight, Collectors.mapping(Pair::getLeft, Collectors.toList())));
        try {
            Map<String, ByteArrayOutputStream> binaryAnnotationModuleData = new HashMap<>();
            for (String label : nodesByLabel.keySet()) {
                ByteArrayOutputStream moduleData = new ByteArrayOutputStream();
                moduleData.write(JEDIS_BINARY_MAGIC >> 8);
                moduleData.write(JEDIS_BINARY_MAGIC);
                final List<JeDISVTDGraphNode> nodesForCurrentLabel = nodesByLabel.get(label);
                final AttributeParser attributeParser = new AttributeParser();
                for (JeDISVTDGraphNode n : nodesForCurrentLabel) {
                    currentXmiElementForLogging = n.getModuleXmlData();
                    final ByteArrayOutputStream nodeData = new ByteArrayOutputStream();

                    currentNodeData = n.getModuleXmlData().getBytes(UTF_8);
                    final XMLStreamReader reader = inputFactory.createXMLStreamReader(new ByteArrayInputStream(currentNodeData), "UTF-8");

                    final XmlStartTag xmlStartTag = attributeParser.parse(currentNodeData);

                    boolean encounteredStart = false;
                    while (reader.hasNext()) {
                        final int eventType = reader.next();
                        if (eventType == XMLStreamConstants.START_ELEMENT && !encounteredStart) {
                            encounteredStart = true;
                            String tagName = reader.getName().getLocalPart();
                            writeInt(mapping.get(tagName), nodeData);
                            nodeData.write(reader.getAttributeCount());
                            for (int i = 0; i < reader.getAttributeCount(); i++) {
                                final String attrName = reader.getAttributeLocalName(i);
                                writeInt(mapping.get(attrName), nodeData);

                                reader.getAttributeValue(i);
                                encodeAttributeValue(reader, i, attrName, xmlStartTag, n, ts, mapping, mappedFeatures, nodeData);
                            }
                        } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                            // Indicate that the element is not yet finished
                            nodeData.write(0);
                            // The string array values are the last thing we want to encode, thus this will be the
                            // last action for the current annotation node
                            encodeEmbeddedStringArrays(reader, mapping, nodeData);
                        }
                    }
                    // Indicate that this node data is finished
                    nodeData.write(1);
                    final byte[] nodeDataBytes = nodeData.toByteArray();
                    moduleData.writeBytes(nodeDataBytes);
                }
                binaryAnnotationModuleData.put(label, moduleData);

            }
            return binaryAnnotationModuleData;

        } catch (XMLStreamException e) {
            log.error("Could not parse XMI element {}", currentXmiElementForLogging, e);
            throw new IllegalArgumentException(e);
        }
    }

    private void encodeEmbeddedStringArrays(XMLStreamReader reader, Map<String, Integer> mapping, ByteArrayOutputStream baos) throws XMLStreamException {
        // First collect the values. Then write them in a compact fashion.
        Map<String, List<String>> valuesByFeature = new HashMap<>();
        while (reader.hasNext()) {
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                String feature = reader.getName().getLocalPart();
                // Forward to the element text which is the feature value string
                reader.next();
                String value = reader.getText();
                valuesByFeature.compute(feature, (k, v) -> v != null ? v : new ArrayList<>()).add(value);
            }
            reader.next();
        }

        // Now write the values in a compact way: Write the encoded feature name once and how many values to expect, then write those values.
        // Write the numbers of string array features that have values
        writeInt(valuesByFeature.keySet().size(), baos);
        for (String feature : valuesByFeature.keySet()) {
            final Collection<String> values = valuesByFeature.get(feature);
            writeInt(mapping.get(feature), baos);
            writeInt(values.size(), baos);
            for (String value : values) {
                writeString(value, baos);
            }
        }
    }

    private void encodeAttributeValue(XMLStreamReader reader, int attributeIndex, String attrName, XmlStartTag xmlStartTag, JeDISVTDGraphNode n, TypeSystem ts, Map<String, Integer> mapping, Map<String, Boolean> mappedFeatures, ByteArrayOutputStream baos) throws MissingBinaryMappingException {
        String typeName = XmiSplitUtilities.resolveListSubtypes(n.getTypeName());
        final Type nodeType = ts.getType(n.getTypeName());
        // the uima.cas.NULL element does not have an actual type
        final Feature feature = nodeType != null ? nodeType.getFeatureByBaseName(attrName) : null;
        if (attrName.equals("xmi:id") || attrName.equals("sofa") || XmiSplitUtilities.isReferenceAttribute(nodeType, attrName, ts)) {
            handleReferenceAttributes(reader, attributeIndex, attrName, baos);
        } else if (XmiSplitUtilities.isListTypeName(typeName)) {
            handleListTypes(reader, attributeIndex, attrName, typeName, mapping, mappedFeatures, baos);
        } else if (XmiSplitUtilities.isMultiValuedFeatureAttribute(nodeType, attrName) || feature.getRange().isArray() || XmiSplitUtilities.isListTypeName(feature.getRange().getName())) {
            handleArrayElementFeature(reader, attributeIndex, attrName, typeName, nodeType, feature, baos, ts);
        } else if (feature.getRange().isPrimitive()) {
            handlePrimitiveFeatures(reader, attributeIndex, xmlStartTag, feature, mapping, mappedFeatures, baos, ts);
        } else
            throw new IllegalArgumentException("Unhandled feature '" + attrName + "' of type '" + n.getTypeName() + "'");
    }

    private void handlePrimitiveFeatures(XMLStreamReader reader, int attributeIndex, XmlStartTag xmlStartTag, Feature feature, Map<String, Integer> mapping, Map<String, Boolean> mappedFeatures, ByteArrayOutputStream baos, TypeSystem ts) throws MissingBinaryMappingException {
        if (ts.subsumes(ts.getType(CAS.TYPE_NAME_STRING), feature.getRange())) {
            writeStringWithMapping(reader, attributeIndex, xmlStartTag, feature.getName(), mappedFeatures, mapping, baos);
        } else {
            final String attributeValue = reader.getAttributeValue(attributeIndex);
            if (ts.subsumes(ts.getType(CAS.TYPE_NAME_FLOAT), feature.getRange())) {
                writeDouble(Double.valueOf(attributeValue), baos);
            } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_DOUBLE), feature.getRange())) {
                writeDouble(Double.valueOf(attributeValue), baos);
            } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_SHORT), feature.getRange())) {
                writeShort(Short.valueOf(attributeValue), baos);
            } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_BYTE), feature.getRange())) {
                baos.write(Byte.valueOf(attributeValue));
            } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_INTEGER), feature.getRange())) {
                writeInt(Integer.valueOf(attributeValue), baos);
            } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_LONG), feature.getRange())) {
                writeLong(Long.valueOf(attributeValue), baos);
            } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_BOOLEAN), feature.getRange())) {
                baos.write(Boolean.valueOf(attributeValue) ? 1 : 0);
            } else
                throw new IllegalArgumentException("Unhandled feature value encoding of feature " + feature.getName() + " of type " + feature.getRange().getName());
        }
    }

    private void handleArrayElementFeature(XMLStreamReader reader, int attributeIndex, String attrName, String typeName, Type nodeType, Feature feature, ByteArrayOutputStream baos, TypeSystem ts) {
        // This branch is entered if we have either an Array type and its elements attribute or
        // some other type with a feature that is multi valued but does not contain references
        // to other types (since the references are handled by the first if)
        final String[] valueSplit = reader.getAttributeValue(attributeIndex).split(" ");
        writeInt(valueSplit.length, baos);
        Stream<String> arrayValues = Stream.of(valueSplit);
        // The list subtype names have already been resolved at the calling method
        String arrayTypeName = XmiSplitUtilities.isMultiValuedFeatureAttribute(nodeType, attrName) ? typeName : feature.getRange().getName();
        Type arrayType = ts.getType(arrayTypeName);
        if (ts.subsumes(ts.getType(CAS.TYPE_NAME_DOUBLE_ARRAY), arrayType) || ts.subsumes(ts.getType(CAS.TYPE_NAME_FLOAT_LIST), arrayType)) {
            arrayValues.mapToDouble(Double::valueOf).forEach(d -> writeDouble(d, baos));
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_SHORT_ARRAY), arrayType)) {
            arrayValues.mapToInt(Integer::valueOf).forEach(i -> writeShort((short) i, baos));
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_BYTE_ARRAY), arrayType)) {
            arrayValues.mapToInt(Integer::valueOf).forEach(baos::write);
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_BOOLEAN_ARRAY), arrayType)) {
            arrayValues.map(s -> Boolean.valueOf(s) ? 1 : 0).forEach(baos::write);
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_INTEGER_ARRAY), arrayType) || ts.subsumes(ts.getType(CAS.TYPE_NAME_INTEGER_LIST), arrayType)) {
            arrayValues.mapToInt(Integer::valueOf).forEach(i -> writeInt(i, baos));
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_LONG_ARRAY), arrayType)) {
            arrayValues.mapToLong(Long::valueOf).forEach(l -> writeLong(l, baos));
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_STRING_ARRAY), arrayType)) {
            // String arrays should only be embedded into the element if they are empty
            if (arrayValues.filter(Predicate.not(String::isBlank)).count() > 0)
                throw new IllegalArgumentException("Unhandled case of a StringArray that is embedded into the type element but is not empty for feature '" + attrName + "' of type '" + typeName + "'.");
            // String of length 0
            writeInt(0, baos);
        } else throw new IllegalArgumentException("Unhandled feature '" + attrName + "' of type '" + typeName + "'.");
    }

    private void handleListTypes(XMLStreamReader reader, int attributeIndex, String attrName, String typeName, Map<String, Integer> mapping, Map<String, Boolean> mappedFeatures, ByteArrayOutputStream baos) throws MissingBinaryMappingException {
        final String attributeValue = reader.getAttributeValue(attributeIndex);
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
                writeStringWithMapping(attributeValue, CAS.FEATURE_FULL_NAME_STRING_LIST_HEAD, mappedFeatures, mapping, baos);
            }
        }
    }

    private void handleReferenceAttributes(XMLStreamReader reader, int attributeIndex, String attrName, ByteArrayOutputStream baos) {
        final String attributeValue = reader.getAttributeValue(attributeIndex);
        if (attrName.equals("xmi:id"))
            writeInt(Integer.valueOf(attributeValue), baos);
        else if (attrName.equals("sofa"))
            baos.write(Byte.valueOf(attributeValue));
        else { // is reference attribute
            if (!attributeValue.isBlank()) {
                final String[] referencedIds = attributeValue.split(" ");
                writeInt(referencedIds.length, baos);
                // -1 means "null"
                Stream.of(referencedIds).map(id -> id.equals("null") ? -1 : Integer.valueOf(id)).forEach(i -> writeInt(i, baos));
            } else {
                // this is an empty reference array
                writeInt(0, baos);
            }
        }
    }

    private void writeStringWithMapping(XMLStreamReader reader, int attributeIndex, XmlStartTag xmlStartTag, String fullFeatureName, Map<String, Boolean> mappedFeatures, Map<String, Integer> mapping, ByteArrayOutputStream baos) throws MissingBinaryMappingException {
        if (mappedFeatures.get(fullFeatureName)) {
            final String value = reader.getAttributeValue(attributeIndex);
            final Integer id = mapping.get(value);
            if (id == null)
                throw new MissingBinaryMappingException(value, "There is no string to ID mapping for the value '" + value + "' of feature '" + fullFeatureName + "'");
            writeInt(id, baos);
        } else {
            final XmlByteSpan attributeValue = xmlStartTag.getAttributes().get(attributeIndex).getAttributeValue();
            writeInt(attributeValue.getLength(), baos);
            writeStringBytes(attributeValue, baos);
        }
    }

    private void writeStringWithMapping(String attributeValue, String fullFeatureName, Map<String, Boolean> mappedFeatures, Map<String, Integer> mapping, ByteArrayOutputStream baos) throws MissingBinaryMappingException {
        if (mappedFeatures.get(fullFeatureName)) {
            final Integer id = mapping.get(attributeValue);
            if (id == null)
                throw new MissingBinaryMappingException(attributeValue, "There is no string to ID mapping for the value '" + attributeValue + "' of feature '" + fullFeatureName + "'");
            writeInt(id, baos);
        } else {
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


    private void writeLong(long l, ByteArrayOutputStream baos) {
        bb8.position(0);
        bb8.putLong(l);
        baos.write(bb8.array(), 0, Long.BYTES);
    }

    private void writeString(String s, ByteArrayOutputStream baos) {

        final byte[] bytes = s.getBytes(UTF_8);
        writeInt(bytes.length, baos);
        baos.writeBytes(bytes);
    }

    private void writeStringBytes(XmlByteSpan xmlByteSpan, ByteArrayOutputStream baos) {
        for (int i = xmlByteSpan.getStart(); i < xmlByteSpan.getEnd(); i++) {
            final byte b = xmlByteSpan.getXmlData()[i];
            baos.write(b);
        }
    }
}