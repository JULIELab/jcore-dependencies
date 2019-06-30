package de.julielab.xml;

import com.ximpleware.XMLModifier;
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
import java.util.stream.Stream;

public class BinaryJeDISNodeDecoder {
    public ByteArrayOutputStream decode(Collection<InputStream> input, TypeSystem ts, Map<Integer, String> mapping, Set<String> mappedFeatures, Map<String, String> namespaceMap) {
        final ByteArrayOutputStream ret = new ByteArrayOutputStream();
        try {

            for (InputStream is : input) {
                final ByteBuffer bb = readInputStreamIntoBuffer(is);
                String prefixedNameType = mapping.get(bb.getInt());
                // '<type:Token '
                ret.write('<');
                writeWs(prefixedNameType, ret);
                final String[] prefixAndTypeName = prefixedNameType.split(":");
                final String typeName = XmiSplitUtilities.convertNSUri(namespaceMap.get(prefixAndTypeName[0])) + prefixAndTypeName[1];
                final Type type = ts.getType(typeName);
                final byte numAttributes = bb.get();
                for (int i = 0; i < numAttributes; i++) {
                    final String attrName = mapping.get(bb.getInt());
                    final Feature feature = type.getFeatureByBaseName(attrName);
                    // 'attrName="attrvalue" '
                    write(attrName, ret);
                    if (attrName.equals("xmi:id") || attrName.equals("sofa") || XmiSplitUtilities.isReferenceAttribute(type, attrName)) {
                        if (attrName.equals("xmi:id"))
                            writeAttrValue(bb.getInt(), ret);
                        else if (attrName.equals("sofa"))
                            writeAttrValue(bb.get(), ret);
                        else { // is reference attribute
                            final int numReferences = bb.getInt();
                            ret.write('"');
                            for (int j = 0; j < numReferences; j++) {
                                final int referenceXmiId = bb.getInt();
                                // -1 means "null"
                                if (referenceXmiId >= 0)
                                    write(referenceXmiId, ret);
                                else write("null", ret);
                                if (j < numReferences - 1)
                                    ret.write(' ');
                            }
                            ret.write('"');
                            ret.write(' ');
                        }
                    } else if (feature.getRange().isArray()) {
                        if (feature.getRange().getComponentType().getName().equals(CAS.TYPE_NAME_DOUBLE)) {

                        }
                        if (feature.getRange().getComponentType().getName().equals(CAS.TYPE_NAME_SHORT)) {

                        }
                        if (feature.getRange().getComponentType().getName().equals(CAS.TYPE_NAME_BYTE)) {

                        }
                        if (feature.getRange().getComponentType().getName().equals(CAS.TYPE_NAME_INTEGER)) {

                        }
                        if (feature.getRange().getComponentType().getName().equals(CAS.TYPE_NAME_LONG)) {

                        }
                    } else if (feature.getRange().isPrimitive()) {
                        if (feature.getRange().getComponentType().getName().equals(CAS.TYPE_NAME_STRING)) {
                            if (mappedFeatures.contains(attrName)) {
                            } else {
                            }
                        }
                        if (feature.getRange().getName().equals(CAS.TYPE_NAME_DOUBLE)) {
                            writeAttrValue(bb.getDouble(), ret);
                        }
                        if (feature.getRange().getName().equals("uima.cas.Short")) {
                            writeAttrValue(bb.getShort(), ret);
                        }
                        if (feature.getRange().getName().equals("uima.cas.Byte")) {
                            writeAttrValue(bb.get(), ret);
                        }
                        if (feature.getRange().getName().equals("uima.cas.Integer")) {
                            writeAttrValue(bb.getInt(), ret);
                        }
                        if (feature.getRange().getName().equals("uima.cas.Long")) {writeAttrValue(bb.getLong(), ret);
                        }
                    }
                }
                // 0 = that's it, 1 = there comes more which would then be the values of StringArrays
                final byte finishedIndicator = bb.get();
                if (finishedIndicator == 0) {
                    writeStringArray(is, ret);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private ByteBuffer readInputStreamIntoBuffer(InputStream is) throws IOException {
        final ByteBuffer bb;
        byte[] buffer = new byte[8192];
        int read;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((read = is.read(buffer)) != -1)
            baos.write(buffer, 0, read);

        bb = ByteBuffer.wrap(baos.toByteArray());
        return bb;
    }

    private void writeStringArray(InputStream is, ByteArrayOutputStream ret) {
    }

    private void writeAttrValue(int i, ByteArrayOutputStream baos) {
        baos.write('=');
        baos.write('"');
        write(i, baos);
        baos.write('"');
        baos.write(' ');
    }

    private void writeAttrValue(double d, ByteArrayOutputStream baos) {
        baos.write('=');
        baos.write('"');
        write(d, baos);
        baos.write('"');
        baos.write(' ');
    }

    private void writeAttrValue(long l, ByteArrayOutputStream baos) {
        baos.write('=');
        baos.write('"');
        write(l, baos);
        baos.write('"');
        baos.write(' ');
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


}
