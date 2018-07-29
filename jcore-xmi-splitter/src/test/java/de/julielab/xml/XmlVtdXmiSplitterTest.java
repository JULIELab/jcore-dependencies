package de.julielab.xml;

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
public class XmlVtdXmiSplitterTest {
    @Test
    public void testEmbeddedFeatures() throws IOException, XMISplitterException, UIMAException, NavException {
        // These embedded features are, for example, StringArrays that can not be references by other annotations
        // than the one it was originally set to.
        VtdXmlXmiSplitter splitter = new VtdXmlXmiSplitter();
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        splitter.process(xmiData, jCas, 0, null);

        VTDNav vn = splitter.getVTDNav();
        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        // There are 1440 XML elements with an XMI:id in the respective document
        assertThat(nodesByXmiId).hasSize(1440);
        JeDISVTDGraphNode tokenWithSynonyms = nodesByXmiId.get(3430);
        String s = vn.toString(tokenWithSynonyms.getByteOffset(), tokenWithSynonyms.getByteLength());
        assertThat(s).contains("<synonyms>exchange</synonyms");
        assertThat(s).contains("<hypernyms>chemical phenomenon</hypernyms");
    }

    @Test
    public void testReferences() throws IOException, XMISplitterException, UIMAException, NavException {
        VtdXmlXmiSplitter splitter = new VtdXmlXmiSplitter();
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
}
