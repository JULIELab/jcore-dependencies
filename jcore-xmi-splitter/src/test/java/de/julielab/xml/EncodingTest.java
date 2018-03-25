package de.julielab.xml;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.julielab.jcore.types.Lemma;
import de.julielab.jcore.types.Token;
import de.julielab.xml.XmiSplitter.XmiSplitterResult;

/**
 * This test requires a new source XMI file because the given one uses a deprecated type system
 * @author faessler
 *
 */
public class EncodingTest {
	
	private static final Logger log = LoggerFactory.getLogger(EncodingTest.class);


	private static final String TEST_XMI = "src/test/resources/semedico.xmi";

	private static final String DOC = "documents";

	private static final String SENTTYPE = "de.julielab.jcore.types.Sentence";
	private static final String TOKTYPE = "de.julielab.jcore.types.Token";
	private static final String POSTYPE = "de.julielab.jcore.types.PennBioIEPOSTag";

	private static final String DOCBIN = "src/test/resources/encoding/documentBytes.bin";
	private static final String TOKBIN = "src/test/resources/encoding/tokenBytes.bin";
	private static final String SENTBIN = "src/test/resources/encoding/sentenceBytes.bin";
	private static final String POSBIN = "src/test/resources/encoding/posBytes.bin";

	private static final String NAMESPACEMAP_BIN = "src/test/resources/encoding/namespacemap.bin";

	private static final String TEST_AE_DESCRIPTOR = "src/test/resources/TestAEDescriptor.xml";
	
	private static final Map<String, Integer> defaultSofaIdMap = new HashMap<>();
	static {
		defaultSofaIdMap.put("_InitialView", 1);
	}
	
	
	@Test
	public void testAssembleDocument() throws Exception {
		CAS aCAS = getTestCAS();
		Map<String, String> nsAndXmiVersionMap = loadNamespaceMap();
		String[] annotationsToRetrieve = { SENTTYPE, TOKTYPE };
		XmiBuilder xmiBuilder = new XmiBuilder(nsAndXmiVersionMap, annotationsToRetrieve);
		LinkedHashMap<String, InputStream> data =loadTestXmiByteData();
		ByteArrayOutputStream xmi = xmiBuilder.buildXmi(data, DOC, aCAS.getTypeSystem());
		byte[] ba = xmi.toByteArray();

		FileOutputStream fos = new FileOutputStream("myfile.xmi");
		fos.write(ba);
		fos.close();

		XmiCasDeserializer.deserialize(new FileInputStream("myfile.xmi"), aCAS);
		FSIterator<Annotation> it = aCAS.getJCas().getAnnotationIndex(Token.type).iterator();
		Token token = (Token) it.next();
		assertEquals("𝒟T,ϕ(y)", token.getLemma().getValue());
		token = (Token) it.next();
		assertEquals("𝒟 2", token.getLemma().getValue());
		token = (Token) it.next();
		assertEquals("𝒟 3", token.getLemma().getValue());
		token = (Token) it.next();
		assertEquals("𝒟 4", token.getLemma().getValue());
		

	}
	
	/**
	 * Generates the test data for the XMIBuilder test by splitting the test xmi
	 * file and then storing the byte arrays to file.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		List<String> elementsToStore = Lists.newArrayList(TOKTYPE, SENTTYPE, POSTYPE);
		CAS cas = getTestCAS();
		XmiCasDeserializer.deserialize(new FileInputStream(TEST_XMI), cas);
		// manipulate the first token to add a unicode character with a 3-byte codepoint
		FSIterator<Annotation> it = cas.getJCas().getAnnotationIndex(Token.type).iterator();
		Token token= (Token) it.next();
		Lemma lemma = new Lemma(cas.getJCas(), token.getBegin(), token.getEnd());
		lemma.setValue("𝒟T,ϕ(y)");
		token.setLemma(lemma);
		
		token = (Token) it.next();
		lemma =  new Lemma(cas.getJCas(), token.getBegin(), token.getEnd());
		lemma.setValue("𝒟 2");
		token.setLemma(lemma);
		
		token = (Token) it.next();
		lemma =  new Lemma(cas.getJCas(), token.getBegin(), token.getEnd());
		lemma.setValue("𝒟 3");
		token.setLemma(lemma);
		
		token = (Token) it.next();
		lemma =  new Lemma(cas.getJCas(), token.getBegin(), token.getEnd());
		lemma.setValue("𝒟 4");
		token.setLemma(lemma);
	
		XmiSplitter xmiSplitter = new XmiSplitter(elementsToStore, true, true, DOC, XmiSplitterTest.BASE_DOCUMENT_ANNOTATIONS);
		// When storing the base document, begin with 0.
		int nextPossibleId = 0;
//		byte[] b = FileUtils.readFileToByteArray(new File(TEST_XMI));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XmiCasSerializer.serialize(cas,baos);

		XmiSplitterResult result = xmiSplitter.process(new ByteArrayInputStream(baos.toByteArray()), cas.getJCas(), nextPossibleId, defaultSofaIdMap);
		Map<String, ByteArrayOutputStream> xmiData = (Map<String, ByteArrayOutputStream>) result.xmiData;
		HashMap<String, String> namespaceMap = (HashMap<String, String>) result.namespaces;
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NAMESPACEMAP_BIN))) {
			oos.writeObject(namespaceMap);
		}
		log.info("Creating test data.");
		try (ByteArrayOutputStream docOS = xmiData.get(DOC);
				ByteArrayOutputStream tokenOS = xmiData.get(TOKTYPE);
				ByteArrayOutputStream sentenceOS = xmiData.get(SENTTYPE);
				ByteArrayOutputStream posOS = xmiData.get(POSTYPE);
				FileOutputStream docBytesFile = new FileOutputStream(DOCBIN);
				FileOutputStream tokenBytesFile = new FileOutputStream(TOKBIN);
				FileOutputStream sentenceBytesFile = new FileOutputStream(SENTBIN);
				FileOutputStream posBytesFile = new FileOutputStream(POSBIN)) {

			docBytesFile.write(docOS.toByteArray());
			byte[] byteArray = tokenOS.toByteArray();
			System.out.println(new String(byteArray));
			tokenBytesFile.write(byteArray);
			sentenceBytesFile.write(sentenceOS.toByteArray());
			posBytesFile.write(posOS.toByteArray());

			// log.debug("{}", new String(docOS.toByteArray()));
			// log.debug("{}", new String(sentenceOS.toByteArray()));
			// log.debug("{}", new String(tokenOS.toByteArray()));
			log.info("Finished.");
		}
		
	}
	
	private static CAS getTestCAS() throws ResourceInitializationException, IOException, InvalidXMLException {
		XMLInputSource input = new XMLInputSource(TEST_AE_DESCRIPTOR);
		ResourceSpecifier resourceSpecifier = UIMAFramework.getXMLParser().parseResourceSpecifier(input);
		AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(resourceSpecifier);
		CAS cas = CasCreationUtils.createCas(ae.getAnalysisEngineMetaData());
		return cas;
	}
	
	private LinkedHashMap<String, InputStream> loadTestXmiByteData() throws Exception {
		LinkedHashMap<String, InputStream> xmiData = new LinkedHashMap<>();
		InputStream docIS = new FileInputStream(DOCBIN);
		InputStream tokenIS = new FileInputStream(TOKBIN);
		InputStream sentenceIS = new FileInputStream(SENTBIN);
		// InputStream posIS = new FileInputStream(POSBIN);

		xmiData.put(DOC, docIS);
		xmiData.put(SENTTYPE, sentenceIS);
		xmiData.put(TOKTYPE, tokenIS);
		// xmiData.put(POSTYPE, posIS);
		return xmiData;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> loadNamespaceMap() throws Exception {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(NAMESPACEMAP_BIN))) {
			return (Map<String, String>) ois.readObject();
		}
	}
}
