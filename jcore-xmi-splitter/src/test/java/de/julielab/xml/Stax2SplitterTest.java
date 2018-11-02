package de.julielab.xml;

import com.ximpleware.NavException;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static javax.xml.stream.XMLStreamConstants.ATTRIBUTE;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.assertj.core.api.Assertions.assertThat;

public class Stax2SplitterTest {
    @Test
    public void test() throws Exception {
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico_token_excerpt.xmi"));
        final StaxXmiSplitter2 splitter = new StaxXmiSplitter2(new HashSet<>(), true, true, "documents", null);
        splitter.process(xmiData, null, 0, Collections.emptyMap());
    }

    @Test
    public void testReferences() throws Exception {
        StaxXmiSplitter2 splitter = new StaxXmiSplitter2(null, false, false, null, null);
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

        JeDISVTDGraphNode tokenNode = nodesByXmiId.get(1135);
        Map<String, List<Integer>> referencedXmiIds = tokenNode.getReferencedXmiIds();
        assertThat(referencedXmiIds.keySet()).containsExactlyInAnyOrder("posTag", "depRel");
        assertThat(referencedXmiIds.get("posTag")).containsExactly(10760);
        assertThat(referencedXmiIds.get("depRel")).containsExactly(7691);
    }
}
