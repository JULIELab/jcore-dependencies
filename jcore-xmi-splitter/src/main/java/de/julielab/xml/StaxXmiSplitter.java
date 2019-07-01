package de.julielab.xml;

import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.*;
import static java.util.stream.Collectors.toList;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class StaxXmiSplitter extends AbstractXmiSplitter {
    public static final String DOCUMENT_MODULE_LABEL = "DOCUMENT-MODULE";
    private static final int NO_SOFA_KEY = -1;
    private static final int SECOND_SOFA_MAP_KEY_START = -2;
    private final static Logger log = LoggerFactory.getLogger(StaxXmiSplitter.class);
    private static final Object depthMarker = new Object();
    private Deque<Object> depthDeque = new ArrayDeque<>();
    private byte[] currentXmiData;
    private XMLInputFactory inputFactory;

    public StaxXmiSplitter(Set<String> moduleAnnotationNames, boolean recursively, boolean storeBaseDocument,
                           String docTableName, Set<String> baseDocumentAnnotations) {
        super(moduleAnnotationNames, recursively, storeBaseDocument, docTableName, baseDocumentAnnotations);
        inputFactory = XMLInputFactory.newFactory();
    }

    /**
     * For large documents, the XMI sofa string can be very large (tenth of megabytes). The StAX XML parser has
     * configurable limit on the maximum size of attribute values. This constructor allows to pass a value for this
     * limit.
     *
     * @param annotationModulesToExtract
     * @param recursively
     * @param storeBaseDocument
     * @param docTableName
     * @param baseDocumentAnnotations
     * @param attribute_size
     */
    public StaxXmiSplitter(Set<String> annotationModulesToExtract, boolean recursively, boolean storeBaseDocument,
                           String docTableName, Set<String> baseDocumentAnnotations, int attribute_size) {
        this(annotationModulesToExtract, recursively, storeBaseDocument, docTableName, baseDocumentAnnotations);
    }

    @Override
    protected String getNodeXml(JeDISVTDGraphNode node) {
        return new String(Arrays.copyOfRange(currentXmiData, node.getByteOffset(), node.getByteOffset() + node.getByteLength()));
    }

    @Override
    public XmiSplitterResult process(byte[] xmiData, JCas aCas, int nextPossibleId, Map<String, Integer> existingSofaIdMap) throws XMISplitterException {
        this.currentXmiData = xmiData;
        try {
            final XMLStreamReader reader = inputFactory.createXMLStreamReader(new ByteArrayInputStream(xmiData), "UTF-8");
            log.debug("Employed StAX implementation: {}", reader.getClass());
            log.debug("Building namespace map");
            Map<String, String> namespaceMap = buildNamespaceMap(reader);
            log.debug("Creating JeDIS nodes");
            nodesByXmiId = createJedisNodes(reader, namespaceMap, aCas);
            log.debug("Labeling nodes");
            labelNodes(nodesByXmiId, moduleAnnotationNames, recursively);
            log.debug("Creating annotation modules");
            annotationModules = createAnnotationModules(nodesByXmiId);
            log.debug("Assigning new XMI IDs");
            ImmutablePair<Integer, Map<String, Integer>> nextXmiIdAndSofaMap = assignNewXmiIds(nodesByXmiId, existingSofaIdMap, nextPossibleId);
            log.debug("Slicing XMI data into annotation module data");
            LinkedHashMap<String, ByteArrayOutputStream> moduleData = createAnnotationModuleData(nodesByXmiId, annotationModules);
            Map<Integer, String> reverseSofaIdMap = nextXmiIdAndSofaMap.right.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            log.debug("Returning XMI annotation module result");
            return new XmiSplitterResult(moduleData, nextXmiIdAndSofaMap.left, namespaceMap, reverseSofaIdMap);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Map<Integer, JeDISVTDGraphNode> createJedisNodes(XMLStreamReader reader, Map<String, String> namespaceMap, JCas aCas) throws XMLStreamException {
        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = new HashMap<>();
        currentSecondSofaMapKey = SECOND_SOFA_MAP_KEY_START;
        forwardTo(reader, r -> depthDeque.size() == 2);
        JeDISVTDGraphNode lastNode = null;
        do {
            // Now that we have reached the beginning of the next element, we can compute the byte length of the previous element
            if (lastNode != null && lastNode.getByteLength() == 0) {
                lastNode.setByteLength(reader.getLocation().getCharacterOffset() - lastNode.getByteOffset());
            }
            String xmiIdValue = reader.getAttributeValue(namespaceMap.get("xmi"), "id");
            if (xmiIdValue != null && !"0".equals(xmiIdValue)) {
                int oldXmiId = Integer.parseInt(reader.getAttributeValue(namespaceMap.get("xmi"), "id"));
                String typeName = getTypeName(reader);
                JeDISVTDGraphNode n = nodesByXmiId.computeIfAbsent(oldXmiId, typeName.equals(CAS_SOFA) ? SofaVTDGraphNode::new : JeDISVTDGraphNode::new);
                n.setByteOffset(reader.getLocation().getCharacterOffset());
                n.setTypeName(typeName);
                String sofaId = reader.getAttributeValue(null, CAS.FEATURE_BASE_NAME_SOFA);
                if (sofaId != null)
                    n.setSofaXmiId(Integer.parseInt(sofaId));
                else
                    n.setSofaXmiId(NO_SOFA_KEY);

                Map<String, List<Integer>> referencedXmiIds = getReferencedXmiIds(reader, n.getTypeName(), aCas.getTypeSystem(), namespaceMap);
                n.setReferencedXmiIds(referencedXmiIds);
                referencedXmiIds.values().stream().flatMap(Collection::stream).filter(id -> id != 0).map(refId -> nodesByXmiId.computeIfAbsent(refId, JeDISVTDGraphNode::new)).forEach(referenced -> referenced.addPredecessor(n));

                if (n.getTypeName().equals(CAS_SOFA)) {
                    nodesByXmiId.put(currentSecondSofaMapKey--, n);
                    ((SofaVTDGraphNode) n).setSofaID(reader.getAttributeValue(null, "sofaID"));
                }
                lastNode = n;
            }
        } while (forwardTo(reader, r -> depthDeque.size() == 2 && r.getEventType() == START_ELEMENT));
        // Set the byte length for the last node
        if (lastNode != null && lastNode.getByteLength() == 0)
            lastNode.setByteLength(reader.getLocation().getCharacterOffset() - lastNode.getByteOffset());
        return nodesByXmiId;
    }

    private Map<String, List<Integer>> getReferencedXmiIds(XMLStreamReader reader, String typeName, TypeSystem ts, Map<String, String> namespaceMap) {
        if (typeName.equals(CAS_NULL) || typeName.equals(CAS_VIEW))
            return Collections.emptyMap();

        Map<String, List<Integer>> referencesByFeatureBaseName = new HashMap<>();
        Type annotationType = ts.getType(typeName);

        Function<String, List<Integer>> refAttributeValue2Integers = referenceString -> Stream.of(referenceString).filter(StringUtils::isNotBlank).map(refStr -> refStr.split("\\s+")).flatMap(Stream::of).map(Integer::parseInt).collect(toList());

        if (ts.subsumes(ts.getType(CAS.TYPE_NAME_FS_ARRAY), annotationType)) {
            String referenceString = reader.getAttributeValue(null, "elements");
            referencesByFeatureBaseName.put("elements", refAttributeValue2Integers.apply(referenceString));
        } else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_LIST_BASE), annotationType)) {
            String referenceString = reader.getAttributeValue(null, CAS.FEATURE_BASE_NAME_TAIL);
            referencesByFeatureBaseName.put(CAS.FEATURE_BASE_NAME_TAIL, refAttributeValue2Integers.apply(referenceString));
            if (XmiSplitUtilities.resolveListSubtypes(annotationType.getName()).equals(CAS.TYPE_NAME_FS_LIST)) {
                final String headReference = reader.getAttributeValue(null, CAS.FEATURE_BASE_NAME_HEAD);
                referencesByFeatureBaseName.put(CAS.FEATURE_BASE_NAME_HEAD, refAttributeValue2Integers.apply(headReference));
            }
        } else {
            List<Feature> features = annotationType.getFeatures();
            for (Feature f : features) {
                if (!f.getName().equals(CAS.FEATURE_FULL_NAME_SOFA) && XmiSplitUtilities.isReferenceFeature(f, ts)) {
                    String referenceString = reader.getAttributeValue(null, f.getShortName());
                    if (referenceString != null) {
                        referencesByFeatureBaseName.put(f.getShortName(), refAttributeValue2Integers.apply(referenceString));
                    }
                }
            }
        }

        return referencesByFeatureBaseName;
    }

    private Map<String, String> buildNamespaceMap(XMLStreamReader reader) throws XMLStreamException {
        Map<String, String> map = new HashMap<>();
        forwardTo(reader, r -> r.getEventType() == START_ELEMENT && r.getName().getLocalPart().equals("XMI"));
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            map.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }
        return map;
    }

    private String getTypeName(XMLStreamReader reader) {
        String namespace = reader.getName().getNamespaceURI();
        String name = reader.getName().getLocalPart();
        String nsUri = XmiSplitUtilities.convertNSUri(namespace);
        final String typeName = nsUri + name;
        return typeName;
    }


    private boolean forwardTo(XMLStreamReader reader, Predicate<XMLStreamReader> predicate) throws XMLStreamException {
        while (reader.hasNext()) {
            int currentEventType = reader.next();
            if (currentEventType == START_ELEMENT)
                depthDeque.push(depthMarker);
            else if (currentEventType == END_ELEMENT)
                depthDeque.pop();

            if (predicate.test(reader)) {
                break;
            }
        }
        if (!reader.hasNext())
            log.debug("Reached end of XML.");
        return reader.hasNext();
    }
}
