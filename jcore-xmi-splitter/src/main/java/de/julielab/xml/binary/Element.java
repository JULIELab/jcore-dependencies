package de.julielab.xml.binary;

import de.julielab.xml.XmiSplitUtilities;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>For XMI documents, this class represents an XMI element belonging to some feature structure representation.</p>
 * <p>The ultimate purpose of this class is to indicate if it can be omitted when assembling an XMI document
 * from a set of loaded annotation modules. Some elements might reference elements that have not been loaded.
 * If these elements are FSArrays or FSLists and only reference non-existing elements, they can be removed
 * alltogether.</p>
 *
 * @see Attribute
 */
class Element extends DataRange {
    private Map<String, Attribute> attributes = new HashMap<>();
    private String typeName;
    private boolean isListNode;
    private boolean isArray;
    private int xmiId;

    public Element(String typeName) {
        this.typeName = typeName;
        this.isListNode = XmiSplitUtilities.isListTypeName(typeName);

    }

    public int getXmiId() {
        return xmiId;
    }

    public void setXmiId(int xmiId) {
        this.xmiId = xmiId;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public boolean isListNode() {
        return isListNode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void addAttribute(Attribute a) {
        attributes.put(a.getName(), a);
        a.setElement(this);
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    public Attribute getAttribute(String name) {
        return attributes.get(name);
    }
}
