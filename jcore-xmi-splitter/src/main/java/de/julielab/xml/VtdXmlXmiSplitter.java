package de.julielab.xml;

import com.google.common.collect.Sets;
import com.ximpleware.*;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class VtdXmlXmiSplitter implements XmiSplitter {
private final static Logger log = LoggerFactory.getLogger(VtdXmlXmiSplitter.class);
    public static final String DOCUMENT_MODULE_LABEL = "DOCUMENT-MODULE";
    private static final int SECOND_SOFA_MAP_KEY_START = -2;
    private static final int NO_SOFA_KEY = -1;
    private static final int SOFA_UNKNOWN = Integer.MIN_VALUE;
    private final Set<String> moduleAnnotationNames;
    private final boolean recursively;
    private final boolean storeBaseDocument;
    private final String docTableName;
    private final Set<String> baseDocumentAnnotations;
    private int currentSecondSofaMapKey;
    private Map<Integer, JeDISVTDGraphNode> nodesByXmiId;
    private Map<String, Set<JeDISVTDGraphNode>> annotationModules;
    private Set<Integer> unavailableXmiId;
    private VTDNav vn;


    public VtdXmlXmiSplitter(Set<String> moduleAnnotationNames, boolean recursively, boolean storeBaseDocument,
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
            log.debug("Building namespace map");
            Map<String, String> namespaceMap = JulieXMLTools.buildNamespaceMap(vn);
            log.debug("Creating JeDIS nodes");
            nodesByXmiId = createJedisNodes(vn, namespaceMap, aCas);
            log.debug("Labeling nodes");
            labelNodes(nodesByXmiId, moduleAnnotationNames, recursively);
            log.debug("Creating annotation modules");
            annotationModules = createAnnotationModules(nodesByXmiId);
            log.debug("Assigning new XMI IDs");
            ImmutablePair<Integer, Map<String, Integer>> nextXmiIdAndSofaMap = assignNewXmiIds(nodesByXmiId, existingSofaIdMap, nextPossibleId);
            log.debug("Slicing XMI data into annotation module data");
            LinkedHashMap<String, ByteArrayOutputStream> moduleData = createAnnotationModuleData(nodesByXmiId, annotationModules, existingSofaIdMap, nextPossibleId, vn);
            Map<Integer, String> reverseSofaIdMap = nextXmiIdAndSofaMap.right.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            log.debug("Returning XMI annotation module result");
            return new XmiSplitterResult(moduleData, nextXmiIdAndSofaMap.left, namespaceMap, reverseSofaIdMap);
        } catch (VTDException e) {
            throw new XMISplitterException(e);
        }
    }

    private ImmutablePair<Integer, Map<String, Integer>> assignNewXmiIds(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Map<String, Integer> existingSofaIdMap, int nextPossibleId) {
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

    private LinkedHashMap<String, ByteArrayOutputStream> createAnnotationModuleData(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Map<String, Set<JeDISVTDGraphNode>> annotationModules, Map<String, Integer> existingSofaIdMap, int nextPossibleId, VTDNav vn) throws NavException, XMISplitterException {
        LinkedHashMap<String, ByteArrayOutputStream> annotationModuleData = new LinkedHashMap<>();
        for (String moduleName : annotationModules.keySet()) {
            if (!storeBaseDocument && moduleName.equals(DOCUMENT_MODULE_LABEL))
                continue;
            Set<JeDISVTDGraphNode> moduleNodes = annotationModules.get(moduleName);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                annotationModuleData.put(moduleName.equals(DOCUMENT_MODULE_LABEL) ? docTableName : moduleName, baos);
                for (JeDISVTDGraphNode node : moduleNodes) {
                    if (node.getSofaXmiId() == SOFA_UNKNOWN)
                        throw new IllegalArgumentException("An annotation module is requested that belongs to a Sofa that is not present in existing document data and that is also not to be stored now. This would bring inconsistency into the stored data because some elements would refer to a Sofa that does not exist.");

                    String xmlElement = vn.toRawString(node.getByteOffset(), node.getByteLength());
                    int oldSofaXmiId = node.getSofaXmiId();
                    // Adapt sofa ID and xmi:id for this annotation
                    if (oldSofaXmiId != NO_SOFA_KEY) {
                        xmlElement = xmlElement.replaceFirst("sofa=\"[0-9]+\"", "sofa=\"" + nodesByXmiId.get(node.getSofaXmiId()).getNewXmiId() + "\"");
                    }
                    xmlElement = xmlElement.replaceFirst("xmi:id=\"[0-9]+\"", "xmi:id=\"" + node.getNewXmiId() + "\"");
                    // Update the XMI IDs of the references
                    for (String featureName : node.getReferencedXmiIds().keySet()) {
                        List<Integer> references = node.getReferencedXmiIds().get(featureName);
                        String newReferenceString = references.stream().map(oldId -> nodesByXmiId.get(oldId).getNewXmiId()).map(String::valueOf).collect(Collectors.joining(" "));
                        xmlElement = xmlElement.replaceFirst(featureName + "=\"[0-9\\s]+\"", featureName + "=\"" + newReferenceString + "\"");
                    }
                    baos.write(xmlElement.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new XMISplitterException(e);
            }
        }
        return annotationModuleData;
    }

    /**
     * Sets the correct XMI:ID values to the Sofa nodes. The values are either derived from <tt>updatedSofaIdMap</tt>,
     * if the sofaID value of the respective sofa is already contained there, or writes its own ID.
     * For Sofas that are not known in the <tt>updatedSofaIdMap</tt>, their original XMI:ID value will be recorded,
     * if the base document data is to be stored. Otherwise, these Sofa nodes will receive a new XMI:ID value equal
     * to {@link #SOFA_UNKNOWN} to indicate that no annotations referencing this sofa can be stored.
     *
     * @param nodesByXmiId
     * @param updatedSofaIdMap
     */
    private void adaptSofaIdMap(Map<Integer, JeDISVTDGraphNode> nodesByXmiId, Map<String, Integer> updatedSofaIdMap) {
        unavailableXmiId = new HashSet<>();
        // The cas:NULL element always has the xmi:id 0
        unavailableXmiId.add(0);
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

    private Map<String, Set<JeDISVTDGraphNode>> createAnnotationModules(Map<Integer, JeDISVTDGraphNode> nodesByXmiId) {
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
                int oldXmiId = Integer.parseInt(vn.toRawString(xmiIdIndex));
                String typeName = getTypeName(vn, namespaceMap, i);
                JeDISVTDGraphNode n = nodesByXmiId.computeIfAbsent(oldXmiId, typeName.equals(CAS_SOFA) ? SofaVTDGraphNode::new : JeDISVTDGraphNode::new);
                n.setVtdIndex(i);
                n.setElementFragment(vn.getElementFragment());
                n.setTypeName(typeName);
                int sofaAttrIndex = vn.getAttrVal("sofa");
                if (sofaAttrIndex > -1)
                    n.setSofaXmiId(Integer.parseInt(vn.toRawString(sofaAttrIndex)));
                else
                    n.setSofaXmiId(NO_SOFA_KEY);

                Map<String, List<Integer>> referencedXmiIds = getReferencedXmiIds(vn, n.getTypeName(), aCas.getTypeSystem());
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

    public static void main(String args[]) {
        String s = "elements=\"2908 2920 2932 2944 2956 2968 2980 2992 3004 3016 3028 3040 3052 3064 3076 3088 3100 3112 3124 3136 3148 3160 3172 3184 3196 3208 3220 3232 3244 3256 3268 3280 3292 3304 3316 3328 3340 3352 3364 3376 3388 3400 3412 3424 3436 3448 3460 3472 3484 3496 3508 3520 3532 3544 3556 3568 3580 3592 3604 3616 3628 3640 3652 3664 3676 3688 3700 3712 3724 3736 3748 3760 3772 3784 3796 3808 3820 3832 3844 3856 3868 3880 3892 3904 3916 3928 3940 3952 3964 3976 3988 4000 4012 4024 4036 4048 4060 4072 4084 4096 4108 4120 4132 4144 4156 4168 4180 4192 4204 4216 4228 4240 4252 4264 4276 4288 4300 4312 4324 4336 4348 4360 4372 4384 4396 4408 4420 4432 4444 4456 4468 4480 4492 4504 4516 4528 4540 4552 4564 4576 4588 4600 4612 4624 4636 4648 4660 4672 4684 4696 4708 4720 4732 4744 4756 4768 4780 4792 4804 4816 4828 4840 4852 4864 4876 4888 4900 4912 4924 4936 4948 4960 4972 4984 4996 5008 5020 5032 5044 5056 5068 5080 5092 5104 5116 5128 5140 5152 5164 5176 5188 5200 5212 5224 5236 5248 5260 5272 5284 5296 5308 5320 5332 5344 5356 5368 5380 5392 5404 5416 5428 5440 5452 5464 5476 5488 5500 5512 5524 5536 5548 5560 5572 5584 5596 5608 5620 5632 5644 5656 5668 5680 5692 5704 5716 5728 5740 5752 5764 5776 5788 5800 5812 5824 5836 5848 5860 5872 5884 5896 5908 5920 5932 5944 5956 5968 5980 5992 6004 6016 6028 6040 6052 6064 6076 6088 6100 6112 6124 6136 6148 6160 6172 6184 6196 6208 6220 6232 6244 6256 6268 6280 6292 6304 6316 6328 6340 6352 6364 6376 6388 6400 6412 6424 6436 6448 6460 6472 6484 6496 6508 6520 6532 6544 6556 6568 6580 6592 6604 6616 6628 6640 6652 6664 6676 6688 6700 6712 6724 6736 6748 6760 6772 6784 6796 6808 6820 6832 6844 6856 6868 6880 6892 6904 6916 6928 6940 6952 6964 6976 6988 7000 7012 7024 7036 7048 7060 7072 7084 7096 7108 7120 7132 7144 7156 7168 7180 7192 7204 7216 7228 7240 7252 7264 7276 7288 7300 7312 7324 7336 7348 7360 7372 7384 7396 7408 7420 7432 7444 7456 7468 7480 7492 7504 7516 7528 7540 7552 7564 7576 7588 7600 7612 7624 7636 7648 7660 7672 7684 7696 7708 7720 7732 7744 7756 7768 7780 7792 7804 7816 7828 7840 7852 7864 7876 7888 7900 7912 7924 7936 7948 7960 7972 7984 7996 8008 8020 8032 8044 8056 8068 8080 8092 8104 8116 8128 8140 8152 8164 8176 8188 8200 8212 8224 8236 8248 8260 8272 8284 8296 8308 8320 8332 8344 8356 8368 8380 8392 8404 8416 8428 8440 8452 8464 8476 8488 8500 8512 8524 8536 8548 8560 8572 8584 8596 8608 8620 8632 8644 8656 8668 8680 8692 8704 8716 8728 8740 8752 8764 8776 8788 8800 8812 8824 8836 8848 8860 8872 8884 8896 8908 8920 8932 8944 8956 8968 8980 8992 9004 9016 9028 9040 9052 9064 9076 9088 9100 9112 9124 9136 9148 9160 9172 9184 9196 9208 9220 9232 9244 9256 9268 9280 9292 9304 9316 9328 9340 9352 9364 9376 9388 9400 9412 9424 9436 9448 9460 9472 9484 9496 9508 9520 9532 9544 9556 9568 9580 9592 9604 9616 9628 9640 9652 9664 9676 9688 9700 9712 9724 9736 9748 9760 9772 9784 9796 9808 9820 9832 9844 9856 9868 9880 9892 9904 9916 9928 9940 9952 9964 9976 9988 10000 10012 10024 10036 10048 10060 10072 10084 10096 10108 10120 10132 10144 10156 10168 10180 10192 10204 10216 10228 10240 10252 10264 10276 10288 10300 10312 10324 10336 10348 10360 10372 10384 10396 10408 10420 10432 10444 10456 10468 10480 10492 10504 10516 10528 10540 10552 10564 10576 10588 10600 10612 10624 10636 10648 10660 10672 10684 10696 10708 10720 10732 10744 10756 10768 10780 10792 10804 10816 10828 10840 10852 10864 10876 10888 10900 10912 10924 10936 10948 10960 10972 10984 10996 11008 11020 11032 11044 11056 11068 11080 11092 11104 11116 11128 11140 11152 11164 11176 11188 11200 11212 11224 11236 11248 11260 11272 11284 11296 11308 11320 11332 11344 11356 11368 11380 11392 11404 11416 11428 11440 11452 11464 11476 11488 11500 11512 11524 11536 11548 11560 11572 11584 11596 11608 11620 11632 11644 11656 11668 11680 11692 11704 11716 11728 11740 11752 11764 11776 11788 11800 11812 11824 11836 11848 11860 11872 11884 11896 11908 11920 11932 11944 11956 11968 11980 11992 12004 12016 12028 12040 12052 12064 12076 12088 12100 12112 12124 12136 12148 12160 12172 12184 12196 12208 12220 12232 12244 12256 12268 12280 12292 12304 12316 12328 12340 12352 12364 12376 12388 12400 12412 12424 12436 12448 12460 12472 12484 12496 12508 12520 12532 12544 12556 12568 12580 12592 12604 12616 12628 12640 12652 12664 12676 12688 12700 12712 12724 12736 12748 12760 12772 12784 12796 12808 12820 12832 12844 12856 12868 12880 12892 12904 12916 12928 12940 12952 12964 12976 12988 13000 13012 13024 13036 13048 13060 13072 13084 13096 13108 13120 13132 13144 13156 13168 13180 13192 13204 13216 13228 13240 13252 13264 13276 13288 13300 13312 13324 13336 13348 13360 13372 13384 13396 13408 13420 13432 13444 13456 13468 13480 13492 13504 13516 13528 13540 13552 13564 13576 13588 13600 13612 13624 13636 13648 13660 13672 13684 13696 13708 13720 13732 13744 13756 13768 13780 13792 13804 13816 13828 13840 13852 13864 13876 13888 13900 13912 13924 13936 13948 13960 13972 13984 13996 14008 14020 14032 14044 14056 14068 14080 14092 14104 14116 14128 14140 14152 14164 14176 14188 14200 14212 14224 14236 14248 14260 14272 14284 14296 14308 14320 14332 14344 14356 14368 14380 14392 14404 14416 14428 14440 14452 14464 14476 14488 14500 14512 14524 14536 14548 14560 14572 14584 14596 14608 14620 14632 14644 14656 14668 14680 14692 14704 14716 14728 14740 14752 14764 14776 14788 14800 14812 14824 14836 14848 14860 14872 14884 14896 14908 14920 14932 14944 14956 14968 14980 14992 15004 15016 15028 15040 15052 15064 15076 15088 15100 15112 15124 15136 15148 15160 15172 15184 15196 15208 15220 15232 15244 15256 15268 15280 15292 15304 15316 15328 15340 15352 15364 15376 15388 15400 15412 15424 15436 15448 15460 15472 15484 15496 15508 15520 15532 15544 15556 15568 15580 15592 15604 15616 15628 15640 15652 15664 15676 15688 15700 15712 15724 15736 15748 15760 15772 15784 15796 15808 15820 15832 15844 15856 15868 15880 15892 15904 15916 15928 15940 15952 15964 15976 15988 16000 16012 16024 16036 16048 16060 16072 16084 16096 16108 16120 16132 16144 16156 16168 16180 16192 16204 16216 16228 16240 16252 16264 16276 16288 16300 16312 16324 16336 16348 16360 16372 16384 16396 16408 16420 16432 16444 16456 16468 16480 16492 16504 16516 16528 16540 16552 16564 16576 16588 16600 16612 16624 16636 16648 16660 16672 16684 16696 16708 16720 16732 16744 16756 16768 16780 16792 16804 16816 16828 16840 16852 16864 16876 16888 16900 16912 16924 16936 16948 16960 16972 16984 16996 17008 17020 17032 17044 17056 17068 17080 17092 17104 17116 17128 17140 17152 17164 17176 17188 17200 17212 17224 17236 17248 17260 17272 17284 17296 17308 17320 17332 17344 17356 17368 17380 17392 17404 17416 17428 17440 17452 17464 17476 17488 17500 17512 17524 17536 17548 17560 17572 17584 17596 17608 17620 17632 17644 17656 17668 17680 17692 17704 17716 17728 17740 17752 17764 17776 17788 17800 17812 17824 17836 17848 17860 17872 17884 17896 17908 17920 17932 17944 17956 17968 17980 17992 18004 18016 18028 18040 18052 18064 18076 18088 18100 18112 18124 18136 18148 18160 18172 18184 18196 18208 18220 18232 18244 18256 18268 18280 18292 18304 18316 18328 18340 18352 18364 18376 18388 18400 18412 18424 18436 18448 18460 18472 18484 18496 18508 18520 18532 18544 18556 18568 18580 18592 18604 18616 18628 18640 18652 18664 18676 18688 18700 18712 18724 18736 18748 18760 18772 18784 18796 18808 18820 18832 18844 18856 18868 18880 18892 18904 18916 18928 18940 18952 18964 18976 18988 19000 19012 19024 19036 19048 19060 19072 19084 19096 19108 19120 19132 19144 19156 19168 19180 19192 19204 19216 19228 19240 19252 19264 19276 19288 19300 19312 19324 19336 19348 19360 19372 19384 19396 19408 19420 19432 19444 19456 19468 19480 19492 19504 19516 19528 19540 19552 19564 19576 19588 19600 19612 19624 19636 19648 19660 19672 19684 19696 19708 19720 19732 19744 19756 19768 19780 19792 19804 19816 19828 19840 19852 19864 19876 19888 19900 19912 19924 19936 19948 19960 19972 19984 19996 20008 20020 20032 20044 20056 20068 20080 20092 20104 20116 20128 20140 20152 20164 20176 20188 20200 20212 20224 20236 20248 20260 20272 20284 20296 20308 20320 20332 20344 20356 20368 20380 20392 20404 20416 20428 20440 20452 20464 20476 20488 20500 20512 20524 20536 20548 20560 20572 20584 20596 20608 20620 20632 20644 20656 20668 20680 20692 20704 20716 20728 20740 20752 20764 20776 20788 20800 20812 20824 20836 20848 20860 20872 20884 20896 20908 20920 20932 20944 20956 20968 20980 20992 21004 21016 21028 21040 21052 21064 21076 21088 21100 21112 21124 21136 21148 21160 21172 21184 21196 21208 21220 21232 21244 21256 21268 21280 21292 21304 21316 21328 21340 21352 21364 21376 21388 21400 21412 21424 21436 21448 21460 21472 21484 21496 21508 21520 21532 21544 21556 21568 21580 21592 21604 21616 21628 21640 21652 21664 21676 21688 21700 21712 21724 21736 21748 21760 21772 21784 21796 21808 21820 21832 21844 21856 21868 21880 21892 21904 21916 21928 21940 21952 21964 21976 21988 22000 22012 22024 22036 22048 22060 22072 22084 22096 22108 22120 22132 22144 22156 22168 22180 22192 22204 22216 22228 22240 22252 22264 22276 22288 22300 22312 22324 22336 22348 22360 22372 22384 22396 22408 22420 22432 22444 22456 22468 22480 22492 22504 22516 22528 22540 22552 22564 22576 22588 22600 22612 22624 22636 22648 22660 22672 22684 22696 22708 22720 22732 22744 22756 22768 22780 22792 22804 22816 22828 22840 22852 22864 22876 22888 22900 22912 22924 22936 22948 22960 22972 22984 22996 23008 23020 23032 23044 23056 23068 23080 23092 23104 23116 23128 23140 23152 23164 23176 23188 23200 23212 23224 23236 23248 23260 23272 23284 23296 23308 23320 23332 23344 23356 23368 23380 23392 23404 23416 23428 23440 23452 23464 23476 23488 23500 23512 23524 23536 23548 23560 23572 23584 23596 23608 23620 23632 23644 23656 23668 23680 23692 23704 23716 23728 23740 23752 23764 23776 23788 23800 23812 23824 23836 23848 23860 23872 23884 23896 23908 23920 23932 23944 23956 23968 23980 23992 24004 24016 24028 24040 24052 24064 24076 24088 24100 24112 24124 24136 24148 24160 24172 24184 24196 24208 24220 24232 24244 24256 24268 24280 24292 24304 24316 24328 24340 24352 24364 24376 24388 24400 24412 24424 24436 24448 24460 24472 24484 24496 24508 24520 24532 24544 24556 24568 24580 24592 24604 24616 24628 24640 24652 24664 24676 24688 24700 24712 24724 24736 24748 24760 24772 24784 24796 24808 24820 24832 24844 24856 24868 24880 24892 24904 24916 24928 24940 24952 24964 24976 24988 25000 25012 25024 25036 25048 25060 25072 25084 25096 25108 25120 25132 25144 25156 25168 25180 25192 25204 25216 25228 25240 25252 25264 25276 25288 25300 25312 25324 25336 25348 25360 25372 25384 25396 25408 25420 25432 25444 25456 25468 25480 25492 25504 25516 25528 25540 25552 25564 25576 25588 25600 25612 25624 25636 25648 25660 25672 25684 25696 25708 25720 25732 25744 25756 25768 25780 25792 25804 25816 25828 25840 25852 25864 25876 25888 25900 25912 25924 25936 25948 25960 25972 25984 25996 26008 26020 26032 26044 26056 26068 26080 26092 26104 26116 26128 26140 26152 26164 26176 26188 26200 26212 26224 26236 26248 26260 26272 26284 26296 26308 26320 26332 26344 26356 26368 26380 26392 26404 26416 26428 26440 26452 26464 26476 26488 26500 26512 26524 26536 26548 26560 26572 26584 26596 26608 26620 26632 26644 26656 26668 26680 26692 26704 26716 26728 26740 26752 26764 26776 26788 26800 26812 26824 26836 26848 26860 26872 26884 26896 26908 26920 26932 26944 26956 26968 26980 26992 27004 27016 27028 27040 27052 27064 27076 27088 27100 27112 27124 27136 27148 27160 27172 27184 27196 27208 27220 27232 27244 27256 27268 27280 27292 27304 27316 27328 27340 27352 27364 27376 27388 27400 27412 27424 27436 27448 27460 27472 27484 27496 27508 27520 27532 27544 27556 27568 27580 27592 27604 27616 27628 27640 27652 27664 27676 27688 27700 27712 27724 27736 27748 27760 27772 27784 27796 27808 27820 27832 27844 27856 27868 27880 27892 27904 27916 27928 27940 27952 27964 27976 27988 28000 28012 28024 28036 28048 28060 28072 28084 28096 28108 28120 28132 28144 28156 28168 28180 28192 28204 28216 28228 28240 28252 28264 28276 28288 28300 28312 28324 28336 28348 28360 28372 28384 28396 28408 28420 28432 28444 28456 28468 28480 28492 28504 28516 28528 28540 28552 28564 28576 28588 28600 28612 28624 28636 28648 28660 28672 28684 28696 28708 28720 28732 28744 28756 28768 28780 28792 28804 28816 28828 28840 28852 28864 28876 28888 28900 28912 28924 28936 28948 28960 28972 28984 28996 29008 29020 29032 29044 29056 29068 29080 29092 29104 29116 29128 29140 29152 29164 29176 29188 29200 29212 29224 29236 29248 29260 29272 29284 29296 29308 29320 29332 29344 29356 29368 29380 29392 29404 29416 29428 29440 29452 29464 29476 29488 29500 29512 29524 29536 29548 29560 29572 29584 29596 29608 29620 29632 29644 29656 29668 29680 29692 29704 29716 29728 29740 29752 29764 29776 29788 29800 29812 29824 29836 29848 29860 29872 29884 29896 29908 29920 29932 29944 29956 29968 29980 29992 30004 30016 30028 30040 30052 30064 30076 30088 30100 30112 30124 30136 30148 30160 30172 30184 30196 30208 30220 30232 30244 30256 30268 30280 30292 30304 30316 30328 30340 30352 30364 30376 30388 30400 30412 30424 30436 30448 30460 30472 30484 30496 30508 30520 30532 30544 30556 30568 30580 30592 30604 30616 30628 30640 30652 30664 30676 30688 30700 30712 30724 30736 30748 30760 30772 30784 30796 30808 30820 30832 30844 30856 30868 30880 30892 30904 30916 30928 30940 30952 30964 30976 30988 31000 31012 31024 31036 31048 31060 31072 31084 31096 31108 31120 31132 31144 31156 31168 31180 31192 31204 31216 31228 31240 31252 31264 31276 31288 31300 31312 31324 31336 31348 31360 31372 31384 31396 31408 31420 31432 31444 31456 31468 31480 31492 31504 31516 31528 31540 31552 31564 31576 31588 31600 31612 31624 31636 31648 31660 31672 31684 31696 31708 31720 31732 31744 31756 31768 31780 31792 31804 31816 31828 31840 31852 31864 31876 31888 31900 31912 31924 31936 31948 31960 31972 31984 31996 32008 32020 32032 32044 32056 32068 32080 32092 32104 32116 32128 32140 32152 32164 32176 32188 32200 32212 32224 32236 32248 32260 32272 32284 32296 32308 32320 32332 32344 32356 32368 32380 32392 32404 32416 32428 32440 32452 32464 32476 32488 32500 32512 32524 32536 32548 32560 32572 32584 32596 32608 32620 32632 32644 32656 32668 32680 32692 32704 32716 32728 32740 32752 32764 32776 32788 32800 32812 32824 32836 32848 32860 32872 32884 32896 32908 32920 32932 32944 32956 32968 32980 32992 33004 33016 33028 33040 33052 33064 33076 33088 33100 33112 33124 33136 33148 33160 33172 33184 33196 33208 33220 33232 33244 33256 33268 33280 33292 33304 33316 33328 33340 33352 33364 33376 33388 33400 33412 33424 33436 33448 33460 33472 33484 33496 33508 33520 33532 33544 33556 33568 33580 33592 33604 33616 33628 33640 33652 33664 33676 33688 33700 33712 33724 33736 33748 33760 33772 33784 33796 33808 33820 33832 33844 33856 33868 33880 33892 33904 33916 33928 33940 33952 33964 33976 33988 34000 34012 34024 34036 34048 34060 34072 34084 34096 34108 34120 34132 34144 34156 34168 34180 34192 34204 34216 34228 34240 34252 34264 34276 34288 34300 34312 34324 34336 34348 34360 34372 34384 34396 34408 34420 34432 34444 34456 34468 34480 34492 34504 34516 34528 34540 34552 34564 34576 34588 34600 34612 34624 34636 34648 34660 34672 34684 34696 34708 34720 34732 34744 34756 34768 34780 34792 34804 34816 34828 34840 34852 34864 34876 34888 34900 34912 34924 34936 34948 34960 34972 34984 34996 35008 35020 35032 35044 35056 35068 35080 35092 35104 35116 35128 35140 35152 35164 35176 35188 35200 35212 35224 35236 35248 35260 35272 35284 35296 35308 35320 35332 35344 35356 35368 35380 35392 35404 35416 35428 35440 35452 35464 35476 35488 35500 35512 35524 35536 35548 35560 35572 35584 35596 35608 35620 35632 35644 35656 35668 35680 35692 35704 35716 35728 35740 35752 35764 35776 35788 35800 35812 35824 35836 35848 35860 35872 35884 35896 35908 35920 35932 35944 35956 35968 35980 35992 36004 36016 36028 36040 36052 36064 36076 36088 36100 36112 36124 36136 36148 36160 36172 36184 36196 36208 36220 36232 36244 36256 36268 36280 36292 36304 36316 36328 36340 36352 36364 36376 36388 36400 36412 36424 36436 36448 36460 36472 36484 36496 36508 36520 36532 36544 36556 36568 36580 36592 36604 36616 36628 36640 36652 36664 36676 36688 36700 36712 36724 36736 36748 36760 36772 36784 36796 36808 36820 36832 36844 36856 36868 36880 36892 36904 36916 36928 36940 36952 36964 36976 36988 37000 37012 37024 37036 37048 37060 37072 37084 37096 37108 37120 37132 37144 37156 37168 37180 37192\"";
        String t = s.replaceFirst("elements" + "=\"[0-9\\s]+\"", "elements" + "=\"" + "hallo" + "\"");
        System.out.println(t);
    }
}
