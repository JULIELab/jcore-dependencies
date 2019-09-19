package de.julielab.xml.binary;

public class XmlAttribute  {
    private XmlByteSpan attributeName;
    private XmlByteSpan attributeValue;

    public XmlByteSpan getAttributeName() {
        return attributeName;
    }

    public XmlByteSpan getAttributeValue() {
        return attributeValue;
    }

    public XmlAttribute(int nameStart, int nameEnd, int valStart, int valEnd, byte[] xmlData) {
        attributeName = new XmlByteSpan(nameStart, nameEnd, xmlData);
        attributeValue = new XmlByteSpan((valStart), valEnd, xmlData);
    }
}
