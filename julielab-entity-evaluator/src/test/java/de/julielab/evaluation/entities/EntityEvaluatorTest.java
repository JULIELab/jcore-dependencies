package de.julielab.evaluation.entities;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.assertj.core.data.Offset;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class EntityEvaluatorTest {

    public static Logger log = LoggerFactory.getLogger(EntityEvaluatorTest.class);

    @Test
    public void testByApiCall() {
        // Format: docId, entityId, begin, end
        String[] g1 = {"1", "1", "2", "4"}; // x
        String[] g2 = {"1", "2", "5", "8"};
        String[] g3 = {"2", "2", "7", "10"}; // x
        String[] g4 = {"3", "3", "3", "7"}; // x
        String[] g5 = {"3", "4", "3", "7"};
        String[] g6 = {"3", "3", "11", "18"}; // x

        // Correct
        String[] p1 = {"1", "1", "2", "4"};
        // FP
        // FN
        String[] p2 = {"2", "1", "0", "3"};
        // Correct
        String[] p3 = {"2", "2", "7", "10"};
        // Correct FN (Count only for mention-wise)
        String[] p4 = {"3", "4", "3", "7"};
        // Correct
        String[] p5 = {"3", "3", "11", "18"};
        // FP
        String[] p6 = {"4", "3", "11", "18"};

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
        String[] g1 = {"d1", "e1", "2", "8"};
        String[] g2 = {"d1", "e2", "5", "8"};
        String[] g3 = {"d2", "e2", "5", "12"};
        EvaluationData gold = new EvaluationData(g1, g2, g3);

        // Correct, but only due to overlapping.
        String[] p1 = {"d1", "e1", "4", "8"};
        // Wrong.
        String[] p2 = {"d2", "e1", "0", "3"};
        // Only one character overlap with g3, here: Wrong
        String[] p3 = {"d2", "e2", "5", "6"};
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
    public void testByApiCallAlternatives() throws Exception {
        String[] g1 = {"d1", "e1", "2", "8"};
        String[] g2 = {"d1", "e2", "5", "8"};
        String[] g3 = {"d2", "e2", "5", "12"};
        EvaluationData gold = new EvaluationData(g1, g2, g3);

        String[] a1 = {"d1", "e1", "4", "8"};
        EvaluationData goldAlt = new EvaluationData(a1);

        // Correct, but only to the alternative.
        String[] p1 = {"d1", "e1", "4", "8"};
        // Wrong.
        String[] p2 = {"d2", "e1", "0", "3"};
        // Only one character overlap with g3, here: Wrong
        String[] p3 = {"d2", "e2", "5", "6"};
        EvaluationData pred = new EvaluationData(p1, p2, p3);

        EntityEvaluator evaluator = new EntityEvaluator();
        EntityEvaluationResult report = evaluator.evaluate(gold, goldAlt, pred).getSingle();

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
        EntityEvaluator.main(new String[]{"-g", "src/test/resources/bc2Gold.genelist",
                "-p", "src/test/resources/bc2Pred.genelist"});
    }

    @Test
    public void testByMainCallNoOffsets() throws IOException {
        EntityEvaluator.main(new String[]{"-g", "src/test/resources/bc2GNtest.genelist",
                "-p", "src/test/resources/bc2GNtest.genelist"});
    }

    @Test
    public void testBANNEROutput() throws IOException {
        // We test the performance of the BANNER tagger trained on BC2GM train on BC2GM test, both with alternatives (e.g. alternatives were also added to the training data).
        // The target performance values were calculated using the alt_eval.pl script of the BC2GM corpus.
        // The EntityEvaluator finds one TP less and one FN more which I didn't pursue further because that is already
        // a minor deviation for two different evaluation algorithms. It is basically the same result.
        // Thus, we expect a certain outcome of this evaluation which is fixed here as a test.
        final EvaluationData gold = EvaluationData.readDataFile(new File("src/test/resources/bc2gmtestgold.genelist"));
        final EvaluationData alt = EvaluationData.readDataFile(new File("src/test/resources/bc2gmtestaltgold.genelist"));
        final EvaluationData pred = EvaluationData.readDataFile(new File("src/test/resources/bc2gmbannerpred.julieeval"));
        final EntityEvaluator evaluator = new EntityEvaluator();
        final EntityEvaluationResults evaluationResults = evaluator.evaluate(gold, alt, pred);
        final double recall = evaluationResults.getOverallResult().getMicroRecallMentionWise();
        final double precision = evaluationResults.getOverallResult().getMicroPrecisionMentionWise();
        final double fscore = evaluationResults.getOverallResult().getMicroFMeasureMentionWise();

        assertThat(recall).isCloseTo(0.8445, Offset.offset(0.01));
        assertThat(precision).isCloseTo(0.8817, Offset.offset(0.01));
        assertThat(fscore).isCloseTo(0.8627, Offset.offset(0.01));
    }

    @Test
    public void overlapTest() {
        // Scenario: An enumeration was recognized as one single entity and split in post-processing. All the entities
        // have the same offsets but different IDs.
        Properties properties = new Properties();
        properties.setProperty(EntityEvaluator.PROP_COMPARISON_TYPE, EvaluationDataEntry.ComparisonType.OVERLAP.name());
        properties.setProperty(EntityEvaluator.PROP_OVERLAP_TYPE, EvaluationDataEntry.OverlapType.CHARS.name());
        properties.setProperty(EntityEvaluator.PROP_OVERLAP_SIZE, "1");
        EntityEvaluator entityEvaluator = new EntityEvaluator(properties);

        EvaluationDataEntry g1 = new EvaluationDataEntry("doc1", "1", 1, 2, "gold");
        EvaluationDataEntry g2 = new EvaluationDataEntry("doc1", "2", 2, 3, "gold");
        EvaluationDataEntry g3 = new EvaluationDataEntry("doc1", "3", 3, 4, "gold");
        EvaluationData gold = new EvaluationData(g1, g2, g3);

        EvaluationDataEntry p1 = new EvaluationDataEntry("doc1", "1", 1, 4, "pred");
        EvaluationDataEntry p2 = new EvaluationDataEntry("doc1", "2", 1, 4, "pred");
        EvaluationDataEntry p3 = new EvaluationDataEntry("doc1", "3", 1, 4, "pred");
        EvaluationData pred = new EvaluationData(p1, p2, p3);

        gold.forEach(e -> {
            e.setComparisonType(EvaluationDataEntry.ComparisonType.OVERLAP);
            e.setOverlapType(EvaluationDataEntry.OverlapType.CHARS);
            e.setOverlapSize(1);
        });
        pred.forEach(e -> {
            e.setComparisonType(EvaluationDataEntry.ComparisonType.OVERLAP);
            e.setOverlapType(EvaluationDataEntry.OverlapType.CHARS);
            e.setOverlapSize(1);
        });

        EntityEvaluationResults evaluate = entityEvaluator.evaluate(gold, pred);

        assertEquals(1.0, evaluate.getSingle().getMacroFMeasureMentionWise(), 0.0000001);
    }

    @Test
    public void overlapTest2() {
        // Scenario: a long and abbreviation form were tagged as one entity and split in a post-processing step.
        // Both new entities have the same offset AND the same ID.
        Properties properties = new Properties();
        properties.setProperty(EntityEvaluator.PROP_COMPARISON_TYPE, EvaluationDataEntry.ComparisonType.OVERLAP.name());
        properties.setProperty(EntityEvaluator.PROP_OVERLAP_TYPE, EvaluationDataEntry.OverlapType.CHARS.name());
        properties.setProperty(EntityEvaluator.PROP_OVERLAP_SIZE, "1");
        EntityEvaluator entityEvaluator = new EntityEvaluator(properties);

        EvaluationDataEntry g1 = new EvaluationDataEntry("doc1", "1", 1, 2, "gold");
        EvaluationDataEntry g2 = new EvaluationDataEntry("doc1", "1", 2, 3, "gold");
        EvaluationData gold = new EvaluationData(g1, g2);

        EvaluationDataEntry p1 = new EvaluationDataEntry("doc1", "1", 1, 4, "pred");
        EvaluationDataEntry p2 = new EvaluationDataEntry("doc1", "1", 1, 4, "pred");
        // The third and fourth should be FPs
        EvaluationDataEntry p3 = new EvaluationDataEntry("doc1", "1", 1, 4, "pred");
        EvaluationDataEntry p4 = new EvaluationDataEntry("doc1", "1", 1, 4, "pred");
        EvaluationData pred = new EvaluationData(p1, p2, p3, p4);

//        gold.forEach(e -> {
//            e.setComparisonType(EvaluationDataEntry.ComparisonType.OVERLAP);
//            e.setOverlapType(EvaluationDataEntry.OverlapType.CHARS);
//            e.setOverlapSize(1);
//        });
//        pred.forEach(e -> {
//            e.setComparisonType(EvaluationDataEntry.ComparisonType.OVERLAP);
//            e.setOverlapType(EvaluationDataEntry.OverlapType.CHARS);
//            e.setOverlapSize(1);
//        });

        EntityEvaluationResults evaluate = entityEvaluator.evaluate(gold, pred);

        assertEquals(2, evaluate.getSingle().getSumTpMentionWise());
        assertEquals(2, evaluate.getSingle().getSumFpMentionWise());
    }

    @Test
    public void overlapTest3() {
        // Scenario: An enumeration was recognized as one single entity and split in post-processing. All the entities
        // have the same offsets but different IDs.
        Properties properties = new Properties();
        properties.setProperty(EntityEvaluator.PROP_COMPARISON_TYPE, EvaluationDataEntry.ComparisonType.OVERLAP.name());
        properties.setProperty(EntityEvaluator.PROP_OVERLAP_TYPE, EvaluationDataEntry.OverlapType.CHARS.name());
        properties.setProperty(EntityEvaluator.PROP_OVERLAP_SIZE, "1");
        EntityEvaluator entityEvaluator = new EntityEvaluator(properties);

        EvaluationDataEntry g1 = new EvaluationDataEntry("doc1", "1", 280, 305, "gold");
        EvaluationData gold = new EvaluationData(g1);

        EvaluationDataEntry p1 = new EvaluationDataEntry("doc1", "1", 269, 305, "pred");
        EvaluationData pred = new EvaluationData(p1);

        EntityEvaluationResults evaluate = entityEvaluator.evaluate(gold, pred);

        assertEquals(1.0, evaluate.getSingle().getMacroFMeasureMentionWise(), 0.0000001);
    }
}
