package de.julielab.xml;

import com.google.common.collect.Sets;
import com.ximpleware.*;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.*;
import static java.util.stream.Collectors.toSet;

public class VtdXmlXmiSplitter extends XmiSplitter {

    public static final String DOCUMENT_MODULE_LABEL = "DOCUMENT-MODULE";
    private static final int SECOND_SOFA_MAP_KEY_START = -2;
    private final Set<String> moduleAnnotationNames;
    private final boolean recursively;
    private final boolean storeBaseDocument;
    private final String docTableName;
    private final Set<String> baseDocumentAnnotations;
    private int currentSecondSofaMapKey;
    private Map<Integer, JeDISVTDGraphNode> nodesByXmiId;
    private Map<String, Set<JeDISVTDGraphNode>> annotationModules;
    private VTDNav vn;

    public VtdXmlXmiSplitter(Set<String> moduleAnnotationNames, boolean recursively, boolean storeBaseDocument,
                             String docTableName, Set<String> baseDocumentAnnotations) {
        this.moduleAnnotationNames = moduleAnnotationNames;
        this.recursively = recursively;
        this.storeBaseDocument = storeBaseDocument;
        this.docTableName = docTableName;
        this.baseDocumentAnnotations = baseDocumentAnnotations == null ? Collections.emptySet() : baseDocumentAnnotations;

        if (moduleAnnotationNames != null && baseDocumentAnnotations != null && !Sets.intersection(moduleAnnotationNames, baseDocumentAnnotations).isEmpty())
            throw new IllegalArgumentException("The annotation types to build modules from and the annotation types to added to the base document overlap in: " + Sets.intersection(moduleAnnotationNames, baseDocumentAnnotations));
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
    public XmiSplitterResult process(byte[] xmiData, JCas aCas, int nextPossibleId, Map<String, Integer> existingSofaIdMap) throws XMISplitterException {


        VTDGen vg = new VTDGen();
        vg.setDoc(xmiData);
        try {
            vg.parse(true);
            vn = vg.getNav();
            Map<String, String> namespaceMap = JulieXMLTools.buildNamespaceMap(vn);
            nodesByXmiId = createJedisNodes(vn, namespaceMap, aCas);
            labelNodes(nodesByXmiId, moduleAnnotationNames, recursively);
            annotationModules = createAnnotationModules(nodesByXmiId);
            XmiSplitterResult xmiSplitterResult = createXmiSplitterResult(nodesByXmiId, annotationModules, existingSofaIdMap, nextPossibleId, vn);
            xmiSplitterResult.namespaces = namespaceMap;
            return xmiSplitterResult;


//            VTDGen annoModule = new VTDGen();
//            annoModule.setDoc("<jedisAnnotationModule></jedisAnnotationModule>".getBytes());
//            annoModule.parse(true);
//            VTDNav annoModNav = annoModule.getNav();
//            XMLModifier annoModMod = new XMLModifier(annoModNav);
//            annoModMod.insertAfterHead("hallo");
//            annoModMod.outputAndReparse();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            annoModMod.output(baos);

        } catch (VTDException e) {
            throw new XMISplitterException(e);
        }
    }

    private XmiSplitterResult createXmiSplitterResult(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Map<String, Set<JeDISVTDGraphNode>> annotationModules, Map<String, Integer> existingSofaIdMap, int nextPossibleId, VTDNav vn) throws NavException {
        Map<String, Integer> updatedSofaIdMap = null != existingSofaIdMap ? new HashMap<>(existingSofaIdMap) : new HashMap<>();
        int currentXmiId = nextPossibleId;
        LinkedHashMap<String, ByteArrayOutputStream> annotationModuleData = new LinkedHashMap<>();
        adaptSofaIdMap(nodesByXmiId, updatedSofaIdMap);
        for (String moduleName : annotationModules.keySet()) {
            Set<JeDISVTDGraphNode> moduleNodes = annotationModules.get(moduleName);
            for (JeDISVTDGraphNode node : moduleNodes) {


                String xmlElement = vn.toString(node.getByteOffset(), node.getByteLength());
            }
        }
        return new XmiSplitterResult(null, 0, null, null);
    }

    /**
     * Sets the correct XMI:ID values to the Sofa nodes. The values are either derived from <tt>updatedSofaIdMap</tt>,
     * if the sofaID value of the respective sofa is already contained there, or writes its own ID.
     * For Sofas that are not known in the <tt>updatedSofaIdMap</tt>, their original XMI:ID value will be recorded,
     * if the base document data is to be stored. Otherwise, these Sofa nodes will receive a new XMI:ID value equal
     * to <code>Integer.MIN_VALUE</code> to indicate that no annotations referencing this sofa can be stored.
     *
     * @param nodesByXmiId
     * @param updatedSofaIdMap
     */
    private void adaptSofaIdMap(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Map<String, Integer> updatedSofaIdMap) {
        // Adapt and / or get the XMI IDs of the sofa elements that are valid in the document annotation module.
        // We need to keep the Sofa XMI ID constant across all modules. Otherwise, some annotations will
        // reference a wrong element since the Sofa XMI:ID can change across serializations.
        int sofaKey = SECOND_SOFA_MAP_KEY_START;
        while (nodesByXmiId.containsKey(sofaKey)) {
            SofaVTDGraphNode sofaNode = (SofaVTDGraphNode) nodesByXmiId.get(sofaKey);
            Integer sofaXmiId = sofaNode.getOldXmiId();
            String sofaID = sofaNode.getSofaID();
            int newSofaXmiId;
            if (!updatedSofaIdMap.containsKey(sofaID)) {
                if (storeBaseDocument) {
                    updatedSofaIdMap.put(sofaID, sofaXmiId);
                    newSofaXmiId = sofaXmiId;
                } else {
                    // This is the signal for "Annotations of this sofa cannot be stored because the Sofa
                    // Itself is not in the potentially existing document data and will also not be stored,
                    // so no annotations referencing it can be stored
                    newSofaXmiId = Integer.MIN_VALUE;
                }
            } else {
                newSofaXmiId = updatedSofaIdMap.get(sofaID);
            }

            sofaNode.setNewXmiId(newSofaXmiId);

            --sofaKey;
        }
    }


    private Map<String, Set<JeDISVTDGraphNode>> createAnnotationModules(Map<Integer, JeDISVTDGraphNode> nodesByXmiId) {
        Map<String, Set<JeDISVTDGraphNode>> modules = new HashMap<>();
        for (JeDISVTDGraphNode node : nodesByXmiId.values()) {
            for (String label : node.getAnnotationModuleLabels()) {
                modules.compute(label, (l, list) -> {
                    Set<JeDISVTDGraphNode> ret;
                    if (list == null) ret = new HashSet<>();
                    else ret = list;
                    ret.add(node);
                    return ret;
                });
            }
        }
        return modules;
    }

    private void labelNodes(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Set<String> moduleAnnotationNames, boolean recursively) {
        if (null == moduleAnnotationNames)
            return;
        for (JeDISVTDGraphNode node : nodesByXmiId.values()) {
            Stream<String> allLabels = determineLabelsForNode(node, moduleAnnotationNames, recursively);
            node.setAnnotationModuleLabels(allLabels.collect(toSet()));
        }
    }

    private Stream<String> determineLabelsForNode(JeDISVTDGraphNode node, Set<String> moduleAnnotationNames, boolean recursively) {
        if (!node.getAnnotationModuleLabels().isEmpty())
            return node.getAnnotationModuleLabels().stream();
        Function<JeDISVTDGraphNode, Stream<String>> fetchLabelsRecursively = n -> n.getPredecessors().stream().flatMap(p -> determineLabelsForNode(p, moduleAnnotationNames, recursively));
        // All labels are "emitted" from annotations on the "to create a module for" list.
        if (XmiSplitUtilities.isAnnotationType(node.getTypeName())) {
            if (moduleAnnotationNames.contains(node.getTypeName()))
                return Stream.of(node.getTypeName());
            else if (storeBaseDocument && baseDocumentAnnotations.contains(node.getTypeName()))
                return Stream.of(DOCUMENT_MODULE_LABEL);
            else if (recursively)
                return fetchLabelsRecursively.apply(node);
            return Stream.empty();
        } else if (storeBaseDocument && node.getTypeName().equals(CAS_SOFA)) {
            return Stream.of(DOCUMENT_MODULE_LABEL);
        }
        return fetchLabelsRecursively.apply(node);
    }

    private Map<Integer, JeDISVTDGraphNode> createJedisNodes(VTDNav vn, Map<String, String> namespaceMap, JCas aCas) throws VTDException {
        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = new HashMap<>();
        currentSecondSofaMapKey = SECOND_SOFA_MAP_KEY_START;
        vn.toElement(VTDNav.FIRST_CHILD);
        vn.toElement(VTDNav.FIRST_CHILD);
        do {
            Integer xmiIdIndex = vn.getAttrVal("xmi:id");
            if (xmiIdIndex >= 0) {
                int i = vn.getCurrentIndex();
                int oldXmiId = Integer.parseInt(vn.toString(xmiIdIndex));
                String typeName = getTypeName(vn, namespaceMap, i);
                JeDISVTDGraphNode n = nodesByXmiId.computeIfAbsent(oldXmiId, typeName.equals(CAS_SOFA) ? SofaVTDGraphNode::new : JeDISVTDGraphNode::new);
                n.setVtdIndex(i);
                n.setElementFragment(vn.getElementFragment());
                n.setTypeName(typeName);
                int sofaAttrIndex = vn.getAttrVal("sofa");
                if (sofaAttrIndex > -1)
                    n.setSofaXmiId(Integer.parseInt(vn.toString(sofaAttrIndex)));
                Stream<Integer> referencedXmiIds = getReferencedXmiIds(vn, n.getTypeName(), aCas.getTypeSystem());
                referencedXmiIds.map(refId -> nodesByXmiId.computeIfAbsent(refId, JeDISVTDGraphNode::new)).forEach(referenced -> referenced.addPredecessor(n));

                if (n.getTypeName().equals(CAS_SOFA))
                    nodesByXmiId.put(currentSecondSofaMapKey--, n);
            }
        } while (vn.toElement(VTDNav.NEXT_SIBLING));
        return nodesByXmiId;
    }

    private Stream<Integer> getReferencedXmiIds(VTDNav vn, String typeName, TypeSystem ts) throws NavException {
        if (typeName.equals(CAS_NULL) || typeName.equals(CAS_VIEW))
            return Stream.empty();
        Type annotationType = ts.getType(typeName);

        Stream.Builder<String> referenceStreamBuilder = Stream.builder();
        if (isFSArray(annotationType)) {
            String referenceString = vn.toString(vn.getAttrVal("elements"));
            referenceStreamBuilder.accept(referenceString);
        } else if (!isFSArray(annotationType)) {
            List<Feature> features = annotationType.getFeatures();
            for (Feature f : features) {
                Type featureType = f.getRange();
                if (isFSArray(featureType) || !isPrimitive(featureType)) {
                    int attributeIndex = vn.getAttrVal(f.getShortName());
                    if (attributeIndex >= 0) {
                        String referenceString = vn.toString(attributeIndex);
                        referenceStreamBuilder.accept(referenceString);
                    }
                }
            }
        }

        return referenceStreamBuilder.build().filter(StringUtils::isNotBlank).map(refStr -> refStr.split("\\s+")).flatMap(Stream::of).map(Integer::parseInt);
    }

    private String getTypeName(VTDNav vn, Map<String, String> namespaceMap, int i) throws NavException {
        String elementName = vn.toString(i);
        int indexOfColon = elementName.indexOf(':');
        String namespace = elementName.substring(0, indexOfColon);
        String name = elementName.substring(indexOfColon + 1);
        String nsUri = XmiSplitUtilities.convertNSUri(namespaceMap.get(namespace));
        return nsUri + name;
    }
}
