package de.julielab.evaluation.entities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FMeasureTest {
	@Test
	public void testGetFMeasure() {
		assertEquals(0.8, FMeasure.getFMeasure(4,  1,  1), 0.0001);
	}
	
	@Test
	public void testgetPrecision() {
		assertEquals(0.75, FMeasure.getPrecision(12, 4,  5), 0.0001);
	}
	
	@Test
	public void testGetRecall() {
		assertEquals(0.44444, FMeasure.getRecall(4,  1,  5), 0.0001);
	}
}
