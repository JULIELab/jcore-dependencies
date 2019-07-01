package de.julielab.xml.util;

import de.julielab.xml.BinaryDecodingResult;
import de.julielab.xml.XmiSplitUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BinaryXmiBuilder {
    private final static Logger log = LoggerFactory.getLogger(BinaryXmiBuilder.class);
    private Map<String, String> namespaces;
    private final ByteBuffer bb8;

    public BinaryXmiBuilder(Map<String, String> nsAndXmiVersionMap) {
        namespaces = nsAndXmiVersionMap;
        bb8 = ByteBuffer.allocate(Math.max(Long.SIZE, Double.SIZE));
    }
    public ByteArrayOutputStream buildXmi(BinaryDecodingResult decodingResult) {
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
            decodingResult.getXmiData().writeTo(ret);
            for (Integer sofaId : decodingResult.getSofaElements().keys()) {
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

    private void write(String s, ByteArrayOutputStream baos) throws IOException {
        baos.write(s.getBytes(UTF_8));
    }

    private void write(int i, ByteArrayOutputStream baos) throws IOException {
       baos.write(String.valueOf(i).getBytes(UTF_8));

    }
}
