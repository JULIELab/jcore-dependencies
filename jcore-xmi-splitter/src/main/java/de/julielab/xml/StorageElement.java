package de.julielab.xml;

import javax.xml.stream.events.XMLEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StorageElement {
    private XMLEvent element;
    private String elementPrefix;
    private String elementNSUri;
    private String javaName;
    private Collection<String> storageKeys;

    public StorageElement(XMLEvent event, Collection<String> storageKeys) {
        this.element = checkIsElement(event);
        this.storageKeys = new HashSet<>(storageKeys);
    }

    public StorageElement(XMLEvent event, String elementPrefix, String elementNSUri, String elementName, String javaName, Collection<String> storageKeys) {

        this.element = checkIsElement(event);
        this.elementPrefix = elementPrefix;
        this.elementNSUri = elementNSUri;
        this.javaName = javaName;
        this.storageKeys = new HashSet<>(storageKeys);
    }

    private XMLEvent checkIsElement(XMLEvent event) {
        if (!event.isStartElement() && !event.isEndElement() && !event.isCharacters())
            throw new IllegalArgumentException("Only start elements, end element events and text content are allowed in this class but has XMLStreamConstants event code " + event.getEventType());
        return event;
    }

    public XMLEvent getElement() {
        return element;
    }

    public void setElement(XMLEvent event) {
        this.element = checkIsElement(event);
    }

    public String getElementPrefix() {
        return elementPrefix;
    }

    public void setElementPrefix(String elementPrefix) {
        this.elementPrefix = elementPrefix;
    }

    public String getElementNSUri() {
        return elementNSUri;
    }

    public void setElementNSUri(String elementNSUri) {
        this.elementNSUri = elementNSUri;
    }

    public String getElementName() {
        if( element.isStartElement())
             return element.asStartElement().getName().getLocalPart() ;
        if (element.isEndElement())
            return element.asEndElement().getName().getLocalPart();
        else return "TEXT";
    }

    public String getElementTypeJavaName() {
        return javaName;
    }

    public void setJavaName(String javaName) {
        this.javaName = javaName;
    }

    public Collection<String> getStorageKeys() {
        return storageKeys;
    }

    public void setStorageKeys(Collection<String> storageKey) {
        this.storageKeys = storageKey;
    }

    public void addStorageKeys(Collection<String> missingKeys) {
        storageKeys.addAll(missingKeys);
    }

    @Override
    public String toString() {
        return getElementName() + ": " + storageKeys.stream().collect(Collectors.joining());
    }
}
