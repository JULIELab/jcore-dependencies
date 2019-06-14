/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/* 
   Fuchun Peng <a href="mailto:fuchun@cs.umass.edu">fuchun@cs.umass.edu</a>, 
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.ie;

import edu.umass.cs.mallet.base.fst.CRF;
import edu.umass.cs.mallet.base.fst.CRF3;
import edu.umass.cs.mallet.base.fst.Transducer;
import edu.umass.cs.mallet.base.fst.TransducerEvaluator;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class IEEvaluator extends TransducerEvaluator
{
	private static Logger logger = Logger.getLogger(IEEvaluator.class.getName());

	int numIterationsToWait = 0;
	int numIterationsToSkip = 0;
	boolean alwaysEvaluateWhenFinished = true;
	boolean printCrfAtEnd = true;

	boolean checkpointTransducer = false;
	String checkpointFilePrefix = null;
	int checkpointIterationsToSkip = 30;

	String viterbiOutputFilePrefix = null;
	int viterbiOutputIterationsToWait = 10;
	int viterbiOutputIterationsToSkip = 10;
	String viterbiOutputEncoding = null;

	private String PUNT = "[,\\.;:?!()*]";
	private Pattern puntPattern = Pattern.compile(PUNT);
	
	public IEEvaluator()
	{
		viterbiOutputFilePrefix = null;
		viterbiOutputIterationsToSkip = 10;
	}

	public IEEvaluator(String viterbiOutputFilePrefix, int viterbiOutputIterationsToSkip)
	{
		this.viterbiOutputFilePrefix = viterbiOutputFilePrefix;
		this.viterbiOutputIterationsToSkip = viterbiOutputIterationsToSkip;
	}

	
	public void printFeatures (InstanceList training)
	{
		Pipe p = training.getInstance(0).getPipe();
		Alphabet targets = p.getTargetAlphabet();
                assert(targets != null);

		//initialize the variables
		for (int i = 0; i < training.size(); i++) {
			System.out.println ("Viterbi path for "+training+" instance #"+i);
			Instance instance = training.getInstance(i);
			Sequence input = (Sequence) instance.getData();
			Sequence trueOutput = (Sequence) instance.getTarget();
			assert (input.size() == trueOutput.size());

			TokenSequence sourceTokenSequence = (TokenSequence)instance.getSource();

			for (int j = 0; j < trueOutput.size(); j++) {
				FeatureVector fv = (FeatureVector) input.get(j);
				if (sourceTokenSequence != null){
					System.out.print (sourceTokenSequence.getToken(j).getText()+": ");
					System.out.println (trueOutput.get(j).toString()+ ": "+ fv.toString(true));
				}
			}
		}

	}	
	
	public void test(Transducer transducer, InstanceList data,
                   String description, PrintStream viterbiOutputStream){
	}

	public boolean evaluate (Transducer crf, boolean finishedTraining, int iteration,
													 boolean converged, double cost,
													 InstanceList training, InstanceList validation, InstanceList testing)
	{
		System.out.println ("Iteration="+iteration+" Cost="+cost);
		// Don't evaluate if it is too early in training to matter
		if (iteration < numIterationsToWait && !(alwaysEvaluateWhenFinished && finishedTraining))
			return true;
		// Only evaluate every 5th iteration
		if (numIterationsToWait > 0
				&& iteration % numIterationsToSkip != 0
				&& !(alwaysEvaluateWhenFinished && finishedTraining))
			return true;

		// Possibly write CRF to a file
		if (checkpointTransducer && iteration > 0
				&& iteration % checkpointIterationsToSkip == 0) {
			String checkFilename = checkpointFilePrefix == null ? "" : checkpointFilePrefix + '.';
			checkFilename = checkFilename + "checkpoint"+iteration+".crf";

			if(crf instanceof CRF)
				((CRF)crf).write (new File (checkFilename));
			else if (crf instanceof CRF3)
				((CRF3)crf).write (new File (checkFilename));

		}

		int numCorrectTokens, totalTokens;
		int[] numTrueSegments, numPredictedSegments, numCorrectSegments;
		int[] numCorrectSegmentsInVocabulary, numCorrectSegmentsOOV;
		int[] numIncorrectSegmentsInVocabulary, numIncorrectSegmentsOOV;
		int[][] matrixEntry;

		InstanceList[] lists = new InstanceList[] {training, validation, testing};
		String[] listnames = new String[] {"Training", "Validation", "Testing"};
		TokenSequence sourceTokenSequence = null;

		if(crf instanceof CRF)
			((CRF)crf).write (new File ("tempCRF"));
		else if(crf instanceof CRF3)
			((CRF3)crf).write (new File ("tempCRF"));


//              IEInterface3 ieInterface = new IEInterface3();
//	        ieInterface.loadCRF((CRF3)crf);
//             	ieInterface.viterbiCRF("wsj_0100.pos");	


		PrintStream viterbiOutputStream = null;
		if ( (iteration >= viterbiOutputIterationsToWait && iteration % viterbiOutputIterationsToSkip == 0) 
			|| (alwaysEvaluateWhenFinished && finishedTraining)
		)
		 {
			if (viterbiOutputFilePrefix == null) {
				viterbiOutputStream = System.out;
			} else {
				String viterbiFilename = null;
				viterbiFilename = viterbiOutputFilePrefix + ".viterbi_"+iteration;
				try {
					FileOutputStream fos = new FileOutputStream (viterbiFilename);
					if (viterbiOutputEncoding == null)
						viterbiOutputStream = new PrintStream (fos);
					else
						viterbiOutputStream = new PrintStream (fos, true, viterbiOutputEncoding);
				} catch (IOException e) {
					logger.warning ("Couldn't open Viterbi output file '"+viterbiFilename+"'; continuing without Viterbi output trace.");
					viterbiOutputStream = null;
				}
			}
		}

		// find out the vocabulary of targets
//		Pipe p = lists[0].getInstance(0).getInstancePipe();
		Pipe p = lists[0].getInstance(0).getPipe();
		Alphabet targets = p.getTargetAlphabet();
                assert(targets != null);

		numTrueSegments = new int[targets.size()];
		numPredictedSegments = new int[targets.size()];
		numCorrectSegments = new int[targets.size()];
		numCorrectSegmentsInVocabulary = new int[targets.size()];
		numCorrectSegmentsOOV = new int[targets.size()];
		numIncorrectSegmentsInVocabulary = new int[targets.size()];
		numIncorrectSegmentsOOV = new int[targets.size()];
		matrixEntry = new int[targets.size()][targets.size()];

		
		int[] numCorrectWholeInstance = new int[lists.length];

		for (int k = 0; k < lists.length; k++) {
			if (lists[k] == null)
				continue;
				
			//initialize the variables
			totalTokens = numCorrectTokens = 0;
			for(int t=0; t<targets.size(); t++){
				numTrueSegments[t] = numPredictedSegments[t] = numCorrectSegments[t] = 0;
				numCorrectSegmentsInVocabulary[t] = numCorrectSegmentsOOV[t] = 0;
				numIncorrectSegmentsInVocabulary[t] = numIncorrectSegmentsOOV[t] = 0;

				for(int tt=0; tt< targets.size(); tt++){
					matrixEntry[t][tt] = 0;
				}
			}

			numCorrectWholeInstance[k] = 0;
			for (int i = 0; i < lists[k].size(); i++) {
				Instance instance = lists[k].getInstance(i);
				Sequence input = (Sequence) instance.getData();
				//String tokens = null;
				//if (instance.getSource() != null)
				//tokens = (String) instance.getSource().toString();
				Sequence trueOutput = (Sequence) instance.getTarget();
				assert (input.size() == trueOutput.size());

				Transducer.ViterbiPath viterbiP = crf.viterbiPath(input);
				Sequence predOutput = viterbiP.output();
				double viterbi_cost = viterbiP.getCost();
				double viterbi_p = Math.exp(-viterbi_cost/predOutput.size());

				if (viterbiOutputStream != null)
					viterbiOutputStream.println ("Viterbi path for "+listnames[k]+" instance #"+i + " p=" + viterbi_p);

//				Sequence predOutput = crf.viterbiPath(input).output();
				assert (predOutput.size() == trueOutput.size());

				sourceTokenSequence = (TokenSequence)instance.getSource();

				boolean wholeInstanceCorrect = true;
				for (int j = 0; j < trueOutput.size(); j++) {
				
					Object predO = predOutput.get(j);
					Object trueO = trueOutput.get(j);
					int predIndex = targets.lookupIndex(predO);
					int trueIndex = targets.lookupIndex(trueO);
				
					String tokenStr = sourceTokenSequence.getToken(j).getText();
					if(puntPattern.matcher(tokenStr).matches()){//ignore punct;
						continue;
					}

					totalTokens ++;
					numTrueSegments[trueIndex] ++;
					numPredictedSegments[predIndex] ++;

					matrixEntry[trueIndex][predIndex] ++;

					if(predIndex == trueIndex){
						numCorrectTokens ++;
						numCorrectSegments[trueIndex] ++;
					}
					else{				
                                                // Here is an incorrect prediction, find out if the word is in the lexicon
//						String sb = sourceTokenSequence.getToken(j).getText();

//                                              if (HashFile.allLexicons.contains(sb) )
//                              	                  numIncorrectSegmentsInVocabulary[trueIndex]++;
//              	                        else
//                      	                          numIncorrectSegmentsOOV[trueIndex]++;

							wholeInstanceCorrect = false;
					}

	
					if (viterbiOutputStream != null) {
						FeatureVector fv = (FeatureVector) input.get(j);
						//viterbiOutputStream.println (tokens.charAt(j)+" "+trueOutput.get(j).toString()+
						//'/'+predOutput.get(j).toString()+"  "+ fv.toString(true));
						if (sourceTokenSequence != null)
							viterbiOutputStream.print (sourceTokenSequence.getToken(j).getText()+": ");
//						viterbiOutputStream.println (trueOutput.get(j).toString()+ '/'+predOutput.get(j).toString()+"  "+ fv.toString(true));
						viterbiOutputStream.println (trueOutput.get(j).toString()+ '/'+predOutput.get(j).toString());


					}
				}

				if(wholeInstanceCorrect) numCorrectWholeInstance[k] ++;
			}

			double accuracy = (double)numCorrectTokens/totalTokens;
                        System.out.println ("\n" + listnames[k] +" accuracy=" + numCorrectTokens +"/"+ totalTokens + " = " +accuracy);

			double wholeInstanceAccuracy = (double)numCorrectWholeInstance[k]/lists[k].size();
			System.out.println ("Whole instance accuracy = " + numCorrectWholeInstance[k] + "/" + lists[k].size() + " = " + wholeInstanceAccuracy);

			for(int t=0; t<targets.size(); t++){
				double precision = numPredictedSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numPredictedSegments[t];
				double recall = numTrueSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numTrueSegments[t];
				double f1 = recall+precision == 0.0 ? 0.0 : (2.0 * recall * precision) / (recall + precision);
				double accuracy_individual = (double)(totalTokens-numPredictedSegments[t]-numTrueSegments[t] + 2*numCorrectSegments[t] )/totalTokens;

                                System.out.println (targets.lookupObject(t) + " precision="+precision+" recall="+recall+" f1="+f1 + " accuracy=" + accuracy_individual);
				System.out.println ("segments true="+numTrueSegments[t]+" pred="+numPredictedSegments[t]+" correct="+numCorrectSegments[t]+" misses="+(numTrueSegments[t]-numCorrectSegments[t])+" alarms="+(numPredictedSegments[t]-numCorrectSegments[t]) + "\n");

//				System.out.println ("correct segments OOV="+numCorrectSegmentsOOV[t]+" IV="+numCorrectSegmentsInVocabulary[t]);
//				System.out.println ("incorrect segments OOV="+numIncorrectSegmentsOOV[t]+" IV="+numIncorrectSegmentsInVocabulary[t]);
			}

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

		if (viterbiOutputStream != null && viterbiOutputFilePrefix != null && viterbiOutputStream != System.out)
			viterbiOutputStream.close();

		if (printCrfAtEnd && finishedTraining){
//			if(crf instanceof CRF)
//				((CRF)crf).print();
//			else if(crf instanceof CRF3)
//				((CRF3)crf).print();

			System.out.println("Finished!");
		}
		
		return true;
	}
	
}
