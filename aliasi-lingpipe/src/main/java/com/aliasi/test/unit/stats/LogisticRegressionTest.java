package com.aliasi.test.unit.stats;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.io.Reporter;
import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.Vector;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.LogisticRegression;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Pair;

import static junit.framework.Assert.assertEquals;

public class LogisticRegressionTest  {

    // WALLET example from:
    // Paul David Allison.  Logistic Regression Using the SAS System: Theory and Application.
    // p. 117.  http://books.google.com/books?id=AcHB61vd-1UC
    // 0 = KEEP_BOTH, 1=KEEP_MONEY, 2=RETURN_BOTH
    static final int[] WALLET_OUTCOME_VECTOR
        = new int[] {
        1,
        1,
        2,
        2,
        0,
        2,
        2,
        2,
        2,
        2,
        1,
        2,
        2,
        2,
        2,
        2,
        2,
        1,
        0,
        1,
        1,
        2,
        2,
        2,
        2,
        1,
        1,
        0,
        2,
        2,
        2,
        2,
        0,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        1,
        2,
        2,
        2,
        2,
        2,
        2,
        1,
        2,
        2,
        2,
        2,
        2,
        0,
        2,
        2,
        0,
        2,
        1,
        0,
        0,
        2,
        2,
        1,
        1,
        1,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        1,
        2,
        2,
        1,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        0,
        0,
        1,
        0,
        1,
        0,
        1,
        0,
        2,
        2,
        1,
        2,
        0,
        2,
        1,
        2,
        2,
        1,
        2,
        2,
        0,
        1,
        1,
        0,
        0,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        1,
        1,
        2,
        1,
        2,
        1,
        2,
        2,
        0,
        2,
        2,
        2,
        2,
        1,
        2,
        1,
        2,
        1,
        2,
        2,
        2,
        2,
        1,
        2,
        2,
        1,
        2,
        2,
        1,
        2,
        1,
        2,
        0,
        2,
        1,
        0,
        1,
        2,
        1,
        2,
        1,
        1,
        0,
        1,
        1,
        0,
        1,
        1,
        2,
        2,
        1,
        0,
        1,
        2,
        1,
        2,
        0,
        1,
        2,
        1,
        2,
        2,
        2,
        2,
        2,
        1, };

    // INTERCEPT, MALE, BUSINESS, PUNISH, EXPLAIN
    static final double[][] WALLET_DATA_MATRIX
        = new double[][] {
        { 1, 0, 0, 2, 0 },
        { 1, 0, 0, 2, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 2, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 2, 1 },
        { 1, 0, 1, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 0, 0, 2, 1 },
        { 1, 0, 0, 3, 0 },
        { 1, 1, 1, 3, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 2, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 1, 1, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 1, 0, 3, 0 },
        { 1, 1, 0, 2, 0 },
        { 1, 1, 0, 2, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 2, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 2, 0 },
        { 1, 1, 0, 1, 0 },
        { 1, 1, 1, 2, 1 },
        { 1, 0, 0, 2, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 2, 0 },
        { 1, 1, 1, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 0, 1, 3, 0 },
        { 1, 1, 0, 2, 0 },
        { 1, 0, 0, 2, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 0 },
        { 1, 1, 1, 1, 0 },
        { 1, 1, 0, 1, 0 },
        { 1, 1, 1, 3, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 3, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 1, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 0 },
        { 1, 1, 0, 3, 1 },
        { 1, 1, 0, 3, 1 },
        { 1, 1, 1, 2, 1 },
        { 1, 1, 0, 2, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 3, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 1, 1, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 1, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 2, 1 },
        { 1, 1, 1, 1, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 1, 1, 1 },
        { 1, 0, 0, 2, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 1, 0, 2, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 1, 3, 0 },
        { 1, 1, 1, 1, 1 },
        { 1, 1, 1, 3, 1 },
        { 1, 0, 0, 3, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 1, 3, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 3, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 1, 1, 1 },
        { 1, 0, 0, 3, 0 },
        { 1, 0, 1, 2, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 1, 2, 0 },
        { 1, 1, 0, 1, 0 },
        { 1, 1, 0, 1, 0 },
        { 1, 0, 0, 2, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 0, 0, 3, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 0, 1, 1, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 0 },
        { 1, 0, 0, 1, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 2, 0 },
        { 1, 1, 1, 2, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 1, 1, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 0 },
        { 1, 0, 1, 2, 1 },
        { 1, 1, 1, 2, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 1, 3, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 0 },
        { 1, 1, 0, 3, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 0, 2, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 1, 2, 0 },
        { 1, 1, 0, 2, 0 },
        { 1, 0, 0, 1, 1 },
        { 1, 0, 1, 3, 0 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 2, 0 },
        { 1, 0, 1, 2, 1 },
        { 1, 0, 0, 2, 0 },
        { 1, 1, 1, 1, 1 },
        { 1, 0, 1, 2, 1 },
        { 1, 0, 0, 3, 0 },
        { 1, 1, 1, 1, 0 },
        { 1, 0, 0, 3, 1 },
        { 1, 1, 0, 2, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 0, 3, 1 },
        { 1, 0, 0, 1, 1 },
        { 1, 1, 1, 1, 1 },
        { 1, 1, 0, 1, 1 },
        { 1, 1, 0, 1, 1 }
    };

    static final double[][] WALLET_EXPECTED_FEATURES
        = new double[][] {
        // INTERCEPT, MALE, BUSINESS, PUNISH, EXPLAIN
        { -3.4712, 1.2673, 1.1804, 1.0817, -1.6006 }, // CAT 0=KEEP_BOTH
        { -1.2917, 1.1699, 0.4179, 0.1957, -0.8040 }, // CAT 1=KEEP_MONEY
        { 0.0, 0.0, 0.0, 0.0, 0.0 }                   // CAT 2=RETURN_BOTH
    };


    @Test
    public void testClass() {

        Vector[] weightVectors = new Vector[2];
        weightVectors[0] = new DenseVector(new double[] { 1, 2, 3 });
        weightVectors[1] = new DenseVector(new double[] { -2, 1, -1 });

        LogisticRegression regression = new LogisticRegression(weightVectors);

        Vector testCase = new DenseVector(new double[] { 1, -1, 2 });

        double prod1 = (1*1) + (-1 * 2) + (2 * 3);
        double prod2 = (1 * -2) + (-1 * 1) + (2 * -1);
        double prod3 = 0;

        double prop1 = Math.exp(prod1);
        double prop2 = Math.exp(prod2);
        double prop3 = Math.exp(prod3);
        assertEquals(1.0,prop3,0.0001);

        double p1 = prop1 / (prop1 + prop2 + prop3);
        double p2 = prop2 / (prop1 + prop2 + prop3);
        double p3 = prop3 / (prop1 + prop2 + prop3);

        double[] expected = new double[] { p1, p2, p3};
        double[] estimated = regression.classify(testCase);
        assertEquals(expected.length, estimated.length);
        for (int i = 0; i < expected.length; ++i)
            assertEquals(expected[i],estimated[i],0.0000001);




    }

    static Vector[] sparseCopy(Vector[] matrix) {
        Vector[] result = new Vector[matrix.length];
        for (int i = 0; i < matrix.length; ++i)
            result[i] = sparseCopy(matrix[i]);
        return result;
    }

    static Vector sparseCopy(Vector v) {
        int[] dims = new int[v.numDimensions()];
        float[] vals = new float[v.numDimensions()];
        for (int i = 0; i < dims.length; ++i) {
            dims[i] = i;
            vals[i] = (float) v.value(i);
        }
        return new SparseFloatVector(dims,vals,v.numDimensions());
    }

    @Test
    public void testEstimation() throws IOException, ClassNotFoundException {
        Vector[] data_matrix = new Vector[WALLET_DATA_MATRIX.length];
        for (int i = 0; i < data_matrix.length; ++i)
            data_matrix[i] = new DenseVector(WALLET_DATA_MATRIX[i]);

        Vector[] sparse_data_matrix = sparseCopy(data_matrix);        
        assertCorrectRegression(data_matrix,null);
        assertCorrectRegression(sparse_data_matrix,null);
        
        //Test Weighted Data 
        Pair<Vector[],Vector[]> convertedData2 = convertDataToWeightedProbs(data_matrix,WALLET_OUTCOME_VECTOR,23,false);
        assertCorrectRegression(convertedData2.a(),convertedData2.b());
        
        convertedData2 = convertDataToWeightedProbs(sparse_data_matrix,WALLET_OUTCOME_VECTOR,23,false);
        assertCorrectRegression(convertedData2.a(),convertedData2.b());
        
        convertedData2 = convertDataToWeightedProbs(data_matrix,WALLET_OUTCOME_VECTOR,23,true);
        assertCorrectRegression(convertedData2.a(),convertedData2.b());
        
        convertedData2 = convertDataToWeightedProbs(sparse_data_matrix,WALLET_OUTCOME_VECTOR,23,true);
        assertCorrectRegression(convertedData2.a(),convertedData2.b());
        
    }
    
    //Returns a new set of input/output vectors in which the outputs have been converted to Vectors
    Pair<Vector[],Vector[]> convertDataToWeightedProbs(Vector[] input_data_matrix, int[] outcomes, int uniqueInputs, boolean consolidateToUniqueInputs) {
    	//Store outcome counts for each unique input
    	Map<Vector,ObjectToCounterMap<Integer>> inputsToOutcomeCounts = new HashMap<Vector,ObjectToCounterMap<Integer>>();
    	int numOutcomes = 0;
    	for(int i=0; i<input_data_matrix.length;i++) {
    		Vector input = input_data_matrix[i];
    		ObjectToCounterMap<Integer> outcomeCount = inputsToOutcomeCounts.get(input);
    		if(outcomeCount==null) {
    			outcomeCount = new ObjectToCounterMap<Integer>();
    			inputsToOutcomeCounts.put(input, outcomeCount);
    		}
    		outcomeCount.increment(outcomes[i]);
    		numOutcomes = Math.max(numOutcomes, outcomes[i]+1);
    	}
    	assertEquals(inputsToOutcomeCounts.keySet().size(),uniqueInputs);  
    	
    	//Convert outcome counts into normalized vectors
    	List<Vector> convertedInputs = new ArrayList<Vector>();
    	List<Vector> convertedOutputs = new ArrayList<Vector>();
    	for(Vector input : inputsToOutcomeCounts.keySet()) {
    		ObjectToCounterMap<Integer> outcomeCounts = inputsToOutcomeCounts.get(input);
    		ObjectToDoubleMap<Integer> convertedOutput = new ObjectToDoubleMap<Integer>();
    		int repeats = 0;
    		for(Integer outcome : outcomeCounts.keySet()) {
    			int count = outcomeCounts.getCount(outcome);
    			convertedOutput.set(outcome, count);
    			repeats += count;
    		}
    		for(Integer outcome : outcomeCounts.keySet()) {
    			double newValue = convertedOutput.getValue(outcome);
    			if (!consolidateToUniqueInputs) newValue /= repeats; //If not reducing, normalize so some of weights is 1.0
    			convertedOutput.set(outcome, newValue);
    		} 
    		int rLim = consolidateToUniqueInputs ? 1 : repeats; //If not reducing, add multiple copies of the the input/output pair to the data
    		for(int r=0; r<rLim; r++) {
    			convertedOutputs.add(new SparseFloatVector(convertedOutput,numOutcomes));
    			convertedInputs.add(input);
    		}
    	}
    	Pair<Vector[],Vector[]> inOutPair = new Pair<Vector[],Vector[]>(convertedInputs.toArray(new Vector[]{}),convertedOutputs.toArray(new Vector[]{}));
    	if(consolidateToUniqueInputs) {
    		assertEquals(inOutPair.a().length,uniqueInputs);
    	} else {
    		assertEquals(inOutPair.a().length,input_data_matrix.length);
    	}
    	return inOutPair;
    }

    void assertCorrectRegression(Vector[] data_matrix, Vector[] outcomes) throws IOException, ClassNotFoundException {
//    	System.out.println(data_matrix.length + " instances");
        Reporter reporter = null; // Reporters.stdOut().setLevel(LogLevel.DEBUG); // null;
        LogisticRegression hotStart = null;
        ObjectHandler<LogisticRegression> handler = null;
        int priorBlockSize = 5;
        LogisticRegression regression = null;
        if(outcomes==null) {
        	//outcomes is array of integers
            regression = LogisticRegression.estimate(data_matrix,
                                          WALLET_OUTCOME_VECTOR,
                                          RegressionPrior.noninformative(),
                                          priorBlockSize,
                                          hotStart,
                                          AnnealingSchedule.inverse(0.05,100),
                                          0.00001, // min improve
                                          5, // rolling avg size
                                          10, // min epochs
                                          500000, // max epochs
                                          handler, // handler for each epoch's regression
                                          reporter);  // no print feedback
        } else {
        	//outcomes is array of vectors
        	regression = LogisticRegression.estimate(data_matrix,
                    outcomes,
                    RegressionPrior.noninformative(),
                    priorBlockSize,
                    hotStart,
                    AnnealingSchedule.inverse(0.05,100),
                    0.00001, // min improve
                    5, // rolling avg size
                    10, // min epochs
                    500000, // max epochs
                    handler, // handler for each epoch's regression
                    reporter);  // no print feedback
        }
        double ALLOWABLE_ERROR = 0.12; //slightly bigger than 0.1 because of consolidated input testing
        Vector[] vs = regression.weightVectors();
        for (int i = 0; i < vs.length; ++i) {
//        	System.out.println();
            for (int j = 0; j < vs[i].numDimensions(); ++j) {
//            	System.out.println(WALLET_EXPECTED_FEATURES[i][j] + "=" + vs[i].value(j) + ", " );
                assertEquals(WALLET_EXPECTED_FEATURES[i][j],vs[i].value(j),ALLOWABLE_ERROR);
            }
        }

        LogisticRegression regression2
            = (LogisticRegression) AbstractExternalizable.compile(regression);

        assertEquals(regression.numOutcomes(),
                     regression2.numOutcomes());
        assertEquals(regression.numInputDimensions(),
                     regression.numInputDimensions());
        Vector[] vs1 = regression.weightVectors();
        Vector[] vs2 = regression2.weightVectors();
        assertEquals(vs1.length,vs2.length);
        assertEquals(vs1.length,vs2.length);
        for (int i = 0; i < vs1.length; ++i)
            assertEquals(vs1[i],vs2[i]);


        hotStart = regression;
        priorBlockSize = 6;
        LogisticRegression regression3 = null;
        if (outcomes == null) {
            regression3 = LogisticRegression.estimate(data_matrix,
                                          WALLET_OUTCOME_VECTOR,
                                          RegressionPrior.noninformative(),
                                          priorBlockSize,
                                          hotStart,
                                          AnnealingSchedule.inverse(0.05,100),
                                          0.0000001, // min improve
                                          5, // rolling avg size
                                          10, // min epochs
                                          500000, // max epochs
                                          handler, // handler for epoch's regression
                                          reporter);  // no print feedback
        } else {
        	regression3 = LogisticRegression.estimate(data_matrix,
                    outcomes,
                    RegressionPrior.noninformative(),
                    priorBlockSize,
                    hotStart,
                    AnnealingSchedule.inverse(0.05,100),
                    0.0000001, // min improve
                    5, // rolling avg size
                    10, // min epochs
                    500000, // max epochs
                    handler, // handler for epoch's regression
                    reporter);  // no print feedback
        }

        vs = regression3.weightVectors();
        for (int i = 0; i < vs.length; ++i) {
            for (int j = 0; j < vs[i].numDimensions(); ++j) {
                assertEquals(WALLET_EXPECTED_FEATURES[i][j],vs[i].value(j),ALLOWABLE_ERROR);
            }
        }



    }
}
