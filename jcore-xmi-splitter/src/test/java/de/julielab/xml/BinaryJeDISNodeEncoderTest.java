package de.julielab.xml;

import de.julielab.jcore.types.*;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.jcore.types.test.MultiValueTypesHolder;
import de.julielab.xml.util.BinaryXmiBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    public void testFullEncodingDecodingBuilding() throws Exception {
        final HashSet<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(Sentence.class.getCanonicalName(), Token.class.getCanonicalName(),
                Gene.class.getCanonicalName(), EventMention.class.getCanonicalName(), Header.class.getCanonicalName(), ResourceEntry.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, false, true, "docs", null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        final XmiSplitterResult splitterResult = splitter.process(xmiData, jCas, 0, Collections.singletonMap("_InitialView", 1));

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult result = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap());
        final List<String> missingItemsForMapping = result.getValuesToMap();
        assertThat(missingItemsForMapping).contains("types:Sentence", "types:Token", "pubmed:Header", "xmi:id", "sofa", "cas:FSArray", "synonyms", "hypernyms", "componentId", "specificType", "protein");

        final Map<String, Integer> mapping = IntStream.range(0, missingItemsForMapping.size()).mapToObj(i -> new ImmutablePair<>(i, missingItemsForMapping.get(i))).collect(Collectors.toMap(Pair::getRight, Pair::getLeft));
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, result.getFeaturesToMap());

        List<InputStream> bais = new ArrayList<>();
        for (String label : encode.keySet()) {
            bais.add(new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(moduleAnnotationNames);
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        final BinaryDecodingResult decode = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, result.getFeaturesToMap(), splitterResult.namespaces);

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
        assertThat(jCas.getAnnotationIndex(Sentence.type).iterator()).isNotEmpty();
        assertThat(jCas.getAnnotationIndex(Token.type).iterator()).isNotEmpty();
        assertThat(jCas.getAnnotationIndex(Gene.type).iterator()).isNotEmpty();
        assertThat(jCas.getAnnotationIndex(Header.type).iterator()).isNotEmpty();
        assertThat(jCas.getAnnotationIndex(ResourceEntry.type).iterator()).isNotEmpty();

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
        StaxXmiSplitter splitter = new StaxXmiSplitter(new HashSet<>(Arrays.asList(Token.class.getCanonicalName())), false, true, "doc", null);
        final XmiSplitterResult splitterResult = splitter.process(baos.toByteArray(), jCas, 0, Collections.singletonMap(CAS.NAME_DEFAULT_SOFA, 1));

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final BinaryStorageAnalysisResult analysisResult = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap());
        final List<String> missingItemsForMapping = analysisResult.getValuesToMap();
        final Map<String, Integer> mapping = IntStream.range(0, missingItemsForMapping.size()).mapToObj(i -> new ImmutablePair<>(i, missingItemsForMapping.get(i))).collect(Collectors.toMap(Pair::getRight, Pair::getLeft));
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, analysisResult.getFeaturesToMap());
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(new HashSet<>(Arrays.asList(Token.class.getCanonicalName())));
        final BinaryDecodingResult decoded = decoder.decode(encode.values().stream().map(ByteArrayOutputStream::toByteArray).map(ByteArrayInputStream::new).collect(Collectors.toList()), jCas.getTypeSystem(), reverseMapping, analysisResult.getFeaturesToMap(), splitterResult.namespaces);
        final BinaryXmiBuilder xmiBuilder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream builtXmiData = xmiBuilder.buildXmi(decoded);
        jCas.reset();
        XmiCasDeserializer.deserialize(new ByteArrayInputStream(builtXmiData.toByteArray()),jCas.getCas());

        final Token t = JCasUtil.selectSingle(jCas, Token.class);
        assertNotNull(t.getSynonyms());
        assertThat(t.getSynonyms().toStringArray()).containsExactly("s1", "s2", "s3");
        assertNotNull(t.getHypernyms());
        assertThat(t.getHypernyms().toStringArray()).containsExactly("h1", "h2");
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
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, "docs", null);
        splitterResult = splitter.process(baos.toByteArray(), jCas, 0, Collections.singletonMap("_InitialView", 1));

        assertTrue(splitterResult.jedisNodesInAnnotationModules.stream().filter(node -> node.getTypeName().equals(CAS.TYPE_NAME_STRING_LIST)).findAny().isPresent());

        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        result = encoder.findMissingItemsForMapping(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), Collections.emptyMap());

        final List<String> missingItemsForMapping = result.getValuesToMap();
        mapping = IntStream.range(0, missingItemsForMapping.size()).mapToObj(i -> new ImmutablePair<>(i, missingItemsForMapping.get(i))).collect(Collectors.toMap(Pair::getRight, Pair::getLeft));

        encode = encoder.encode(splitterResult.jedisNodesInAnnotationModules, jCas.getTypeSystem(), mapping, result.getFeaturesToMap());
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

        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(moduleAnnotationNames);
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        decodedData = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, result.getFeaturesToMap(), splitterResult.namespaces);
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
