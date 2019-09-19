package de.julielab.xml.binary;

import java.util.List;

public class XmlStartTag extends XmlByteSpan{
    private List<XmlAttribute> attributes;

    public List<XmlAttribute> getAttributes() {
        return attributes;
    }

    public XmlStartTag(int start, int end, List<XmlAttribute> attributes, byte[] elementData) {
        super(start, end, elementData);
        this.attributes = attributes;
    }
}
