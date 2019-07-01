package de.julielab.xml;

import com.ximpleware.NavException;
import de.julielab.jcore.types.*;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.jcore.types.test.MultiValueTypesHolder;
import de.julielab.xml.util.BinaryXmiBuilder;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.*;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class BinaryJeDISNodeEncoderTest {


    @Test
    public void testBinary() throws IOException, XMISplitterException, UIMAException, NavException {
        final HashSet<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(Sentence.class.getCanonicalName(), Token.class.getCanonicalName(),
                Gene.class.getCanonicalName(), EventMention.class.getCanonicalName(), Organism.class.getCanonicalName(), Header.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, false, false, null, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        final XmiSplitterResult splitterResult = splitter.process(xmiData, jCas, 0, Collections.singletonMap("_InitialView", 1));

        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final Collection<JeDISVTDGraphNode> nodesWithLabel = nodesByXmiId.values().stream().filter(n -> !n.getAnnotationModuleLabels().isEmpty()).collect(Collectors.toList());
        final BinaryStorageAnalysisResult result = encoder.findMissingItemsForMapping(nodesWithLabel, jCas.getTypeSystem(), Collections.emptyMap());
        final List<String> missingItemsForMapping = result.getValuesToMap();
        assertThat(missingItemsForMapping).contains("types:Sentence", "types:Token", "pubmed:Header", "xmi:id", "sofa", "cas:FSArray", "synonyms", "hypernyms", "componentId", "specificType", "protein");

        final Map<String, Integer> mapping = IntStream.range(0, missingItemsForMapping.size()).mapToObj(i -> new ImmutablePair<>(i, missingItemsForMapping.get(i))).collect(Collectors.toMap(Pair::getRight, Pair::getLeft));
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(nodesWithLabel, jCas.getTypeSystem(), mapping, result.getFeaturesToMap());

        List<InputStream> bais = new ArrayList<>();
        for (String label : encode.keySet()) {
            bais.add(new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(moduleAnnotationNames);
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, result.getFeaturesToMap(), splitterResult.namespaces);

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
        hypernyms.copyFromArray(new String[]{"h1", "h2", "h3"}, 0, 0, 2);
        token.setHypernyms(hypernyms);
        token.addToIndexes();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        StaxXmiSplitter splitter = new StaxXmiSplitter(new HashSet<>(Arrays.asList(Token.class.getCanonicalName())), false, false, null, null);
        splitter.process(baos.toByteArray(), jCas, 0, null);

        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final Collection<JeDISVTDGraphNode> nodesWithLabel = nodesByXmiId.values().stream().filter(n -> !n.getAnnotationModuleLabels().isEmpty()).collect(Collectors.toList());
        final BinaryStorageAnalysisResult missingItemsForMapping = encoder.findMissingItemsForMapping(nodesWithLabel, jCas.getTypeSystem(), Collections.emptyMap());
    }

    @Test
    public void testNumericalArrays() throws Exception {
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
        fsNoRef.set(1, new Annotation(jCas, 2, 3));
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
        //System.out.println(baos.toString(UTF_8));
        final HashSet<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(MultiValueTypesHolder.class.getCanonicalName()));
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, "docs", null);
        final XmiSplitterResult splitterResult = splitter.process(baos.toByteArray(), jCas, 0, Collections.singletonMap("_InitialView", 1));

        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final Collection<JeDISVTDGraphNode> nodesWithLabel = nodesByXmiId.values().stream().filter(n -> !n.getAnnotationModuleLabels().isEmpty()).collect(Collectors.toList());
        final BinaryStorageAnalysisResult result = encoder.findMissingItemsForMapping(nodesWithLabel, jCas.getTypeSystem(), Collections.emptyMap());
        final List<String> missingItemsForMapping = result.getValuesToMap();
        final Map<String, Integer> mapping = IntStream.range(0, missingItemsForMapping.size()).mapToObj(i -> new ImmutablePair<>(i, missingItemsForMapping.get(i))).collect(Collectors.toMap(Pair::getRight, Pair::getLeft));
        final Map<String, ByteArrayOutputStream> encode = encoder.encode(nodesWithLabel, jCas.getTypeSystem(), mapping, result.getFeaturesToMap());

        System.out.println(splitterResult.xmiData.get(MultiValueTypesHolder.class.getCanonicalName()).toString(UTF_8));
        List<InputStream> bais = new ArrayList<>();
        for (String label : encode.keySet()) {
            bais.add(new ByteArrayInputStream(encode.get(label).toByteArray()));
        }
        final BinaryJeDISNodeDecoder decoder = new BinaryJeDISNodeDecoder(moduleAnnotationNames);
        final Map<Integer, String> reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        final BinaryDecodingResult decodedData = decoder.decode(bais, jCas.getTypeSystem(), reverseMapping, result.getFeaturesToMap(), splitterResult.namespaces);
        System.out.println(decodedData.getXmiData().toString(UTF_8));

        final BinaryXmiBuilder xmiBuilder = new BinaryXmiBuilder(splitterResult.namespaces);
        final ByteArrayOutputStream xmiData = xmiBuilder.buildXmi(decodedData);
        JCas newJCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types", "arrayAndListHolderTestType");
        System.out.println(xmiData.toString(UTF_8));
        XmiCasDeserializer.deserialize(new ByteArrayInputStream(xmiData.toByteArray()), newJCas.getCas());

    }


}
