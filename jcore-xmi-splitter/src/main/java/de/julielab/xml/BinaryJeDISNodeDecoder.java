package de.julielab.xml;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class BinaryJeDISNodeDecoder {

    private Set<String> annotationLabelsToLoad;
    private int currentXmiId = -1;
    private int currentSofaId = -1;

    public BinaryJeDISNodeDecoder(Set<String> annotationLabelsToLoad) {
        this.annotationLabelsToLoad = annotationLabelsToLoad;
    }

    public BinaryDecodingResult decode(Collection<InputStream> input, TypeSystem ts, Map<Integer, String> mapping, Set<String> mappedFeatures, Map<String, String> namespaceMap) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // sofa id -> element IDs
        final Multimap<Integer, Integer> sofaElementsMap = HashMultimap.create();
        final BinaryDecodingResult res = new BinaryDecodingResult(baos, sofaElementsMap);
        try {

            // the label is the fully qualified UIMA type name
          for (InputStream is : input) {
                final ByteBuffer bb = XmiSplitUtilities.readInputStreamIntoBuffer(is);
                while (bb.position() < bb.limit()) {
                    String prefixedNameType = mapping.get(bb.getInt());
                    // '<type:Token '
                    baos.write('<');
                    writeWs(prefixedNameType, baos);
                    final String[] prefixAndTypeName = prefixedNameType.split(":");
                    final String typeName = XmiSplitUtilities.convertNSUri(namespaceMap.get(prefixAndTypeName[0])) + prefixAndTypeName[1];
                    final Type type = ts.getType(typeName);
                    final byte numAttributes = bb.get();
                    currentSofaId = currentXmiId = -1;
                    for (int i = 0; i < numAttributes; i++) {
                        readAttribute(bb, typeName, type, mappedFeatures, mapping, ts, res);
                    }
                    if (currentSofaId != -1 && currentXmiId != -1) {
                        if(annotationLabelsToLoad.contains(typeName))
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
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private void readAttribute(ByteBuffer bb, String typeName, Type type, Set<String> mappedFeatures, Map<Integer, String> mapping, TypeSystem ts, BinaryDecodingResult res) {
        final ByteArrayOutputStream baos = res.getXmiData();
        final String attrName = mapping.get(bb.getInt());
        final Feature feature = type.getFeatureByBaseName(attrName);
        // 'attrName="attrvalue" '
        write(attrName, baos);
        baos.write('=');
        baos.write('"');
        if (attrName.equals("xmi:id") || attrName.equals("sofa") || XmiSplitUtilities.isReferenceAttribute(type, attrName, ts)) {
            handleReferenceAttributes(bb,typeName, attrName, res);
        } else if (XmiSplitUtilities.isMultiValuedFeatureAttribute(type, attrName) || feature.getRange().isArray() || XmiSplitUtilities.isListTypeName(feature.getRange().getName())) {
            handleArrayElementFeature(bb, type, attrName, baos);
        } else if (XmiSplitUtilities.isListTypeName(typeName)) {
            handleListTypes(bb, typeName, attrName, mapping, mappedFeatures, baos);
        } else if (feature.getRange().isPrimitive()) {
            handlePrimitiveFeatures(bb, mappedFeatures, mapping, baos, attrName, feature);
        } //else throw new IllegalArgumentException("Unhandled attribute '" + attrName + "' of type '" + typeName + "'.");
        baos.write('"');
        baos.write(' ');
    }

    private void handlePrimitiveFeatures(ByteBuffer bb, Set<String> mappedFeatures, Map<Integer, String> mapping, ByteArrayOutputStream ret, String attrName, Feature feature) {
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_STRING)) {
            writeStringWithMapping(bb, attrName, mappedFeatures, mapping, ret);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_DOUBLE)) {
            write(bb.getDouble(), ret);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_SHORT)) {
            write(bb.getShort(), ret);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_BYTE)) {
            write(bb.get(), ret);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_INTEGER)) {
            write(bb.getInt(), ret);
        }
        if (feature.getRange().getName().equals(CAS.TYPE_NAME_LONG)) {
            write(bb.getLong(), ret);
        }
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

    private void handleReferenceAttributes(ByteBuffer bb, String typeName, String attrName, BinaryDecodingResult res) {
        final ByteArrayOutputStream baos = res.getXmiData();
        if (attrName.equals("xmi:id")) {
            currentXmiId = bb.getInt();
            write(currentXmiId, baos);
        }
        else if (attrName.equals("sofa")) {
            currentSofaId = bb.get();
            write(currentSofaId, baos);
        }
        else { // is reference attribute
            final int numReferences = bb.getInt();
            for (int j = 0; j < numReferences; j++) {
                final int referenceXmiId = bb.getInt();
                // -1 means "null"
                if (referenceXmiId >= 0)
                    write(referenceXmiId, baos);
                else write("null", baos);
                if (j < numReferences - 1)
                    baos.write(' ');
            }
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
        baos.writeBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    private void writeWs(String s, ByteArrayOutputStream baos) {
        baos.writeBytes(s.getBytes(StandardCharsets.UTF_8));
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
        return String.valueOf(i).getBytes(StandardCharsets.UTF_8);
    }

    private byte[] getStringBytes(long l) {
        return String.valueOf(l).getBytes(StandardCharsets.UTF_8);
    }

    private byte[] getStringBytes(double d) {
        return String.valueOf(d).getBytes(StandardCharsets.UTF_8);
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
