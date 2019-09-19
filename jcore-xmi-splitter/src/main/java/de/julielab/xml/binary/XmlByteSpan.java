package de.julielab.xml.binary;

import static java.nio.charset.StandardCharsets.UTF_8;

public class XmlByteSpan {
    private int start;
    private int end;
    private byte[] xmlData;

    public XmlByteSpan(int start, int end, byte[] xmlData) {
        if (start < 0)
            throw new IllegalArgumentException("Start offset must by >= 0 but was " + start);
        if (end < 0)
            throw new IllegalArgumentException("End offset must be >= 0 but was " + end);
        if (end < start)
            throw new IllegalArgumentException("End offset must be >= start offset but start = " + start + " and end = " + end);
        this.start = start;
        this.end = end;
        this.xmlData = xmlData;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public byte[] getXmlData() {
        return xmlData;
    }

    public int getLength() {
        return end - start;
    }

    @Override
    public String toString() {
        return new String(xmlData, start, end - start, UTF_8);
    }
}
