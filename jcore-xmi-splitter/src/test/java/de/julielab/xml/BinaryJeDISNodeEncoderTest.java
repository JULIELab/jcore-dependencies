package de.julielab.xml;

import de.julielab.jcore.types.*;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.jcore.types.test.MultiValueTypesHolder;
import de.julielab.xml.binary.*;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
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
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        final XmiSplitterResult splitterResult1 = splitter.process(baos1.toByteArray(), jCas, 0, Collections.emptyMap());

        jCas.reset();
        final Token t3 = new Token(jCas);
        t3.setComponentId("TokenSplitter2");
        t3.setOrthogr("lowercase");
        t3.addToIndexes();
        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos2);
        final XmiSplitterResult splitterResult2 = splitter.process(baos2.toByteArray(), jCas, 0, Collections.emptyMap());

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
    }

    @Test
    public void testFullEncodingDecodingBuilding() throws Exception {
        final HashSet<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(Sentence.class.getCanonicalName(), Token.class.getCanonicalName(),
                Gene.class.getCanonicalName(), EventMention.class.getCanonicalName(), Header.class.getCanonicalName(), ResourceEntry.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, false, true, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        final XmiSplitterResult splitterResult = splitter.process(xmiData, jCas, 0, Collections.singletonMap("_InitialView", 1));

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult result = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());
        final List<String> missingItemsForMapping = result.getMissingValuesToMap();
        assertThat(missingItemsForMapping).contains("types:Sentence", "types:Token", "pubmed:Header", "xmi:id", "sofa", "cas:FSArray", "synonyms", "hypernyms", "componentId", "specificType", "protein");

        final Map<String, Integer> mapping = result.getMissingItemsMapping();
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, result.getMissingFeaturesToMap());

        List<InputStream> bais = new ArrayList<>();
        for (String label : encode.keySet()) {
            bais.add(new ByteArrayInputStream(encode.get(label).toByteArray()));
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
        final XmiSplitterResult splitterResult = splitter.process(baos.toByteArray(), jCas, 0, Collections.singletonMap(CAS.NAME_DEFAULT_SOFA, 1));

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult analysisResult = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());
        final List<String> missingItemsForMapping = analysisResult.getMissingValuesToMap();
        final Map<String, Integer> mapping = analysisResult.getMissingItemsMapping();
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, analysisResult.getMissingFeaturesToMap());
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(new HashSet<>(Arrays.asList(Token.class.getCanonicalName())), false);
        final BinaryDecodingResult decoded = decoder.decode(encode.values().stream().map(ByteArrayOutputStream::toByteArray).map(ByteArrayInputStream::new).collect(Collectors.toList()), jCas.getTypeSystem(), reverseMapping, analysisResult.getMissingFeaturesToMap(), splitterResult.namespaces);
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

    private JCas getPartiallyLoadedModuleData(boolean omitElementsWithMissingReferences) throws UIMAException, SAXException, XMISplitterException {
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
        final XmiSplitterResult splitterResult = splitter.process(baos.toByteArray(), jCas, 0, Collections.singletonMap(CAS.NAME_DEFAULT_SOFA, 1));

        // ---------- Binary encoding of the modules
        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult analysisResult = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap(), Collections.emptyMap());
        final List<String> missingItemsForMapping = analysisResult.getMissingValuesToMap();
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
        final BinaryDecodingResult decoded = decoder.decode(encode.keySet().stream().filter(annotationLabelsToLoad::contains).map(encode::get).map(ByteArrayOutputStream::toByteArray).map(ByteArrayInputStream::new).collect(Collectors.toList()), jCas.getTypeSystem(), reverseMapping, analysisResult.getMissingFeaturesToMap(), splitterResult.namespaces);

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
        System.out.println(baos.toString(UTF_8));
        moduleAnnotationNames = new HashSet<>(Arrays.asList(MultiValueTypesHolder.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, null);
        splitterResult = splitter.process(baos.toByteArray(), jCas, 0, Collections.singletonMap("_InitialView", 1));

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
        List<InputStream> bais = new ArrayList<>();
        for (String label : encode.keySet()) {
            bais.add(new ByteArrayInputStream(encode.get(label).toByteArray()));
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


}
