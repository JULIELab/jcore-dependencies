package de.julielab.xml;

import static de.julielab.xml.XmiSplitUtilities.TYPES_NAMESPACE;
import static de.julielab.xml.XmiSplitUtilities.getTypeJavaName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.xml.namespace.NamespaceContext;
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
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctc.wstx.api.WstxInputProperties;

public class XmiBuilder {

    /**
     * Start tags of elements whose xmi ids should not appear in the
     * cas:View-element.
     */
    public static final List<String> noViewMembers = Arrays.asList("cas:NULL", "cas:Sofa", "cas:FSArray");
    private static final Logger log = LoggerFactory.getLogger(XmiBuilder.class);
    private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private static final QName xmiIdQName = new QName("http://www.omg.org/XMI", "id");
    private static final QName fsArrayElements = new QName("elements");
    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private static String xmiTag = "xmi:XMI";
    /**
     * The map has the structure " <tt>T -> missingFeat_1,missingFeat_2,...</tt>
     * " where <tt>missingFeat_i</tt> are the base names of features referencing
     * types which are potentially missing.<br/>
     * <b>Example</b>:<br/>
     * The type <tt>de.julielab.jules.types.Token</tt> is loaded, it has a
     * feature <tt>posTag</tt>, but the PoS-Tags have not been loaded explicitly
     * (they could still be directly with the tokens though!). The feature base
     * name is <tt>posTag</tt> (the full name would be
     * <tt>de.julielab.jules.types.Token:posTag</tt>) and is added to the list
     * of potentially missing features of the <tt>Token</tt> type.
     */
    Map<String, Collection<String>> potentiallyMissingFeatures;
    private Map<String, InputStream> xmiData;
    private String docTableName;
    /**
     * Maps a reader to each table name;
     */
    private LinkedHashMap<String, XMLEventReader> readers;
    /**
     * Stores xmi ids of all elements (except for those defined in
     * {@link #noViewMembers}) to put them in the cas:View-element. TODO: How do
     * we handle annotations that somehow also do not appear in the
     * View-Element?
     */
    private LinkedHashSet<String> viewXmiIds;
    private byte[] xmlStartDocBytes;
    private byte[] xmiEndBytes;
    private byte[] xmiStartFinishBytes;
    private byte[] xmiStartBytes;
    private TypeSystem ts;
    private Set<String> annotationNames;
    /**
     * May be set to give the size - or an estimate - of the XMI size to be
     * built next. This is used to size the ByteArrayOutputStream the complete
     * XMI is written to, reducing the need for buffer resizing and possibly
     * even OutOfMemory errors due to resizing.
     */
    private int dataSize = 100000;

    // Map<String, Collection<String>> definitivelyMissingFeatures;

    public XmiBuilder(Map<String, String> nsAndXmiVersionMap, String[] annotationsToRetrieve, long attribute_size) {
        this.annotationNames = completeTypeNames(annotationsToRetrieve);
        setXMIStartElementData(nsAndXmiVersionMap);
        if (attribute_size > 0)
            inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, attribute_size);
    }

    public XmiBuilder(Map<String, String> nsAndXmiVersionMap, String[] annotationsToRetrieve) {
        this(nsAndXmiVersionMap, annotationsToRetrieve, 0);
    }

    /**
     * Builds a single xmi representation out of the base document and the
     * selected annotations.
     *
     * @param xmiData  The xmi data (document and annotations) as input streams
     *                 mapped from storage keys. It is a <tt>LinkedHashMap</tt> because
     *                 the order of the annotation data - e.g. first Sentences, then
     *                 Tokens - influences the internal index order in the CAS. Thus,
     *                 when serializing, the order of the <tt>LinkedHashMap</tt> will
     *                 be reflected in the XMI. Since we rely on the order of
     *                 appearance of annotations to determine the end of the base
     *                 document, we must provide the correct order.
     * @param docTable The name of the table containing the base document.
     * @param ts
     * @return The combined xmi representation as output stream.
     */
    public ByteArrayOutputStream buildXmi(LinkedHashMap<String, InputStream> xmiData, String docTable, TypeSystem ts) {
        this.xmiData = xmiData;
        this.docTableName = docTable;
        this.ts = ts;
        // Create only once since this method only has "constant" information,
        // i.e. the map would be always the same even if we created it each time
        // from scratch.
        if (null == potentiallyMissingFeatures)
            createFeatureExcludeMap();
        makeValidXML();
        readers = new LinkedHashMap<String, XMLEventReader>();
        viewXmiIds = new LinkedHashSet<String>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(dataSize);
        Attribute xmiIdAtt = null;
        try {
            XMLEventWriter writer = outputFactory.createXMLEventWriter(baos);
            initializeReaders();

            // What happens now:
            // We want to add the annotations we got with the xmiData to the
            // base document so that we end up with a
            // base document that is annotated by the passed annotations.
            // To achieve this, we first iterate through the elements of the
            // base document and do nothing more than to
            // memorize the XMI IDs of those elements so we can add them later
            // to the cas:view element (the list of XMI
            // elements in the CAS indexes).
            // When we reach the XMI end event, i.e. the last element of the
            // base document, we start to add all passed
            // annotations. So we "squeeze" the annotations right into the base
            // document before it is closed. Then, we
            // also add the cas:view element and be done.
            XMLEventReader docReader = readers.get(docTableName);
            if (docReader == null)
                throw new IllegalArgumentException("No XMI data was passed for the given document data key \"" + docTableName + "\".");
            while (docReader.hasNext()) {
                XMLEvent docEvent = docReader.nextEvent();

                // This is base document element, just save its XMI ID and
                // continue.
                if (docEvent.isStartElement()) {
                    StartElement docStart = docEvent.asStartElement();
                    QName docStartQName = docStart.getName();
                    String docStartPrefix = docStartQName.getPrefix();
                    String docStartName = docStartQName.getLocalPart();
                    String docStartTag = docStartPrefix + ":" + docStartName;

                    xmiIdAtt = docStart.getAttributeByName(xmiIdQName);
                    if (!(xmiIdAtt == null)) {
                        if (!noViewMembers.contains(docStartTag)) {
                            String xmiId = xmiIdAtt.getValue();
                            viewXmiIds.add(xmiId);
                        }

                    }
                } // Here comes and element, could be closing XMI element
                else if (docEvent.isEndElement()) {
                    EndElement element = docEvent.asEndElement();
                    QName elementQName = element.getName();
                    String elementPrefix = elementQName.getPrefix();
                    String elementName = elementQName.getLocalPart();
                    String tag = elementPrefix + ":" + elementName;

                    // determine if this is the xmi end element, i.e. if we have
                    // reached the end of the base document
                    // that currently does not contain any annotations other
                    // than the "base" annotations (authors, mesh
                    // headings etc).
                    // So now we go through all annotations we have also got and
                    // add them to the base document before we
                    // close it by adding the XMI end element.
                    if (tag.equals(xmiTag)) {
                        List<XMLEvent> annotationElements = new ArrayList<>();
                        Map<String, StartElement> seenXmiElements = new HashMap<>();
                        // add the other annotations before the xmi end element
                        // First: Get all annotation elements for all annotation
                        // types
                        for (String tableName : readers.keySet()) {
                            if (!tableName.equals(docTableName)) {
                                XMLEventReader annotationReader = readers.get(tableName);

                                while (annotationReader.hasNext()) {
                                    XMLEvent annotationEvent = annotationReader.nextEvent();
                                    // only add elements that are not the xmi
                                    // element
                                    if (annotationEvent.isStartDocument() || annotationEvent.isEndDocument())
                                        continue;
                                    if (annotationEvent.isStartElement()) {
                                        StartElement startElement = annotationEvent.asStartElement();
                                        QName startQName = startElement.getName();
                                        String startPrefix = startQName.getPrefix();
                                        String startName = startQName.getLocalPart();
                                        String startTag = startPrefix + ":" + startName;
                                        if (startTag.equals(xmiTag)) {
                                            continue;
                                        }

                                        xmiIdAtt = startElement.getAttributeByName(xmiIdQName);
                                        if (!(xmiIdAtt == null)) {
                                            String xmiId = xmiIdAtt.getValue();
                                            if (!noViewMembers.contains(startTag)) {
                                                viewXmiIds.add(xmiId);
                                            }
                                            // It is legally possible that an element occurs multiple times.
                                            // This happens when multiple annotations of different annotation types
                                            // have the very same feature structure as their feature.
                                            // Then, we only want to load one version of the element. Before we do this,
                                            // however, we check if they are really the same.
                                            if (null != seenXmiElements.get(xmiId))
                                                if (!equals(seenXmiElements.get(xmiId), startElement))
                                                    throw new IllegalArgumentException(
                                                            "Detected XMI ID clash between the following to elements: "
                                                                    + seenXmiElements.get(xmiId).getName().getLocalPart() + " and " + startElement.getName().getLocalPart()
                                                                    + "; the input data is inconsistent, please check the consistency of your database.");
                                            if (null == seenXmiElements.get(xmiId))
                                                seenXmiElements.put(xmiId, startElement);
                                        }
                                    } else if (annotationEvent.isEndElement()) {
                                        // Here, we do only one thing: Catch the
                                        // XMI end tag and ignore it
                                        EndElement endElement = annotationEvent.asEndElement();
                                        QName endQName = endElement.getName();
                                        String endPrefix = endQName.getPrefix();
                                        String endName = endQName.getLocalPart();
                                        String endTag = endPrefix + ":" + endName;
                                        if (endTag.equals(xmiTag)) {
                                            continue;
                                        }
                                    }
                                    annotationElements.add(annotationEvent);
                                }
                            }
                        }
                        // Up to now, we have ALL annotation XMI elements for
                        // ALL types stored in annotationElements;
                        // in seenXmiElements we have stored the XMI IDs of all
                        // those annotations.

                        // Remove attributes as FSArrays from missing feature
                        // types. I.e. if an annotation A references another
                        // annotation A' but A' was not passed
                        // (perhaps because it is not needed by the user), we
                        // have to delete the reference to A' to
                        // avoid XMI deserialization errors.
                        for (int i = 0; i < annotationElements.size(); i++) {
                            if (annotationElements.get(i).isStartElement()) {
                                StartElement annotationElementWithoutInvalidRefrences = removeInvalidFeatureReferences(
                                        annotationElements.get(i).asStartElement(), seenXmiElements);
                                annotationElements.set(i, annotationElementWithoutInvalidRefrences);
                            }
                        }
                        // When we delete a type which is stored in an FSArray,
                        // we also have to delete the FSArray. Here we count how
                        // many deleted elements we have been coming across so
                        // we can skip the respective end tags until the counter
                        // is 0 again.
                        int deletedStartElementCount = 0;
                        boolean inDeletedElement = false;
                        // Actually write all the annotation elements.
                        for (XMLEvent event : annotationElements) {
                            XMLEvent toWrite = event;

                            // First of all, check whether we are within a
                            // deleted element. Don't add nested elements.
                            if (!toWrite.isEndElement() && inDeletedElement) {
                                if (toWrite.isStartElement()) {
                                    xmiIdAtt = toWrite.asStartElement().getAttributeByName(xmiIdQName);
                                    if (!(xmiIdAtt == null)) {
                                        String xmiId = xmiIdAtt.getValue();
                                        viewXmiIds.remove(xmiId);
                                    }
                                    // Note if we are going further down the
                                    // tree so we know how many end elements
                                    // belong to the deleted parent element.
                                    deletedStartElementCount++;
                                }
                                log.trace("Deleted element: {}; Reason: Nested in deleted element.", toWrite);
                                continue;
                            }

                            // Check whether this is an deleted element.
                            if (event.isStartElement()) {
                                xmiIdAtt = toWrite.asStartElement().getAttributeByName(xmiIdQName);
                                if (!(xmiIdAtt == null)) {
                                    String xmiId = xmiIdAtt.getValue();
                                    if (null == seenXmiElements.get(xmiId)) {
                                        viewXmiIds.remove(xmiId);
                                        deletedStartElementCount++;
                                        inDeletedElement = true;
                                        log.trace(
                                                "Deleted element: {}; Reason: Explicitly deleted for referencing non existing XMI ID.",
                                                toWrite);
                                        continue;
                                    }
                                }
                            }

                            // Check whether this end element belongs to a
                            // deleted start element.
                            if (toWrite.isEndElement() && deletedStartElementCount > 0) {
                                deletedStartElementCount--;
                                // If we have deleted the last end element
                                // belonging to a deleted start element, we are
                                // now outside of deleted elements again.
                                if (deletedStartElementCount == 0)
                                    inDeletedElement = false;
                                log.trace("Deleted element: {}; Reason: Nested in deleted element.", toWrite);
                                continue;
                            }

                            // If the current element has not been deleted and
                            // was not nested into a deleted element, write it!
                            writer.add(toWrite);
                        }
                        // create the cas:View-element
                        String members = StringUtils.join(viewXmiIds, " ");
                        Attribute membersAttribute = eventFactory.createAttribute("members", members);
                        // Attribute sofaAttribute =
                        // eventFactory.createAttribute(
                        // "sofa", "1");
                        StartElement viewStart = eventFactory.createStartElement("cas", "http:///uima/cas.ecore",
                                "View");
                        EndElement viewEnd = eventFactory.createEndElement("cas", "http:///uima/cas.ecore", "View");
                        writer.add(viewStart);
                        // writer.add(sofaAttribute);
                        writer.add(membersAttribute);
                        writer.add(viewEnd);
                    }
                }
                writer.add(docEvent);
            }
            writer.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return baos;
    }

    private boolean equals(StartElement e1, StartElement e2) {
        boolean equal = e1.getName().equals(e2.getName());
        Iterator a1It = e1.getAttributes();
        Iterator a2It = e2.getAttributes();
        Set<QName> a1Names = new HashSet<>();
        Set<QName> a2Names = new HashSet<>();
        Set<String> a1Values = new HashSet<>();
        Set<String> a2Values = new HashSet<>();
        while (a1It.hasNext() && a2It.hasNext()) {
            Attribute a1 = (Attribute) a1It.next();
            Attribute a2 = (Attribute) a2It.next();
            a1Names.add(a1.getName());
            a2Names.add(a2.getName());
            a1Values.add(a1.getValue());
            a2Values.add(a2.getValue());
        }
        equal = equal && !a1It.hasNext() && !a2It.hasNext();
        equal = equal && a1Names.equals(a2Names);
        equal = equal && a1Values.equals(a2Values);

        return equal;
    }

    /**
     * <p>
     * If <tt>startElement</tt> is an annotation element referencing - directly
     * or through an FSArray - a type which is not present (i.e. has not been
     * loaded), a new <tt>startElement</tt> without the attribute referencing
     * the missing type is returned.
     * </p>
     * <p>
     * If <tt>startElement</tt> belongs to a type which has potentially missing
     * feature types, <tt>seenXmiElement</tt> is used to determine whether a
     * referenced <tt>xmi:id</tt> is missing indeed. If so, the referenced type
     * is added to the <tt>definitivelyMissingFeatures</tt> map, following the
     * same format as {@link #potentiallyMissingFeatures} . Definitively missing
     * feature attributes are deleted without further checking (because they are
     * only included in the map, if they have been checked before).
     * </p>
     * <p>
     * There are three types of (potentially) missing feature values:
     * <ul>
     * <li>The feature references an XMI element <tt>e</tt> - or an FSArray
     * referencing an XMI element <tt>e</tt> - where <tt>e</tt> could not be
     * found (not in <tt>seenXmiElements</tt>) because the type has been stored
     * separately and has not been loaded. Such types are added to the
     * <tt>definitivelyMissingFeatures</tt> map.</li>
     * <li>The feature type has not been loaded and thus is checked as
     * potentially missing but is found in <tt>seenXmiElements</tt>. Such
     * elements are then removed from the list of potentially missing feature
     * types and not checked again.</li>
     * <li>The type of the feature has not been loaded AND is never referenced,
     * because it has not been annotated before (null feature values are not
     * stored in the XMI document). Such types will be included in the
     * <tt>potentiallyMissingFeatures</tt> map but won't be ever checked since
     * there will be no attribute referencing the type.</li>
     * </ul>
     * Hence, after a few documents have been built from split XMI data, the
     * definitively missing types will be known and must not be checked any
     * more. The map of potentially missing features will shrink to those
     * features not loaded but not used either. Thus, these types won't be
     * checked either. So after a short time, no checks will be required any
     * more.
     * </p>
     *
     * @param startElement
     * @param seenXmiElements
     * @return
     */
    @SuppressWarnings("unchecked")
    private StartElement removeInvalidFeatureReferences(StartElement startElement,
                                                        Map<String, StartElement> seenXmiElements) {
        String elementName = startElement.getName().getLocalPart();
        String javaname = getTypeJavaName(startElement) + elementName;
        if (null == potentiallyMissingFeatures || !potentiallyMissingFeatures.keySet().contains(javaname)) {
            // For this type we are certain to have all recursive annotations
            // (e.g. because it doesn't have any).
            return startElement;
        }

        // If we came until this point, some attributes belong to annotations we
        // don't have. We cannot directly delete these attributes from the XML
        // element, so create a new StartElement with a reduced attribute set.
        List<Attribute> reducedAttributeList = new ArrayList<>();
        // Collection<String> missingFeatures =
        // definitivelyMissingFeatures.get(javaname);
        Collection<String> missingFeatureCandidates = potentiallyMissingFeatures.get(javaname);
        boolean reduceAttributeList = false;
        for (Iterator<Attribute> attIt = startElement.getAttributes(); attIt.hasNext(); ) {
            boolean removeAttribute = false;
            Attribute att = attIt.next();
            QName qname = att.getName();
            String xmiRefId = att.getValue();
            String featureBaseName = qname.getLocalPart();

            // if (null != missingFeatures &&
            // missingFeatures.contains(featureBaseName)) {
            // removeAttribute = true;
            // } else
            if (missingFeatureCandidates.contains(featureBaseName)) {
                boolean annotationMissing = false;
                // We will now look for an element actually encoding a type -
                // not just an FSArray - referenced by xmiRefId (or by an
                // FSArray that is referenced by xmiRefId).
                Type type = ts.getType(javaname);
                Feature feature = type.getFeatureByBaseName(featureBaseName);
                Type featureRange = feature.getRange();
                boolean featureHasArrayRange = featureRange.isArray();
                // boolean arrayFound = false;
                if (featureHasArrayRange) {
                    // Only when multiple references to the same feature array is allowed, an FSArray XMI element
                    // is created. Otherwise, all referenced element xmi:ids are just mentioned as the feature value
                    // itself
                    if (feature.isMultipleReferencesAllowed()) {
                        // The type range is an array, thus we have to follow the
                        // reference to the array and check whether the array's
                        // references are valid.
                        StartElement fsArray = seenXmiElements.get(xmiRefId);

                        if (null == fsArray) {
                            annotationMissing = true;
                        } else {
                            Attribute attributeByName = fsArray.getAttributeByName(fsArrayElements);
                            if (attributeByName == null)
                                throw new IllegalStateException("Programming error: The array feature " + featureBaseName + " should be accessed as FSArray for the element " + elementName + ". However, it does not have the \"" + fsArrayElements + "\" attribute. The value of the " + featureBaseName + " attribute is \"" + xmiRefId + "\".");
                            String elementsString = attributeByName.getValue();
                            String[] elements = elementsString.split(" ");
                            for (String element : elements) {
                                if (!seenXmiElements.containsKey(element))
                                    annotationMissing = true;
                            }
                        }
                    } else {
                        String elementsString = xmiRefId;
                        String[] elements = elementsString.split(" ");
                        for (String element : elements) {
                            if (!seenXmiElements.containsKey(element))
                                annotationMissing = true;
                        }
                    }
                } else {
                    // The xmiRefId directly references a type, check whether we
                    // saw the referenced element.
                    if (seenXmiElements.get(xmiRefId) == null)
                        annotationMissing = true;
                }
                if (annotationMissing) {
                    // if (null == missingFeatures) {
                    // missingFeatures = new HashSet<>();
                    // definitivelyMissingFeatures.put(javaname,
                    // missingFeatures);
                    // }
                    // missingFeatures.add(featureBaseName);
                    removeAttribute = true;
                    // log.debug(
                    // "The feature \"{}\" of type \"{}\" has been set to be
                    // definitively missing because it references XMI ID {}{}
                    // but no element with this ID has been found.",
                    // new Object[] { featureBaseName, javaname, xmiRefId,
                    // (featureHasArrayRange && arrayFound) ? " via an FSArray"
                    // : "" });
                }
                // else {
                // missingFeatureCandidates.remove(featureBaseName);
                // log.debug(
                // "The feature \"{}\" of type \"{}\" has been found at least
                // once and thus is removed from the list of potentially missing
                // features.",
                // featureBaseName, javaname);
                // }
            }
            if (!removeAttribute) {
                reducedAttributeList.add(att);
            } else {
                // Remove FSArrays with null-references
                if (null != seenXmiElements.get(xmiRefId)) {
                    seenXmiElements.remove(xmiRefId);
                }
            }
            if (removeAttribute)
                reduceAttributeList = true;
        }

        if (!reduceAttributeList)
            return startElement;

        StartElement newElement = eventFactory.createStartElement(startElement.getName(),
                reducedAttributeList.iterator(), startElement.getNamespaces());

        return newElement;
    }

    /**
     * <p>
     * Creates the map <tt>potentiallyMissingFeatures</tt> of types T that could
     * have features referencing other types <tt>T'</tt> where <tt>T'</tt> is
     * not present.
     * </p>
     * <p>
     * Type <tt>T'</tt> is deemed potentially not present if the annotation
     * names from which the XMI documents should be built do not contain
     * <tt>T'</tt>. If this is the case, it is NOT certain that <tt>T'</tt> is
     * missing since it could be saved together with <tt>T</tt> (this happens
     * when splitting the XMI was done with "recursive=true" and not storing
     * <tt>T'</tt> separately).
     * </p>
     * <p>
     * The map has the structure "<tt>T -> missingFeat_1,missingFeat_2,...</tt>"
     * where <tt>missingFeat_i</tt> are the base names of features referencing
     * types which are potentially missing.<br/>
     * <b>Example</b>:<br/>
     * The type <tt>de.julielab.jules.types.Token</tt> is loaded, it has a
     * feature <tt>posTag</tt>, but the PoS-Tags have not been loaded explicitly
     * (they could still be directly with the tokens though!). The feature base
     * name is <tt>posTag</tt> (the full name would be
     * <tt>de.julielab.jules.types.Token:posTag</tt>) and is added to the list
     * of potentially missing features of the <tt>Token</tt> type.
     * </p>
     */
    private void createFeatureExcludeMap() {
        if (null == annotationNames)
            return;
        potentiallyMissingFeatures = new HashMap<>();
        // definitivelyMissingFeatures = new HashMap<>();
        for (String typeName : annotationNames) {
            Type type = ts.getType(typeName);

            if (null == type)
                throw new IllegalArgumentException(
                        "The type \"" + typeName + "\" could not be found in the type system.");

            for (Feature feature : type.getFeatures()) {
                if (feature.getRange().isPrimitive() || feature.getName().startsWith("uima.cas.AnnotationBase"))
                    continue;
                // For arrays, get the element type.
                Type rangeType = feature.getRange().getComponentType();
                if (null == rangeType)
                    rangeType = feature.getRange();
                String featureTypeName = rangeType.getName();
                if (!annotationNames.contains(featureTypeName)) {
                    String featureBaseName = feature.getShortName();
                    Collection<String> excludeFeatures = potentiallyMissingFeatures.get(typeName);
                    if (null == excludeFeatures) {
                        excludeFeatures = new HashSet<>();
                        potentiallyMissingFeatures.put(typeName, excludeFeatures);
                    }
                    excludeFeatures.add(featureBaseName);
                }
            }
        }
        log.debug("The following features for specified types have been declared to be potentially missing: {}",
                potentiallyMissingFeatures);
    }

    /**
     * Complete type names when they are not fully qualified but only the class
     * name itself is given. Completion is done by appending
     * {@link XmiSplitUtilities#TYPES_NAMESPACE}. This will be wrong of course,
     * when the type was actually from another package. An example of this
     * approach to fail would be when only "Header" is given and
     * "...types.pubmed.Header" was meant instead of "...types.Header".
     *
     * @param annotationsToRetrieve
     * @return
     */
    private Set<String> completeTypeNames(String[] annotationsToRetrieve) {
        if (null != annotationsToRetrieve && annotationsToRetrieve.length > 0) {
            Set<String> annotationNames = new HashSet<>();
            for (int i = 0; i < annotationsToRetrieve.length; i++) {
                String annotationName = annotationsToRetrieve[i];
                if (!annotationName.contains(".")) {
                    String typeName = TYPES_NAMESPACE + annotationsToRetrieve[i];
                    annotationNames.add(typeName);
                } else {
                    annotationNames.add(annotationName);
                }
            }
            return annotationNames;
        }
        return null;
    }

    protected void setXMIStartElementData(Map<String, String> namespacesAndXmiVersion) {
        if (null == namespacesAndXmiVersion || namespacesAndXmiVersion.size() == 0) {
            log.debug(
                    "No namespaces mapping were passed. It is assumed that all given XMI documents are complete and valid.");
            return;
        }

        List<Attribute> xmiAtt = new ArrayList<>();
        List<Namespace> xmiNS = new ArrayList<>();
        for (Entry<String, String> nsEntry : namespacesAndXmiVersion.entrySet()) {
            if (nsEntry.getKey().equals("xmi:version")) {
                QName qName = new QName(nsEntry.getKey());
                xmiAtt.add(eventFactory.createAttribute(qName, nsEntry.getValue()));
            } else
                xmiNS.add(eventFactory.createNamespace(nsEntry.getKey(), nsEntry.getValue()));
        }

        StartDocument startDoc = eventFactory.createStartDocument();
        StartElement xmiStart = eventFactory.createStartElement("xmi", "http://www.omg.org/XMI", "XMI",
                xmiAtt.iterator(), xmiNS.iterator());

        try {
            xmlStartDocBytes = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(xmlStartDocBytes.length * 2);
            XMLEventWriter w = outputFactory.createXMLEventWriter(baos);
            baos = new ByteArrayOutputStream();
            w = outputFactory.createXMLEventWriter(baos);
            w.add(xmiStart);
            w.add(eventFactory.createCharacters("dummy"));
            w.add(eventFactory.createEndElement("xmi", "http://www.omg.org/XMI", "XMI"));
            w.close();
            byte[] ba = baos.toByteArray();
            // Evil hack to the incomplete opening element.
            String xmiStr = new String(ba);
            xmiStartBytes = (xmiStr.substring(0, xmiStr.indexOf('>'))).getBytes();
            // xmiStartBytes = ba;
            xmiStartFinishBytes = ">".getBytes();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        xmiEndBytes = "</xmi:XMI>".getBytes();
    }

    private void makeValidXML() {
        // We just look at one for all; if this is null, we just use the
        // original streams and hope they contain valid XMI.
        if (null == xmlStartDocBytes) {
            return;
        }

        for (Entry<String, InputStream> xmiEntry : xmiData.entrySet()) {
            InputStream xmiStream = xmiEntry.getValue();
            // We use a Vector here only because we can get an enumeration out
            // of it which we need for the SequenceInputStream.
            Vector<InputStream> streamVec = new Vector<>();
            if (xmiEntry.getKey().equals(docTableName))
                streamVec.add(new ByteArrayInputStream(xmlStartDocBytes));
            streamVec.add(new ByteArrayInputStream(xmiStartBytes));
            streamVec.add(new ByteArrayInputStream(xmiStartFinishBytes));
            streamVec.add(xmiStream);
            streamVec.add(new ByteArrayInputStream(xmiEndBytes));
            SequenceInputStream completeXmiStream = new SequenceInputStream(streamVec.elements());
            xmiEntry.setValue(completeXmiStream);
        }

    }

    /**
     * Maps a reader for each input stream to each table name.
     */
    private void initializeReaders() {
        for (String tableName : xmiData.keySet()) {
            XMLEventReader reader;
            try {
                reader = inputFactory.createXMLEventReader(xmiData.get(tableName));
                readers.put(tableName, reader);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The name of the table containing the base document.
     *
     * @param docTableName
     */
    public void setdocTableName(String docTableName) {
        this.docTableName = docTableName;
    }

    /**
     * The xmi data (document and annotations) as input streams mapped to table
     * names.
     *
     * @param xmiData
     */
    public void setXmiData(HashMap<String, InputStream> xmiData) {
        this.xmiData = xmiData;
    }

    public void setInputSize(int dataSize) {
        this.dataSize = dataSize;
    }
}
