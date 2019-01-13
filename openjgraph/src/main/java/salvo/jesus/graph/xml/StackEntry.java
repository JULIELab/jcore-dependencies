package salvo.jesus.graph.xml;

import org.xml.sax.Attributes;

/**
 * An entry in a stack for tracing where we are in the SAX processing
 */
class StackEntry {
    private String      elementName;
    private Attributes  elementAttributes;

    public StackEntry( String name, Attributes attribs ) {
        this.elementName = name;
        this.elementAttributes = attribs;
    }

    public String getElementName() {
        return this.elementName;
    }

    public Attributes getElementAttributes() {
        return this.elementAttributes;
    }


}