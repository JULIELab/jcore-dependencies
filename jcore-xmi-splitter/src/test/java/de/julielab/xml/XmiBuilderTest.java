package de.julielab.xml;

import de.julielab.jcore.types.AutoDescriptor;
import de.julielab.jcore.types.DocumentClass;
import de.julielab.xml.util.XMISplitterException;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class XmiBuilderTest {

    @Test
    public void testArrayShareNotAllowed() throws UIMAException, SAXException, XMISplitterException, IOException {
        // In this test we check that we handle correctly arrays that may not be shared. Those arrays are represented
        // in memory as FSArray but in XMI the FSArray is left out: the array-valued feature just lists all the
        // xmi:id references itself, without creating an FSArray element.
        // This is the case with the documentClasses feature of the AutoDescriptor.
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        jCas.setDocumentText("This is p < 0.5.");

        AutoDescriptor ad = new AutoDescriptor(jCas);
        ad.addToIndexes();
        FSArray fsa = new FSArray(jCas, 1);
        DocumentClass dc = new DocumentClass(jCas);
        dc.setClassname("myclass");
        fsa.set(0, dc);
        //fsa.set(1, dc);
        ad.setDocumentClasses(fsa);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        System.out.println(new String(baos.toByteArray()));
        VtdXmlXmiSplitter splitter = new VtdXmlXmiSplitter(new HashSet<>(Arrays.asList(AutoDescriptor.class.getCanonicalName())), true, true, Collections.emptySet());
        XmiSplitterResult result = splitter.process(baos.toByteArray(), jCas, 0, null);

        XmiBuilder builder = new XmiBuilder(result.namespaces, new String[]{AutoDescriptor.class.getCanonicalName()});
        LinkedHashMap<String, InputStream> inputMap = result.xmiData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ByteArrayInputStream(e.getValue().toByteArray()), (k, v) -> v, LinkedHashMap::new));
        ByteArrayOutputStream builtXmi = builder.buildXmi(inputMap, XmiSplitter.DOCUMENT_MODULE_LABEL, jCas.getTypeSystem());

        jCas.reset();
        ByteArrayInputStream finalIs = new ByteArrayInputStream(builtXmi.toByteArray());
        XmiCasDeserializer.deserialize(finalIs, jCas.getCas());
        assertThatCode(() -> JCasUtil.selectSingle(jCas, AutoDescriptor.class)).doesNotThrowAnyException();

        AutoDescriptor ad2 = JCasUtil.selectSingle(jCas, AutoDescriptor.class);
        assertThat(ad2.getDocumentClasses()).isNotNull();
        assertThat(ad2.getDocumentClasses()).hasSize(1);
        assertThat(ad2.getDocumentClasses().get(0)).extracting("classname").containsExactly("myclass");
    }

    @Test
    public void testArrayShareNotAllowedMultipleElements() throws UIMAException, SAXException, XMISplitterException, IOException {
        // In this test we check that we handle correctly arrays that may not be shared. Those arrays are represented
        // in memory as FSArray but in XMI the FSArray is left out: the array-valued feature just lists all the
        // xmi:id references itself, without creating an FSArray element
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        jCas.setDocumentText("This is p < 0.5.");

        AutoDescriptor ad = new AutoDescriptor(jCas);
        ad.addToIndexes();
        FSArray fsa = new FSArray(jCas, 2);
        DocumentClass dc = new DocumentClass(jCas);
        dc.setClassname("myclass");
        fsa.set(0, dc);
        fsa.set(1, dc);
        ad.setDocumentClasses(fsa);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        System.out.println(new String(baos.toByteArray()));
        VtdXmlXmiSplitter splitter = new VtdXmlXmiSplitter(new HashSet<>(Arrays.asList(AutoDescriptor.class.getCanonicalName())), true, true, Collections.emptySet());
        XmiSplitterResult result = splitter.process(baos.toByteArray(), jCas, 0, null);

        XmiBuilder builder = new XmiBuilder(result.namespaces, new String[0]);
        LinkedHashMap<String, InputStream> inputMap = result.xmiData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ByteArrayInputStream(e.getValue().toByteArray()), (k, v) -> v, LinkedHashMap::new));
        assertThatCode(() -> builder.buildXmi(inputMap, XmiSplitter.DOCUMENT_MODULE_LABEL, jCas.getTypeSystem())).doesNotThrowAnyException();
    }
}
