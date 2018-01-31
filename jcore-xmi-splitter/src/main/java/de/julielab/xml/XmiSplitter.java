package de.julielab.xml;

import static de.julielab.xml.XmiSplitUtilities.convertNSUri;
import static de.julielab.xml.XmiSplitUtilities.getTypeJavaName;
import static de.julielab.xml.XmiSplitUtilities.isFSArray;
import static de.julielab.xml.XmiSplitUtilities.isPrimitive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.api.WstxOutputProperties;

public class XmiSplitter {

	private static final Logger log = LoggerFactory.getLogger(XmiSplitter.class);
	private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private static final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	static {
		// we split the XMI into non-valid XML slices, so don't check for
		// correct structure
		outputFactory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, Boolean.FALSE);
	}
	private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
	private static final QName xmiIdQName = new QName("http://www.omg.org/XMI", "id");

	private boolean storeAll = false;
	private List<String> elementsToStore;
	private boolean recursively = false;
	private boolean storeBaseDocument;
	private String docTableName;
	@Deprecated
	private String firstAnnotationType;
	@Deprecated
	private boolean docMode = false;
	private int othersCounter;
	// private XMLEvent startDocument;
	/**
	 * Maps from the sofa xmi:id attribute to its sofaID - i.e. the Sofa name.
	 * This map is always reset upon the parsing of a new document and thus only
	 * valid for the currently parsed document.
	 */
	private Map<Integer, String> currentSofaIdMap;
	/**
	 * Maps from the Sofa name to the sofa XMI ID as passed by the map at the
	 * call to {@link #process(ByteArrayInputStream, JCas, int, Map)}. The
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
	 * TODO
	 */
	public XmiSplitter(List<String> elementsToStore, boolean recursively, boolean storeBaseDocument,
			String docTableName, Set<String> baseDocumentAnnotations, int attribute_size) {
		this(elementsToStore, recursively, storeBaseDocument, docTableName, baseDocumentAnnotations);
		inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, attribute_size);
	}

	/**
	 * Creates an XmiSplitter that selects specified annotations and returns
	 * them separately. Optionally, the base document will be returned as well.
	 * This if optimal if the xmi data already contains some annotations
	 * together with the base document, but this still has to be stored for the
	 * first time.
	 * 
	 * @param elementsToStore
	 *            A list of annotations to select and to return separately.
	 *            These should be given as the fully qualified java names of the
	 *            annotation types. If not, the fully qualified java name will
	 *            be retrieved from the types namespace and used as table name.
	 * @param recursively
	 *            If set to true, annotations that are features of the selected
	 *            annotations will also be returned.
	 * @param storeBaseDocument
	 *            If set to true, the base document will be returned as well. In
	 *            this case the name of the document table as well as as the
	 *            first annotation type has to be given.
	 * @param docTableName
	 *            The table to store the base document. Can be null if
	 *            storeBaseDocument is set to false.
	 * @param firstAnnotationType
	 *            The name of the first annotation that is not an element of the
	 *            base document. Can be null if storeBaseDocument is set to
	 *            false.
	 */
	public XmiSplitter(List<String> elementsToStore, boolean recursively, boolean storeBaseDocument,
			String docTableName, Set<String> baseDocumentAnnotations) {
		this.storeBaseDocument = storeBaseDocument;
		this.baseDocumentAnnotations = baseDocumentAnnotations != null ? baseDocumentAnnotations
				: new HashSet<>();
		if (storeBaseDocument) {
			if (docTableName == null)
				throw new IllegalStateException(
						"If storeBaseDocument is set to true, the table name to store the base document has to be given!");
			this.docTableName = docTableName;
			this.firstAnnotationType = firstAnnotationType;
			this.elementsToStore = new ArrayList<String>();
			this.elementsToStore.add(docTableName);
			for (String element : elementsToStore) {
				this.elementsToStore.add(element);
			}
			this.baseDocumentAnnotations.add("uima.cas.NULL");
			this.baseDocumentAnnotations.add("uima.tcas.DocumentAnnotation");
			this.baseDocumentAnnotations.add("uima.cas.Sofa");
		} else {
			this.elementsToStore = elementsToStore;
		}
		this.recursively = recursively;
		log.info(XmiSplitter.class.getName() + " initialized.");
	}

	/**
	 * TODO
	 */
	public XmiSplitter(String tableName, int attribute_size) {
		this(tableName);
		inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, attribute_size);
	}

	/**
	 * Creates an XmiSplitter that returns the whole xmi data. It will also
	 * return the highest xmi id assigned so far. This is optimal if the xmi
	 * data contains only the base document which has to be stored for the first
	 * time.
	 * 
	 * @param tableName
	 *            The table to store the xmi data.
	 */
	public XmiSplitter(String tableName) {
		storeAll = true;
		elementsToStore = new ArrayList<String>();
		elementsToStore.add(tableName);
		log.info(XmiSplitter.class.getName() + " initialized.");
	}

	/**
	 * Processes the xmi data according to the parameters set for the
	 * XmiSplitter.
	 * 
	 * @param bais
	 *            The current xmi data as input stream.
	 * @param aCas
	 *            The current cas.
	 * @param nextPossibleId
	 *            The next xmi id that can be assigned to the new annotations.
	 *            If the base document has not yet been stored this should be 0.
	 * @return A list containing the xmi data (only unless the complete XMI
	 *         document should be stored), i.e. separate annotations and
	 *         optionally the base document, as output streams mapped to the
	 *         table names - a
	 *         <code>HashMap{@literal<}String, ByteArrayOutputStream{@literal>}</code>
	 *         - the next possible xmi id - an <code>Integer</code> - to store
	 *         in the database and a namespace map derived from XMI opening
	 *         element where each namespace prefix is mapped to the namespace
	 *         URI (only unless the complete XMI document should be stored).
	 * @throws IOException
	 */
	public XmiSplitterResult process(ByteArrayInputStream bais, JCas aCas, int nextPossibleId,
			Map<String, Integer> existingSofaIdMap) throws IOException {
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
			determineMaxXmiId(bais);
		} else {
			storeSelected(bais);
		}
		return new XmiSplitterResult(xmiData, currentMaxXmiId + 1, namespaces, currentSofaIdMap);
		// List<Object> result = new ArrayList<Object>();
		// result.add(xmiData);
		// result.add(nextId);
		// result.add(namespaces);
		// return result;
	}

	private void checkParameters(TypeSystem typeSystem) {
		for (int i = 1; i < elementsToStore.size(); i++) {
			String javaName = elementsToStore.get(i);
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
	 * @param bais
	 *            The current xmi data as input stream.
	 */
	public int determineMaxXmiId(ByteArrayInputStream bais) {
		// String tableName = elementsToStore.get(0);
		byte[] ba = getByteArray(bais);
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
	 * @param bais
	 *            The current xmi data as input stream.
	 */
	private void storeSelected(ByteArrayInputStream bais) {
		writers = new LinkedHashMap<String, Map.Entry<XMLEventWriter, ByteArrayOutputStream>>();
		xmiStartTag = null;

		HashMap<String, String> xmiIdsToRetrieve = new HashMap<String, String>();
		//
		LinkedHashMap<String, List<Object>> elementsToWrite = new LinkedHashMap<String, List<Object>>();
		HashMap<String, String> idMap = new HashMap<String, String>();
		Map<String, String> specialXmiIds = new HashMap<>();
		Set<XMLEvent> specialElements = new HashSet<>();

		if (storeBaseDocument)
			docMode = true;
		othersCounter = -1;
		processAndParse(bais, xmiIdsToRetrieve, elementsToWrite, idMap, specialXmiIds, specialElements);
	}

	/**
	 * Parses the xmi data and retrieves all relevant elements according to the
	 * parameters set for the XmiSplitter. This is a recursive method that does
	 * successively add referenced annotations from the given annotations to be
	 * stored, if the recursive parameter is set to true.
	 * 
	 * @param bais
	 *            The current xmi data as input stream.
	 * @param xmiIdsToRetrieve
	 *            Stores xmi ids of elements that are complex features of the
	 *            ones added. Maps the associated table names, i.e. the elements
	 *            they recursively belong to, to the xmi ids. Is filled with the
	 *            XMI IDs of annotations that have been defined as to be stored
	 *            as well as annotations belonging to the base document, if it
	 *            should be stored.
	 * @param elementsToWrite
	 *            Stores all elements that will be written later with new xmi
	 *            ids. Maps the elements as lists to xmi ids. For elements that
	 *            are parts of child nodes or that are end tags the mapping is
	 *            done to a negative number. I.e. the actual XML elements of the
	 *            annotations that should be stored (i.e. belonging to the base
	 *            document or being defined as an annotation to be stored)
	 * @param idMap
	 *            Maps old xmi ids to new xmi ids.
	 * @param specialXmiIds
	 *            The XMI IDs of the Sofa and cas:NULL elements. We store them
	 *            to avoid collisions with one of those two because their xmi:id
	 *            attributes have to remain constant. If they could change in an
	 *            update processing, it would cause inconsistency with already
	 *            stored annotations referring to a Sofa which xmi:id has been
	 *            changed.
	 * @param specialElements
	 *            The XML elements belonging to Sofa or cas:NULL elements.
	 */
	@SuppressWarnings("rawtypes")
	private void processAndParse(ByteArrayInputStream bais, HashMap<String, String> xmiIdsToRetrieve,
			LinkedHashMap<String, List<Object>> elementsToWrite, HashMap<String, String> idMap,
			Map<String, String> specialXmiIds, Set<XMLEvent> specialElements) {

		byte[] ba = getByteArray(bais);
		ByteArrayInputStream currentBais = new ByteArrayInputStream(ba);

		try {
			XMLEventReader reader = inputFactory.createXMLEventReader(currentBais);
			// some elements have child nodes;
			// this is used to make sure these are stored as well and the end
			// tags of their parents
			// come at the right place
			String openElement = "";
			String openTable = "";
			boolean isOpen = false;
			// XMI IDs of Sofas and cas:NULL
			// Map<String, String> specialXmiIds = new HashMap<>();
			// Set<XMLEvent> specialElements = new HashSet<>();

			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				boolean isDocElement = false;
				boolean isAnnotation = false;
				boolean isFeature = false;
				String tableName = "";

				if (isOpen) {
					if (event.isEndElement()) {
						EndElement endElement = event.asEndElement();
						QName endElementQName = endElement.getName();
						String endElementName = endElementQName.getLocalPart();
						// this is either the end tag of a child node or of the
						// parent itself;
						// add it to the elements to write but with a negative
						// number instead of
						// an xmi id
						List<Object> list = new ArrayList<Object>();
						list.add(event);
						list.add(openTable);
						elementsToWrite.put(Integer.toString(othersCounter), list);
						othersCounter--;
						if (endElementName.equals(openElement)) {
							// stop looking for child nodes until the start tag
							// of the next
							// relevant element is encountered
							isOpen = false;
							openElement = "";
							openTable = "";
						}
					} else if (event.isStartElement() || event.isCharacters()) {
						// this is the start tag of a child node or its text
						// content
						List<Object> list = new ArrayList<Object>();
						list.add(event);
						list.add(openTable);
						elementsToWrite.put(Integer.toString(othersCounter), list);
						othersCounter--;
					}
				}

				if (event.isStartElement()) {
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
					if (!(xmiIdAtt == null)) {
						String xmiId = xmiIdAtt.getValue();

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
						if (storeBaseDocument && (baseDocumentAnnotations.contains(javaName) || baseDocumentAnnotations.contains("all"))
								&& !elementsToWrite.containsKey(xmiId)) {
							isDocElement = true;
							tableName = docTableName;
						} else if (elementsToStore.contains(javaName) && !elementsToWrite.containsKey(xmiId)) {
							isAnnotation = true;
							tableName = javaName;
						} else if (xmiIdsToRetrieve.containsKey(xmiId) && !elementsToWrite.containsKey(xmiId)) {
							isFeature = true;
							tableName = xmiIdsToRetrieve.get(xmiId);
						}

						if (isDocElement || isAnnotation || isFeature) {
							isOpen = true;
							openElement = elementName;
							openTable = tableName;
							if (isDocElement || (isAnnotation && recursively) || isFeature) {
								// look for attributes that are complex features
								// and store xmi ids of corresponding elements
								// together with the associated table names,
								// i.e. the elements they recursively belong to
								Iterator attributes = element.getAttributes();
								Type annotationType = cas.getTypeSystem().getType(javaName);
								if (!(annotationType == null)) {
									if (isFSArray(annotationType)) {
										while (attributes.hasNext()) {
											Attribute attribute = (Attribute) attributes.next();
											QName attributeQName = attribute.getName();
											String attributePrefix = attributeQName.getPrefix();
											String attributeName = attributeQName.getLocalPart();
											if (!(attributePrefix.equals("xmi") && attributeName.equals("id"))) {
												String value = attribute.getValue();
												String[] elements = splitElements(value);
												for (int i = 0; i < elements.length; i++) {
													if (!elementsToWrite.containsKey(elements[i])
															&& !elements[i].equals(""))
														xmiIdsToRetrieve.put(elements[i], tableName);
												}
											}
										}
									} else if (!isFSArray(annotationType)) {
										while (attributes.hasNext()) {
											Attribute attribute = (Attribute) attributes.next();
											QName attributeQName = attribute.getName();
											String attributePrefix = attributeQName.getPrefix();
											String attributeName = attributeQName.getLocalPart();
											if (!(attributePrefix.equals("xmi") && attributeName.equals("id"))) {
												if (!isPrimitive(annotationType, attributeName)) {
													String value = attribute.getValue();
													String[] elements = splitElements(value);
													for (int i = 0; i < elements.length; i++) {
														if (!elementsToWrite.containsKey(elements[i])
																&& !elements[i].equals(""))
															xmiIdsToRetrieve.put(elements[i], tableName);
													}
												}
											}
										}
									}
								} else {
									// this element is the unique <cas:NULL
									// xmi:id="..."> element
									// retain its xmi:id
									casNULLId = Integer.parseInt(xmiId);
									specialXmiIds.put(xmiId, xmiId);
									specialElements.add(element);
								}
							}
							// add the element as list (element, prefix,
							// namespace uri, element name, java name, table
							// name)
							// mapped to xmi id;
							// assign new xmi id
							List<Object> list = new ArrayList<Object>();
							list.add(element);
							list.add(elementPrefix);
							list.add(elementNSUri);
							list.add(elementName);
							list.add(javaName);
							list.add(tableName);
							elementsToWrite.put(xmiId, list);
							// for the special cas:NULL element, we have already
							// set the value and don't want to override it
							if (!specialXmiIds.keySet().contains(xmiId)) {
								idMap.put(xmiId, Integer.toString(nextId));
								nextId++;
							}
							// remove xmi id from the "wanted" list
							if (xmiIdsToRetrieve.containsKey(xmiId))
								xmiIdsToRetrieve.remove(xmiId);
						}
					}
				}
			}
			reader.close();
			// Now give an element, that perhaps got the Sofa XMI ID as new ID
			// another XMI ID to avoid collisions (because the Sofa keeps it ID
			// since it may be referenced by elements stored somewhere outside
			// of the current document).
			Set<String> oldXmiIdCollisions = new HashSet<>();
			for (String oldXmiId : idMap.keySet()) {
				if (!specialElements.contains(elementsToWrite.get(oldXmiId).get(0))) {
					String newXmiId = idMap.get(oldXmiId);
					if (specialXmiIds.keySet().contains(newXmiId)) {
						oldXmiIdCollisions.add(oldXmiId);
						if (log.isDebugEnabled()) {
							List<Object> list = elementsToWrite.get(oldXmiId);
							Object o = null;
							if (list != null && !list.isEmpty())
								o = list.get(0);
							log.debug(
									"Element \"{}\" with old XMI ID {} and new XMI ID {} collides with special XMI ID of element {}",
									new Object[] { ((XMLEvent) o).asStartElement().getName().getLocalPart(), oldXmiId,
											newXmiId, specialXmiIds.get(newXmiId) });
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
				log.debug("Giving element {} with old XMI ID {} new XMI ID: {}", new Object[] {
						((XMLEvent) elementsToWrite.get(oldXmiId).get(0)).asStartElement().getName().getLocalPart(),
						oldXmiId, nextId });
				nextId++;
			}
			if (!xmiIdsToRetrieve.isEmpty()) {
				docMode = false;
				currentBais = new ByteArrayInputStream(ba);
				processAndParse(currentBais, xmiIdsToRetrieve, elementsToWrite, idMap, specialXmiIds, specialElements);
			} else {
				// Now, all XMI ID collisions have been resolved; put the
				// special ID
				// map into the general map so we can continue now assigning new
				// IDs
				idMap.putAll(specialXmiIds);
				writeWithNewXmiIds(elementsToWrite, idMap);
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the retrieved elements with new xmi ids.
	 * 
	 * @param elementsToWrite
	 *            Stores all elements that will be written later with new xmi
	 *            ids. Maps the elements as lists to xmi ids.
	 * @param idMap
	 *            Maps old xmi ids to new xmi ids.
	 */
	@SuppressWarnings("rawtypes")
	private void writeWithNewXmiIds(LinkedHashMap<String, List<Object>> elementsToWrite,
			HashMap<String, String> idMap) {

		try {
			for (String id : elementsToWrite.keySet()) {
				List<Object> list = elementsToWrite.get(id);

				// determine if this is the start tag of a parent (id >= 0,
				// since xmi id is not negative), whose xmi id
				// has to be changed, or a part of a child node or an end tag
				// (id < 0)
				if (Integer.parseInt(id) >= 0) {
					StartElement element = (StartElement) list.get(0);
					String elementPrefix = (String) list.get(1);
					String elementNSUri = (String) list.get(2);
					String elementName = (String) list.get(3);
					String javaName = (String) list.get(4);
					String tableName = (String) list.get(5);
					StartElement start = eventFactory.createStartElement(elementPrefix, elementNSUri, elementName);
					XMLEventWriter tableWriter = writers.get(tableName).getKey();
					tableWriter.add(start);
					dataWrittenSet.add(tableName);

					Iterator attributes = element.getAttributes();
					Type annotationType = cas.getTypeSystem().getType(javaName);
					if (!(annotationType == null)) {
						if (isFSArray(annotationType)) {
							while (attributes.hasNext()) {
								Attribute attribute = (Attribute) attributes.next();
								QName attributeQName = attribute.getName();
								String attributePrefix = attributeQName.getPrefix();
								String attributeName = attributeQName.getLocalPart();
								String attributeNSUri = attributeQName.getNamespaceURI();
								String value = attribute.getValue();

								if (attributePrefix.equals("xmi") & attributeName.equals("id")) {
									value = idMap.get(value);
									int xmiId = Integer.parseInt(value);
									if (currentMaxXmiId < xmiId)
										currentMaxXmiId = xmiId;
								} else {
									String[] elements = splitElements(value);
									for (int i = 0; i < elements.length; i++) {
										elements[i] = idMap.get(elements[i]);
									}
									value = StringUtils.join(elements, " ");
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
									value = elementName.toLowerCase().equals("sofa") ? value : idMap.get(value);
									int xmiId = Integer.parseInt(value);
									if (currentMaxXmiId < xmiId)
										currentMaxXmiId = xmiId;
								} else if (!isPrimitive(annotationType, attributeName)) {
									String[] elements = splitElements(value);
									for (int i = 0; i < elements.length; i++) {
										elements[i] = idMap.get(elements[i]);
									}
									value = StringUtils.join(elements, " ");
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

							// if (attributePrefix.equals("xmi")
							// & attributeName.equals("id")) {
							// value = idMap.get(value);
							// }
							Attribute newAttribute = eventFactory.createAttribute(attributePrefix, attributeNSUri,
									attributeName, value);
							tableWriter.add(newAttribute);
						}
					}
				} else if (Integer.parseInt(id) < 0) {
					XMLEvent event = (XMLEvent) list.get(0);
					String tableName = (String) list.get(1);
					writers.get(tableName).getKey().add(event);
				}
			}
			finalizeWriters();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Maps a writer together with its associated output stream to each table
	 * name and adds the xmi start tag to each writer.
	 */
	private void initializeWriters() {
		for (String tableName : elementsToStore) {
			Map.Entry<XMLEventWriter, ByteArrayOutputStream> entry = getWriterEntry();
			if (!(xmiStartTag == null)) {
				writers.put(tableName, entry);
			} else {
				log.warn("There is no xmi start tag to add to the writers. Xmi data might not be readable later!");
			}
		}
	}

	/**
	 * Adds the xmi end tag to each writer and retrieves the output streams as
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
	 * @param bais
	 *            The current xmi data as input stream.
	 */
	@SuppressWarnings("rawtypes")
	public void setAllJavaNames(ByteArrayInputStream bais) {
		String typesNSUri = null;

		try {
			XMLEventReader reader = inputFactory.createXMLEventReader(bais);
			outerloop: while (reader.hasNext()) {
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
				for (int i = 0; i < elementsToStore.size(); i++) {
					if (!elementsToStore.get(i).contains(".")) {
						String javaName = typesJavaName + elementsToStore.get(i);
						elementsToStore.set(i, javaName);
					}
				}
			} else {
				for (int i = 1; i < elementsToStore.size(); i++) {
					if (!elementsToStore.get(i).contains(".")) {
						String javaName = typesJavaName + elementsToStore.get(i);
						elementsToStore.set(i, javaName);
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
	 * @param elements
	 *            A string possibly consisting of several xmi ids.
	 * @return
	 */
	private String[] splitElements(String elements) {
		String[] separateElements = elements.split(" ");
		return separateElements;
	}

	/**
	 * Retrieves a byte array from this input stream for multiple use.
	 * 
	 * @param bais
	 *            The current xmi data as input stream.
	 * @return
	 */
	public byte[] getByteArray(ByteArrayInputStream bais) {
		byte[] ba = null;
		try {
			ba = IOUtils.toByteArray(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ba;
	}

	/**
	 * Returns the specified annotations to select.
	 * 
	 * @return
	 */
	public List<String> getAnnotationsToStore() {
		if (!storeBaseDocument) {
			return elementsToStore;
		} else {
			ArrayList<String> annosWithoutBaseDocument = new ArrayList<String>();
			for (int i = 1; i < elementsToStore.size(); i++) {
				annosWithoutBaseDocument.add(elementsToStore.get(i));
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

	public class XmiSplitterResult {

		public XmiSplitterResult(LinkedHashMap<String, ByteArrayOutputStream> xmiData, Integer nextId,
				Map<String, String> namespaces, Map<Integer, String> currentSofaIdMap) {
			this.xmiData = xmiData;
			maxXmiId = nextId;
			this.namespaces = namespaces;
			this.currentSofaIdMap = currentSofaIdMap;
		}

		public LinkedHashMap<String, ByteArrayOutputStream> xmiData;
		public int maxXmiId;
		public Map<String, String> namespaces;
		public Map<Integer, String> currentSofaIdMap;
	}
}
