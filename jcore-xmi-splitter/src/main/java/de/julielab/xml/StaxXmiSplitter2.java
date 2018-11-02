package de.julielab.xml;

import com.google.common.collect.Sets;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.XmlStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.*;
import static de.julielab.xml.XmiSplitUtilities.isFSArray;
import static de.julielab.xml.XmiSplitUtilities.isPrimitive;
import static java.util.stream.Collectors.toList;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class StaxXmiSplitter2 implements XmiSplitter {
    public static final String DOCUMENT_MODULE_LABEL = "DOCUMENT-MODULE";
    private static final int NO_SOFA_KEY = -1;
    private static final int SECOND_SOFA_MAP_KEY_START = -2;
    private final static Logger log = LoggerFactory.getLogger(StaxXmiSplitter2.class);
    private static final Object depthMarker = new Object();
    private final Set<String> moduleAnnotationNames;
    private final boolean recursively;
    private final boolean storeBaseDocument;
    private final String docTableName;
    private final Set<String> baseDocumentAnnotations;
    private int currentSecondSofaMapKey;
    private Map<Integer, JeDISVTDGraphNode> nodesByXmiId;
    private Map<String, Set<JeDISVTDGraphNode>> annotationModules;
    private Set<Integer> unavailableXmiId;
    private Deque<Object> depthDeque = new ArrayDeque<>();

    public StaxXmiSplitter2(Set<String> moduleAnnotationNames, boolean recursively, boolean storeBaseDocument,
                            String docTableName, Set<String> baseDocumentAnnotations) {
        this.moduleAnnotationNames = moduleAnnotationNames != null ? new HashSet<>(moduleAnnotationNames) : null;
        this.recursively = recursively;
        this.storeBaseDocument = storeBaseDocument;
        this.docTableName = docTableName;
        this.baseDocumentAnnotations = baseDocumentAnnotations == null ? Collections.emptySet() : baseDocumentAnnotations;

        if (storeBaseDocument)
            this.moduleAnnotationNames.add(DOCUMENT_MODULE_LABEL);

        if (moduleAnnotationNames != null && baseDocumentAnnotations != null && !Sets.intersection(moduleAnnotationNames, baseDocumentAnnotations).isEmpty())
            throw new IllegalArgumentException("The annotation types to build modules from and the annotation types to added to the base document overlap in: " + Sets.intersection(moduleAnnotationNames, baseDocumentAnnotations));
    }

    @Override
    public XmiSplitterResult process(byte[] xmiData, JCas aCas, int nextPossibleId, Map<String, Integer> existingSofaIdMap) throws XMISplitterException {
        try {
            final XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(xmiData));
            log.debug("Building namespace map");
            Map<String, String> namespaceMap = buildNamespaceMap(reader);
            log.debug("Creating JeDIS nodes");
            nodesByXmiId = createJedisNodes(reader, namespaceMap, aCas);
//            log.debug("Labeling nodes");
//            labelNodes(nodesByXmiId, moduleAnnotationNames, recursively);
//            log.debug("Creating annotation modules");
//            annotationModules = createAnnotationModules(nodesByXmiId);
//            log.debug("Assigning new XMI IDs");
//            ImmutablePair<Integer, Map<String, Integer>> nextXmiIdAndSofaMap = assignNewXmiIds(nodesByXmiId, existingSofaIdMap, nextPossibleId);
//            log.debug("Slicing XMI data into annotation module data");
//            LinkedHashMap<String, ByteArrayOutputStream> moduleData = createAnnotationModuleData(nodesByXmiId, annotationModules, existingSofaIdMap, nextPossibleId, vn);
//            Map<Integer, String> reverseSofaIdMap = nextXmiIdAndSofaMap.right.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
//            log.debug("Returning XMI annotation module result");
//            return new XmiSplitterResult(moduleData, nextXmiIdAndSofaMap.left, namespaceMap, reverseSofaIdMap);
//        } catch (VTDException e) {
//            throw new XMISplitterException(e);
//        }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<Integer, JeDISVTDGraphNode> createJedisNodes(XMLStreamReader reader, Map<String, String> namespaceMap, JCas aCas) throws XMLStreamException {
        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = new HashMap<>();
        currentSecondSofaMapKey = SECOND_SOFA_MAP_KEY_START;
        forwardTo(reader, r -> depthDeque.size() == 2);
        do {
            String xmiIdValue = reader.getAttributeValue(namespaceMap.get("xmi"), "id");
            if (xmiIdValue != null) {
                int oldXmiId = Integer.parseInt(reader.getAttributeValue(namespaceMap.get("xmi"), "id"));
                String typeName = getTypeName(reader);
                JeDISVTDGraphNode n = nodesByXmiId.computeIfAbsent(oldXmiId, typeName.equals(CAS_SOFA) ? SofaVTDGraphNode::new : JeDISVTDGraphNode::new);
                n.setStartOffset(reader.getLocation().getCharacterOffset());
                n.setTypeName(typeName);
                String sofaId = reader.getAttributeValue(namespaceMap.get("cas"),"sofa");
                if (sofaId != null)
                    n.setSofaXmiId(Integer.parseInt(sofaId));
                else
                    n.setSofaXmiId(NO_SOFA_KEY);

                Map<String, List<Integer>> referencedXmiIds = getReferencedXmiIds(reader, n.getTypeName(), aCas.getTypeSystem(), namespaceMap);
                n.setReferencedXmiIds(referencedXmiIds);
                referencedXmiIds.values().stream().flatMap(Collection::stream).map(refId -> nodesByXmiId.computeIfAbsent(refId, JeDISVTDGraphNode::new)).forEach(referenced -> referenced.addPredecessor(n));

                if (n.getTypeName().equals(CAS_SOFA)) {
                    nodesByXmiId.put(currentSecondSofaMapKey--, n);
                    ((SofaVTDGraphNode) n).setSofaID(reader.getAttributeValue(null, "sofaID"));
                }
            }
        } while (forwardTo(reader, r -> depthDeque.size() == 2 && r.getEventType() == START_ELEMENT));
        return nodesByXmiId;
    }

    private Map<String, List<Integer>> getReferencedXmiIds(XMLStreamReader reader, String typeName, TypeSystem ts, Map<String, String> namespaceMap)  {
        if (typeName.equals(CAS_NULL) || typeName.equals(CAS_VIEW))
            return Collections.emptyMap();

        Map<String, List<Integer>> referencesByFeatureBaseName = new HashMap<>();
        Type annotationType = ts.getType(typeName);

        Function<String, List<Integer>> refAttributeValue2Integers = referenceString -> Stream.of(referenceString).filter(StringUtils::isNotBlank).map(refStr -> refStr.split("\\s+")).flatMap(Stream::of).map(Integer::parseInt).collect(toList());

        if (isFSArray(annotationType)) {
            String referenceString = reader.getAttributeValue(null, "elements");
            referencesByFeatureBaseName.put("elements", refAttributeValue2Integers.apply(referenceString));
        } else if (!isFSArray(annotationType)) {
            List<Feature> features = annotationType.getFeatures();
            for (Feature f : features) {
                Type featureType = f.getRange();
                if (isFSArray(featureType) || !isPrimitive(featureType)) {
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

    private String getTypeName(XMLStreamReader reader){
        String namespace = reader.getName().getNamespaceURI();
        String name = reader.getName().getLocalPart();
        String nsUri = XmiSplitUtilities.convertNSUri(namespace);
        return nsUri + name;
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

    Map<Integer, JeDISVTDGraphNode> getNodesByXmiId() {
        return nodesByXmiId;
    }
}
