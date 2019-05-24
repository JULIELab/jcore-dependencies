package de.julielab.evaluation.entities;

import static org.junit.Assert.*;

import org.junit.Test;

import de.julielab.evaluation.entities.EvaluationDataEntry.ComparisonType;
import de.julielab.evaluation.entities.EvaluationDataEntry.OverlapType;

public class EvaluationDataEntryTest {
	@Test
	public void testExactMatch() {
		EvaluationDataEntry entry1 = new EvaluationDataEntry("d1", "e1", 0, 10);
		EvaluationDataEntry entry2 = new EvaluationDataEntry("d1", "e1", 0, 10);
		assertTrue(entry1.equals(entry2));
		assertTrue(entry2.equals(entry1));
		assertEquals(0, entry1.compareTo(entry2));
		EvaluationDataEntry docEntry1 = entry1.toDocWiseEntry();
		EvaluationDataEntry docEntry2 = entry2.toDocWiseEntry();
		assertEquals(docEntry1, docEntry2);

		entry2.setDocId("d2");
		assertFalse(entry1.equals(entry2));
		assertFalse(entry2.equals(entry1));
		assertNotSame(0, entry1.compareTo(entry2));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertNotSame(docEntry1, docEntry2);
		entry2.setDocId("d1");
		assertTrue(entry1.equals(entry2));
		assertTrue(entry2.equals(entry1));
		assertEquals(0, entry1.compareTo(entry2));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertEquals(docEntry1, docEntry2);

		entry2.setBegin(2);
		assertEquals(entry2.getBegin(), 2);
		assertEquals(entry2.getEnd(), 10);
		assertFalse(entry1.equals(entry2));
		assertFalse(entry2.equals(entry1));
		assertNotSame(0, entry1.compareTo(entry2));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertNotSame(docEntry1, docEntry2);
		entry2.setBegin(0);
		
		entry2.setEnd(7);
		assertFalse(entry1.equals(entry2));
		assertFalse(entry2.equals(entry1));
		assertNotSame(0, entry1.compareTo(entry2));
		
	}

	@Test
	public void testOverlappingMatch() {
		EvaluationDataEntry entry1 = new EvaluationDataEntry("d1", "e1", 0, 10);
		EvaluationDataEntry entry2 = new EvaluationDataEntry("d1", "e1", 5, 15);
		EvaluationDataEntry docEntry1;
		EvaluationDataEntry docEntry2;
		assertFalse(entry1.equals(entry2));
		assertFalse(entry2.equals(entry1));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertNotSame(docEntry1, docEntry2);

		entry1.setComparisonType(ComparisonType.OVERLAP);
		entry1.setOverlapType(OverlapType.CHARS);
		entry1.setOverlapSize(2);
		entry2.setComparisonType(ComparisonType.OVERLAP);
		entry2.setOverlapType(OverlapType.CHARS);
		entry2.setOverlapSize(2);
		assertTrue(entry1.equals(entry2));
		assertTrue(entry2.equals(entry1));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertEquals(docEntry1, docEntry2);
		
		entry1.setOverlapSize(5);
		entry2.setOverlapSize(5);
		assertTrue(entry1.equals(entry2));
		assertTrue(entry2.equals(entry1));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertEquals(docEntry1, docEntry2);
		
		entry1.setOverlapSize(6);
		entry2.setOverlapSize(6);
		assertFalse(entry1.equals(entry2));
		assertFalse(entry2.equals(entry1));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertNotSame(docEntry1, docEntry2);
		
		entry1.setOverlapType(OverlapType.PERCENT);
		entry1.setOverlapSize(30);
		entry2.setOverlapType(OverlapType.PERCENT);
		entry2.setOverlapSize(30);
		assertTrue(entry1.equals(entry2));
		assertTrue(entry2.equals(entry1));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertEquals(docEntry1, docEntry2);
		
		entry1.setOverlapSize(50);
		entry2.setOverlapSize(50);
		assertTrue(entry1.equals(entry2));
		assertTrue(entry2.equals(entry1));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertEquals(docEntry1, docEntry2);
		
		entry1.setOverlapSize(51);
		entry2.setOverlapSize(51);
		assertFalse(entry1.equals(entry2));
		assertFalse(entry2.equals(entry1));
		docEntry1 = entry1.toDocWiseEntry();
		docEntry2 = entry2.toDocWiseEntry();
		assertNotSame(docEntry1, docEntry2);
		
	}
}
