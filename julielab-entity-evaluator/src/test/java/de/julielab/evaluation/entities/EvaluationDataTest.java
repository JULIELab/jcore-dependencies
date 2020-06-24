package de.julielab.evaluation.entities;

import de.julielab.evaluation.entities.format.GeneNormalizationNoOffsetsFormat;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.*;

public class EvaluationDataTest {
	@Test
	public void testReadCompleteData() {
		EvaluationData data = EvaluationData.readDataFile(new File("src/test/resources/test-data-complete.genelist"));
		assertEquals(2, data.size());
		EvaluationDataEntry e1 = data.get(0);
		assertTrue(e1.isMention());
		assertEquals("1", e1.getDocId());
		assertEquals("1", e1.getEntityId());
		assertEquals(0, e1.getBegin());
		assertEquals(4, e1.getEnd());
		assertEquals("gen1", e1.getEntityString());
		assertEquals("system1", e1.getRecognitionSystem());
		
		EvaluationDataEntry e2 = data.get(1);
		assertTrue(e2.isMention());
		assertEquals("1", e2.getDocId());
		assertEquals("2", e2.getEntityId());
		assertEquals(5, e2.getBegin());
		assertEquals(8, e2.getEnd());
		assertEquals("gen2", e2.getEntityString());
		assertEquals("system2", e2.getRecognitionSystem());
	}
	
	@Test
	public void testReadNoOffsetData() {
		EvaluationData data = EvaluationData.readDataFile(new File("src/test/resources/test-data-no-offsets.genelist"), new GeneNormalizationNoOffsetsFormat());
		assertEquals(2, data.size());
		EvaluationDataEntry e1 = data.get(0);
		assertFalse(e1.isMention());
		assertEquals("1", e1.getDocId());
		assertEquals("1", e1.getEntityId());
		assertEquals(-1, e1.getBegin());
		assertEquals(-1, e1.getEnd());
		assertEquals("gen1", e1.getEntityString());
		assertEquals("system1", e1.getRecognitionSystem());
		
		EvaluationDataEntry e2 = data.get(1);
		assertFalse(e2.isMention());
		assertEquals("1", e2.getDocId());
		assertEquals("2", e2.getEntityId());
		assertEquals(-1, e2.getBegin());
		assertEquals(-1, e2.getEnd());
		assertEquals("gen2", e2.getEntityString());
		assertEquals("system2", e2.getRecognitionSystem());
	}
	
	@Test
	public void testReadNoSystemsData() {
		EvaluationData data = EvaluationData.readDataFile(new File("src/test/resources/test-data-no-systems.genelist"));
		assertEquals(2, data.size());
		EvaluationDataEntry e1 = data.get(0);
		assertTrue(e1.isMention());
		assertEquals("1", e1.getDocId());
		assertEquals("1", e1.getEntityId());
		assertEquals(0, e1.getBegin());
		assertEquals(4, e1.getEnd());
		assertEquals("gen1", e1.getEntityString());
		assertEquals(null, e1.getRecognitionSystem());
		
		EvaluationDataEntry e2 = data.get(1);
		assertTrue(e2.isMention());
		assertEquals("1", e2.getDocId());
		assertEquals("2", e2.getEntityId());
		assertEquals(5, e2.getBegin());
		assertEquals(8, e2.getEnd());
		assertEquals("gen2", e2.getEntityString());
		assertEquals(null, e2.getRecognitionSystem());
	}

	@Test
	public void testGroupByLabel() {
		EvaluationData data = EvaluationData.readDataFile(new File("src/test/resources/test-data-label-grouping.genelist"));
		Map<String, EvaluationData> m = data.groupByEntityLabel();
		System.out.println(m);

	}
}
