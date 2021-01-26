package de.julielab.xml;

import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.*;
import static java.util.stream.Collectors.toList;


public class VtdXmlXmiSplitter extends AbstractXmiSplitter {
    private final static Logger log = LoggerFactory.getLogger(VtdXmlXmiSplitter.class);
    private static final int SECOND_SOFA_MAP_KEY_START = -2;
    private static final int NO_SOFA_KEY = -1;
    private VTDNav vn;


    public VtdXmlXmiSplitter(Set<String> moduleAnnotationNames, boolean recursively, boolean storeBaseDocument,
                             Set<String> baseDocumentAnnotations) {
        super(moduleAnnotationNames, recursively, storeBaseDocument, baseDocumentAnnotations);
    }

    public VTDNav getVTDNav() {
        return vn;
    }

    Map<String, Set<JeDISVTDGraphNode>> getAnnotationModules() {
        return annotationModules;
    }

    Map<Integer, JeDISVTDGraphNode> getNodesByXmiId() {
        return nodesByXmiId;
    }

    @Override
    protected String getNodeXml(JeDISVTDGraphNode node) throws XMISplitterException {
        try {
            return vn.toRawString(node.getByteOffset(), node.getByteLength());
        } catch (NavException e) {
            throw new XMISplitterException(e);
        }
    }

    @Override
    public XmiSplitterResult process(byte[] xmiData, TypeSystem ts, int nextPossibleId, Map<String, Integer> existingSofaIdMap) throws XMISplitterException {
        VTDGen vg = new VTDGen();
        vg.setDoc(xmiData);
        try {
            vg.parse(true);
            vn = vg.getNav();
            log.debug("Building namespace map");
            Map<String, String> namespaceMap = JulieXMLTools.buildNamespaceMap(vn);
            log.debug("Creating JeDIS nodes");
            nodesByXmiId = createJedisNodes(vn, namespaceMap, ts);
            log.debug("Labeling nodes");
            labelNodes(nodesByXmiId, moduleAnnotationNames, recursively);
            log.debug("Creating annotation modules");
            annotationModules = createAnnotationModules(nodesByXmiId);
            log.debug("Assigning new XMI IDs");
            ImmutablePair<Integer, Map<String, Integer>> nextXmiIdAndSofaMap = assignNewXmiIds(nodesByXmiId, existingSofaIdMap, nextPossibleId);
            log.debug("Slicing XMI data into annotation module data");
            Map<Integer, Integer> oldSofaXmiId2NewSofaXmiId = new HashMap<>();//existingSofaIdMap.keySet().stream().collect(Collectors.toMap(existingSofaIdMap::get, oldSofaName -> nextXmiIdAndSofaMap.getRight().get(existingSofaIdMap.get(oldSofaName))));
            //nextXmiIdAndSofaMap.getRight().values().stream().filter(newSofaXmiId -> !oldSofaXmiId2NewSofaXmiId.values().contains(newSofaXmiId)).forEach(newSofaXmiId -> oldSofaXmiId2NewSofaXmiId.put(newSofaXmiId, newSofaXmiId));
            LinkedHashMap<String, ByteArrayOutputStream> moduleData = createAnnotationModuleData(nodesByXmiId, oldSofaXmiId2NewSofaXmiId, annotationModules, ts);
            Map<Integer, String> reverseSofaIdMap = nextXmiIdAndSofaMap.right.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            log.debug("Returning XMI annotation module result");
            System.out.println(nextXmiIdAndSofaMap.right);
            return new XmiSplitterResult(moduleData, nextXmiIdAndSofaMap.left, namespaceMap, reverseSofaIdMap, nodesByXmiId.keySet().stream().filter(id -> id >= 0).map(nodesByXmiId::get).collect(Collectors.toList()));
        } catch (VTDException e) {
            throw new XMISplitterException(e);
        }
    }


    private Map<Integer, JeDISVTDGraphNode> createJedisNodes(VTDNav vn, Map<String, String> namespaceMap, TypeSystem ts) throws VTDException {
        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = new HashMap<>();
        currentSecondSofaMapKey = SECOND_SOFA_MAP_KEY_START;
        vn.toElement(VTDNav.FIRST_CHILD);
        vn.toElement(VTDNav.FIRST_CHILD);
        do {
            int xmiIdIndex = vn.getAttrVal("xmi:id");
            if (xmiIdIndex >= 0) {
                int i = vn.getCurrentIndex();
                int oldXmiId = Integer.parseInt(vn.toRawString(xmiIdIndex));
                String typeName = getTypeName(vn, namespaceMap, i);
                JeDISVTDGraphNode n = nodesByXmiId.computeIfAbsent(oldXmiId, typeName.equals(CAS_SOFA) ? SofaVTDGraphNode::new : JeDISVTDGraphNode::new);
                n.setElementFragment(vn.getElementFragment());
                n.setTypeName(typeName);
                int sofaAttrIndex = vn.getAttrVal("sofa");
                if (sofaAttrIndex > -1)
                    n.setSofaXmiId(Integer.parseInt(vn.toRawString(sofaAttrIndex)));
                else
                    n.setSofaXmiId(NO_SOFA_KEY);

                Map<String, List<Integer>> referencedXmiIds = getReferencedXmiIds(vn, n.getTypeName(), ts);
                n.setReferencedXmiIds(referencedXmiIds);
                referencedXmiIds.values().stream().flatMap(Collection::stream).map(refId -> nodesByXmiId.computeIfAbsent(refId, JeDISVTDGraphNode::new)).forEach(referenced -> referenced.addPredecessor(n));

                if (n.getTypeName().equals(CAS_SOFA)) {
                    nodesByXmiId.put(currentSecondSofaMapKey--, n);
                    ((SofaVTDGraphNode) n).setSofaID(vn.toRawString(vn.getAttrVal("sofaID")));
                }
            }
        } while (vn.toElement(VTDNav.NEXT_SIBLING));
        return nodesByXmiId;
    }

    private Map<String, List<Integer>> getReferencedXmiIds(VTDNav vn, String typeName, TypeSystem ts) throws NavException {
        if (typeName.equals(CAS_NULL) || typeName.equals(CAS_VIEW))
            return Collections.emptyMap();

        Map<String, List<Integer>> referencesByFeatureBaseName = new HashMap<>();
        Type annotationType = ts.getType(typeName);

        Function<String, List<Integer>> refAttributeValue2Integers = referenceString -> Stream.of(referenceString).filter(StringUtils::isNotBlank).map(refStr -> refStr.split("\\s+")).flatMap(Stream::of).map(Integer::parseInt).collect(toList());

        if (isFSArray(annotationType)) {
            String referenceString = vn.toRawString(vn.getAttrVal("elements"));
            referencesByFeatureBaseName.put("elements", refAttributeValue2Integers.apply(referenceString));
        } else if (!isFSArray(annotationType)) {
            List<Feature> features = annotationType.getFeatures();
            for (Feature f : features) {
                Type featureType = f.getRange();
                if (isFSArray(featureType) || !isPrimitive(featureType)) {
                    int attributeIndex = vn.getAttrVal(f.getShortName());
                    if (attributeIndex >= 0) {
                        String referenceString = vn.toRawString(attributeIndex);
                        referencesByFeatureBaseName.put(f.getShortName(), refAttributeValue2Integers.apply(referenceString));
                    }
                }
            }
        }

        return referencesByFeatureBaseName;
    }

    private String getTypeName(VTDNav vn, Map<String, String> namespaceMap, int i) throws NavException {
        String elementName = vn.toRawString(i);
        int indexOfColon = elementName.indexOf(':');
        String namespace = elementName.substring(0, indexOfColon);
        String name = elementName.substring(indexOfColon + 1);
        String nsUri = XmiSplitUtilities.convertNSUri(namespaceMap.get(namespace));
        return nsUri + name;
    }
}
