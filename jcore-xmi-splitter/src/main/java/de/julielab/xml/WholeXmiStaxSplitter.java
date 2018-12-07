package de.julielab.xml;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import de.julielab.xml.util.XMISplitterException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.*;

public class WholeXmiStaxSplitter implements XmiSplitter {

    private static final Logger log = LoggerFactory.getLogger(StaxXmiSplitter.class);
    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private static final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private static final QName xmiIdQName = new QName("http://www.omg.org/XMI", "id");
    private static final QName elementsQName = new QName("", "elements");
    private static final QName invalidElementQName = new QName("http://de.julielab/xmi/splitter", "invalidElement");

    static {
        // we split the XMI into non-valid XML slices, so don't check for
        // correct structure
        //outputFactory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, Boolean.FALSE);
    }

    private boolean storeAll = false;
    private List<String> annotationModulesToExtract;
    private Set<String> elementsToStoreSet;
    private boolean recursively = false;
    private boolean storeBaseDocument;
    private String docTableName;
    @Deprecated
    private String firstAnnotationType;
    @Deprecated
    private boolean docMode = false;
    // private XMLEvent startDocument;
    private int othersCounter;
    /**
     * Maps from the sofa xmi:id attribute to its sofaID - i.e. the Sofa name.
     * This map is always reset upon the parsing of a new document and thus only
     * valid for the currently parsed document.
     */
    private Map<Integer, String> currentSofaIdMap;
    /**
     * Maps from the Sofa name to the sofa XMI ID as passed by the map at the
     * call to {@link #process(byte[], JCas, int, Map)}. The
     * intention for this map is to map the Sofa sofaID - which is unique to
     * each CAS - to the sofa xmi:id in the original base docment that is stored
     * in the database. All new annotations should refer to this sofa because
     * they will be loaded and deserialized together with this original base
     * document.
     */
    private Map<String, Integer> originalSofaIdMap;
    /**
     * The current cas; This is used to determine if the selected annotations
     * contain complex features.
     */
    private JCas cas;
    /**
     * The next xmi id that can be assigned to the new annotations. This is used
     * to avoid clashes with already existing annotations.
     */
    private Integer nextId;
    /**
     * Records the maximum XMI ID of the currently split document with respect
     * to the given next possible XMI ID (i.e. the max ID is never smaller than
     * the given next possible ID or any ID occurring in the current document).
     */
    private Integer currentMaxXmiId;
    /**
     * The XMI ID of the cas:NULL element. Just like the Sofa, we want to
     * exclude this element from dynamic XMI ID assignment since typically it is
     * always the first element with XMI ID 0 and there were seemingly (not
     * completely confirmed) issues when this was not the case.
     */
    private Integer casNULLId;
    /**
     * Maps a writer together with its associated output stream to each table
     * name.
     */
    private Map<String, Map.Entry<XMLEventWriter, ByteArrayOutputStream>> writers;
    /**
     * The xmi start tag containing the namespaces.
     */
    private StartElement xmiStartTag;
    /**
     * The xmi data (entire document or separate annotations) as output streams
     * mapped to table names.
     */
    private LinkedHashMap<String, ByteArrayOutputStream> xmiData;
    /**
     * The (annotation) namespaces of the XMI start element organized in a map
     * that maps from namespace prefix to the namespace URI. Additionally the
     * map contains the <tt>xmi:version</tt> attribute (actually all attributes,
     * but this should be the only one). Its value is the XMI version of the
     * current document.
     */
    private Map<String, String> namespaces;
    /**
     * Set to indicate whether an annotation type has data and thus should be
     * stored. Without this check, we would write the XMI header with no content
     * for annotations not existing for a document.
     */
    private Set<String> dataWrittenSet;
    private boolean parametersChecked;
    private Set<String> baseDocumentAnnotations;


    /**
     * Creates an StaxXmiSplitter that returns the whole xmi data. It will also
     * return the highest xmi id assigned so far. This is optimal if the xmi
     * data contains only the base document which has to be stored for the first
     * time.
     *
     * @param tableName The table to store the xmi data.
     * @param attribute_size The maximum attribute size accepted by the employed StAX parser.
     */
    public WholeXmiStaxSplitter(String tableName, int attribute_size) {
        this(tableName);
        //inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, attribute_size);
    }

    /**
     * Creates an StaxXmiSplitter that returns the whole xmi data. It will also
     * return the highest xmi id assigned so far. This is optimal if the xmi
     * data contains only the base document which has to be stored for the first
     * time.
     *
     * @param tableName The table to store the xmi data.
     */
    public WholeXmiStaxSplitter(String tableName) {
        storeAll = true;
        annotationModulesToExtract = new ArrayList<String>();
        annotationModulesToExtract.add(tableName);
        log.info(StaxXmiSplitter.class.getName() + " initialized.");
    }

    /**
     * Processes the xmi data according to the parameters set for the
     * StaxXmiSplitter.
     *
     * @param ba           The current xmi data.
     * @param aCas           The current cas.
     * @param nextPossibleId The next xmi id that can be assigned to the new annotations.
     *                       If the base document has not yet been stored this should be 0.
     * @return A list containing the xmi data (only unless the complete XMI
     * document should be stored), i.e. separate annotations and
     * optionally the base document, as output streams mapped to the
     * table names - a
     * <code>HashMap{@literal<}String, ByteArrayOutputStream{@literal>}</code>
     * - the next possible xmi id - an <code>Integer</code> - to store
     * in the database and a namespace map derived from XMI opening
     * element where each namespace prefix is mapped to the namespace
     * URI (only unless the complete XMI document should be stored).
     * @throws IOException
     */
    public XmiSplitterResult process(byte[] ba, JCas aCas, int nextPossibleId,
                                     Map<String, Integer> existingSofaIdMap) throws XMISplitterException {
        if (!parametersChecked)
            checkParameters(aCas.getTypeSystem());

        this.originalSofaIdMap = existingSofaIdMap;
        cas = aCas;
        nextId = nextPossibleId;
        currentMaxXmiId = nextPossibleId;
        xmiData = new LinkedHashMap<String, ByteArrayOutputStream>();
        namespaces = new HashMap<>();
        dataWrittenSet = new HashSet<String>();
        currentSofaIdMap = new HashMap<>();
        if (storeAll) {
            determineMaxXmiId(ba);
        } else {
            storeSelected(ba);
        }
        return new XmiSplitterResult(xmiData, currentMaxXmiId + 1, namespaces, currentSofaIdMap);
        // List<Object> result = new ArrayList<Object>();
        // result.add(xmiData);
        // result.add(nextId);
        // result.add(namespaces);
        // return result;
    }

    private void checkParameters(TypeSystem typeSystem) {
        for (int i = 1; i < annotationModulesToExtract.size(); i++) {
            String javaName = annotationModulesToExtract.get(i);
            if (typeSystem.getType(javaName) == null)
                if (recursively)
                    throw new IllegalArgumentException("The type system does not contain the type \"" + javaName
                            + "\". However, to be able to store referenced annotations recursively, the type system information is required.");
        }

    }

    /**
     * Determines the maximum XMI ID assigned in the XMI data given by
     * <tt>bais</tt> plus one.
     *
     * @param ba The current xmi data as input stream.
     */
    public int determineMaxXmiId(byte[] ba) {
        ByteArrayInputStream currentBais = new ByteArrayInputStream(ba);

        try {
            XMLEventReader thisReader = inputFactory.createXMLEventReader(currentBais);
            while (thisReader.hasNext()) {
                XMLEvent event = thisReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = event.asStartElement();
                    Attribute xmiIdAtt = element.getAttributeByName(xmiIdQName);
                    if (!(xmiIdAtt == null)) {
                        int xmiId = Integer.parseInt(xmiIdAtt.getValue());
                        if (xmiId > nextId)
                            nextId = xmiId;
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return nextId++;
    }

    /**
     * Parses and returns the selected annotations separately. If
     * storeBaseDocument is set to true the base document will also be returned.
     *
     * @param bais The current xmi data as input stream.
     */
    private void storeSelected(byte[] bais) throws XMISplitterException {
        writers = new LinkedHashMap<String, Map.Entry<XMLEventWriter, ByteArrayOutputStream>>();
        xmiStartTag = null;

        if (storeBaseDocument)
            docMode = true;
        othersCounter = -1;
        Multimap<String, String> xmiIdsToRetrieve = HashMultimap.create();
        LinkedHashMap<String, StorageElement> elementsToWrite = new LinkedHashMap<>();
        HashMap<String, String> idMap = new HashMap<>();
        Map<String, String> specialXmiIds = new HashMap<>();
        Set<XMLEvent> specialElements = new HashSet<>();

        processAndParse(bais, xmiIdsToRetrieve, elementsToWrite, idMap, specialXmiIds, specialElements);
    }

    /**
     * Parses the xmi data and retrieves all relevant elements according to the
     * parameters set for the StaxXmiSplitter. This is a recursive method that does
     * successively add referenced annotations from the given annotations to be
     * stored, if the recursive parameter is set to true.
     *
     * @param ba                 The current xmi data.
     * @param xmiIdsToRetrieve The global variable {@link #annotationModulesToExtract} contains all annotation names the should be extracted
     *                         from any given XMI document for storage. However, the respective XMI annotation elements may refer
     *                         to complex features like arrays and lists. To also store these feature structures together with
     *                         the original annotation, {@code xmiIdsToRetrieve} maps their XMI IDs to the storage keys associated
     *                         with their original annotations. As soon as the actual XMI element is encountered,
     *                         it is added to {@code elementsToWrite} and removed from {@code xmiIdsToRetrieve}. This method
     *                         is recursively called until {@code xmiIdsToRetrieve} is empty.
     * @param elementsToWrite  Stores all elements that will be written later with new xmi
     *                         ids. Maps the elements to XMI IDs. For elements that
     *                         are parts of child nodes of annotation elements or that are end tags, the mapping is
     *                         done to a negative number. I.e. the actual XML elements of the
     *                         annotations that should be stored (belonging to the base
     *                         document or being defined as an annotation to be stored)
     * @param idMap            Maps old xmi ids to new xmi ids.
     * @param specialXmiIds    The XMI IDs of the Sofa and cas:NULL elements. We store them
     *                         to avoid collisions with one of those two because their xmi:id
     *                         attributes have to remain constant. If they could change in an
     *                         update processing, it would cause inconsistency with already
     *                         stored annotations referring to a Sofa which xmi:id has been
     *                         changed.
     * @param specialElements  The XML elements belonging to Sofa or cas:NULL elements.
     */
    @SuppressWarnings("rawtypes")
    private void processAndParse(byte[] ba, Multimap<String, String> xmiIdsToRetrieve, LinkedHashMap<String, StorageElement> elementsToWrite, HashMap<String, String> idMap, Map<String, String> specialXmiIds, Set<XMLEvent> specialElements) throws XMISplitterException {

        ByteArrayInputStream currentBais = new ByteArrayInputStream(ba);

        try {
            XMLEventReader reader = inputFactory.createXMLEventReader(currentBais);
            // some elements have child nodes;
            // this is used to make sure these are stored as well and the end
            // tags of their parents
            // come at the right place
            String enclosingElementName = "";
            Collection<String> enclosingStorageKey = Collections.emptyList();
            Map<String, Type> xmi2ElementType = new HashMap<>();
            // Indicates that we are currently inside an annotation XML element that has children.
            boolean withinElement = false;

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                // Groups annotation elements and associated feature structures and text data that should be stored
                // together. When recursively storing annotations or when different annotations reference the
                // very same feature structure (e.g. an FSArray) is may actually happen that a single feature structure
                // must be stored under multiple keys so that each annotation type contains all its associated
                // feature structures within its own storage key. This data duplication is necessary for the case
                // that only one of the referencing types is loaded later. When multiple referencing types are loaded,
                // the duplication is reversed by the XMIBuilder.
                Collection<String> storageKey = Collections.emptyList();

                if (withinElement) {
                    if (event.isEndElement()) {
                        EndElementHandler endElementHandler = new EndElementHandler(event, enclosingElementName, enclosingStorageKey, elementsToWrite, withinElement).invoke();
                        enclosingElementName = endElementHandler.getEnclosingElementName();
                        enclosingStorageKey = endElementHandler.getEnclosingStorageKey();
                        withinElement = endElementHandler.isWithinElement();
                    } else if (event.isStartElement() || event.isCharacters()) {
                        // this is the start tag of a child node or its text
                        // content
                        StorageElement storageElement = new StorageElement(event, enclosingStorageKey);
                        checkStorageKeysNotEmpty(storageElement);
                        elementsToWrite.put(Integer.toString(othersCounter), storageElement);
                        othersCounter--;
                    }
                }

                if (event.isStartElement()) {
                    StartElementHandler startElementHandler = new StartElementHandler(event, xmiIdsToRetrieve, elementsToWrite, idMap, specialXmiIds, specialElements, enclosingElementName, withinElement, enclosingStorageKey, storageKey).invoke();
                    enclosingElementName = startElementHandler.getEnclosingElementName();
                    enclosingStorageKey = startElementHandler.getEnclosingStorageKey();
                    withinElement = startElementHandler.isWithinElement();
                }
            }
            reader.close();


            // Now give an element, that perhaps got the Sofa XMI ID as new ID
            // another XMI ID to avoid collisions (because the Sofa keeps it ID
            // since it may be referenced by elements stored somewhere outside
            // of the current document).
            Set<String> oldXmiIdCollisions = new HashSet<>();
            for (String oldXmiId : elementsToWrite.keySet()) {
                if (!specialElements.contains(elementsToWrite.get(oldXmiId).getElement())) {
                    String newXmiId = idMap.get(oldXmiId);
                    if (specialXmiIds.keySet().contains(newXmiId)) {
                        oldXmiIdCollisions.add(oldXmiId);
                        if (log.isDebugEnabled()) {
                            StorageElement storageElement = elementsToWrite.get(oldXmiId);
                            XMLEvent o = null;
                            if (storageElement != null && storageElement.getElement() != null)
                                o = storageElement.getElement();
                            log.debug(
                                    "Element \"{}\" with old XMI ID {} and new XMI ID {} collides with special XMI ID of element {} \"{}\"",
                                    o.asStartElement().getName().getLocalPart(), oldXmiId,
                                    newXmiId, specialXmiIds.get(newXmiId), elementsToWrite.get(newXmiId).getElement().asStartElement().getName().getLocalPart());
                        }
                    }
                }
            }
            for (String oldXmiId : oldXmiIdCollisions) {
                // in rare cases it can happen that the xmi IDs assigned by UIMA
                // are larger than the number of annotations in the document.
                // Then it might come to a collision even at this point when we
                // try to get a new XMI ID that is not already taken but
                // actually belongs - e.g. - to the Sofa
                while (specialXmiIds.containsKey(String.valueOf(nextId)))
                    ++nextId;
                idMap.put(oldXmiId, Integer.toString(nextId));
                log.debug("Giving element {} with old XMI ID {} new XMI ID: {}",
                        elementsToWrite.get(oldXmiId).getElement().asStartElement().getName().getLocalPart(),
                        oldXmiId, nextId);
                nextId++;
            }


            if (!xmiIdsToRetrieve.isEmpty()) {
                docMode = false;
                currentBais = new ByteArrayInputStream(ba);
                processAndParse(ba, xmiIdsToRetrieve, elementsToWrite, idMap, specialXmiIds, specialElements);
            } else {
                // Now, all XMI ID collisions have been resolved; put the
                // special ID
                // map into the general map so we can continue now assigning new
                // IDs
                removeLooseEdgesAndFS(elementsToWrite, idMap, cas.getTypeSystem());
                idMap.putAll(specialXmiIds);
                writeWithNewXmiIds(elementsToWrite, idMap);
            }
        } catch (XMLStreamException e) {
            throw new XMISplitterException(e);
        }
    }

    /**
     * Removes XMI IDs fom the {@code idMap} that belong to elements that should be omitted from output. Those are
     * FSArrays that only reference annotations which are not marked for output and thus can't be loaded
     * again. The FSArrays then represent loose edges we want to remove.
     *
     * @param elementsToWrite The output list.
     * @param idMap           The XMI map (old XMI -> new XMI)
     * @param ts              The CAS type system.
     */
    private void removeLooseEdgesAndFS(LinkedHashMap<String, StorageElement> elementsToWrite, Map<String, String> idMap, TypeSystem ts) {
        Set<String> looseXmiIds = new HashSet<>();
        for (String xmiId : idMap.keySet()) {
            StorageElement storageElement = elementsToWrite.get(xmiId);
            XMLEvent event = storageElement.getElement();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                Type elementType;
                try {
                    elementType = ts.getType(storageElement.getElementTypeJavaName());
                } catch (NullPointerException e) {
                    // This happens for XMI elements that to not directly represent a feature structure.
                    // For JCoRe Tokens may have synonyms which are represented as child nodes of the token nodes.
                    // But the synonyms are just strings and no feature structures.
                    continue;
                }
                // the element type is null for the cas:NULL element
                // Also, we only handle FSArrays (or other collection feature structures, if necessary).
                // Annotations that should be stored will never be removed.
                if (elementType != null && isFSArray(elementType)) {
                    // Get the 'elements' attribute of the FSArray to check the references
                    Attribute elementsAttribute = element.getAttributeByName(elementsQName);
                    String[] xmiReferences = elementsAttribute.getValue().split("\\s+");
                    int looseReferences = 0;
                    for (String xmiReference : xmiReferences) {
                        if (!elementsToWrite.keySet().contains(xmiReference)) {
                            looseXmiIds.add(xmiReference);
                            ++looseReferences;
                        }
                    }
                    if (looseReferences == xmiReferences.length) {
                        log.trace("Feature structure of type {} is an array type and all its elements are not on the" +
                                " output list. The feature structure is removed from the output list as well.", elementType);
                        looseXmiIds.add(xmiId);
                    }
                }
            }
        }

        if (!looseXmiIds.isEmpty()) {
            log.debug("Found {} loose XMI ID references, including array types for which all references are missing. " +
                    "Removing the respective elements from the output list.", looseXmiIds.size());
            for (String looseXmiId : looseXmiIds) {
                // elementsToWrite.remove(looseXmiId);
                idMap.remove(looseXmiId);
            }
            removeLooseEdgesAndFS(elementsToWrite, idMap, ts);
        }
    }

    /**
     * Expects an attribute whose value are whitespace separated integers. These integers must represent XMI IDs
     * of feature structures referenced by the current feature structure XMI element. Then, it is ensured that
     * {@code elementsToWrite} records the respective references to be stored with respect to the given
     * storage keys, if not already done
     *
     * @param attribute        The XMI ID reference attribute.
     * @param storageKey       The storage keys to associate the referenced structures with. Those will typically be the
     *                         the keys with which the annotations referencing the feature structure are stored.
     * @param xmiIdsToRetrieve The queue recording which XMI elements still to collect.
     * @param elementsToWrite  Already recorded elements to be written out and collected to the associated storage keys.
     */
    private void recordReferencesForExtraction(Attribute attribute, Collection<String> storageKey, Multimap<String, String> xmiIdsToRetrieve, LinkedHashMap<String, StorageElement> elementsToWrite) {
        String value = attribute.getValue();
        String[] elements = splitElements(value);
        for (int i = 0; i < elements.length; i++) {
            final String referenceXmiId = elements[i];
            if (!referenceXmiId.equals("")) {
                if (!elementsToWrite.containsKey(referenceXmiId)) {
                    storageKey.forEach(key -> xmiIdsToRetrieve.put(referenceXmiId, key));
                } else if (!elementsToWrite.get(referenceXmiId).getStorageKeys().containsAll(storageKey)) {
                    StorageElement storageElement = elementsToWrite.get(referenceXmiId);
                    Set<String> storageElementStorageKey = new HashSet<>(storageElement.getStorageKeys());
                    // Only add the current storage keys to the referenced feature structure if it is not
                    // an annotation marked to be stored itself. We want to avoid duplication.
                    if (!elementsToStoreSet.contains(storageElement.getElementTypeJavaName())) {
                        Set<String> currentStorageKey = new HashSet<>(storageKey);
                        Set<String> missingKeys = Sets.difference(currentStorageKey, storageElementStorageKey);
                        storageElement.addStorageKeys(missingKeys);
                    }
                }
            }
        }
    }

    /**
     * Writes the retrieved elements with new xmi ids.
     *
     * @param elementsToWrite Stores all elements that will be written later with new xmi
     *                        ids. Maps the elements as lists to xmi ids.
     * @param idMap           Maps old xmi ids to new xmi ids.
     */
    @SuppressWarnings("rawtypes")
    private void writeWithNewXmiIds(LinkedHashMap<String, StorageElement> elementsToWrite,
                                    HashMap<String, String> idMap) {

        try {
            // Required to get the correct storage key for child and end elements since the storage keys are only
            // associated with their start element.
            Deque<Collection<String>> storageKeyStack = new ArrayDeque<>();
            // Required because some elements in elementsToWrite should actually not be written. The XMI IDs of elements
            // that have been removed - mostly FSArray elements that only point to annotations that are not being
            // stored - have been removed from the idMap. But we can only see for the start element that we
            // don't want to write it. Its text contents, child elements and end tag would be written anyway.
            // Thus, we use the deque to mark the elements that should not be written as "invalidElements".
            Deque<QName> validElementStack = new ArrayDeque<>();
            for (String id : elementsToWrite.keySet()) {
                StorageElement storageElement = elementsToWrite.get(id);
                checkStorageKeysNotEmpty(storageElement);
                // determine if this is the start tag of a parent (id >= 0,
                // since xmi id is not negative), whose xmi id
                // has to be changed, or a part of a child node or an end tag
                // (id < 0)
                if (Integer.parseInt(id) >= 0) {
                    if (!idMap.containsKey(id)) {
                        validElementStack.add(invalidElementQName);
                        continue;
                    }
                    StartElement element = storageElement.getElement().asStartElement();
                    String elementPrefix = storageElement.getElementPrefix();
                    String elementNSUri = storageElement.getElementNSUri();
                    String elementName = storageElement.getElementName();
                    String javaName = storageElement.getElementTypeJavaName();
                    validElementStack.add(element.getName());
                    Collection<String> storageKeys = storageElement.getStorageKeys();
                    storageKeyStack.add(storageKeys);
                    StartElement start = eventFactory.createStartElement(elementPrefix, elementNSUri, elementName);
                    for (String key : storageKeys) {
                        XMLEventWriter tableWriter = writers.get(key).getKey();
                        tableWriter.add(start);
                        dataWrittenSet.addAll(storageKeys);
                        Iterator attributes = element.getAttributes();
                        Type annotationType = cas.getTypeSystem().getType(javaName);
                        if (annotationType != null) {
                            if (isFSArray(annotationType)) {
                                while (attributes.hasNext()) {
                                    Attribute attribute = (Attribute) attributes.next();
                                    QName attributeQName = attribute.getName();
                                    String attributePrefix = attributeQName.getPrefix();
                                    String attributeName = attributeQName.getLocalPart();
                                    String attributeNSUri = attributeQName.getNamespaceURI();
                                    String value = attribute.getValue();

                                    if (attributePrefix.equals("xmi") && attributeName.equals("id")) {
                                        value = Objects.requireNonNull(idMap.get(value));
                                        int xmiId = Integer.parseInt(value);
                                        if (currentMaxXmiId < xmiId)
                                            currentMaxXmiId = xmiId;
                                    } else {
                                        // This is the 'elements' attribute of the FSArray
                                        String[] elements = splitElements(value);
                                        value = Stream.of(elements)
                                                .filter(elementsToWrite::containsKey)
                                                .map(idMap::get)
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.joining(" "));
                                    }
                                    Attribute newAttribute = eventFactory.createAttribute(attributePrefix, attributeNSUri,
                                            attributeName, value);
                                    tableWriter.add(newAttribute);
                                }
                            } else if (!isFSArray(annotationType)) {
                                while (attributes.hasNext()) {
                                    Attribute attribute = (Attribute) attributes.next();
                                    QName attributeQName = attribute.getName();
                                    String attributePrefix = attributeQName.getPrefix();
                                    String attributeName = attributeQName.getLocalPart();
                                    String attributeNSUri = attributeQName.getNamespaceURI();
                                    String value = attribute.getValue();

                                    if (attributePrefix.equals("xmi") && attributeName.equals("id")) {
                                        // Sofas must keep their original XMI ID
                                        // because all annotations reference the
                                        // sofa. If we would change the sofa's XMI
                                        // ID and did not update all annotations,
                                        // the old annotations would be invalid.
                                        value = elementName.toLowerCase().equals("sofa") ? value : Objects.requireNonNull(idMap.get(value));
                                        int xmiId = Integer.parseInt(value);
                                        if (currentMaxXmiId < xmiId)
                                            currentMaxXmiId = xmiId;
                                    } else if (!isPrimitive(annotationType, attributeName)) {
                                        String[] elements = splitElements(value);
                                        value = Stream.of(elements)
                                                .filter(elementsToWrite::containsKey)
                                                .map(idMap::get)
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.joining(" "));
                                    } else {
                                        if (attributeName.equals("sofa")) {
                                            // we need to replace the sofa reference
                                            // to the original sofa stored in the
                                            // database, if it already exists (if
                                            // not, the originalSofaIdMap should be
                                            // empty)
                                            Integer referencedSofaXmiId = Integer.parseInt(attribute.getValue());
                                            String referencedSofaName = currentSofaIdMap.get(referencedSofaXmiId);
                                            Integer originalSofaXmiId = originalSofaIdMap.get(referencedSofaName);
                                            if (null != originalSofaXmiId) {
                                                value = String.valueOf(originalSofaXmiId);
                                            }
                                        }
                                    }
                                    Attribute newAttribute = eventFactory.createAttribute(attributePrefix, attributeNSUri,
                                            attributeName, value);
                                    tableWriter.add(newAttribute);
                                }
                            }

                        } else {
                            // Here, the annotation is actually NULL: The special
                            // <cas:NULL xmi:id="..."> element that is unique at the
                            // beginning of the XMI data
                            // For this element, we want to leave its xmi:id
                            // untouched, just to be sure (typically 0).
                            while (attributes.hasNext()) {
                                Attribute attribute = (Attribute) attributes.next();
                                QName attributeQName = attribute.getName();
                                String attributePrefix = attributeQName.getPrefix();
                                String attributeName = attributeQName.getLocalPart();
                                String attributeNSUri = attributeQName.getNamespaceURI();
                                String value = attribute.getValue();

                                Attribute newAttribute = eventFactory.createAttribute(attributePrefix, attributeNSUri,
                                        attributeName, value);
                                tableWriter.add(newAttribute);
                            }
                        }
                    }
                } else if (Integer.parseInt(id) < 0) {
                    // Here, we handle text contents and end elements. The need to get the correct storage keys,
                    // associated with their respective start element, and also need to check if there start element
                    // hasn't been declared to be invalid, i.e. removed from output.
                    // Finally, for end elements we must remove the last deque elements belonging to their start
                    // elements.
                    XMLEvent event = storageElement.getElement();
                    Collection<String> storageKeys = storageKeyStack.peekLast();
                    checkStorageKeysNotEmpty(storageElement);
                    if (!validElementStack.peekLast().equals(invalidElementQName)) {
                        for (String key : storageKeys) {
                            writers.get(key).getKey().add(event);
                        }
                        if (event.isEndElement() && event.asEndElement().getName().equals(validElementStack.peekLast())) {
                            validElementStack.removeLast();
                            storageKeyStack.removeLast();
                        }
                    }
                }
            }
            finalizeWriters();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void checkStorageKeysNotEmpty(StorageElement storageElement) {
        assert !storageElement.getStorageKeys().isEmpty() : "XML element or text item " + storageElement.getElementName() + " should be extracted and stored " +
                "but was not given any storage keys. This is a programming error.";
    }

    /**
     * Retrieves the output streams as
     * results.
     */
    private void finalizeWriters() {
        for (String tableName : writers.keySet()) {
            // This keeps xmiData from containing any data for the table which
            // has no data.
            if (!dataWrittenSet.contains(tableName))
                continue;

            try {
                xmiData.put(tableName, writers.get(tableName).getValue());
                writers.get(tableName).getKey().close();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a writer entry consisting of a writer together with its
     * associated output stream.
     *
     * @return
     */
    private Map.Entry<XMLEventWriter, ByteArrayOutputStream> getWriterEntry() {
        Map.Entry<XMLEventWriter, ByteArrayOutputStream> entry = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(baos);
            entry = new AbstractMap.SimpleEntry<XMLEventWriter, ByteArrayOutputStream>(eventWriter, baos);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return entry;
    }

    /**
     * Retrieves the types namespace uri from the xmi element in order to create
     * the fully qualified java name for all annotations that have not been
     * given in this form.
     *
     * @param bais The current xmi data as input stream.
     */
    @SuppressWarnings("rawtypes")
    public void setAllJavaNames(ByteArrayInputStream bais) {
        String typesNSUri = null;

        try {
            XMLEventReader reader = inputFactory.createXMLEventReader(bais);
            outerloop:
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = event.asStartElement();
                    QName elementQName = element.getName();
                    String prefix = elementQName.getPrefix();
                    String elementName = elementQName.getLocalPart();
                    if (prefix.equals("xmi") & elementName.equals("XMI")) {
                        Iterator namespaces = element.getNamespaces();
                        while (namespaces.hasNext()) {
                            Namespace ns = (Namespace) namespaces.next();
                            if (ns.getName().getPrefix().equals("xmlns")
                                    & ns.getName().getLocalPart().equals("types")) {
                                typesNSUri = ns.getValue();
                                break outerloop;
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        if (!(typesNSUri == null)) {
            String typesJavaName = convertNSUri(typesNSUri);
            if (!storeBaseDocument) {
                for (int i = 0; i < annotationModulesToExtract.size(); i++) {
                    if (!annotationModulesToExtract.get(i).contains(".")) {
                        String javaName = typesJavaName + annotationModulesToExtract.get(i);
                        annotationModulesToExtract.set(i, javaName);
                    }
                }
            } else {
                for (int i = 1; i < annotationModulesToExtract.size(); i++) {
                    if (!annotationModulesToExtract.get(i).contains(".")) {
                        String javaName = typesJavaName + annotationModulesToExtract.get(i);
                        annotationModulesToExtract.set(i, javaName);
                    }
                }
                if (!firstAnnotationType.contains("."))
                    firstAnnotationType = typesJavaName + firstAnnotationType;
            }
        } else {
            log.warn("There are annotations that have not been given as the fully qualified java name, but this could"
                    + " not be retrieved from the types namespace. Table names might be in the wrong form!");
        }
    }

    /**
     * Splits the value of the elements attribute of an FSArray, since it might
     * refer to several elements.
     *
     * @param elements A string possibly consisting of several xmi ids.
     * @return
     */
    private String[] splitElements(String elements) {
        String[] separateElements = elements.split(" ");
        return separateElements;
    }

    /**
     * Returns the specified annotations to select.
     *
     * @return
     */
    public List<String> getAnnotationsToStore() {
        if (!storeBaseDocument) {
            return annotationModulesToExtract;
        } else {
            ArrayList<String> annosWithoutBaseDocument = new ArrayList<String>();
            for (int i = 1; i < annotationModulesToExtract.size(); i++) {
                annosWithoutBaseDocument.add(annotationModulesToExtract.get(i));
            }
            return annosWithoutBaseDocument;
        }
    }

    /**
     * Return the first annotation type that is not an element of the base
     * document.
     *
     * @return
     */
    public String getFirstAnnotationtype() {
        return firstAnnotationType;
    }

    private class EndElementHandler {
        private XMLEvent event;
        private String enclosingElementName;
        private Collection<String> enclosingStorageKey;
        private LinkedHashMap<String, StorageElement> elementsToWrite;
        private boolean withinElement;

        public EndElementHandler(XMLEvent event, String enclosingElementName, Collection<String> enclosingStorageKey, LinkedHashMap<String, StorageElement> elementsToWrite, boolean withinElement) {
            this.event = event;
            this.enclosingElementName = enclosingElementName;
            this.enclosingStorageKey = enclosingStorageKey;
            this.elementsToWrite = elementsToWrite;
            this.withinElement = withinElement;
        }

        public String getEnclosingElementName() {
            return enclosingElementName;
        }

        public Collection<String> getEnclosingStorageKey() {
            return enclosingStorageKey;
        }

        public boolean isWithinElement() {
            return withinElement;
        }

        public EndElementHandler invoke() {
            EndElement endElement = event.asEndElement();
            QName endElementQName = endElement.getName();
            String endElementName = endElementQName.getLocalPart();
            // this is either the end tag of a child node or of the
            // parent itself;
            // add it to the elements to write but with a negative
            // number instead of
            // an xmi id
            StorageElement storageElement = new StorageElement(endElement, enclosingStorageKey);
            checkStorageKeysNotEmpty(storageElement);
            elementsToWrite.put(Integer.toString(othersCounter), storageElement);
            othersCounter--;
            if (endElementName.equals(enclosingElementName)) {
                // stop looking for child nodes until the start tag
                // of the next
                // relevant element is encountered
                withinElement = false;
                enclosingElementName = "";
                enclosingStorageKey = Collections.emptyList();
            }
            return this;
        }
    }

    private class StartElementHandler {
        private XMLEvent event;
        private Multimap<String, String> xmiIdsToRetrieve;
        private LinkedHashMap<String, StorageElement> elementsToWrite;
        private HashMap<String, String> idMap;
        private Map<String, String> specialXmiIds;
        private Set<XMLEvent> specialElements;
        private String enclosingElementName;
        private boolean withinElement;
        private Collection<String> enclosingStorageKey;
        private Collection<String> storageKey;

        public StartElementHandler(XMLEvent event, Multimap<String, String> xmiIdsToRetrieve, LinkedHashMap<String, StorageElement> elementsToWrite, HashMap<String, String> idMap, Map<String, String> specialXmiIds, Set<XMLEvent> specialElements, String enclosingElementName, boolean withinElement, Collection<String> enclosingStorageKey, Collection<String> storageKey) {
            this.event = event;
            this.xmiIdsToRetrieve = xmiIdsToRetrieve;
            this.elementsToWrite = elementsToWrite;
            this.idMap = idMap;
            this.specialXmiIds = specialXmiIds;
            this.specialElements = specialElements;
            this.enclosingElementName = enclosingElementName;
            this.withinElement = withinElement;
            this.enclosingStorageKey = enclosingStorageKey;
            this.storageKey = storageKey;
        }

        public String getEnclosingElementName() {
            return enclosingElementName;
        }

        public Collection<String> getEnclosingStorageKey() {
            return enclosingStorageKey;
        }

        public boolean isWithinElement() {
            return withinElement;
        }

        public StartElementHandler invoke() {
            StartElement element = event.asStartElement();
            QName elementQName = element.getName();
            String elementPrefix = elementQName.getPrefix();
            String elementNSUri = elementQName.getNamespaceURI();
            String elementName = elementQName.getLocalPart();
            String javaName = getTypeJavaName(element) + elementName;

            // store the xmi start tag of this document once and
            // initialize writers with it
            if (elementPrefix.equals("xmi") && elementName.equals("XMI") && xmiStartTag == null) {
                xmiStartTag = event.asStartElement();
                Iterator it = xmiStartTag.getAttributes();
                while (it.hasNext()) {
                    // Sometimes there are empty attributes...
                    Attribute att = (Attribute) it.next();
                    String name = att.getName().getPrefix() + ":" + att.getName().getLocalPart();
                    namespaces.put(name, att.getValue());
                }
                Iterator nsit = xmiStartTag.getNamespaces();
                while (nsit.hasNext()) {
                    Namespace ns = (Namespace) nsit.next();
                    namespaces.put(ns.getPrefix(), ns.getNamespaceURI());
                }
                initializeWriters();
            }

            Attribute xmiIdAtt = element.getAttributeByName(xmiIdQName);
            if (xmiIdAtt != null) {
                final String xmiId = xmiIdAtt.getValue();

                // Add the Sofas of this document to the sofa ID
                // map. If we want to add annotations in the future,
                // we need to make sure that the Sofa IDs are
                // correct. Problem is, in some cases, the Sofa Id
                // changes from the first deserialization to future
                // deserializations. That means, if a once-stored
                // base document is loaded again, the Sofa Id might
                // change during deserialization and new annotations
                // will refer to the new xmi:ID. Thus, the
                // annotations are invalid if we don't store the
                // base document again. But IF we store the base
                // document again, we would invalidate old
                // annotations referring to the old Sofa ID. Thus,
                // we need to keep a map and change the Sofa
                // reference to the correct ID.
                if (elementName.equals("Sofa")) {
                    Iterator attributes = element.getAttributes();
                    String sofaID = null;
                    while (attributes.hasNext()) {
                        Attribute attribute = (Attribute) attributes.next();
                        QName attributeQName = attribute.getName();
                        String attributeName = attributeQName.getLocalPart();
                        if (attributeName.equals("sofaID"))
                            sofaID = attribute.getValue();
                    }
                    if (sofaID == null)
                        throw new IllegalStateException("Sofa element without a Sofa ID occurred: " + element);
                    currentSofaIdMap.put(Integer.parseInt(xmiId), sofaID);
                    specialXmiIds.put(xmiId, xmiId);
                    specialElements.add(element);
                }

                // it is possible that elements we come over now are
                // already contained in the elementsToWrite map because
                // they might be referenced by previously encountered
                // elements (e.g. the AbstractText might refer to
                // AbstractSections).
                boolean toBeWritten = false;
                if (storeBaseDocument && (baseDocumentAnnotations.contains(javaName) || baseDocumentAnnotations.contains("all"))
                        && !elementsToWrite.containsKey(xmiId)) {
                    toBeWritten = true;
                    storageKey = new ArrayList<>(Collections.singleton(docTableName));
                } else if (annotationModulesToExtract.contains(javaName) && !elementsToWrite.containsKey(xmiId)) {
                    toBeWritten = true;
                    storageKey = new ArrayList<>(Collections.singleton(javaName));
                    // it is possible that the annotation is also a feature that is already sought
                    if (xmiIdsToRetrieve.containsKey(xmiId)) {
                        // But even if we want to store recursively, we do not store annotations
                        // on their own AND as part of a recursively stored other annotation type.
                        // So clear the already given storage keys assigned to the current annotation.
                        xmiIdsToRetrieve.removeAll(xmiId);
                    }
                } else if (xmiIdsToRetrieve.containsKey(xmiId) && !elementsToWrite.containsKey(xmiId)) {
                    if (isFSArray(cas.getTypeSystem().getType(javaName)) || recursively) {
                        toBeWritten = true;
                        storageKey = new ArrayList<>(xmiIdsToRetrieve.get(xmiId));
                    } else {
                        xmiIdsToRetrieve.removeAll(xmiId);
                    }
                }

                if (toBeWritten) {
                    withinElement = true;
                    enclosingElementName = elementName;
                    enclosingStorageKey = storageKey;
                    Type annotationType = cas.getTypeSystem().getType(javaName);
                    if (annotationType != null) {
                        recordXmiReferences(element, annotationType, xmiIdsToRetrieve, elementsToWrite, storageKey);
                    } else {
                        // this element is the unique <cas:NULL
                        // xmi:id="..."> element
                        // retain its xmi:id
                        casNULLId = Integer.parseInt(xmiId);
                        specialXmiIds.put(xmiId, xmiId);
                        specialElements.add(element);
                    }
                    StorageElement storageElement = new StorageElement(element, elementPrefix, elementNSUri, elementName, javaName, storageKey);
                    elementsToWrite.put(xmiId, storageElement);

                    // for the special cas:NULL element, we have already
                    // set the value and don't want to override it
                    if (!specialXmiIds.keySet().contains(xmiId)) {
                        idMap.put(xmiId, Integer.toString(nextId));
                        nextId++;
                    }

                    // remove xmi id from the "wanted" list with respect the current storage keys
                    if (xmiIdsToRetrieve.containsKey(xmiId)) {
                        for (String key : storageKey)
                            xmiIdsToRetrieve.remove(xmiId, key);
                    }
                }

            }
            return this;
        }

        /**
         * Maps a writer together with its associated output stream to each table
         * name and adds the xmi start tag to each writer.
         */
        private void initializeWriters() {
            for (String tableName : annotationModulesToExtract) {
                Map.Entry<XMLEventWriter, ByteArrayOutputStream> entry = getWriterEntry();
                if (!(xmiStartTag == null)) {
                    writers.put(tableName, entry);
                } else {
                    log.warn("There is no xmi start tag to add to the writers. Xmi data might not be readable later!");
                }
            }
        }

        /**
         * For the current element {@code element} that corresponds to a feature structure of type {@code annotationType}
         * (could also be an FSArray), searches for features that reference other feature structures. Depending of whether
         * the referenced feature structures are included in the list of annotations to be stored and whether annotations
         * should be stored recursively, feature structure references are recorded in the {@code xmiIdsToRetrieve}
         * map. This map is used when coming across XMI elements that should not necessarily be stored by themselves
         * but are referenced recursively or represent (collections of) primitive feature values.
         *
         * @param element          The current XMI element.
         * @param annotationType   The feature structure type associated with the current XMI element.
         * @param xmiIdsToRetrieve The map that records elements that have been referenced by other elements and must still be resolved.
         * @param elementsToWrite  The element output map.
         * @param storageKey       The storage key of the current XMI element. This storage key is propagated to referenced feature structures in case of recursive storage.
         */
        private void recordXmiReferences(StartElement element, Type annotationType, Multimap<String, String> xmiIdsToRetrieve, LinkedHashMap<String, StorageElement> elementsToWrite, Collection<String> storageKey) {
            // look for attributes that are complex features
            // and store xmi ids of corresponding elements
            // together with the associated table names,
            // i.e. the elements they recursively belong to
            Iterator attributes = element.getAttributes();
            if (isFSArray(annotationType)) {
                while (attributes.hasNext()) {
                    Attribute attribute = (Attribute) attributes.next();
                    QName attributeQName = attribute.getName();
                    String attributePrefix = attributeQName.getPrefix();
                    String attributeName = attributeQName.getLocalPart();
                    if (!(attributePrefix.equals("xmi") && attributeName.equals("id"))) {
                        recordReferencesForExtraction(attribute, storageKey, xmiIdsToRetrieve, elementsToWrite);
                    }
                }
            } else if (!isFSArray(annotationType)) {
                while (attributes.hasNext()) {
                    Attribute attribute = (Attribute) attributes.next();
                    QName attributeQName = attribute.getName();
                    String attributePrefix = attributeQName.getPrefix();
                    String attributeName = attributeQName.getLocalPart();
                    if (!(attributePrefix.equals("xmi") && attributeName.equals("id"))) {
                        Type featureType = getFeatureType(annotationType, attributeName);
                        if (isFSArray(featureType) || (!isPrimitive(featureType) && recursively)) {
                            recordReferencesForExtraction(attribute, storageKey, xmiIdsToRetrieve, elementsToWrite);
                        }
                    }
                }
            }
        }
    }
}