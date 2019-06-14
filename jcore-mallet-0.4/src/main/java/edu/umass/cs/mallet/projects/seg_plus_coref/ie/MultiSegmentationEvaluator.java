/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/**
	 Evaluate segmentation f1 for several different tags (marked in OIB format).
	 For example, tags might be B-PERSON I-PERSON O B-LOCATION I-LOCATION O...

   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>

	Modified by Fuchun Peng to output the format requested by Ben's co-reference program
	July 2003
 */

//package edu.umass.cs.mallet.base.fst;

package edu.umass.cs.mallet.projects.seg_plus_coref.ie;

import edu.umass.cs.mallet.base.fst.*;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.logging.Logger;

public class MultiSegmentationEvaluator extends TransducerEvaluator
{
	private static Logger logger = Logger.getLogger(SegmentationEvaluator.class.getName());

	int numIterationsToWait = 0;
	int numIterationsToSkip = 0;
	boolean alwaysEvaluateWhenFinished = true;
	boolean printCrfAtEnd = false;

	boolean checkpointTransducer = false;
	String checkpointFilePrefix = "checkfile";
	int checkpointIterationsToSkip = 30;

	// equals() is called on these objects to determine if this token is the start or continuation of a segment.
	// A tag not equal to any of these is an "other".
	// is not part of the segment).
	Object[] segmentStartTags;
	Object[] segmentContinueTags;
	Object[] segmentStartOrContinueTags;
	
	boolean viterbiOutput = true;
	String viterbiOutputFilePrefix = "viterbiResult";
	int viterbiOutputIterationsToWait = 0;
	int viterbiOutputIterationsToSkip = 50;
	String viterbiOutputEncoding = "UTF-8";

	int evalIterations = 0;

	public MultiSegmentationEvaluator (Object[] segmentStartTags, Object[] segmentContinueTags)
	{
		this(segmentStartTags, segmentContinueTags, null, 50);
	}

        public MultiSegmentationEvaluator (Object[] segmentStartTags, Object[] segmentContinueTags, 
		String viterbiOutputFilePrefix, int viterbiOutputIterationsToSkip)
        {
                this.segmentStartTags = segmentStartTags;
                this.segmentContinueTags = segmentContinueTags;
                assert (segmentStartTags.length == segmentContinueTags.length);

		this.viterbiOutputFilePrefix = viterbiOutputFilePrefix;
		this.viterbiOutputIterationsToSkip = viterbiOutputIterationsToSkip;
        }

	public int getEvalIterations() {
		return evalIterations;
	}

	public void test(Transducer transducer, InstanceList data,
                   String description, PrintStream viterbiOutputStream)
	{
	}

	
	public boolean evaluate (Transducer crf, boolean finishedTraining, int iteration,
													 boolean converged, double cost,
													 InstanceList training, InstanceList validation, InstanceList testing)
	{
		// Count the total number of evaluate calls, not the CRF's notion of iteration number,
		// because feature induction calls CRF.train() multiple times, and the iteration
		// number gets reset each time.
//		iteration = evalIterations++; //comment out by Fuchun

		System.out.println ("Evaluator Iteration="+iteration+" Cost="+cost);
		// Don't evaluate if it is too early in training to matter
		if (iteration < numIterationsToWait && !(alwaysEvaluateWhenFinished && finishedTraining))
			return true;
		// Only evaluate some iterations
		if (numIterationsToSkip > 0
				&& iteration % (numIterationsToSkip+1) != 0
				&& !(alwaysEvaluateWhenFinished && finishedTraining))
			return true;

		// Possibly write CRF to a file
		if (crf instanceof CRF && checkpointTransducer && iteration > 0
				&& iteration % (checkpointIterationsToSkip) == 0) {
			String checkFilename = checkpointFilePrefix == null ? "" : checkpointFilePrefix + '.';
			checkFilename = checkFilename + "checkpoint"+iteration+".crf";
			((CRF)crf).write (new File (checkFilename));
		}

		int numCorrectTokens, totalTokens;
		int[] numTrueSegments, numPredictedSegments, numCorrectSegments;
		int allIndex = segmentStartTags.length;
		numTrueSegments = new int[allIndex+1];
		numPredictedSegments = new int[allIndex+1];
		numCorrectSegments = new int[allIndex+1];

		InstanceList[] lists = new InstanceList[] {training, validation, testing};
		String[] listnames = new String[] {"Training", "Validation", "Testing"};

		if(crf instanceof CRF)
			((CRF)crf).write (new File ("tempCRF"));
		else if(crf instanceof CRF3)
			((CRF3)crf).write (new File ("tempCRF"));

		Pipe p = lists[0].getInstance(0).getPipe();
		Alphabet targets = p.getTargetAlphabet();
                assert(targets != null);
		int[][] matrixEntry = new int[targets.size()][targets.size()];
	
		for (int k = 0; k < lists.length; k++) {
			if (lists[k] == null)
				continue;

			System.out.println("current iteration = " + iteration + 
				" viterbiOutputIterationsToWait= " + viterbiOutputIterationsToWait +
				" viterbiOutputIterationsToSkip= " + viterbiOutputIterationsToSkip + "\n");

			PrintStream viterbiOutputStream = null;
			if ((iteration >= viterbiOutputIterationsToWait && iteration % (viterbiOutputIterationsToSkip) == 0)
				|| (alwaysEvaluateWhenFinished && finishedTraining)) {
				if (viterbiOutputFilePrefix == null) {
					viterbiOutputStream = System.out;
				} else {
					String viterbiFilename = null;
					viterbiFilename = viterbiOutputFilePrefix + "."+listnames[k] + ".viterbi_" + iteration;
					try {
						FileOutputStream fos = new FileOutputStream (viterbiFilename);
						if (viterbiOutputEncoding == null)
							viterbiOutputStream = new PrintStream (fos);
						else
							viterbiOutputStream = new PrintStream (fos, true, viterbiOutputEncoding);

	//					((CRF)crf).write (new File(viterbiOutputFilePrefix + "."+listnames[k] + iteration+".crf"));
					} catch (IOException e) {
						logger.warning ("Couldn't open Viterbi output file '"+viterbiFilename+"'; continuing without Viterbi output trace.");
						viterbiOutputStream = null;
					}
				}
			}


			for(int row=0; row<targets.size(); row++){
				for(int col=0; col<targets.size(); col++){
					matrixEntry[row][col] = 0;
				}
			}

			totalTokens = numCorrectTokens = 0;
			for (int n = 0; n < numTrueSegments.length; n++)
				numTrueSegments[n] = numPredictedSegments[n] = numCorrectSegments[n] = 0;

			for (int i = 0; i < lists[k].size(); i++) {
				if (viterbiOutputStream != null)
					viterbiOutputStream.println ("Viterbi path for "+listnames[k]+" instance #"+i);
				Instance instance = lists[k].getInstance(i);
//				String sgmlFilename = (String)instance.getName();
//				ieInterface.viterbiCRF(new File(sgmlFilename), true);	

				java.util.Vector offsetSequence = (java.util.Vector)instance.getName();
	
				Sequence input = (Sequence) instance.getData();
				//String tokens = null;
				//if (instance.getSource() != null)
				//tokens = (String) instance.getSource().toString();
				Sequence trueOutput = (Sequence) instance.getTarget();
				assert (input.size() == trueOutput.size());
				Sequence predOutput = crf.viterbiPath(input).output();
				assert (predOutput.size() == trueOutput.size());

				TokenSequence sourceTokenSequence = (TokenSequence)instance.getSource();
				assert(sourceTokenSequence.size() == trueOutput.size());

				int trueStart, predStart;				// -1 for non-start, otherwise index into segmentStartTag
				for (int j = 0; j < trueOutput.size(); j++) {

					totalTokens++;
					if (trueOutput.get(j).equals(predOutput.get(j)))
						numCorrectTokens++;
	
					int predIndex = targets.lookupIndex(predOutput.get(j));
					int trueIndex = targets.lookupIndex(trueOutput.get(j));

					matrixEntry[trueIndex][predIndex] ++;

					trueStart = predStart = -1;
					// Count true segment starts
					for (int n = 0; n < segmentStartTags.length; n++) {
						if (segmentStartTags[n].equals(trueOutput.get(j))) {
							numTrueSegments[n]++;
							numTrueSegments[allIndex]++;
							trueStart = n;
							break;
						}
					}
					// Count predicted segment starts
					for (int n = 0; n < segmentStartTags.length; n++) {
						if (segmentStartTags[n].equals(predOutput.get(j))) {
							numPredictedSegments[n]++;
							numPredictedSegments[allIndex]++;
							predStart = n;
						}
					}

					if (trueStart != -1 && trueStart == predStart) {
						// Truth and Prediction both agree that the same segment tag-type is starting now
						int m;
						boolean trueContinue = false;
						boolean predContinue = false;
						for (m = j+1; m < trueOutput.size(); m++) {
							trueContinue = segmentContinueTags[predStart].equals (trueOutput.get(m));
							predContinue = segmentContinueTags[predStart].equals (predOutput.get(m));
							if (!trueContinue || !predContinue) {
								if (trueContinue == predContinue) {
									// They agree about a segment is ending somehow
									numCorrectSegments[predStart]++;
									numCorrectSegments[allIndex]++;
								}
								break;
							}
						}
						// for the case of the end of the sequence
						if (m == trueOutput.size()) {
							if (trueContinue == predContinue) {
								numCorrectSegments[predStart]++;
								numCorrectSegments[allIndex]++;
							}
						}
					}


					if (viterbiOutputStream != null) {
						FeatureVector fv = (FeatureVector) input.get(j);
						//viterbiOutputStream.println (tokens.charAt(j)+" "+trueOutput.get(j).toString()+
						//'/'+predOutput.get(j).toString()+"  "+ fv.toString(true));

						if (sourceTokenSequence != null)
							viterbiOutputStream.print (sourceTokenSequence.getToken(j).getText() + ": "
							+ ((Integer)offsetSequence.get(j)).intValue() + ": "
							);  


						viterbiOutputStream.println (trueOutput.get(j).toString()+ '/'+predOutput.get(j).toString()+"  "+ fv.toString(true));
//						viterbiOutputStream.println (trueOutput.get(j).toString()+ '/'+predOutput.get(j).toString());					
						
					}
				}
			}
			DecimalFormat f = new DecimalFormat ("0.####");
			System.out.println (listnames[k] +" accuracy="+f.format(((double)numCorrectTokens)/totalTokens));
			for (int n = 0; n < numCorrectSegments.length; n++) {
				System.out.print ((n < allIndex ? segmentStartTags[n].toString() : "OVERALL") +' ');
				double precision = numPredictedSegments[n] == 0 ? 1 : ((double)numCorrectSegments[n]) / numPredictedSegments[n];
				double recall = numTrueSegments[n] == 0 ? 1 : ((double)numCorrectSegments[n]) / numTrueSegments[n];
				double f1 = recall+precision == 0.0 ? 0.0 : (2.0 * recall * precision) / (recall + precision);
				System.out.print (" segments true="+numTrueSegments[n]+" pred="+numPredictedSegments[n]+" correct="+numCorrectSegments[n]+
													" misses="+(numTrueSegments[n]-numCorrectSegments[n])+" alarms="+(numPredictedSegments[n]-numCorrectSegments[n]));
				System.out.println (" precision="+f.format(precision)+" recall="+f.format(recall)+" f1="+f.format(f1));
			}

			if (viterbiOutputStream != null && viterbiOutputFilePrefix != null && viterbiOutputStream != System.out)
				viterbiOutputStream.close();


			System.out.println("\n Confusion Matrix (row: true label, col: predicted label)");
			System.out.print("\t");
			for(int t=0; t<targets.size(); t++){
				System.out.print(targets.lookupObject(t) + "\t");
			}
			System.out.println();

			for(int t=0; t< targets.size(); t++){
				System.out.print(targets.lookupObject(t)+"\t");
				for(int tt=0; tt<targets.size(); tt++){
					System.out.print(matrixEntry[t][tt] + "\t");
				}
				System.out.println();
			}


		}

		if (printCrfAtEnd && finishedTraining && crf instanceof CRF)
			((CRF)crf).print ();
		return true;
	}
	
}
