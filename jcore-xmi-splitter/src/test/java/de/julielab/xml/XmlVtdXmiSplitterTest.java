package de.julielab.xml;

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import de.julielab.jcore.types.DependencyRelation;
import de.julielab.jcore.types.PennBioIEPOSTag;
import de.julielab.jcore.types.Sentence;
import de.julielab.jcore.types.Token;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
public class XmlVtdXmiSplitterTest {
    @Test
    public void testEmbeddedFeatures() throws IOException, XMISplitterException, UIMAException, NavException {
        // These embedded features are, for example, StringArrays that can not be references by other annotations
        // than the one it was originally set to.
        VtdXmlXmiSplitter splitter = new VtdXmlXmiSplitter(null, false, false, null, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        splitter.process(xmiData, jCas, 0, null);

        VTDNav vn = splitter.getVTDNav();
        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        // There are 1440 XML elements with an XMI:id in the respective document
        // Also, we add the sofa with the special ID -2 (as a duplicate to its original, but a priori unknown ID)
        assertThat(nodesByXmiId).hasSize(1441);
        JeDISVTDGraphNode tokenWithSynonyms = nodesByXmiId.get(3430);
        String s = vn.toString(tokenWithSynonyms.getByteOffset(), tokenWithSynonyms.getByteLength());
        assertThat(s).contains("<synonyms>exchange</synonyms");
        assertThat(s).contains("<hypernyms>chemical phenomenon</hypernyms");
    }

    @Test
    public void testReferences() throws IOException, XMISplitterException, UIMAException, NavException {
        VtdXmlXmiSplitter splitter = new VtdXmlXmiSplitter(null, false, false, null, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        splitter.process(xmiData, jCas, 0, null);

        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        JeDISVTDGraphNode posTag = nodesByXmiId.get(5374);
        List<JeDISVTDGraphNode> predecessors = posTag.getPredecessors();
        assertThat(predecessors).hasSize(1);
        JeDISVTDGraphNode fsArray = predecessors.get(0);
        assertThat(fsArray.getTypeName()).isEqualTo("uima.cas.FSArray");
        assertThat(fsArray.getOldXmiId()).isEqualTo(10301);
        assertThat(fsArray.getPredecessors()).hasSize(1);
        JeDISVTDGraphNode token = fsArray.getPredecessors().get(0);
        assertThat(token.getTypeName()).isEqualTo("de.julielab.jcore.types.Token");
        assertThat(token.getOldXmiId()).isEqualTo(3430);
    }

    @Test
    public void testLabelNonRecursively() throws IOException, XMISplitterException, UIMAException, NavException {
        // "Non"Recursively here does mean that annotation modules always only contain annotations of a single
        // annotation type. Thus, tokens do not contain their dependency relations, for example.
        Set<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(
                Token.class.getCanonicalName(),
                DependencyRelation.class.getCanonicalName(),
                Sentence.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName()));
        VtdXmlXmiSplitter splitter = new VtdXmlXmiSplitter(moduleAnnotationNames, false, false, null, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        splitter.process(xmiData, jCas, 0, null);

        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        for (JeDISVTDGraphNode n : nodesByXmiId.values()) {
            if (moduleAnnotationNames.contains(n.getTypeName()))
                assertThat(n.getAnnotationModuleLabels()).containsExactly(n.getTypeName());
        }
        assertThat(nodesByXmiId.values().stream().map(JeDISVTDGraphNode::getAnnotationModuleLabels).flatMap(Collection::stream).collect(Collectors.toSet())).containsExactlyInAnyOrder(Token.class.getCanonicalName(),
                DependencyRelation.class.getCanonicalName(),
                Sentence.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName());

        // The FSArray assigning POSTags to the token should be included with the token
        JeDISVTDGraphNode posTagFSArray = nodesByXmiId.get(10649);
        assertThat(posTagFSArray.getAnnotationModuleLabels()).containsExactly(Token.class.getCanonicalName());

        JeDISVTDGraphNode resourceEntryListArray = nodesByXmiId.get(10264);
        assertThat(resourceEntryListArray.getAnnotationModuleLabels()).isEmpty();

        // The dependency relation should not be included with the token annotation module
        JeDISVTDGraphNode depRel = nodesByXmiId.get(6852);
        assertThat(depRel.getAnnotationModuleLabels()).containsExactly(DependencyRelation.class.getCanonicalName());
    }

    @Test
    public void testLabelRecursively() throws IOException, XMISplitterException, UIMAException, NavException {
        // Recursively here does mean that annotation modules contain annotations of their original type as well as
        // annotations referenced directly or indirectly from this type. For example, the token module would
        // also include POSTags and dependency relations.
        // Here, we will define to create Token and PennBioIEPOSTag labels. The token references dependency relations
        // as well as POSTags. Since the DependencyRelation type is not in the list, it should be included with the
        // tokens. The PosTags should not.
        Set<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(
                Token.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName()));
        VtdXmlXmiSplitter splitter = new VtdXmlXmiSplitter(moduleAnnotationNames, true, false, null, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        splitter.process(xmiData, jCas, 0, null);

        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        // The dependency relation should be included with the token annotation module
        JeDISVTDGraphNode depRel = nodesByXmiId.get(6852);
        assertThat(depRel.getAnnotationModuleLabels()).containsExactlyInAnyOrder(Token.class.getCanonicalName());

        JeDISVTDGraphNode posTag = nodesByXmiId.get(3610);
        // The POSTag should be stored on its own
        assertThat(posTag.getAnnotationModuleLabels()).containsExactlyInAnyOrder(PennBioIEPOSTag.class.getCanonicalName());
    }
}
