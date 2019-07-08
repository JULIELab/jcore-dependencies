package de.julielab.xml.binary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <p>This class replaces the {@link de.julielab.xml.XmiBuilder} for the assembly of an XMI document from
 * the base document and a set of annotation modules in the case that the document and annotation modules come
 * in the binary format.</p>
 */
public class BinaryXmiBuilder {
    private final static Logger log = LoggerFactory.getLogger(BinaryXmiBuilder.class);
    private Map<String, String> namespaces;

    public BinaryXmiBuilder(Map<String, String> nsAndXmiVersionMap) {
        namespaces = nsAndXmiVersionMap;
    }

    public ByteArrayOutputStream buildXmi(BinaryDecodingResult decodingResult) {
        final byte[] xmiData = decodingResult.getXmiData().toByteArray();
        final ByteArrayOutputStream ret = new ByteArrayOutputStream();
        try {
            write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ret);
            write("<xmi:XMI ", ret);
            for (String prefix : namespaces.keySet()) {
                write("xmlns:", ret);
                write(prefix, ret);
                write("=\"", ret);
                write(namespaces.get(prefix), ret);
                write("\" ", ret);
            }
            ret.write('>');
            write("<cas:NULL xmi:id=\"0\" />", ret);
            writeContent(xmiData, decodingResult, ret);
            for (Integer sofaId : decodingResult.getSofaElements().keySet()) {
                final Collection<Integer> annotationIds = decodingResult.getSofaElements().get(sofaId);
                write("<cas:View sofa=\"", ret);
                write(sofaId, ret);
                write("\" members=\"", ret);
                write(annotationIds.stream().map(String::valueOf).collect(Collectors.joining(" ")), ret);
                write("\" />", ret);
            }
            write("</xmi:XMI>", ret);
            return ret;
        } catch (IOException e) {
            log.error("Could not create final XMI document", e);
            throw new IllegalArgumentException("Could not create final XMI document", e);
        }
    }

    private void writeContent(byte[] xmiData, BinaryDecodingResult decodingResult, ByteArrayOutputStream ret) throws IOException {
        final List<DataRange> toModify = decodingResult.getXmiPortionsToModify();

        if (!toModify.isEmpty()) {
            int currentEnd = 0;
            for (int i = 0; i < toModify.size(); i++) {
                DataRange dataRange = toModify.get(i);
                // Check if this element to modify has effectively been removed be its predecessor element.
                // This happens for attributes belonging to elements to be omitted.
                if (dataRange.getBegin() < currentEnd)
                    continue;
                if (dataRange.isToBeOmitted() && decodingResult.isShrinkArraysAndListsIfReferenceNotLoaded()) {
                    ret.write(xmiData, currentEnd, dataRange.getBegin() - currentEnd);
                    currentEnd = dataRange.getEnd();
                    // If this is the last XMI part to be omitted, write the remainder of the data into the final result.
                    if (i == toModify.size() - 1)
                        ret.write(xmiData, dataRange.getEnd(), xmiData.length - dataRange.getEnd());
                } else if (dataRange instanceof Attribute) {
                    // There should only be one case where a DataRange is not to be omitted: When it is an
                    // attribute and still has some - but not all - references.
                    ret.write(xmiData, currentEnd, dataRange.getBegin() - currentEnd);
                    currentEnd = dataRange.getEnd();
                    Attribute a = (Attribute) dataRange;
                    write(a.getName(), ret);
                    write("=\"", ret);
                    // When we shrink the references, we just omit all references to non-loaded elements
                    if (decodingResult.isShrinkArraysAndListsIfReferenceNotLoaded())
                        write(a.getFoundReferences().stream().map(String::valueOf).collect(Collectors.joining(" ")), ret);
                    // When we don't shrink, we let point references to non-loaded elements to null (id 0 is always the cas:NULL element).
                    else
                        write(a.getReferencedIds().stream().map(id -> a.getFoundReferences().contains(id) ? id : 0).map(String::valueOf).collect(Collectors.joining(" ")), ret);
                    write("\" ", ret);
                    // If this is the last XMI part to be omitted, write the remainder of the data into the final result.
                    if (i == toModify.size() - 1)
                        ret.write(xmiData, dataRange.getEnd(), xmiData.length - dataRange.getEnd());
                }

            }
        } else {
            ret.write(xmiData);
        }
    }

    private void write(String s, ByteArrayOutputStream baos) throws IOException {
        baos.write(s.getBytes(UTF_8));
    }

    private void write(int i, ByteArrayOutputStream baos) throws IOException {
        baos.write(String.valueOf(i).getBytes(UTF_8));

    }
}