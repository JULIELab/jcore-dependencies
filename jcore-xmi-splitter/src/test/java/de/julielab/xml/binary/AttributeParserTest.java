package de.julielab.xml.binary;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;


public class AttributeParserTest {

    @Test
    public void parse() {
        byte[] xmlData = "<pubmed:ManualDescriptor xmi:id=\"15\" sofa=\"1\" begin=\"0\" end=\"0\" meSHList=\"22\" chemicalList=\"9\"  ><geneSymbolList>DR\"X3\"</geneSymbolList><geneSymbolList>DRB</geneSymbolList><geneSymbolList>HLA-DRB1</geneSymbolList><geneSymbolList>HLA-DRB3</geneSymbolList></pubmed:ManualDescriptor>".getBytes();
        AttributeParser parser = new AttributeParser();
        XmlStartTag tag = parser.parse(xmlData);
        assertEquals(tag.getAttributes().size(), 6);
        List<XmlAttribute> a = tag.getAttributes();
        assertEquals("xmi:id", a.get(0).getAttributeName().toString());
        assertEquals("15", a.get(0).getAttributeValue().toString());
        assertEquals("sofa", a.get(1).getAttributeName().toString());
        assertEquals("1", a.get(1).getAttributeValue().toString());
        assertEquals("begin", a.get(2).getAttributeName().toString());
        assertEquals("0", a.get(2).getAttributeValue().toString());
        assertEquals("end", a.get(3).getAttributeName().toString());
        assertEquals("0", a.get(3).getAttributeValue().toString());
        assertEquals("meSHList", a.get(4).getAttributeName().toString());
        assertEquals("22", a.get(4).getAttributeValue().toString());
        assertEquals("chemicalList", a.get(5).getAttributeName().toString());
        assertEquals("9", a.get(5).getAttributeValue().toString());
    }

}