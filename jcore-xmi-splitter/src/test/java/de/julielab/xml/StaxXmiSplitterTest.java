package de.julielab.xml;

import com.ximpleware.NavException;
import de.julielab.jcore.types.*;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class StaxXmiSplitterTest {

    public static final HashSet<String> BASE_DOCUMENT_ANNOTATIONS = new HashSet<>(Arrays.asList("de.julielab.jcore.types.AbstractSectionHeading", "de.julielab.jcore.types.AbstractSection", "de.julielab.jcore.types.AbstractText"));

    @Test
    public void testEmbeddedFeatures() throws IOException, XMISplitterException, UIMAException, NavException {
        // These embedded features are, for example, StringArrays that can not be references by other annotations
        // than the one it was originally set to.
        StaxXmiSplitter splitter = new StaxXmiSplitter(null, false, false, null, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        splitter.process(xmiData, jCas, 0, null);

        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        // There are 1440 XML elements with an XMI:id in the respective document
        // Also, we add the sofa with the special ID -2 (as a duplicate to its original, but a priori unknown ID)
        assertThat(nodesByXmiId).hasSize(1441);
        JeDISVTDGraphNode tokenWithSynonyms = nodesByXmiId.get(3430);
        String s = new String(Arrays.copyOfRange(xmiData, tokenWithSynonyms.getByteOffset(), tokenWithSynonyms.getByteOffset() + tokenWithSynonyms.getByteLength()));
        assertThat(s).contains("<synonyms>exchange</synonyms");
        assertThat(s).contains("<hypernyms>chemical phenomenon</hypernyms");
    }

    @Test
    public void testReferences() throws Exception {
        StaxXmiSplitter splitter = new StaxXmiSplitter(null, false, false, null, null);
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

    @Test
    public void testLabelNonRecursively() throws IOException, XMISplitterException, UIMAException, NavException {
        // "Non"Recursively here does mean that annotation modules always only contain annotations of a single
        // annotation type. Thus, tokens do not contain their dependency relations, for example.
        Set<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(
                Token.class.getCanonicalName(),
                DependencyRelation.class.getCanonicalName(),
                Sentence.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, false, false, null, null);
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
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, false, null, null);
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

    @Test
    public void testAnnotationModules() throws IOException, XMISplitterException, UIMAException, NavException {
        Set<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(
                Token.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, null, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        splitter.process(xmiData, jCas, 0, null);

        Map<String, Set<JeDISVTDGraphNode>> annotationModules = splitter.getAnnotationModules();
        assertThat(annotationModules.keySet()).containsExactlyInAnyOrder(Token.class.getCanonicalName(), PennBioIEPOSTag.class.getCanonicalName(), StaxXmiSplitter.DOCUMENT_MODULE_LABEL);

        // Check that all nodes ended up in the correct module. That means they should have the label of
        // their module.
        for (Map.Entry<String, Set<JeDISVTDGraphNode>> e : annotationModules.entrySet()) {
            String typeName = e.getKey();
            Set<JeDISVTDGraphNode> nodes = e.getValue();
            for (JeDISVTDGraphNode node : nodes) {
                assertThat(node.getAnnotationModuleLabels()).containsExactly(typeName);
            }
        }
    }

    @Test
    public void testResult() throws Exception {
        Set<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(
                Token.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName()));
        Set<String> baseDocumentAnnotations = new HashSet<>(Arrays.asList(Title.class.getCanonicalName(), Header.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, "docs", baseDocumentAnnotations);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        XmiSplitterResult result = splitter.process(xmiData, jCas, 0, Collections.emptyMap());

        assertThat(result.currentSofaIdMap).hasSize(1);
        assertThat(result.maxXmiId).isGreaterThan(1);
        assertThat(result.namespaces).hasSize(6);
        // Tokens, PosTags and document data
        assertThat(result.xmiData).hasSize(3);
        assertThat(result.xmiData.keySet()).containsExactlyInAnyOrder(Token.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName(), "docs");


        byte[] tokenBytes = result.xmiData.get(Token.class.getCanonicalName()).toByteArray();

        String tokenData = new String(tokenBytes, StandardCharsets.UTF_8);
        // There should be tokens, dependency relations and arrays but no postags
        assertThat(tokenData.indexOf("types:Token")).isGreaterThan(0);
        assertThat(tokenData.indexOf("types:DependencyRelation")).isGreaterThan(0);
        assertThat(tokenData.indexOf("cas:FSArray")).isGreaterThan(0);
        assertThat(tokenData.indexOf("types:" + PennBioIEPOSTag.class.getSimpleName())).isEqualTo(-1);

        byte[] baseDocBytes = result.xmiData.get("docs").toByteArray();

        String baseDocData = new String(baseDocBytes, StandardCharsets.UTF_8);
        assertThat(baseDocData.indexOf("<cas:Sofa")).isGreaterThan(-1);
        assertThat(baseDocData.indexOf("<types:Title")).isGreaterThan(-1);
        assertThat(baseDocData.indexOf("<pubmed:Header")).isGreaterThan(-1);
        assertThat(baseDocData.indexOf("<types:AuthorInfo")).isGreaterThan(-1);
    }

    @Test
    public void testSplitAndBuild() throws Exception {
        Set<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(
                Token.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName()));
        Set<String> baseDocumentAnnotations = new HashSet<>(Arrays.asList(Title.class.getCanonicalName(), Header.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, "docs", baseDocumentAnnotations);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        XmiSplitterResult result = splitter.process(xmiData, jCas, 0, Collections.emptyMap());

        XmiBuilder builder = new XmiBuilder(result.namespaces, moduleAnnotationNames.stream().toArray(String[]::new));
        LinkedHashMap<String, InputStream> inputMap = result.xmiData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ByteArrayInputStream(e.getValue().toByteArray()), (k, v) -> v, LinkedHashMap::new));
        ByteArrayOutputStream newXmiData = builder.buildXmi(inputMap, "docs", jCas.getTypeSystem());

        jCas.reset();

        assertThatCode(() -> XmiCasDeserializer.deserialize(new ByteArrayInputStream(newXmiData.toByteArray()), jCas.getCas())).doesNotThrowAnyException();

        assertThat(JCasUtil.select(jCas, Token.class)).hasSize(208);
        assertThat(JCasUtil.select(jCas, DependencyRelation.class)).hasSize(208);
        assertThat(JCasUtil.select(jCas, PennBioIEPOSTag.class)).hasSize(208);
        assertThat(JCasUtil.select(jCas, Gene.class)).hasSize(0);
    }

    @Test
    public void testSpecialXMLCharacter() throws Exception {
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        jCas.setDocumentText("This is p < 0.5.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);

        StaxXmiSplitter splitter = new StaxXmiSplitter(Collections.emptySet(), true, true, "docs", Collections.emptySet());
        XmiSplitterResult result = splitter.process(baos.toByteArray(), jCas, 0, null);

        XmiBuilder builder = new XmiBuilder(result.namespaces, new String[0]);
        LinkedHashMap<String, InputStream> inputMap = result.xmiData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ByteArrayInputStream(e.getValue().toByteArray()), (k, v) -> v, LinkedHashMap::new));
        assertThatCode(() -> builder.buildXmi(inputMap, "docs", jCas.getTypeSystem())).doesNotThrowAnyException();
    }

    @Test
    public void testNonsharedArrayWithMultipleEntries() throws Exception {
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        jCas.setDocumentText("This is p < 0.5.");

        AutoDescriptor ad = new AutoDescriptor(jCas);
        ad.addToIndexes();
        FSArray fsa = new FSArray(jCas, 2);
        DocumentClass dc = new DocumentClass(jCas);
        dc.setClassname("myclass");
        DocumentClass dc2 = new DocumentClass(jCas);
        dc2.setClassname("anotherclass");
        fsa.set(0, dc);
        fsa.set(1, dc2);
        ad.setDocumentClasses(fsa);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        StaxXmiSplitter splitter = new StaxXmiSplitter(Collections.emptySet(), true, true, "docs", new HashSet<>(Arrays.asList(AutoDescriptor.class.getCanonicalName())));
        XmiSplitterResult result = splitter.process(baos.toByteArray(), jCas, 0, null);

        XmiBuilder builder = new XmiBuilder(result.namespaces, new String[0]);
        LinkedHashMap<String, InputStream> inputMap = result.xmiData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ByteArrayInputStream(e.getValue().toByteArray()), (k, v) -> v, LinkedHashMap::new));
        ByteArrayOutputStream builtXmi = builder.buildXmi(inputMap, "docs", jCas.getTypeSystem());
        jCas.reset();
        assertThatCode(() -> XmiCasDeserializer.deserialize(new ByteArrayInputStream(builtXmi.toByteArray()), jCas.getCas())).doesNotThrowAnyException();
    }

    @Test
    public void testAACRXMI() throws Exception {
        // There shouldn't be anything special about the document used in this test, yet it showed strange - and wrong -
        // splitting behaviour in practice.
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        Set<String> moduleAnnotationNames = Collections.emptySet();
        Set<String> baseAnnotations = new HashSet<>(Arrays.asList("de.julielab.jcore.types.AbstractText",
                "de.julielab.jcore.types.Title",
                "de.julielab.jcore.types.Header",
                "de.julielab.jcore.types.pubmed.Header",
                "de.julielab.jcore.types.pubmed.ManualDescriptor"));
        final XmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, "documents", baseAnnotations);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream("src/test/resources/test-xmis/AACR_2017-4458.xmi"));
        final XmiSplitterResult splitterResult = splitter.process(bytes, jCas, 0, null);

        final XmiBuilder xmiBuilder = new XmiBuilder(splitterResult.namespaces, new String[0]);
        LinkedHashMap<String, InputStream> inputMap = splitterResult.xmiData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ByteArrayInputStream(e.getValue().toByteArray()), (k, v) -> v, LinkedHashMap::new));
        final ByteArrayOutputStream builtXmi = xmiBuilder.buildXmi(inputMap, "documents", jCas.getTypeSystem());
        assertThatCode(() -> XmiCasDeserializer.deserialize(new ByteArrayInputStream(builtXmi.toByteArray()), jCas.getCas())).doesNotThrowAnyException();
    }

    @Test
    public void testAACRXMI2() throws Exception {
        // There shouldn't be anything special about the document used in this test, yet it showed strange - and wrong -
        // splitting behaviour in practice.
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        Set<String> moduleAnnotationNames = Collections.emptySet();
        Set<String> baseAnnotations = new HashSet<>(Arrays.asList("de.julielab.jcore.types.AbstractText",
                "de.julielab.jcore.types.Title",
                "de.julielab.jcore.types.Header",
                "de.julielab.jcore.types.pubmed.Header",
                "de.julielab.jcore.types.pubmed.ManualDescriptor"));
        final XmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, "documents", baseAnnotations);

        final byte[] bytes = IOUtils.toByteArray(new FileInputStream("src/test/resources/test-xmis/AACR_2017-2282.xmi"));
        final XmiSplitterResult splitterResult = splitter.process(bytes, jCas, 0, null);

        final XmiBuilder xmiBuilder = new XmiBuilder(splitterResult.namespaces, new String[0]);
        LinkedHashMap<String, InputStream> inputMap = splitterResult.xmiData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ByteArrayInputStream(e.getValue().toByteArray()), (k, v) -> v, LinkedHashMap::new));
        final ByteArrayOutputStream builtXmi = xmiBuilder.buildXmi(inputMap, "documents", jCas.getTypeSystem());
        System.out.println(new String(builtXmi.toByteArray()));
        assertThatCode(() -> XmiCasDeserializer.deserialize(new ByteArrayInputStream(builtXmi.toByteArray()), jCas.getCas())).doesNotThrowAnyException();
    }


    @Test
    public void testCorrectSofaId() throws Exception {
        // This test checks that already existing sofa XMI mappings are respected. This is a crucial feature
        // because if an existing sofaID <-> sofa xmi:id mapping is not respected, annotations added to an existing
        // base document might have differing xmi references to the sofa which will render the data inconsistent.
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        jCas.setDocumentText("This is a simple sentence.");
        new Sentence(jCas, 0, jCas.getDocumentText().length()).addToIndexes();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);

        StaxXmiSplitter splitter = new StaxXmiSplitter(new HashSet<>(Arrays.asList("de.julielab.jcore.types.Sentence")), true, true, "docs", Collections.emptySet());
        Map<String, Integer> sofaIdMap = new HashMap<>();
        sofaIdMap.put("_InitialView", 9999);
        XmiSplitterResult result = splitter.process(baos.toByteArray(), jCas, 0, sofaIdMap);

        assertThat(new String(result.xmiData.get("de.julielab.jcore.types.Sentence").toByteArray(), "UTF-8")).contains("sofa=\"9999\"");
    }

}
