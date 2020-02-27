package de.julielab.xml.binary;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.julielab.xml.XmiSplitUtilities;
import de.julielab.xml.XmiSplitter;
import de.julielab.xml.util.XMIBuilderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BinaryJeDISNodeDecoder {
    /**
     * @see {@link BinaryJeDISNodeEncoder#JEDIS_BINARY_MAGIC}
     */
    public static final int JEDIS_BINARY_MAGIC = BinaryJeDISNodeEncoder.JEDIS_BINARY_MAGIC;
    private final static Logger log = LoggerFactory.getLogger(BinaryJeDISNodeDecoder.class);
    private Set<String> annotationLabelsToLoad;
    private boolean shrinkArraysAndListsIfReferenceNotLoaded;
    private int currentXmiId;
    private int currentSofaId;
    private Element currentElement;
    private Set<Integer> xmiIds;

    public BinaryJeDISNodeDecoder(Set<String> annotationLabelsToLoad, boolean shrinkArraysAndListsIfReferenceNotLoaded) {
        this.annotationLabelsToLoad = annotationLabelsToLoad;
        this.shrinkArraysAndListsIfReferenceNotLoaded = shrinkArraysAndListsIfReferenceNotLoaded;
    }

    private void init() {
        currentXmiId = -1;
        currentSofaId = -1;
        currentElement = null;
        xmiIds = new HashSet<>();
    }

    /**
     * <p>Important: The base document module data must be associated with the {@link XmiSplitter#DOCUMENT_MODULE_LABEL} key
     * in <tt>input</tt>.</p>
     *
     * @param input
     * @param ts
     * @param reversedMapping
     * @param mappedFeatures
     * @param namespaceMap
     * @return
     * @throws IOException
     */
    public BinaryDecodingResult decode(Map<String, InputStream> input, TypeSystem ts, Map<Integer, String> reversedMapping, Map<String, Boolean> mappedFeatures, Map<String, String> namespaceMap) throws IOException, XMIBuilderException {
        if (namespaceMap == null || namespaceMap.isEmpty())
            throw new IllegalArgumentException("The XMI namespace map passed to the BinaryJeDISNodeDecoder is empty. This is an error because it is required for decoding of binary annotation modules.");
        // Reset internal states
        init();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // sofa id -> element IDs
        final Multimap<Integer, Integer> sofaElementsMap = HashMultimap.create();
        final BinaryDecodingResult res = new BinaryDecodingResult(baos, sofaElementsMap, shrinkArraysAndListsIfReferenceNotLoaded);
        Map<Integer, Element> elementsWithReferences = new HashMap<>();
        // the label is the fully qualified UIMA type name
        for (String moduleLabel : input.keySet()) {
            final InputStream is = input.get(moduleLabel);
            short header = (short) (((0xff & is.read()) << 8) | (0xff & is.read()));
            if (header != JEDIS_BINARY_MAGIC)
                throw new IOException("Not in JeDIS binary format.");
            final ByteBuffer bb = XmiSplitUtilities.readInputStreamIntoBuffer(is);
            while (bb.position() < bb.limit()) {
                final int binaryTypeId = bb.getInt();
                String prefixedNameType = reversedMapping.get(binaryTypeId);
                if (prefixedNameType == null)
                    throw new XMIBuilderException("The binary element ID " + binaryTypeId + " is not contained in the mapping. It should be the prefixed type name of an annotation element.");
                int elementStart = baos.size();
                // '<type:Token '
                baos.write('<');
                writeWs(prefixedNameType, baos);
                final String[] prefixAndTypeName = prefixedNameType.split(":");
                final String prefix = namespaceMap.get(prefixAndTypeName[0]);
                if (prefix == null)
                    throw new IllegalArgumentException("The namespace map does not include an entry for the prefix '" + prefix + "' which is contained in the decoded data.");
                final String typeName = XmiSplitUtilities.convertNSUri(prefix) + prefixAndTypeName[1];

                currentElement = new Element(typeName);

                final Type type = ts.getType(typeName);
                final byte numAttributes = bb.get();
                currentSofaId = currentXmiId = -1;
                for (int i = 0; i < numAttributes; i++) {
                    readAttribute(bb, typeName, type, mappedFeatures, reversedMapping, ts, res);
                }


                if (currentSofaId != -1 && currentXmiId != -1) {
                    if (moduleLabel.equals(XmiSplitter.DOCUMENT_MODULE_LABEL) && !typeName.equals(CAS.TYPE_NAME_SOFA))
                        sofaElementsMap.put(currentSofaId, currentXmiId);
                    else if (annotationLabelsToLoad.contains(typeName))
                        sofaElementsMap.put(currentSofaId, currentXmiId);
                }
                // 0 = that's it, 1 = there comes more which would then be the values of StringArrays
                final byte finishedIndicator = bb.get();
                if (finishedIndicator == 0) {
                    baos.write('>');
                    writeStringArray(bb, ts.getType(typeName), ts, reversedMapping, mappedFeatures, baos);
                    baos.write('<');
                    baos.write('/');
                    write(prefixedNameType, baos);
                    // Read the last byte that now should indicate finished
                    bb.get();
                } else {
                    baos.write('/');
                }
                baos.write('>');
                int elementEnd = baos.size();
                currentElement.setRange(elementStart, elementEnd);
                currentElement.setArray(type.isArray());
                currentElement.setXmiId(currentXmiId);
                elementsWithReferences.put(currentXmiId, currentElement);
            }
        }
        final HashSet<Integer> seenElementIds = new HashSet<>();
        for (Element e : elementsWithReferences.values()) {
            tagElementsForOmission(e, elementsWithReferences, 0, seenElementIds);
        }
        final List<DataRange> rangesToManipulate;
        Stream<DataRange> dataRangeStream = elementsWithReferences.values().stream()
                // Line up all elements and their attributes. Those are the XMI elements that might need
                // to be manipulated in the BinaryXmiBuilder when building the final XMI.
                .flatMap(e -> Stream.concat(Stream.of(e), e.getAttributes().values().stream()));
        if (shrinkArraysAndListsIfReferenceNotLoaded) {
            dataRangeStream = dataRangeStream
                    // Only collect those elements that actually need to be manipulated.
                    .filter(dr -> dr.isToBeOmitted() || (dr instanceof JeDISAttribute && ((JeDISAttribute) dr).isModified()));
        } else {
            dataRangeStream = dataRangeStream.filter(JeDISAttribute.class::isInstance).filter(a -> ((JeDISAttribute) a).isModified());
        }
        rangesToManipulate = dataRangeStream
                // Sort ascending by begin offset and, for data ranges with the same begin offset, descending
                // be end offset. The idea is that an element should be placed preceeding its attributes.
                // Thus, when the element is to be omitted, we can just jump to the end of the element and
                // ignore its attributes.
                .sorted((dr1, dr2) -> Integer.compare(dr1.getBegin(), dr2.getBegin()) != 0 ? Integer.compare(dr1.getBegin(), dr2.getBegin()) : Integer.compare(dr2.getEnd(), dr1.getEnd()))
                .collect(Collectors.toList());
        res.setXmiPortionsToModify(rangesToManipulate);
        return res;
    }

    /**
     * <p>
     * Follows reference links recursively and marks those attribute for omission whose complete references do not exist
     * in the given data and those elements which represent FSArrays or FSList nodes that also only reference elements
     * which are not present or tagged for omission themselves.
     * </p>
     * <p>
     * For omitted list nodes, the XMI ID references of other elements referencing them are updated to the first non-omitted node.
     * </p>
     *
     * @param e
     * @param elements
     * @param recursionLevel
     * @param seenElementIds
     */
    private void tagElementsForOmission(Element e, Map<Integer, Element> elements, int recursionLevel, Set<Integer> seenElementIds) {
        if (!seenElementIds.add(e.getXmiId()))
            return;
        String intendation = StringUtils.repeat("    ", recursionLevel);
        log.debug(intendation + e.getTypeName() + "(" + e.getXmiId() + ")");
        if (e.isListNode())
            log.debug(intendation + "WARNING: List node");
        boolean omitAttribute = false;
        // Here we follow all reference attributes recursively down in search of elements to be omitted.
        // Thus, after this loop, we are  sure that all elements directly or indirectly referenced from this
        // element have been handeled.
        for (JeDISAttribute a : e.getAttributes().values()) {
            log.debug("    " + intendation + a.getName() + ": " + a.getReferencedIds());
            for (Integer id : a.getReferencedIds()) {
                final Element element = elements.get(id);
                if (element != null) {
                    tagElementsForOmission(element, elements, recursionLevel + 2, seenElementIds);
                    if (!element.isToBeOmitted())
                        a.addFoundReference(id);
                }
            }
            if (a.getFoundReferences().size() < a.getReferencedIds().size())
                a.setModified(true);
            omitAttribute |= a.isToBeOmitted();
        }
        if (shrinkArraysAndListsIfReferenceNotLoaded) {
            // Set FSArrays without existing references and list nodes with missing head reference to be omitted
            e.setToBeOmitted((e.isArray() && omitAttribute) || (e.isListNode() && e.getAttributes().containsKey(CAS.FEATURE_BASE_NAME_HEAD) && e.getAttribute(CAS.FEATURE_BASE_NAME_HEAD).isToBeOmitted()));

            // Handle list references. Repair broken list chains in case intermediate nodes are to be omitted.
            for (JeDISAttribute a : e.getAttributes().values()) {
                for (Integer referencedId : a.getReferencedIds()) {
                    final Element referencedElement = elements.get(referencedId);
                    if (referencedElement != null && referencedElement.isListNode() && referencedElement.isToBeOmitted()) {
                        closeLinkedListGapDueToOmission(e, a.getName(), elements, intendation);
                    }
                }
            }
        }

        log.debug(intendation + e.isToBeOmitted());
    }

    private void closeLinkedListGapDueToOmission(Element e, String attrName, Map<Integer, Element> elements, String intendation) {
        final JeDISAttribute attribute = e.getAttribute(attrName);
        IntStream.Builder idsToAdd = IntStream.builder();
        IntStream.Builder idsToRemove = IntStream.builder();
        for (Integer referencedId : attribute.getReferencedIds()) {
            final Element referencedElement = elements.get(referencedId);
            Element nextNode = null;
            if (referencedElement.isToBeOmitted() && referencedElement.isListNode() && referencedElement.getAttributes().containsKey(CAS.FEATURE_BASE_NAME_TAIL)) {
                idsToRemove.add(referencedId);
                nextNode = elements.get(referencedElement.getAttribute(CAS.FEATURE_BASE_NAME_TAIL).getReferencedIds().iterator().next());
                if (nextNode.isToBeOmitted()) {
                    while (nextNode.isToBeOmitted()) {
                        final Integer nextNodeId = nextNode.getAttribute(CAS.FEATURE_BASE_NAME_TAIL).getReferencedIds().iterator().next();
                        nextNode = elements.get(nextNodeId);
                    }
                }
                if (nextNode != null)
                    idsToAdd.add(nextNode.getXmiId());
            }
            if (nextNode != null) {
                log.debug(intendation + "exchanging " + attrName + " ID " + referencedId + " with " + nextNode.getXmiId());
                // We will exchange at least one ID for this attribute, mark it for modification
                attribute.setModified(true);
            }
        }
        idsToRemove.build().mapToObj(Integer::valueOf).forEach(attribute.getReferencedIds()::remove);
        idsToAdd.build().forEach(i -> {
            attribute.getReferencedIds().add(i);
            attribute.getFoundReferences().add(i);
        });
    }

    private void readAttribute(ByteBuffer bb, String typeName, Type type, Map<String, Boolean> mappedFeatures, Map<Integer, String> mapping, TypeSystem ts, BinaryDecodingResult res) {
        final ByteArrayOutputStream baos = res.getXmiData();
        final int attrNameCode = bb.getInt();
        final String attrName = mapping.get(attrNameCode);
        if (attrName == null) {
            throw new IllegalArgumentException("The binary code integer '" + attrNameCode + "' should encode an attribute name of the UIMA type element '" + typeName + "' but was not found in the mapping. Buffer position: " + bb.position());
        }
        final Feature feature = type.getFeatureByBaseName(attrName);
        int attributeBegin = baos.size();
        // 'attrName="attrvalue" '
        write(attrName, baos);

        JeDISAttribute attribute = null;


        baos.write('=');
        baos.write('"');
        // Handle reference features for non-multivalue types. The type is a token, for example. FSArrays are handled here if the array is not allowed for multiple reference.
        // In such cases, no separate array XML element like <cas:FSArray...> is created but the references are just put into the feature attribute itself like
        // <type:Token ... deprel="25 85" .../>
        if (attrName.equals("xmi:id") || attrName.equals("sofa")) {
            handleXmiIdAndSofaAttributes(bb, attrName, res);
        } else if (XmiSplitUtilities.isReferenceAttribute(type, attrName, ts)) {
            attribute = new JeDISAttribute(attrName);
            handleReferenceAttributes(bb, res, attribute);
            // The next 'else if' handles Array and List types. Here, the type itself is an FSArray, DoubleArray and the feature is "elements" or the feature points to a list node.
        } else if (XmiSplitUtilities.isMultiValuedFeatureAttribute(type, attrName) || feature.getRange().isArray() || XmiSplitUtilities.isListTypeName(feature.getRange().getName())) {
            handleArrayElementFeature(bb, type, attrName, baos);
//            isReferenceAttribute = true;
            // The next 'else if' handles list elements themselves
        } else if (XmiSplitUtilities.isListTypeName(typeName)) {
            handleListTypes(bb, typeName, ts, attrName, mapping, mappedFeatures, baos);
//            isReferenceAttribute = true;
        } else if (feature.getRange().isPrimitive()) {
            handlePrimitiveFeatures(bb, mappedFeatures, mapping, baos, attrName, feature, ts);
        } else throw new IllegalArgumentException("Unhandled attribute '" + attrName + "' of type '" + typeName + "'.");
        baos.write('"');
        baos.write(' ');
        int attributeEnd = baos.size();
        if (attribute != null) {
            attribute.setRange(attributeBegin, attributeEnd);
            currentElement.addAttribute(attribute);
        }
    }

    private void handlePrimitiveFeatures(ByteBuffer bb, Map<String, Boolean> mappedFeatures, Map<Integer, String> mapping, ByteArrayOutputStream ret, String attrName, Feature feature, TypeSystem ts) {
        if (ts.subsumes(ts.getType(CAS.TYPE_NAME_STRING), feature.getRange())) {
            writeStringWithMapping(bb, feature.getName(), ts, mappedFeatures, mapping, ret);
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_FLOAT), feature.getRange())) {
            write(bb.getDouble(), ret);
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_DOUBLE), feature.getRange())) {
            write(bb.getDouble(), ret);
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_SHORT), feature.getRange())) {
            write(bb.getShort(), ret);
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_BYTE), feature.getRange())) {
            write(bb.get(), ret);
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_INTEGER), feature.getRange())) {
            write(bb.getInt(), ret);
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_LONG), feature.getRange())) {
            write(bb.getLong(), ret);
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_BOOLEAN), feature.getRange())) {
            write(bb.get() == 1 ? "true" : "false", ret);
        } else
            throw new IllegalArgumentException("Unhandled feature value decoding of feature " + feature.getName() + " of type " + feature.getRange().getName());
    }

    private void handleArrayElementFeature(ByteBuffer bb, Type type, String attrName, ByteArrayOutputStream ret) {
        final Feature feature = type.getFeatureByBaseName(attrName);
        final String typeName = XmiSplitUtilities.isMultiValuedFeatureAttribute(type, attrName) ? type.getName() : feature.getRange().getName();
        final int length = bb.getInt();
        Consumer<ByteBuffer> componentValueConsumer;
        if (typeName.equals(CAS.TYPE_NAME_DOUBLE_ARRAY) || typeName.equals(CAS.TYPE_NAME_FLOAT_LIST)) {
            componentValueConsumer = buf -> write(buf.getDouble(), ret);
        } else if (typeName.equals(CAS.TYPE_NAME_SHORT_ARRAY)) {
            componentValueConsumer = buf -> write(bb.getShort(), ret);
        } else if (typeName.equals(CAS.TYPE_NAME_BYTE_ARRAY)) {
            componentValueConsumer = buf -> write(bb.get(), ret);
        } else if (typeName.equals(CAS.TYPE_NAME_INTEGER_ARRAY) || typeName.equals(CAS.TYPE_NAME_INTEGER_LIST)) {
            componentValueConsumer = buf -> write(bb.getInt(), ret);
        } else if (typeName.equals(CAS.TYPE_NAME_LONG_ARRAY)) {
            componentValueConsumer = buf -> write(bb.getLong(), ret);
        } else if (typeName.equals(CAS.TYPE_NAME_STRING_ARRAY)) {
            // String arrays embedded into the type element should always be empty (otherwise an error
            // is already raised at encoding)
            componentValueConsumer = buf -> bb.getInt();
        } else
            throw new IllegalArgumentException("Unsupported UIMA array type: " + typeName);
        for (int i = 0; i < length; i++) {
            componentValueConsumer.accept(bb);
            if (i < length - 1)
                ret.write(' ');
        }
    }

    private void handleListTypes(ByteBuffer bb, String typeName, TypeSystem ts, String attrName, Map<Integer, String> mapping, Map<String, Boolean> mappedFeatures, ByteArrayOutputStream ret) {
        // Handle the list node elements themselves. Their features are "head" and "tail", head being
        // the value of the linked list node, tail being a reference to the next node, if it exists.
        // The tail is a xmi:id reference to the next list node
        String baseListType = XmiSplitUtilities.resolveListSubtypes(typeName);
        if (attrName.equals(CAS.FEATURE_BASE_NAME_TAIL)) {
            write(bb.getInt(), ret);
        } else if (attrName.equals(CAS.FEATURE_BASE_NAME_HEAD)) {
            // The head contains the actual value
            if (baseListType.equals(CAS.TYPE_NAME_FLOAT_LIST)) {
                write(bb.getDouble(), ret);
            } else if (baseListType.equals(CAS.TYPE_NAME_FS_LIST)) {
                write(bb.getInt(), ret);
            } else if (baseListType.equals(CAS.TYPE_NAME_INTEGER_LIST)) {
                write(bb.getInt(), ret);
            } else if (baseListType.equals(CAS.TYPE_NAME_STRING_LIST)) {
                writeStringWithMapping(bb, CAS.FEATURE_FULL_NAME_STRING_LIST_HEAD, ts, mappedFeatures, mapping, ret);
            } else throw new IllegalArgumentException("Unhandled UIMA list type: " + typeName);
        }
    }

    private void handleXmiIdAndSofaAttributes(ByteBuffer bb, String attrName, BinaryDecodingResult res) {
        final ByteArrayOutputStream baos = res.getXmiData();
        if (attrName.equals("xmi:id")) {
            currentXmiId = bb.getInt();
            write(currentXmiId, baos);
            xmiIds.add(currentXmiId);
        } else if (attrName.equals("sofa")) {
            currentSofaId = bb.get();
            write(currentSofaId, baos);
        }
    }

    private void handleReferenceAttributes(ByteBuffer bb, BinaryDecodingResult res, JeDISAttribute attribute) {
        final ByteArrayOutputStream baos = res.getXmiData();
        final int numReferences = bb.getInt();
        for (int j = 0; j < numReferences; j++) {
            final int referenceXmiId = bb.getInt();
            attribute.addReferencedId(referenceXmiId);
            // -1 means "null"
            if (referenceXmiId >= 0)
                write(referenceXmiId, baos);
            else write("null", baos);
            if (j < numReferences - 1)
                baos.write(' ');
        }
    }


    private void writeStringArray(ByteBuffer bb, Type type, TypeSystem ts, Map<Integer, String> mapping, Map<String, Boolean> mappedFeatures, ByteArrayOutputStream ret) {
        final int numStringArrayFeatures = bb.getInt();
        for (int i = 0; i < numStringArrayFeatures; i++) {
            final String featureBaseName = mapping.get(bb.getInt());
            final int numValues = bb.getInt();
            for (int j = 0; j < numValues; j++) {
                ret.write('<');
                write(featureBaseName, ret);
                ret.write('>');
                writeStringWithMapping(bb, type.getFeatureByBaseName(featureBaseName).getName(), ts, mappedFeatures, mapping, ret);
                ret.write('<');
                ret.write('/');
                write(featureBaseName, ret);
                ret.write('>');
            }
        }
    }


    private void write(String s, ByteArrayOutputStream baos) {
        baos.writeBytes(s.getBytes(UTF_8));
    }

    private void writeWs(String s, ByteArrayOutputStream baos) {
        baos.writeBytes(s.getBytes(UTF_8));
        baos.write(' ');
    }

    private void write(int i, ByteArrayOutputStream baos) {
        baos.writeBytes(getStringBytes(i));
    }

    private void write(double d, ByteArrayOutputStream baos) {
        baos.writeBytes(getStringBytes(d));
    }

    private void write(long l, ByteArrayOutputStream baos) {
        baos.writeBytes(getStringBytes(l));
    }

    private byte[] getStringBytes(int i) {
        return String.valueOf(i).getBytes(UTF_8);
    }

    private byte[] getStringBytes(long l) {
        return String.valueOf(l).getBytes(UTF_8);
    }

    private byte[] getStringBytes(double d) {
        return String.valueOf(d).getBytes(UTF_8);
    }

    private void writeStringWithMapping(ByteBuffer bb, String fullFeatureName, TypeSystem ts, Map<String, Boolean> mappedFeatures, Map<Integer, String> mapping, ByteArrayOutputStream baos) {
        final Feature feature = ts.getFeatureByFullName(fullFeatureName);
        final boolean featureIsStringArray = ts.subsumes(ts.getType(CAS.TYPE_NAME_STRING_ARRAY), feature.getRange());
        final boolean featureIsStringList = ts.subsumes(ts.getType(CAS.TYPE_NAME_STRING_LIST), feature.getRange());
        // We currently do not handle arrays as lists of strings
        if (!featureIsStringArray && !featureIsStringList && mappedFeatures.get(fullFeatureName)) {
            write(mapping.get(bb.getInt()), baos);
        } else {
            int length = bb.getInt();
            baos.write(bb.array(), bb.position(), length);
            bb.position(bb.position() + length);
        }
    }


}
