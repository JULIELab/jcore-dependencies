package de.julielab.xml;

import com.ximpleware.*;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.julielab.xml.XmiSplitUtilities.isFSArray;
import static de.julielab.xml.XmiSplitUtilities.isPrimitive;

public class VtdXmlXmiSplitter extends XmiSplitter {

    private static final String CAS_NULL = "uima.cas.NULL";
    private static final String CAS_VIEW = "uima.cas.View";
    private Map<Integer, JeDISVTDGraphNode> nodesByXmiId;
    private VTDNav vn;

    public VTDNav getVTDNav() {
        return vn;
    }

    public Map<Integer, JeDISVTDGraphNode> getNodesByXmiId() {
        return nodesByXmiId;
    }

    @Override
    public XmiSplitterResult process(byte[] xmiData, JCas aCas, int nextPossibleId, Map<String, Integer> existingSofaIdMap) throws XMISplitterException {
        nodesByXmiId = new HashMap<>();

        VTDGen vg = new VTDGen();
        vg.setDoc(xmiData);
        try {
            vg.parse(true);
            vn = vg.getNav();
            createJedisNodes(aCas, vn);


            VTDGen annoModule = new VTDGen();
            annoModule.setDoc("<jedisAnnotationModule></jedisAnnotationModule>".getBytes());
            annoModule.parse(true);
            VTDNav annoModNav = annoModule.getNav();
            XMLModifier annoModMod = new XMLModifier(annoModNav);
            annoModMod.insertAfterHead("hallo");
            annoModMod.outputAndReparse();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            annoModMod.output(baos);

        } catch (ParseException e) {
            throw new XMISplitterException(e);
        } catch (VTDException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createJedisNodes(JCas aCas, VTDNav vn) throws VTDException {
        Map<String, String> namespaceMap = JulieXMLTools.buildNamespaceMap(vn);
        vn.toElement(VTDNav.FIRST_CHILD);
        vn.toElement(VTDNav.FIRST_CHILD);
        do {
            Integer xmiIdIndex = vn.getAttrVal("xmi:id");
            if (xmiIdIndex >= 0) {
                int i = vn.getCurrentIndex();
                int oldXmiId = Integer.parseInt(vn.toString(xmiIdIndex));
                JeDISVTDGraphNode n = nodesByXmiId.computeIfAbsent(oldXmiId, JeDISVTDGraphNode::new);
                n.setVtdIndex(i);
                n.setElementFragment(vn.getElementFragment());
                n.setTypeName(getTypeName(vn, namespaceMap, i));
                int sofaAttrIndex = vn.getAttrVal("sofa");
                if (sofaAttrIndex > -1)
                    n.setSofaXmiId(Integer.parseInt(vn.toString(sofaAttrIndex)));
                Stream<Integer> referencedXmiIds = getReferencedXmiIds(vn, n.getTypeName(), aCas.getTypeSystem());
                referencedXmiIds.map(refId -> nodesByXmiId.computeIfAbsent(refId, JeDISVTDGraphNode::new)).forEach(referenced -> referenced.addPredecessor(n));
            }
        } while (vn.toElement(VTDNav.NEXT_SIBLING));
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
