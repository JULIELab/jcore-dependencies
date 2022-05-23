package de.julielab.xml;

import com.google.common.collect.Sets;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.cas.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.CAS_SOFA;
import static java.util.stream.Collectors.toSet;

public abstract class AbstractXmiSplitter implements XmiSplitter {
private final static Logger log = LoggerFactory.getLogger(AbstractXmiSplitter.class);
    private static final int NO_SOFA_KEY = -1;
    private static final int SECOND_SOFA_MAP_KEY_START = -2;
    private static final int SOFA_UNKNOWN = Integer.MIN_VALUE;
    protected final Set<String> moduleAnnotationNames;
    protected final boolean recursively;
    protected final boolean storeBaseDocument;
    private final Set<String> baseDocumentAnnotations;
    protected int currentSecondSofaMapKey;
    protected Map<Integer, JeDISVTDGraphNode> nodesByXmiId;
    protected Map<String, Set<JeDISVTDGraphNode>> annotationModules;
    private Set<Integer> unavailableXmiId;

    public AbstractXmiSplitter(Set<String> moduleAnnotationNames, boolean recursively, boolean storeBaseDocument, Set<String> baseDocumentAnnotations) {
        this.moduleAnnotationNames = moduleAnnotationNames != null ? new HashSet<>(moduleAnnotationNames) : null;
        this.recursively = recursively;
        this.storeBaseDocument = storeBaseDocument;
        this.baseDocumentAnnotations = baseDocumentAnnotations == null ? Collections.emptySet() : baseDocumentAnnotations;

        if (storeBaseDocument)
            this.moduleAnnotationNames.add(DOCUMENT_MODULE_LABEL);

        if (moduleAnnotationNames != null && baseDocumentAnnotations != null && !Sets.intersection(moduleAnnotationNames, baseDocumentAnnotations).isEmpty())
            throw new IllegalArgumentException("The annotation types to build modules from and the annotation types to added to the base document overlap in: " + Sets.intersection(moduleAnnotationNames, baseDocumentAnnotations));
    }

    protected ImmutablePair<Integer, Map<String, Integer>> assignNewXmiIds(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Map<String, Integer> existingSofaIdMap, int nextPossibleId) {
        Map<String, Integer> updatedSofaIdMap = null != existingSofaIdMap ? new HashMap<>(existingSofaIdMap) : new HashMap<>();
        int currentXmiId = nextPossibleId;
        // Give the Sofa nodes their new XMI ID
        adaptSofaIdMap(nodesByXmiId, updatedSofaIdMap);
        for (String moduleName : annotationModules.keySet()) {
            if (!storeBaseDocument && moduleName.equals(DOCUMENT_MODULE_LABEL))
                continue;
            Set<JeDISVTDGraphNode> moduleNodes = annotationModules.get(moduleName);
            for (JeDISVTDGraphNode node : moduleNodes) {
                // For Sofas, we have done the XMI:ID assignment at the beginning of the method
                if (!(node instanceof SofaVTDGraphNode)) {
                    // Get the next free new xmi:id for this annotation
                    while (unavailableXmiId.contains(currentXmiId))
                        ++currentXmiId;
                    node.setNewXmiId(currentXmiId);
                    unavailableXmiId.add(currentXmiId);
                    ++currentXmiId;
                }
            }
        }
        return new ImmutablePair<>(currentXmiId, updatedSofaIdMap);
    }

    /**
     * Sets the correct XMI:ID values to the Sofa nodes. The values are either derived from <tt>updatedSofaIdMap</tt>,
     * if the sofaID value of the respective sofa is already contained there, or writes its own ID.
     * For Sofas that are not known in the <tt>updatedSofaIdMap</tt>, their original XMI:ID value will be recorded,
     * if the base document data is to be stored. Otherwise, these Sofa nodes will receive a new XMI:ID value equal
     * to {@link #SOFA_UNKNOWN} to indicate that no annotations referencing this sofa can be stored.
     *
     * @param nodesByXmiId
     * @param updatedSofaIdMap Should be a copy of the previous sofa ID map, if existing. When the base document is written, this should be an empty map that will be filled in this method.
     */
    protected void adaptSofaIdMap(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Map<String, Integer> updatedSofaIdMap) {
        unavailableXmiId = new HashSet<>();
        // The cas:NULL element always has the xmi:id 0
        unavailableXmiId.add(0);
        // Adapt and / or get the XMI IDs of the sofa elements that are valid in the document annotation module.
        // We need to keep the Sofa XMI ID constant across all modules. Otherwise, some annotations will
        // reference a wrong element since the Sofa XMI:ID can change across serializations.
        int sofaKey = SECOND_SOFA_MAP_KEY_START;
        int newSofaId = 1;
        while (nodesByXmiId.containsKey(sofaKey)) {
            SofaVTDGraphNode sofaNode = (SofaVTDGraphNode) nodesByXmiId.get(sofaKey);
            //Integer sofaXmiId = sofaNode.getOldXmiId();
            String sofaID = sofaNode.getSofaID();
            int newSofaXmiId;
            if (!updatedSofaIdMap.containsKey(sofaID)) {
                if (storeBaseDocument) {
                    updatedSofaIdMap.put(sofaID, newSofaId);
                    newSofaXmiId = newSofaId;
                    ++newSofaId;
                } else {
                    // This is the signal for "Annotations of this sofa cannot be stored because the Sofa
                    // Itself is not in the potentially existing document data and will also not be stored,
                    // so no annotations referencing it can be stored
                    newSofaXmiId = SOFA_UNKNOWN;
                }
            } else {
                newSofaXmiId = updatedSofaIdMap.get(sofaID);
            }

            sofaNode.setNewXmiId(newSofaXmiId);
            unavailableXmiId.add(newSofaXmiId);

            --sofaKey;
        }
    }

    protected Map<String, Set<JeDISVTDGraphNode>> createAnnotationModules(Map<Integer, JeDISVTDGraphNode> nodesByXmiId) {
        Map<String, Set<JeDISVTDGraphNode>> modules = new HashMap<>();
        for (JeDISVTDGraphNode node : nodesByXmiId.values()) {
            for (String label : node.getAnnotationModuleLabels()) {
                // Only create the modules for those labels actually to be stored
                if (moduleAnnotationNames.contains(label)) {
                    modules.compute(label, (l, list) -> {
                        Set<JeDISVTDGraphNode> ret;
                        if (list == null) ret = new HashSet<>();
                        else ret = list;
                        ret.add(node);
                        return ret;
                    });
                }
            }
        }
        return modules;
    }

    protected void labelNodes(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Set<String> moduleAnnotationNames, boolean recursively) {
        if (null == moduleAnnotationNames)
            return;
        for (JeDISVTDGraphNode node : nodesByXmiId.values()) {
            Stream<String> allLabels = determineLabelsForNode(node, moduleAnnotationNames, recursively, new HashSet<>());
            node.setAnnotationModuleLabels(allLabels.collect(toSet()));
        }
    }

    protected Stream<String> determineLabelsForNode(JeDISVTDGraphNode node, Set<String> moduleAnnotationNames, boolean recursively, HashSet<JeDISVTDGraphNode> alreadyVisited) {
        try {
            if (!alreadyVisited.add(node))
                return Stream.empty();
            if (node == JeDISVTDGraphNode.CAS_NULL)
                return Stream.empty();
            if (!node.getAnnotationModuleLabels().isEmpty())
                return node.getAnnotationModuleLabels().stream();
            Function<JeDISVTDGraphNode, Stream<String>> fetchLabelsRecursively = n -> n.getPredecessors().stream().flatMap(p -> determineLabelsForNode(p, moduleAnnotationNames, recursively, alreadyVisited));
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
        } catch (Throwable e) {
            log.error("Error when trying to determine the labels for node {}. Module annotation names are: {}", node, moduleAnnotationNames);
            throw e;
        }
    }

    Map<Integer, JeDISVTDGraphNode> getNodesByXmiId() {
        return nodesByXmiId;
    }

    protected abstract String getNodeXml(JeDISVTDGraphNode node) throws XMISplitterException;

    protected LinkedHashMap<String, ByteArrayOutputStream> createAnnotationModuleData(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Map<Integer, Integer> oldSofaXmiId2NewSofaXmiId, Map<String, Set<JeDISVTDGraphNode>> annotationModules, TypeSystem ts) throws XMISplitterException {
        LinkedHashMap<String, ByteArrayOutputStream> annotationModuleData = new LinkedHashMap<>();


        // referenced ID -> node and feature containing the reference
        Map<Integer, Set<Pair<JeDISVTDGraphNode, String>>> backwardReferences = new HashMap<>();
        for (JeDISVTDGraphNode n : nodesByXmiId.values()) {
            for (String feature : n.getReferencedXmiIds().keySet()) {
                for (Integer referencedElementIds : n.getReferencedXmiIds().get(feature))
                    backwardReferences.compute(referencedElementIds, (k, v) -> v != null ? v : new HashSet<>()).add(new ImmutablePair<>(n, feature));
            }
        }


        for (String moduleName : annotationModules.keySet()) {
            if (!storeBaseDocument && moduleName.equals(DOCUMENT_MODULE_LABEL))
                continue;
            Set<JeDISVTDGraphNode> moduleNodes = annotationModules.get(moduleName);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                annotationModuleData.put(moduleName, baos);

                // We now traverse all the nodes that should go in the current module.
                // We will adapt the XMI IDs and also remove features and even whole nodes when their
                // subsequent references are not stored at all.
                for (JeDISVTDGraphNode node : moduleNodes) {
                    if (node.getSofaXmiId() == SOFA_UNKNOWN)
                        throw new IllegalArgumentException("An annotation module is requested that belongs to a Sofa that is not present in existing document data and that is also not to be stored now. This would bring inconsistency into the stored data because some elements would refer to a Sofa that does not exist.");

                    String xmlElement = getNodeXml(node);
                    // Check the node references for dead links
                    for (String featureName : node.getReferencedXmiIds().keySet()) {
                        List<Integer> references = node.getReferencedXmiIds().get(featureName);
                        Optional<Integer> anyReferenceId = references.stream().filter(nodesByXmiId::containsKey).map(oldId -> oldId == 0 ? oldId : nodesByXmiId.get(oldId).getNewXmiId()).filter(Objects::nonNull).findAny();
                        if (!anyReferenceId.isPresent() && (XmiSplitUtilities.isListTypeName(node.getTypeName()) || ts.getType(node.getTypeName()).isArray())) {
                            xmlElement = null;
                        }
                    }
                    if (xmlElement == null) {
                        // Clear the annotation labels for this node because it should not be stored
                        node.getAnnotationModuleLabels().clear();
                        final Set<Pair<JeDISVTDGraphNode, String>> referencingNodes = backwardReferences.get(node.getOldXmiId());
                        for (Pair<JeDISVTDGraphNode, String> pairAndFeature : referencingNodes) {
                            // For all the nodes having features referencing the current node - which is to be deleted - remove the reference.
                            pairAndFeature.getLeft().getReferencedXmiIds().get(pairAndFeature.getRight()).remove(node.getOldXmiId());
                        }
                        nodesByXmiId.remove(node.getOldXmiId());
                    }
                }

                // Now update the XMI IDs of the elements that remain (still have an annotation module label)
                for (JeDISVTDGraphNode node : moduleNodes) {
                    if (!node.getAnnotationModuleLabels().isEmpty()) {
                        String xmlElement = getNodeXml(node);
                        int oldSofaXmiId = node.getSofaXmiId();
                        // Adapt sofa ID and xmi:id for this annotation
                        if (oldSofaXmiId != NO_SOFA_KEY) {
                            //xmlElement = xmlElement.replaceFirst("sofa=\"[0-9]+\"", "sofa=\"" + oldSofaXmiId2NewSofaXmiId.get(node.getSofaXmiId()) + "\"");
                            xmlElement = xmlElement.replaceFirst("sofa=\"[0-9]+\"", "sofa=\"" + nodesByXmiId.get(node.getSofaXmiId()).getNewXmiId() + "\"");
                        }
                        xmlElement = xmlElement.replaceFirst("xmi:id=\"[0-9]+\"", "xmi:id=\"" + node.getNewXmiId() + "\"");
                        // Update the XMI IDs of the references
                        for (String featureName : node.getReferencedXmiIds().keySet()) {
                            List<Integer> references = node.getReferencedXmiIds().get(featureName);
                            String newReferenceString = references.stream().filter(nodesByXmiId::containsKey).map(oldId -> oldId == 0 ? oldId : nodesByXmiId.get(oldId).getNewXmiId()).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(" "));
                            if (!newReferenceString.isBlank())
                                xmlElement = xmlElement.replaceFirst(featureName + "=\"[0-9\\s]+\"", featureName + "=\"" + newReferenceString + "\"");
                            else {
                                xmlElement = xmlElement.replaceFirst( featureName + "=\"[^\"]+\"", "");
                            }
                        }
                        node.setModuleData(xmlElement);
                        baos.write(node.getModuleXmlData().getBytes(StandardCharsets.UTF_8));
                    }
                }
            } catch (IOException e) {
                throw new XMISplitterException(e);
            }
        }
        return annotationModuleData;
    }

    Map<String, Set<JeDISVTDGraphNode>> getAnnotationModules() {
        return annotationModules;
    }
}
