package de.julielab.evaluation.entities;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class EntityEvaluatorTest {

	public static Logger log = LoggerFactory.getLogger(EntityEvaluatorTest.class);

	@Test
	public void testByApiCall() {
		String[] g1 = { "1", "1", "2", "4" }; // x
		String[] g2 = { "1", "2", "5", "8" };
		String[] g3 = { "2", "2", "7", "10" }; // x
		String[] g4 = { "3", "3", "3", "7" }; // x
		String[] g5 = { "3", "4", "3", "7" };
		String[] g6 = { "3", "3", "11", "18" }; // x

		// Correct
		String[] p1 = { "1", "1", "2", "4" };
		// FP
		// FN
		String[] p2 = { "2", "1", "0", "3" };
		// Correct
		String[] p3 = { "2", "2", "7", "10" };
		// Correct FN (Count only for mention-wise)
		String[] p4 = { "3", "4", "3", "7" };
		// Correct
		String[] p5 = { "3", "3", "11", "18" };
		// FP
		String[] p6 = { "4", "3", "11", "18" };

		EvaluationData gold = new EvaluationData();
		gold.add(g1);
		gold.add(g2);
		gold.add(g3);
		gold.add(g4);
		gold.add(g5);
		gold.add(g6);

		List<String[]> gList = Lists.newArrayList(g1, g2, g3, g4, g5, g6);
		EvaluationData gold2 = new EvaluationData(gList);

		assertEquals(gold, gold2);

		EvaluationData pred = new EvaluationData(p1, p2, p3, p4, p5, p6);

		ArrayList<String[]> pList = Lists.newArrayList(p1, p2, p3, p4, p5, p6);

		EntityEvaluator evaluator = new EntityEvaluator();
		EntityEvaluationResult r1 = evaluator.evaluate(gold, pred).getSingle();
		EntityEvaluationResult r2 = evaluator.evaluate(gList, pList).getSingle();

		assertEquals(r1, r2);

		log.debug(r1.getEvaluationReportLong());

		assertEquals(r1.getSumTpDocWise(), 4);
		assertEquals(r1.getSumFpDocWise(), 2);
		assertEquals(r1.getSumFnDocWise(), 1);

		assertEquals(r1.getSumTpMentionWise(), 4);
		assertEquals(r1.getSumFpMentionWise(), 2);
		assertEquals(r1.getSumFnMentionWise(), 2);
	}

	@Test
	public void testByApiCallOverlap() throws Exception {
		String[] g1 = { "d1", "e1", "2", "8" };
		String[] g2 = { "d1", "e2", "5", "8" };
		String[] g3 = { "d2", "e2", "5", "12" };
		EvaluationData gold = new EvaluationData(g1, g2, g3);

		// Correct, but only due to overlapping.
		String[] p1 = { "d1", "e1", "4", "8" };
		// Wrong.
		String[] p2 = { "d2", "e1", "0", "3" };
		// Only one character overlap with g3, here: Wrong
		String[] p3 = { "d2", "e2", "5", "6" };
		EvaluationData pred = new EvaluationData(p1, p2, p3);

		// First try with a default evaluator, i.e. exact matching.
		EntityEvaluator evaluator = new EntityEvaluator();
		EntityEvaluationResult report = evaluator.evaluate(gold, pred).getSingle();

		assertEquals(2, report.getSumTpDocWise());
		assertEquals(1, report.getSumFpDocWise());
		assertEquals(1, report.getSumFnDocWise());

		assertEquals(0, report.getSumTpMentionWise());
		assertEquals(3, report.getSumFpMentionWise());
		assertEquals(3, report.getSumFnMentionWise());

		// Now evaluate with overlapping.
		// First: Absolute count of character-overlap
		evaluator = new EntityEvaluator(new File("src/test/resources/overlap-2-chars.properties"));
		report = evaluator.evaluate(gold, pred).getSingle();

		assertEquals(2, report.getSumTpDocWise());
		assertEquals(1, report.getSumFpDocWise());
		assertEquals(1, report.getSumFnDocWise());

		assertEquals(1, report.getSumTpMentionWise());
		assertEquals(2, report.getSumFpMentionWise());
		assertEquals(2, report.getSumFnMentionWise());

		// Second: 50-percent-overlap
		evaluator = new EntityEvaluator(new File("src/test/resources/overlap-50-percent.properties"));
		report = evaluator.evaluate(gold, pred).getSingle();

		assertEquals(2, report.getSumTpDocWise());
		assertEquals(1, report.getSumFpDocWise());
		assertEquals(1, report.getSumFnDocWise());

		assertEquals(1, report.getSumTpMentionWise());
		assertEquals(2, report.getSumFpMentionWise());
		assertEquals(2, report.getSumFnMentionWise());
	}

	@Test
	public void testByMainCall() throws IOException {
		EntityEvaluator.main(new String[] { "-g", "src/test/resources/bc2Gold.genelist",
				"-p", "src/test/resources/bc2Pred.genelist" });
	}

	@Test
	public void testByMainCallNoOffsets() throws IOException {
		EntityEvaluator.main(new String[] { "-g", "src/test/resources/bc2GNtest.genelist",
				"-p", "src/test/resources/bc2GNtest.genelist" });
	}
}
