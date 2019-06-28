package de.julielab.xml;

import com.ximpleware.NavException;
import de.julielab.jcore.types.*;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.jcore.utility.JCoReTools;
import de.julielab.xml.util.XMISplitterException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class BinaryJeDISNodeEncoderTest {


    @Test
    public void testBinary() throws IOException, XMISplitterException, UIMAException, NavException {
        StaxXmiSplitter splitter = new StaxXmiSplitter(new HashSet<>(Arrays.asList(Sentence.class.getCanonicalName(), Token.class.getCanonicalName(),
                Gene.class.getCanonicalName(), EventMention.class.getCanonicalName(), Organism.class.getCanonicalName(), Header.class.getCanonicalName())), false, false, null, null);
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        splitter.process(xmiData, jCas, 0, Collections.singletonMap("_InitialView", 1));

        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final Collection<JeDISVTDGraphNode> nodesWithLabel = nodesByXmiId.values().stream().filter(n -> !n.getAnnotationModuleLabels().isEmpty()).collect(Collectors.toList());
        final List<String> missingItemsForMapping = encoder.findMissingItemsForMapping(nodesWithLabel, jCas.getTypeSystem(), Collections.emptyMap());
        assertThat(missingItemsForMapping).contains("types:Sentence", "types:Token", "pubmed:Header", "xmi:id", "sofa", "cas:FSArray", "synonyms", "hypernyms", "componentId", "specificType", "protein");

        final Map<String, Integer> mapping = IntStream.range(0, missingItemsForMapping.size()).mapToObj(i -> new ImmutablePair<>(i, missingItemsForMapping.get(i))).collect(Collectors.toMap(Pair::getRight, Pair::getLeft));
        encoder.encode(nodesWithLabel, jCas.getTypeSystem(), mapping);
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
        hypernyms.copyFromArray( new String[]{"h1", "h2", "h3"}, 0, 0, 2);
        token.setHypernyms(hypernyms);
        token.addToIndexes();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        StaxXmiSplitter splitter = new StaxXmiSplitter(new HashSet<>(Arrays.asList( Token.class.getCanonicalName())), false, false, null, null);
        splitter.process(baos.toByteArray(), jCas, 0, null);

        Map<Integer, JeDISVTDGraphNode> nodesByXmiId = splitter.getNodesByXmiId();
        final BinaryJeDISNodeEncoder encoder = new BinaryJeDISNodeEncoder();
        final Collection<JeDISVTDGraphNode> nodesWithLabel = nodesByXmiId.values().stream().filter(n -> !n.getAnnotationModuleLabels().isEmpty()).collect(Collectors.toList());
        final List<String> missingItemsForMapping = encoder.findMissingItemsForMapping(nodesWithLabel, jCas.getTypeSystem(), Collections.emptyMap());
    }


}
