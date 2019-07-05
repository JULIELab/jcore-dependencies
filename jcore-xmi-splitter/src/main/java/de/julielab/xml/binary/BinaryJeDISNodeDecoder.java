package de.julielab.xml.binary;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.julielab.xml.XmiSplitUtilities;
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

import static java.nio.charset.StandardCharsets.UTF_8;

public class BinaryJeDISNodeDecoder {
    private final static Logger log = LoggerFactory.getLogger(BinaryJeDISNodeDecoder.class);
    private Set<String> annotationLabelsToLoad;
    private int currentXmiId;
    private int currentSofaId;
    private Element currentElement;
    private Set<Integer> xmiIds;

    public BinaryJeDISNodeDecoder(Set<String> annotationLabelsToLoad) {
        this.annotationLabelsToLoad = annotationLabelsToLoad;
    }

    private void init() {
        currentXmiId = -1;
        currentSofaId = -1;
        currentElement = null;
        xmiIds = new HashSet<>();
    }

    public BinaryDecodingResult decode(Collection<InputStream> input, TypeSystem ts, Map<Integer, String> mapping, Set<String> mappedFeatures, Map<String, String> namespaceMap) {
        // Reset internal states
        init();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // sofa id -> element IDs
        final Multimap<Integer, Integer> sofaElementsMap = HashMultimap.create();
        final BinaryDecodingResult res = new BinaryDecodingResult(baos, sofaElementsMap);
        try {
            Map<Integer, Element> elementsWithReferences = new HashMap<>();
            // the label is the fully qualified UIMA type name
            for (InputStream is : input) {
                final ByteBuffer bb = XmiSplitUtilities.readInputStreamIntoBuffer(is);
                while (bb.position() < bb.limit()) {
                    String prefixedNameType = mapping.get(bb.getInt());
                    int elementStart = baos.size();
                    // '<type:Token '
                    baos.write('<');
                    writeWs(prefixedNameType, baos);
                    final String[] prefixAndTypeName = prefixedNameType.split(":");
                    final String typeName = XmiSplitUtilities.convertNSUri(namespaceMap.get(prefixAndTypeName[0])) + prefixAndTypeName[1];

                    currentElement = new Element(typeName);

                    final Type type = ts.getType(typeName);
                    final byte numAttributes = bb.get();
                    currentSofaId = currentXmiId = -1;
                    for (int i = 0; i < numAttributes; i++) {
                        readAttribute(bb, typeName, type, mappedFeatures, mapping, ts, res);
                    }


                    if (currentSofaId != -1 && currentXmiId != -1) {
                        if (annotationLabelsToLoad.contains(typeName))
                            sofaElementsMap.put(currentSofaId, currentXmiId);
                    }
                    // 0 = that's it, 1 = there comes more which would then be the values of StringArrays
                    final byte finishedIndicator = bb.get();
                    if (finishedIndicator == 0) {
                        baos.write('>');
                        writeStringArray(bb, mapping, mappedFeatures, baos);
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

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        for (Attribute a : e.getAttributes().values()) {
            log.debug("    " + intendation + a.getName() + ": " + a.getReferencedIds());
            for (Integer id : a.getReferencedIds()) {
                final Element element = elements.get(id);
                if (element != null) {
                    tagElementsForOmission(element, elements, recursionLevel + 2, seenElementIds);
                    if (!element.isToBeOmitted())
                        a.addFoundReference(id);
                }
            }
            omitAttribute |= a.isToBeOmitted();
        }
        e.setToBeOmitted((e.isArray() && omitAttribute) || (e.isListNode() && e.getAttributes().containsKey(CAS.FEATURE_BASE_NAME_HEAD) && e.getAttribute(CAS.FEATURE_BASE_NAME_HEAD).isToBeOmitted()));

        // Handle list references. Repair broken list chains in case intermediate nodes are to be omitted.
        for (Attribute a : e.getAttributes().values()) {
            for (Integer referencedId : a.getReferencedIds()) {
                final Element referencedElement = elements.get(referencedId);
                if (referencedElement != null && referencedElement.isListNode() && referencedElement.isToBeOmitted()) {
                    closeLinkedListGapDueToOmission(e, a.getName(), elements, intendation);
                }
            }

        }

        log.debug(intendation + e.isToBeOmitted());
    }

    private void closeLinkedListGapDueToOmission(Element e, String attrName, Map<Integer, Element> elements, String intendation) {
        for (Integer referencedId : e.getAttribute(attrName).getReferencedIds()) {
            final Element referencedElement = elements.get(referencedId);
            Element nextNode = null;
            if (referencedElement.isToBeOmitted() && referencedElement.isListNode() && referencedElement.getAttributes().containsKey(CAS.FEATURE_BASE_NAME_TAIL)) {
                nextNode = elements.get(referencedElement.getAttribute(CAS.FEATURE_BASE_NAME_TAIL).getReferencedIds().iterator().next());
                if (nextNode.isToBeOmitted()) {
                    while (nextNode.isToBeOmitted()) {
                        final Integer nextNodeId = nextNode.getAttribute(CAS.FEATURE_BASE_NAME_TAIL).getReferencedIds().iterator().next();
                        nextNode = elements.get(nextNodeId);
                    }
                }
            }
            if (nextNode != null) {

                final Set<Integer> tailId = e.getAttribute(attrName).getReferencedIds();
                log.debug(intendation + "exchanging " + attrName + " ID " + referencedId + " with " + nextNode.getXmiId());
                tailId.remove(referencedId);
                tailId.add(nextNode.getXmiId());
            }
        }
    }

    private void readAttribute(ByteBuffer bb, String typeName, Type type, Set<String> mappedFeatures, Map<Integer, String> mapping, TypeSystem ts, BinaryDecodingResult res) {
        final ByteArrayOutputStream baos = res.getXmiData();
        final String attrName = mapping.get(bb.getInt());
        final Feature feature = type.getFeatureByBaseName(attrName);
        int attributeBegin = baos.size();
        // 'attrName="attrvalue" '
        write(attrName, baos);

        Attribute attribute = null;


        baos.write('=');
        baos.write('"');
        // Handle reference features for non-multivalue types. The type is a token, for example. FSArrays are handled here if the array is not allowed for multiple reference.
        // In such cases, no separate array XML element like <cas:FSArray...> is created but the references are just put into the feature attribute itself like
        // <type:Token ... deprel="25 85" .../>
        if (attrName.equals("xmi:id") || attrName.equals("sofa")) {
            handleXmiIdAndSofaAttributes(bb, attrName, res);
        } else if (XmiSplitUtilities.isReferenceAttribute(type, attrName, ts)) {
            attribute = new Attribute(attrName);
            handleReferenceAttributes(bb, res, attribute);
            // The next 'else if' handles Array and List types. Here, the type itself is an FSArray, DoubleArray and the feature is "elements" or the feature points to a list node.
        } else if (XmiSplitUtilities.isMultiValuedFeatureAttribute(type, attrName) || feature.getRange().isArray() || XmiSplitUtilities.isListTypeName(feature.getRange().getName())) {
            handleArrayElementFeature(bb, type, attrName, baos);
//            isReferenceAttribute = true;
            // The next 'else if' handles list elements themselves
        } else if (XmiSplitUtilities.isListTypeName(typeName)) {
            handleListTypes(bb, typeName, attrName, mapping, mappedFeatures, baos);
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

    private void handlePrimitiveFeatures(ByteBuffer bb, Set<String> mappedFeatures, Map<Integer, String> mapping, ByteArrayOutputStream ret, String attrName, Feature feature, TypeSystem ts) {
        if (ts.subsumes(ts.getType(CAS.TYPE_NAME_STRING), feature.getRange())) {
            writeStringWithMapping(bb, attrName, mappedFeatures, mapping, ret);
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
        } else
            throw new IllegalArgumentException("Unsupported UIMA array type: " + typeName);
        for (int i = 0; i < length; i++) {
            componentValueConsumer.accept(bb);
            if (i < length - 1)
                ret.write(' ');
        }
    }

    private void handleListTypes(ByteBuffer bb, String typeName, String attrName, Map<Integer, String> mapping, Set<String> mappedFeatures, ByteArrayOutputStream ret) {
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
                writeStringWithMapping(bb, attrName, mappedFeatures, mapping, ret);
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

    private void handleReferenceAttributes(ByteBuffer bb, BinaryDecodingResult res, Attribute attribute) {
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


    private void writeStringArray(ByteBuffer bb, Map<Integer, String> mapping, Set<String> mappedFeatures, ByteArrayOutputStream ret) {
        final int numStringArrayFeatures = bb.getInt();
        for (int i = 0; i < numStringArrayFeatures; i++) {
            final String featureName = mapping.get(bb.getInt());
            final int numValues = bb.getInt();
            for (int j = 0; j < numValues; j++) {
                ret.write('<');
                write(featureName, ret);
                ret.write('>');
                writeStringWithMapping(bb, featureName, mappedFeatures, mapping, ret);
                ret.write('<');
                ret.write('/');
                write(featureName, ret);
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

    private void writeStringWithMapping(ByteBuffer bb, String attrName, Set<String> mappedFeatures, Map<Integer, String> mapping, ByteArrayOutputStream baos) {
        if (mappedFeatures.contains(attrName)) {
            write(mapping.get(bb.getInt()), baos);
        } else {
            int length = bb.getInt();
            baos.write(bb.array(), bb.position(), length);
            bb.position(bb.position() + length);
        }
    }


}
