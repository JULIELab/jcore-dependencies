package de.julielab.xml;

import de.julielab.jcore.types.PennBioIEPOSTag;
import de.julielab.jcore.types.Title;
import de.julielab.jcore.types.Token;
import de.julielab.jcore.types.pubmed.Header;
import org.apache.commons.io.IOUtils;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
@Ignore
public class SpeedTests {

    @Test
    public void semedicoXmiVTD() throws Exception {
        Set<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(
                Token.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName()));
        Set<String> baseDocumentAnnotations = new HashSet<>(Arrays.asList(Title.class.getCanonicalName(), Header.class.getCanonicalName()));
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");

        for (int i = 0; i < 10000; i++) {
        VtdXmlXmiSplitter splitter = new VtdXmlXmiSplitter(moduleAnnotationNames, true, true, "docs", baseDocumentAnnotations);
            XmiSplitterResult result = splitter.process(xmiData, jCas, 0, Collections.emptyMap());

        }

    }

    @Test
    public void semedicoXmiStax() throws Exception {
        Set<String> moduleAnnotationNames = new HashSet<>(Arrays.asList(
                Token.class.getCanonicalName(),
                PennBioIEPOSTag.class.getCanonicalName()));
        Set<String> baseDocumentAnnotations = new HashSet<>(Arrays.asList(Title.class.getCanonicalName(), Header.class.getCanonicalName()));
        byte[] xmiData = IOUtils.toByteArray(new FileInputStream("src/test/resources/semedico.xmi"));
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        for (int i = 0; i < 10000; i++) {
        StaxXmiSplitter splitter = new StaxXmiSplitter(moduleAnnotationNames, true, true, "docs", baseDocumentAnnotations);
            XmiSplitterResult result = splitter.process(xmiData, jCas, 0, Collections.emptyMap());
        }
    }
}
