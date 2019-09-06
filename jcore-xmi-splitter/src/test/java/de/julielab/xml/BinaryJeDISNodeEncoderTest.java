package de.julielab.xml;

import de.julielab.jcore.types.*;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.jcore.types.test.MultiValueTypesHolder;
import de.julielab.xml.binary.*;
import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.*;
import org.apache.uima.jcas.tcas.Annotation;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;
import static org.testng.Assert.*;

public class BinaryJeDISNodeEncoderTest {


    // Those fields are written by tests to be used by following tests.
    // The test order is determined by the @Test(dependsOnMethods=...) annotation
    private Map<String, ByteArrayOutputStream> encode;
    private XmiSplitterResult splitterResult;
    private HashSet<String> moduleAnnotationNames;
    private BinaryStorageAnalysisResult result;
    private Map<String, Integer> mapping;
    private BinaryDecodingResult decodedData;

    @Test
    public void testFindMissingItemsForMapping() throws Exception {
        final StaxXmiSplitter splitter = new StaxXmiSplitter(Collections.singleton(Token.class.getCanonicalName()), false, false, Collections.emptySet());

        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        final Token t1 = new Token(jCas);
        t1.setComponentId("TokenSplitter");
        t1.addToIndexes();
        final Token t2 = new Token(jCas);
        t2.setComponentId("TokenSplitter");
        t2.addToIndexes();
        final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos1);
        final XmiSplitterResult splitterResult1 = splitter.process(baos1.toByteArray(), jCas.getTypeSystem(), 0, Collections.emptyMap());

        jCas.reset();
        final Token t3 = new Token(jCas);
        t3.setComponentId("TokenSplitter2");
        t3.setOrthogr("lowercase");
        t3.addToIndexes();
        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos2);
        final XmiSplitterResult splitterResult2 = splitter.process(baos2.toByteArray(), jCas.getTypeSystem(), 0, Collections.emptyMap());

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult analysisResult1 = encoder.findMissingItemsForMapping(splitterResult1.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());
        assertThat(analysisResult1.getMissingValuesToMap()).containsExactlyInAnyOrder("types:Token", "sofa", "componentId", "xmi:id", "end", "begin", "TokenSplitter");
        assertThat(analysisResult1.getMissingFeaturesToMap()).containsEntry("de.julielab.jcore.types.Annotation:componentId", true);
        assertThat(analysisResult1.getMissingItemsMapping())
                .containsKeys("types:Token", "sofa", "componentId", "xmi:id", "end", "begin", "TokenSplitter");

        final BinaryStorageAnalysisResult analysisResult2 = encoder.findMissingItemsForMapping(splitterResult2.jedisNodesInAnnotationModules, jCas.getTypeSystem(), analysisResult1.getMissingItemsMapping(), analysisResult1.getMissingFeaturesToMap());
        assertThat(analysisResult2.getMissingValuesToMap()).containsExactlyInAnyOrder("orthogr", "TokenSplitter2");
        assertThat(analysisResult2.getMissingFeaturesToMap()).containsEntry("de.julielab.jcore.types.Token:orthogr", false);
        assertThat(analysisResult2.getMissingItemsMapping()).containsKeys("orthogr", "TokenSplitter2");

        final Map<String, Integer> completeMapping = new HashMap<>(analysisResult1.getMissingItemsMapping());
        completeMapping.putAll(analysisResult2.getMissingItemsMapping());
        final Map<String, Boolean> completeFeaturesToMap = new HashMap<>(analysisResult1.getMissingFeaturesToMap());
        completeFeaturesToMap.putAll(analysisResult2.getMissingFeaturesToMap());

        // Check that now there are now missing features any more
        final BinaryStorageAnalysisResult analysisResult3 = encoder.findMissingItemsForMapping(splitterResult1.jedisNodesInAnnotationModules, jCas.getTypeSystem(), completeMapping, completeFeaturesToMap);
        assertThat(analysisResult3.getMissingItemsMapping()).isEmpty();
        assertThat(analysisResult3.getMissingFeaturesToMap()).isEmpty();

        final BinaryStorageAnalysisResult analysisResult4 = encoder.findMissingItemsForMapping(splitterResult2.jedisNodesInAnnotationModules, jCas.getTypeSystem(), completeMapping, completeFeaturesToMap);
        assertThat(analysisResult4.getMissingItemsMapping()).isEmpty();
        assertThat(analysisResult4.getMissingFeaturesToMap()).isEmpty();
    }

    @Test
    public void testFullEncodingDecodingBuilding() throws Exception {
        final HashSet<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(Sentence.class.getCanonicalName(), Token.class.getCanonicalName(),
                Gene.class.getCanonicalName(), EventMention.class.getCanonicalName(), Header.class.getCanonicalName(), ResourceEntry.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, false, true, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        final XmiSplitterResult splitterResult = splitter.process(xmiData, jCas.getTypeSystem(), 0, Collections.singletonMap("_InitialView", 1));

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult result = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());
        final Set<String> missingItemsForMapping = result.getMissingValuesToMap();
        assertThat(missingItemsForMapping).contains("types:Sentence", "types:Token", "pubmed:Header", "xmi:id", "sofa", "cas:FSArray", "synonyms", "hypernyms", "componentId", "specificType", "protein");

        final Map<String, Integer> mapping = result.getMissingItemsMapping();
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, result.getMissingFeaturesToMap());

        Map<String, InputStream> bais = new HashMap<>();
        for (String label : encode.keySet()) {
            bais.put(label, new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(moduleAnnotationNames, false);
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        final BinaryDecodingResult decode = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, result.getMissingFeaturesToMap(), splitterResult.namespaces);

        final BinaryXmiBuilder builder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream rebuiltxmiData = builder.buildXmi(decode);

        // There was one bug once where there were hundreds of cas:View elements
        final String xmiString = rebuiltxmiData.toString(UTF_8);
        final Matcher casViewMatcher = Pattern.compile("<cas:View").matcher(xmiString);
        int numCasViewElements = 0;
        while (casViewMatcher.find())
            ++numCasViewElements;
        assertEquals(numCasViewElements, 1);
        // Another bug caused duplicate sofa elements
        final Matcher sofaElementMatcher = Pattern.compile("<cas:Sofa").matcher(xmiString);
        int numSofaElements = 0;
        while (sofaElementMatcher.find())
            ++numSofaElements;
        assertEquals(numSofaElements, 1);

        jCas.reset();
        XmiCasDeserializer.deserialize(new ByteArrayInputStream(rebuiltxmiData.toByteArray()), jCas.getCas());

        final Collection<EventMention> eventMentions = JCasUtil.select(jCas, EventMention.class);
        // we stored the event mentions...
        assertEquals(12, eventMentions.size());
        for (EventMention em : eventMentions) {
            // but not the types referenced from the event mention. So the ID should be there...
            assertThat(em.getId()).isNotNull();
            // but not the trigger or the arguments
            assertThat(em.getTrigger()).isNull();
            assertThat(em.getArguments()).isNull();
        }
        assertThat(jCas.getAnnotationIndex(Sentence.type).iterator()).hasNext();
        assertThat(jCas.getAnnotationIndex(Sentence.type).iterator()).hasNext();
        assertThat(jCas.getAnnotationIndex(Token.type).iterator()).hasNext();
        assertThat(jCas.getAnnotationIndex(Gene.type).iterator()).hasNext();
        assertThat(jCas.getAnnotationIndex(Header.type).iterator()).hasNext();
        assertThat(jCas.getAnnotationIndex(ResourceEntry.type).iterator()).hasNext();

        for (Gene g : JCasUtil.select(jCas, Gene.class)) {
            assertThat(g.getResourceEntryList()).isNotNull();
            assertThat(g.getResourceEntryList()).hasSize(2);
            for (FeatureStructure re : g.getResourceEntryList()) {
                assertThat(((ResourceEntry) re).getEntryId()).isNotNull();
            }
        }


    }

    @Test
    public void testDocumentModuleAnnotations() throws Exception {
        // Here we just check that those annotations that were added to the document module
        // end up in the CAS indexes
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        // We will add the header to the base document module
        final Header h = new Header(jCas);
        h.setDocId("1234");
        h.addToIndexes();
        final Token token = new Token(jCas);
        final StringArray synonyms = new StringArray(jCas, 3);
        synonyms.copyFromArray(new String[]{"s1", "s2", "s3"}, 0, 0, 3);
        token.setSynonyms(synonyms);
        final StringArray hypernyms = new StringArray(jCas, 2);
        hypernyms.copyFromArray(new String[]{"h1", "h2"}, 0, 0, 2);
        token.setHypernyms(hypernyms);
        token.addToIndexes();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        StaxXmiSplitter splitter = new StaxXmiSplitter(new HashSet<>(Arrays.asList(Token.class.getCanonicalName())), false, true, Collections.singleton(Header.class.getCanonicalName()));
        final XmiSplitterResult splitterResult = splitter.process(baos.toByteArray(), jCas.getTypeSystem(), 0, Collections.singletonMap(CAS.NAME_DEFAULT_SOFA, 1));

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult analysisResult = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());
        final Map<String, Integer> mapping = analysisResult.getMissingItemsMapping();
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, analysisResult.getMissingFeaturesToMap());
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(new HashSet<>(Arrays.asList(Token.class.getCanonicalName())), false);
        Map<String, InputStream> bais = new HashMap<>();
        for (String label : encode.keySet()) {
            bais.put(label, new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryDecodingResult decoded = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, analysisResult.getMissingFeaturesToMap(), splitterResult.namespaces);
        final BinaryXmiBuilder xmiBuilder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream builtXmiData = xmiBuilder.buildXmi(decoded);
        jCas.reset();
        XmiCasDeserializer.deserialize(new ByteArrayInputStream(builtXmiData.toByteArray()), jCas.getCas());


        final de.julielab.jcore.types.Header header = JCasUtil.selectSingle(jCas, de.julielab.jcore.types.Header.class);
        assertNotNull(header);
        assertThat(header.getDocId()).isEqualTo("1234");
        final Token t = JCasUtil.selectSingle(jCas, Token.class);
        assertNotNull(t.getSynonyms());
        assertThat(t.getSynonyms().toStringArray()).containsExactly("s1", "s2", "s3");
        assertNotNull(t.getHypernyms());
        assertThat(t.getHypernyms().toStringArray()).containsExactly("h1", "h2");
    }

    @Test
    public void testStringArrays() throws Exception {
        // These embedded features are, for example, StringArrays that can not be references by other annotations
        // than the one it was originally set to.
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        final Token token = new Token(jCas);
        final StringArray synonyms = new StringArray(jCas, 3);
        synonyms.copyFromArray(new String[]{"s1", "s2", "s3"}, 0, 0, 3);
        token.setSynonyms(synonyms);
        final StringArray hypernyms = new StringArray(jCas, 2);
        hypernyms.copyFromArray(new String[]{"h1", "h2"}, 0, 0, 2);
        token.setHypernyms(hypernyms);
        token.addToIndexes();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        StaxXmiSplitter splitter = new StaxXmiSplitter(new HashSet<>(Arrays.asList(Token.class.getCanonicalName())), false, true, null);
        final XmiSplitterResult splitterResult = splitter.process(baos.toByteArray(), jCas.getTypeSystem(), 0, Collections.singletonMap(CAS.NAME_DEFAULT_SOFA, 1));

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult analysisResult = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());
        final Map<String, Integer> mapping = analysisResult.getMissingItemsMapping();
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, analysisResult.getMissingFeaturesToMap());
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(new HashSet<>(Arrays.asList(Token.class.getCanonicalName())), false);
        Map<String, InputStream> bais = new HashMap<>();
        for (String label : encode.keySet()) {
            bais.put(label, new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryDecodingResult decoded = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, analysisResult.getMissingFeaturesToMap(), splitterResult.namespaces);
        final BinaryXmiBuilder xmiBuilder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream builtXmiData = xmiBuilder.buildXmi(decoded);
        jCas.reset();
        XmiCasDeserializer.deserialize(new ByteArrayInputStream(builtXmiData.toByteArray()), jCas.getCas());

        final Token t = JCasUtil.selectSingle(jCas, Token.class);
        assertNotNull(t.getSynonyms());
        assertThat(t.getSynonyms().toStringArray()).containsExactly("s1", "s2", "s3");
        assertNotNull(t.getHypernyms());
        assertThat(t.getHypernyms().toStringArray()).containsExactly("h1", "h2");
    }

    @Test
    public void testPartialModuleLoadingNoShrinking() throws Exception {
        JCas jCas = getPartiallyLoadedModuleData(false);

        final Token t = JCasUtil.selectSingle(jCas, Token.class);
        assertNotNull(t.getPosTag());
        assertEquals(t.getPosTag().size(), 2);
        assertNotNull(t.getPosTag(0));
        assertNull(t.getPosTag(1));
        assertNotNull(t.getDepRel());
        assertEquals(t.getDepRel().size(), 1);
        assertNull(t.getDepRel(0));

        final MultiValueTypesHolder h = JCasUtil.selectSingle(jCas, MultiValueTypesHolder.class);
        // The FSList still has a value for middle node
        assertNotNull(h.getFslist());
        assertNull(h.getFslist().getNthElement(0));
        assertNotNull(h.getFslist().getNthElement(1));
        assertNull(h.getFslist().getNthElement(2));
    }

    @Test
    public void testPartialModuleLoadingWithShrinking() throws Exception {
        JCas jCas = getPartiallyLoadedModuleData(true);

        final Token t = JCasUtil.selectSingle(jCas, Token.class);
        assertNotNull(t.getPosTag());
        assertEquals(t.getPosTag().size(), 1);
        assertNotNull(t.getPosTag(0));
        assertNull(t.getDepRel());

        final MultiValueTypesHolder h = JCasUtil.selectSingle(jCas, MultiValueTypesHolder.class);
        // The FSList still has a value for middle node
        assertNotNull(h.getFslist());
        assertNotNull(h.getFslist().getNthElement(0));
        // There should only be the one element checked above
        assertThatThrownBy(() -> h.getFslist().getNthElement(1)).isInstanceOf(ClassCastException.class);

    }

    private JCas getPartiallyLoadedModuleData(boolean omitElementsWithMissingReferences) throws Exception {
        // ----------- Creating a CAS with annotations
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types", "arrayAndListHolderTestType");
        final Token token = new Token(jCas);
        final FSArray postags = new FSArray(jCas, 2);
        postags.set(0, new PennBioIEPOSTag(jCas, 1, 1));
        postags.set(1, new GeniaPOSTag(jCas, 1, 2));
        token.setPosTag(postags);
        final FSArray deprels = new FSArray(jCas, 1);
        deprels.set(0, new DependencyRelation(jCas, 2, 1));
        token.setDepRel(deprels);
        token.addToIndexes();

        final MultiValueTypesHolder holder = new MultiValueTypesHolder(jCas);
        final NonEmptyFSList fl = new FSList(jCas).push(new Abbreviation(jCas)).push(new Sentence(jCas)).push(new Abbreviation(jCas));
        holder.setFslist(fl);
        holder.addToIndexes();

        // --------- Creating annotation modules
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        // Store all the types separatly. Later own, we will only load the tokens and the pos tags
        StaxXmiSplitter splitter = new StaxXmiSplitter(new HashSet<>(Arrays.asList(
                Token.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName(),
                GeniaPOSTag.class.getCanonicalName(),
                DependencyRelation.class.getCanonicalName(),
                MultiValueTypesHolder.class.getCanonicalName(),
                Abbreviation.class.getCanonicalName(),
                Sentence.class.getCanonicalName())), false, true, null);
        final XmiSplitterResult splitterResult = splitter.process(baos.toByteArray(), jCas.getTypeSystem(), 0, Collections.singletonMap(CAS.NAME_DEFAULT_SOFA, 1));

        // ---------- Binary encoding of the modules
        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult analysisResult = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());
        final Map<String, Integer> mapping = analysisResult.getMissingItemsMapping();
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, analysisResult.getMissingFeaturesToMap());

        // ------------ Decode the binary data while omitting some modules to produce missing elements
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        final HashSet<String> annotationLabelsToLoad = new HashSet<>(Arrays.asList(StaxXmiSplitter.DOCUMENT_MODULE_LABEL,
                Token.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName(),
                MultiValueTypesHolder.class.getCanonicalName(),
                Sentence.class.getCanonicalName()));
        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(annotationLabelsToLoad, omitElementsWithMissingReferences);
        // Omit the dependency relations and the abbreviations on loading.
        // Omitting the abbreviations is particularly mean: The FSList contains two Abbreviations on positions 0 and 2 and one Sentence in the middle.
        Map<String, InputStream> bais = new HashMap<>();
        for (String label : encode.keySet()) {
            if (annotationLabelsToLoad.contains(label))
            bais.put(label, new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryDecodingResult decoded = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, analysisResult.getMissingFeaturesToMap(), splitterResult.namespaces);

        // ------------ Build the XMI
        final BinaryXmiBuilder xmiBuilder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream builtXmiData = xmiBuilder.buildXmi(decoded);
        jCas.reset();
        assertThatCode(() -> XmiCasDeserializer.deserialize(new ByteArrayInputStream(builtXmiData.toByteArray()), jCas.getCas())).doesNotThrowAnyException();
        return jCas;
    }

    @Test
    public void testEncodeArraysAndLists() throws Exception {
        // These embedded features are, for example, StringArrays that can not be references by other annotations
        // than the one it was originally set to.
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types", "arrayAndListHolderTestType");
        final MultiValueTypesHolder holder = new MultiValueTypesHolder(jCas);
        holder.addToIndexes();
        final DoubleArray doubles = new DoubleArray(jCas, 3);
        doubles.set(0, .3);
        doubles.set(1, 1.4);
        doubles.set(2, 7.567);
        holder.setDa(doubles);
        final DoubleArray doublesNoref = new DoubleArray(jCas, 3);
        doublesNoref.set(0, 1.3);
        doublesNoref.set(1, 2.4);
        doublesNoref.set(2, 8.567);
        holder.setDaNoRef(doublesNoref);
        final ShortArray shorts = new ShortArray(jCas, 2);
        shorts.set(0, (short) 10);
        shorts.set(1, (short) 20);
        holder.setSa(shorts);
        final ShortArray shortsNoRef = new ShortArray(jCas, 2);
        shortsNoRef.set(0, (short) 20);
        shortsNoRef.set(1, (short) 30);
        holder.setSaNoRef(shortsNoRef);
        IntegerList integersNoRef = new IntegerList(jCas).push(1).push(2).push(3);
        holder.setIlNoRef(integersNoRef);
        IntegerList integers = new IntegerList(jCas).push(4).push(5);
        holder.setIl(integers);
        final FSArray fs = new FSArray(jCas, 2);
        fs.set(0, new Annotation(jCas, 1, 1));
        fs.set(1, new Annotation(jCas, 1, 2));
        holder.setFs(fs);
        final FSArray fsNoRef = new FSArray(jCas, 3);
        fsNoRef.set(0, new Annotation(jCas, 2, 1));
        fsNoRef.set(1, new Annotation(jCas, 2, 2));
        holder.setFsNoRef(fsNoRef);
        final NonEmptyFSList fslist = new FSList(jCas).push(new Annotation(jCas, 1, 1)).push(new Annotation(jCas, 1, 2)).push(new Annotation(jCas, 1, 3));
        holder.setFslist(fslist);
        final NonEmptyFSList fslistNoRef = new FSList(jCas).push(new Annotation(jCas, 2, 1)).push(new Annotation(jCas, 2, 2));
        holder.setFslistNoRef(fslistNoRef);
        final StringList sl = new StringList(jCas).push("eins").push("zwei").push("drei").push("vier");
        holder.setSl(sl);
        final NonEmptyStringList slNoRef = new StringList(jCas).push("noref-eins").push("noref-zwei").push("noref drei");
        holder.setSlNoRef(slNoRef);


        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        moduleAnnotationNames = new HashSet<>(Arrays.asList(MultiValueTypesHolder.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, null);
        splitterResult = splitter.process(baos.toByteArray(), jCas.getTypeSystem(), 0, Collections.singletonMap("_InitialView", 1));

        assertTrue(splitterResult.jedisNodesInAnnotationModules.stream().filter(node -> node.getTypeName().equals(CAS.TYPE_NAME_STRING_LIST)).findAny().isPresent());

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        result = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());

        mapping = result.getMissingItemsMapping();

        encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, result.getMissingFeaturesToMap());
        assertNotNull(encode);
        // There should be data for the base document and for MultiValueTypesHolder type
        assertEquals(encode.size(), 2);


    }

    @Test(dependsOnMethods = "testEncodeArraysAndLists")
    public void testDecodeArraysAndLists() throws Exception {
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types", "arrayAndListHolderTestType");
        Map<String, InputStream> bais = new HashMap<>();
        for (String label : encode.keySet()) {
            bais.put(label, new ByteArrayInputStream(encode.get(label).toByteArray()));
        }

        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(moduleAnnotationNames, false);
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        decodedData = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, result.getMissingFeaturesToMap(), splitterResult.namespaces);
        assertThat(decodedData.getXmiData().toString(UTF_8)).contains("cas:FSArray",
                "test:" + MultiValueTypesHolder.class.getSimpleName(),
                "NonEmptyStringList",
                "cas:StringList",
                "head",
                "tail",
                "elements",
                "xmi:id",
                "DoubleArray",
                "0.3 1.4 7.567",
                "1.3 2.4 8.567");
    }

    @Test(dependsOnMethods = "testDecodeArraysAndLists")
    public void testBuildDecodedBinaryXmi() throws Exception {
        final BinaryXmiBuilder xmiBuilder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream xmiData = xmiBuilder.buildXmi(decodedData);
        JCas newJCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types", "arrayAndListHolderTestType");
        XmiCasDeserializer.deserialize(new ByteArrayInputStream(xmiData.toByteArray()), newJCas.getCas());

        final MultiValueTypesHolder holder = JCasUtil.selectSingle(newJCas, MultiValueTypesHolder.class);
        assertNotNull(holder);
        assertNotNull(holder.getDa());
        assertNotNull(holder.getDaNoRef());
        assertNotNull(holder.getSa());
        assertNotNull(holder.getSaNoRef());
        assertNotNull(holder.getIl());
        assertNotNull(holder.getIlNoRef());
        assertNotNull(holder.getFs());
        assertNotNull(holder.getFsNoRef());
        assertNotNull(holder.getFslist());
        assertNotNull(holder.getFslistNoRef());
        assertNotNull(holder.getSa());
        assertNotNull(holder.getSaNoRef());
        assertNotNull(holder.getSl());
        assertNotNull(holder.getSlNoRef());

        assertThat(holder.getDa().toArray()).containsExactly(0.3, 1.4, 7.567);
        assertThat(holder.getDaNoRef().toArray()).containsExactly(1.3, 2.4, 8.567);
        assertThat(holder.getSa().toArray()).containsExactly((short) 10, (short) 20);
        assertThat(holder.getSaNoRef().toArray()).containsExactly((short) 20, (short) 30);
        assertThat(holder.getIl().getNthElement(0)).isEqualTo(5);
        assertThat(holder.getIl().getNthElement(1)).isEqualTo(4);
        assertThatThrownBy(() -> holder.getIl().getNthElement(2)).isInstanceOf(ClassCastException.class);
        assertThat(holder.getIlNoRef().getNthElement(0)).isEqualTo(3);
        assertThat(holder.getIlNoRef().getNthElement(1)).isEqualTo(2);
        assertThat(holder.getIlNoRef().getNthElement(2)).isEqualTo(1);
        assertThatThrownBy(() -> holder.getIlNoRef().getNthElement(3)).isInstanceOf(CASRuntimeException.class).hasMessage("JCas getNthElement method called with index \"3\" larger than the length of the list.");
        assertThat(((Annotation) holder.getFs().get(0)).getBegin()).isEqualTo(1);
        assertThat(((Annotation) holder.getFs().get(0)).getEnd()).isEqualTo(1);
        assertThat(((Annotation) holder.getFs().get(1)).getBegin()).isEqualTo(1);
        assertThat(((Annotation) holder.getFs().get(1)).getEnd()).isEqualTo(2);
        assertThat(holder.getFs().size()).isEqualTo(2);
        assertThat(((Annotation) holder.getFsNoRef().get(0)).getBegin()).isEqualTo(2);
        assertThat(((Annotation) holder.getFsNoRef().get(0)).getEnd()).isEqualTo(1);
        assertThat(((Annotation) holder.getFsNoRef().get(1)).getBegin()).isEqualTo(2);
        assertThat(((Annotation) holder.getFsNoRef().get(1)).getEnd()).isEqualTo(2);
        assertThat(((Annotation) holder.getFsNoRef().get(2))).isNull();
        assertThat(holder.getFsNoRef().size()).isEqualTo(3);
        assertThat(((Annotation) holder.getFslist().getNthElement(0)).getBegin()).isEqualTo(1);
        assertThat(((Annotation) holder.getFslist().getNthElement(0)).getEnd()).isEqualTo(3);
        assertThat(((Annotation) holder.getFslist().getNthElement(1)).getBegin()).isEqualTo(1);
        assertThat(((Annotation) holder.getFslist().getNthElement(1)).getEnd()).isEqualTo(2);
        assertThat(((Annotation) holder.getFslist().getNthElement(2)).getBegin()).isEqualTo(1);
        assertThat(((Annotation) holder.getFslist().getNthElement(2)).getEnd()).isEqualTo(1);
        assertThatThrownBy(() -> holder.getFslist().getNthElement(4)).isInstanceOf(ClassCastException.class).hasMessageContaining("class org.apache.uima.jcas.cas.FSList cannot be cast to class org.apache.uima.jcas.cas.NonEmptyFSList");
        assertThat(((Annotation) holder.getFslistNoRef().getNthElement(0)).getBegin()).isEqualTo(2);
        assertThat(((Annotation) holder.getFslistNoRef().getNthElement(0)).getEnd()).isEqualTo(2);
        assertThat(((Annotation) holder.getFslistNoRef().getNthElement(1)).getBegin()).isEqualTo(2);
        assertThat(((Annotation) holder.getFslistNoRef().getNthElement(1)).getEnd()).isEqualTo(1);
        assertThatThrownBy(() -> holder.getFslistNoRef().getNthElement(2)).isOfAnyClassIn(CASRuntimeException.class).hasMessageContaining("JCas getNthElement method called with index \"2\" larger than the length of the list.");
        assertThat(holder.getSl().getNthElement(0)).isEqualTo("vier");
        assertThat(holder.getSl().getNthElement(1)).isEqualTo("drei");
        assertThat(holder.getSl().getNthElement(2)).isEqualTo("zwei");
        assertThat(holder.getSl().getNthElement(3)).isEqualTo("eins");
        assertThat(holder.getSlNoRef().getNthElement(0)).isEqualTo("noref drei");
        assertThat(holder.getSlNoRef().getNthElement(1)).isEqualTo("noref-zwei");
        assertThat(holder.getSlNoRef().getNthElement(2)).isEqualTo("noref-eins");
    }
    @Test
    public void testXmlSpecialCharacters() throws Exception {
        final Set<String> moduleAnnotationNames = Collections.emptySet();
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, false, true, null);
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        final String docText = "For each < there is an > and sometimes &, ' or \" characters in between.";
        jCas.setDocumentText(docText);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);

        // Split, convert, assemble...
        final XmiSplitterResult splitterResult = splitter.process(baos.toByteArray(), jCas.getTypeSystem(), 0, Collections.singletonMap("_InitialView", 1));

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult result = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());

        final Map<String, Integer> mapping = result.getMissingItemsMapping();
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, result.getMissingFeaturesToMap());

        Map<String, InputStream> bais = new HashMap<>();
        for (String label : encode.keySet()) {
            bais.put(label, new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(moduleAnnotationNames, false);
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        final BinaryDecodingResult decode = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, result.getMissingFeaturesToMap(), splitterResult.namespaces);

        final BinaryXmiBuilder builder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream rebuiltxmiData = builder.buildXmi(decode);
        // ---- End assembly

        jCas.reset();
        assertThatCode(() -> XmiCasDeserializer.deserialize(new ByteArrayInputStream(rebuiltxmiData.toByteArray()), jCas.getCas()));
        assertThat(jCas.getDocumentText()).isEqualTo(docText);
    }

    @Test
    public void testProblematicClinicalTrialsDoc() throws Exception {
        final Set<String> moduleAnnotationNames = Collections.emptySet();
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, false, true,
                new HashSet<>(Arrays.asList("de.julielab.jcore.types.ct.Header",
                "de.julielab.jcore.types.ct.BriefTitle",
                "de.julielab.jcore.types.ct.OfficialTitle",
                "de.julielab.jcore.types.ct.Summary",
                "de.julielab.jcore.types.ct.Description",
                "de.julielab.jcore.types.ct.OutcomeMeasure",
                "de.julielab.jcore.types.ct.OutcomeDescription",
                "de.julielab.jcore.types.ct.Condition",
                "de.julielab.jcore.types.ct.InterventionType",
                "de.julielab.jcore.types.ct.InterventionName",
                "de.julielab.jcore.types.ct.ArmGroupDescription",
                "de.julielab.jcore.types.ct.Inclusion",
                "de.julielab.jcore.types.ct.Exclusion",
                "de.julielab.jcore.types.pubmed.ManualDescriptor")));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types", "de.julielab.jcore.types.jcore-document-meta-clinicaltrial-types",
                "de.julielab.jcore.types.jcore-document-structure-clinicaltrial-types");
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/splitTestDocs/NCT03093415.xmi"));

        // Split, convert, assemble...
        final XmiSplitterResult splitterResult = splitter.process(xmiData, jCas.getTypeSystem(), 0, Collections.singletonMap("_InitialView", 1));

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult result = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());

        final Map<String, Integer> mapping = result.getMissingItemsMapping();
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, result.getMissingFeaturesToMap());

        Map<String, InputStream> bais = new HashMap<>();
        for (String label : encode.keySet()) {
            bais.put(label, new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(moduleAnnotationNames, false);
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        final BinaryDecodingResult decode = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, result.getMissingFeaturesToMap(), splitterResult.namespaces);

        final BinaryXmiBuilder builder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream rebuiltxmiData = builder.buildXmi(decode);
        // ---- End assembly

        jCas.reset();
        assertThatCode(() -> XmiCasDeserializer.deserialize(new ByteArrayInputStream(rebuiltxmiData.toByteArray()), jCas.getCas()));
    }

    @Test
    public void testStringArrayIssue() throws Exception {
        final JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-document-meta-pubmed-types");
        final de.julielab.jcore.types.pubmed.ManualDescriptor pmd = new de.julielab.jcore.types.pubmed.ManualDescriptor(jCas);
        final StringArray geneSymbol = new StringArray(jCas, 0);
        pmd.setGeneSymbolList(geneSymbol);
        pmd.addToIndexes();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        StaxXmiSplitter splitter = new StaxXmiSplitter(Collections.emptySet(), true, true, Collections.singleton(de.julielab.jcore.types.pubmed.ManualDescriptor.class.getCanonicalName()));
        XmiSplitterResult splitterResult = splitter.process(baos.toByteArray(), jCas.getTypeSystem(), 0, Collections.singletonMap("_InitialView", 1));


        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult result = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());

        final Map<String, Integer> mapping = result.getMissingItemsMapping();

        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, result.getMissingFeaturesToMap());

        Map<String, InputStream> bais = new HashMap<>();
        for (String label : encode.keySet()) {
            bais.put(label, new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(Collections.emptySet(), false);
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        final BinaryDecodingResult decode = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, this.result.getMissingFeaturesToMap(), splitterResult.namespaces);

        final BinaryXmiBuilder builder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream rebuiltxmiData = builder.buildXmi(decode);
        // ---- End assembly

        jCas.reset();
        assertThatCode(() -> XmiCasDeserializer.deserialize(new ByteArrayInputStream(rebuiltxmiData.toByteArray()), jCas.getCas()));

        final de.julielab.jcore.types.pubmed.ManualDescriptor md = JCasUtil.selectSingle(jCas, de.julielab.jcore.types.pubmed.ManualDescriptor.class);
        assertThat(md.getGeneSymbolList().size()).isEqualTo(0);
    }

    @Test
    public void testErrorDoc1() throws Exception {
       Set<String> baseDocTypes = Stream.of("de.julielab.jcore.types.pubmed.Header",
                "de.julielab.jcore.types.pubmed.ManualDescriptor",
                "de.julielab.jcore.types.AbstractText",
                "de.julielab.jcore.types.Title").collect(Collectors.toSet());
       Set<String> moduleNames = Stream.of("de.julielab.jcore.types.Sentence",
               "de.julielab.jcore.types.Token",
               "de.julielab.jcore.types.Abbreviation",
               "de.julielab.jcore.types.Organism",
               "de.julielab.jcore.types.EventTrigger",
               "de.julielab.jcore.types.LikelihoodIndicator",
               "de.julielab.jcore.types.ChunkADJP",
               "de.julielab.jcore.types.ChunkADJVP",
               "de.julielab.jcore.types.ChunkCONJP",
               "de.julielab.jcore.types.ChunkNP",
               "de.julielab.jcore.types.ChunkPP",
               "de.julielab.jcore.types.ChunkSBAR",
               "de.julielab.jcore.types.ChunkVP",
               "banner:de.julielab.jcore.types.Gene",
               "bannerbiosem:de.julielab.jcore.types.EventMention").collect(Collectors.toSet());
        final Map<String, Integer> binaryMapping = IOUtils.readLines(new FileInputStream(Path.of("src", "test", "resources", "errorcase1", "real_binary_mapping1.tsv").toFile()), "UTF-8").stream().map(l -> l.split("\t")).collect(Collectors.toMap(s -> s[0], s -> Integer.valueOf(s[1])));
        final Map<String, Boolean> featuresToMap = IOUtils.readLines(new FileInputStream(Path.of("src", "test", "resources", "errorcase1", "real_features_to_map.tsv").toFile()), "UTF-8").stream().map(l -> l.split("\t")).collect(Collectors.toMap(s -> s[0], s -> s.equals("t") ? true : false));

        final JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        final byte[] fullDocBytes = IOUtils.toByteArray(new FileInputStream(Path.of("src", "test", "resources", "errorcase1", "15790300.xmi").toFile()));

        final StaxXmiSplitter splitter = new StaxXmiSplitter(moduleNames, true, true, baseDocTypes);
        System.out.println("Input XMI byte size: " + fullDocBytes.length);
        final XmiSplitterResult splitResult = splitter.process(fullDocBytes, jCas.getTypeSystem(), 0, Collections.emptyMap());

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        System.out.println(splitResult.jedisNodesInAnnotationModules.size());
        System.out.println(binaryMapping.size());
        System.out.println(featuresToMap.size());
        System.out.println("Num BaseDoc JedisNodes: " + splitResult.jedisNodesInAnnotationModules.stream().filter(n -> n.annotationModuleLabels.contains(XmiSplitter.DOCUMENT_MODULE_LABEL)).count());
        final List<JeDISVTDGraphNode> baseDocNodes = splitResult.jedisNodesInAnnotationModules.stream().filter(n -> n.annotationModuleLabels.contains(XmiSplitter.DOCUMENT_MODULE_LABEL)).collect(Collectors.toList());
        for (JeDISVTDGraphNode n : baseDocNodes)
            System.out.println(n);
        final Map<String, ByteArrayOutputStream> encodingResult = encoder.encode(splitResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), binaryMapping, featuresToMap);

        System.out.println(Arrays.toString(encodingResult.get(XmiSplitter.DOCUMENT_MODULE_LABEL).toByteArray()));
        System.out.println(encodingResult.get(XmiSplitter.DOCUMENT_MODULE_LABEL).toByteArray().length);
        System.out.println(encodingResult.get(XmiSplitter.DOCUMENT_MODULE_LABEL).toString(UTF_8));
//        System.exit(2);

//        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(Collections.emptySet(), false);
//
//        Map<String, InputStream> decodingInput = new HashMap<>();
//        decodingInput.put(XmiSplitter.DOCUMENT_MODULE_LABEL, new ByteArrayInputStream(encodingResult.get(XmiSplitter.DOCUMENT_MODULE_LABEL).toByteArray()));
//        final Map<Integer, String> reverseMapping = binaryMapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
//
//        final BinaryDecodingResult decodingResult = decoder.decode(decodingInput, jCas.getTypeSystem(), reverseMapping, featuresToMap, splitResult.namespaces);
//
//        final BinaryXmiBuilder xmiBuilder = new BinaryXmiBuilder(splitResult.namespaces);
//        final ByteArrayOutputStream assembledXmiBaos = xmiBuilder.buildXmi(decodingResult);
//
//        XmiCasDeserializer.deserialize(new ByteArrayInputStream(assembledXmiBaos.toByteArray()), jCas.getCas());
//       IOUtils.write(assembledXmiBaos.toString(UTF_8), new FileOutputStream("myxmi.xmi"));
    }

    @Test
    public void decodeErrorDoc1() throws Exception{
        final JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        final Map<String, Integer> binaryMapping = IOUtils.readLines(new FileInputStream(Path.of("src", "test", "resources", "errorcase1", "real_binary_mapping1.tsv").toFile()), "UTF-8").stream().map(l -> l.split("\t")).collect(Collectors.toMap(s -> s[0], s -> Integer.valueOf(s[1])));
        final Map<String, Boolean> featuresToMap = IOUtils.readLines(new FileInputStream(Path.of("src", "test", "resources", "errorcase1", "real_features_to_map.tsv").toFile()), "UTF-8").stream().map(l -> l.split("\t")).collect(Collectors.toMap(s -> s[0], s -> s.equals("t") ? true : false));
        final Map<String, String> nsMap = IOUtils.readLines(new FileInputStream(Path.of("src", "test", "resources", "errorcase1", "nsMap.tsv").toFile()), "UTF-8").stream().map(l -> l.split("\t")).collect(Collectors.toMap(s -> s[0], s -> s[1]));
        String base64GzippedXmiBaseDocData = "H4sIAAAAAAAAAKVWz48bNRSedOluu/39C1pxsVQJWpRGW8Gyi0BC2U2rLrRVut2WqhJCjseTsZjYwfZkGSSExIELZwR3/gkk/gjEoRIXLpy5cODAje95nE3SbnJpJM9kZuz33ve97/mZ/5AkjXdXkiTRGK9gnG7g8ntS/76L9z7GcpeXBWvj3x8Yx+6JbW6twbLGOTwf6bZp4eNj0dSpOaYaZ3C50B4obQo5KgullWBtoVK8/gnjN4xHNO9Ug1ZvHI326OHP+t74EPeTn+1o5RUvHiu5j+cHFL6XX3rcv0Gwpzu27LNUFmokbcVMxvisTw6fLLNmwLwZKsELlhk7KAvuldGOKe2lTmVKb9kwN96klYYJwXwuLR9WrTeu3lx7fy+uPWwGU46VLprgbMStkj6EMuCF6muuPeM6ZUMrb0zeuM+VZqlyxqbSuiYCEUWZKt1nW2Zf6jcd6ygnuZNh7cNyKG2mBIhgW9zhui0LXLgVQDvgLdZmOZcDciJKC6tN5qsQc1Gx9RuHkXKtfbd9vYn/nrxyh9iH1qTgs0mQeA9WeoBF/oXR4NfjqVcR7tpZTxlXaTx62Bxyn+9zfDU1S2R3JMkkiLdmmFcWiHeesGvd4c4TON7PlcjhXpQhHdJR8Jm0UlO+ETamg1+V4m+qRiqQIwDbtWr0qXTeloISyYwg2LApNSP7AQFFwGPQXNe5BqFIQY4EmNIKGRQzpPiswlS2z0eykLrv8xYbJ31aXeCM8ZFRqQs0YGFhdB8uAmYH+1DrSCH/VnJRa4w7Z4QKgewrnzNXOS9JQDxFWhRA8BpDNmPFQResV3qmvGMiJA7BlF4VZF6RdrOilFpMsoJFrkRUUsuxUZFzi0CkhR8lHDmhmTHNsBw0VrNQwENYRCmnWc7zXu0uLotJ5X0kifIT3tFCxJAicJCWIzIrRyjXZqgQibKQTRSHQJHAvw6+J6RO+XWhhGKdNonrGz1OldXt7LXYDmFzJEWnUDN4TRUkPVdFrB4gzysXaAqizYllPEidc9A0oKAD27KvZHT2ImPjLMOCK3tOflHWYPGd1Aet2olq4wKSHOCaviTIdZqn9plpr2R3lgHoGJsCBFmUUTBTgkQ50Y5iyikNTFNGjEuWlTrlhI++uqEUPmS69qOyrHQUBRWUjBHBbhBwIMwcUEWOUegU6JgHK2M+415A5hX2ooNM1AaR9j7wHFhOZd/ytP4W94YDmwhkzA6M1vVABcV9M6iqtLo5kXc0F0iCARJlIQ/dzlvUPvi4zR2Z15tOx+cCY6XuhyvPGtMd8tKiDrnSZh0+Cq0stMhPjCkyN2mR7U5okWNbxxa1yCvbxDzkUTVZF6U6gMagAgA7aJChJ14fW1ueF9lfsXuS1b/j/9cwPoj/w+XTcZ9dOcwMx/iZIk5RrKSmGRwXFuG4FPpwZyzph3VKZzF8eyJaujwPwy45v7m+8d7a22trWHd19jmhE0HyLGIhKG8ldZpPYpzF6GIsYfvG7T9Cee9W5+7O/Vs0vXcqej85z7sh9j5CW9CQVRtFIAoJV5ejP8rtr8jYU9wV+d28ievXeHMHEzDjSgfPv9DsdzCWksbrGe6v4kNO6+8eiQFQVv6NgS9hnJ+h+eIimpfvlAOun+P1x+W4dGkesls0mQj8ntx3p5rMV9RU27Sdu5naOTcvinm1M4F3NMKjaFbrGcmlGYgnFkE8F8KjDcnEAxahbbyI9vwitKubmxvr63vtp5sbEfZhh9FQ8+Oj7Oo8e+FUvFvhBHEbqP4hDw9FjqIPO39dtCjfB6UEp+yRphIAt3i1JYuMO2xq940lMDj/0Aas0yZ79HEriftHx2iNxlKN94/d2zN6PTsvLhPiClvzlEwpDS8j04Oj/fFFWZonouT5430yo40zURuUQ8JHvmhfuRhVcbzxP+Vc4ZyqDAAA";
        final byte[] gzippedXmiBaseDocData = Base64.getDecoder().decode(base64GzippedXmiBaseDocData);
        final ByteArrayInputStream gzippedXmiBaseDocDataBaos = new ByteArrayInputStream(gzippedXmiBaseDocData);
        final GZIPInputStream gzippedXmiBaseDocDataIs = new GZIPInputStream(gzippedXmiBaseDocDataBaos);
        final byte[] xmiBaseDocBytes = IOUtils.toByteArray(gzippedXmiBaseDocDataIs);
        System.out.println(Arrays.toString(xmiBaseDocBytes));
        System.out.println(xmiBaseDocBytes.length);

        Map<String, InputStream> decodingInput = new HashMap<>();
        decodingInput.put(XmiSplitter.DOCUMENT_MODULE_LABEL, new ByteArrayInputStream(xmiBaseDocBytes));
        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(Collections.emptySet(), false);
        final Map<Integer, String> reverseMapping = binaryMapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        decoder.decode(decodingInput, jCas.getTypeSystem(), reverseMapping, featuresToMap, nsMap);
    }

}
