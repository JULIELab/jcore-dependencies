package de.julielab.xml.binary;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>For XMI documents, this class represents one XMI element attribute whose contents are references to other elements.</p>
 * <p>The ultimate task for this class is to track those references that need to be removed in partial XMI annotation
 * module loading. If no references of this attribute have been loaded, the attribute should be removed alltogether
 * when rebuilding the final XMI document by {@link BinaryXmiBuilder}.</p>
 * @see Element
 */
class JeDISAttribute extends DataRange {
    private List<Integer> referencedIds = new ArrayList<>();
    private List<Integer> foundReferences = new ArrayList<>();
    private String name;
    private Element element;
    private boolean isModified;

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    public JeDISAttribute(String name) {

        this.name = name;
    }

    public List<Integer> getFoundReferences() {
        return foundReferences;
    }

    public void addFoundReference(Integer id) {
        foundReferences.add(id);
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getReferencedIds() {
        return referencedIds;
    }

    public void addReferencedId(Integer id) {
        referencedIds.add(id);
    }

    public boolean hasRemovedReferences() {
        return foundReferences.size() < referencedIds.size();
    }

    @Override
    public boolean isToBeOmitted() {
        return foundReferences.isEmpty();
    }
}
