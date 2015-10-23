/** 
 * Evaluator.java
 * 
 * Copyright (c) 2006, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: faessler
 * 
 * Current version: 1.0.6
 * Since version:   1.0
 *
 * Creation date: Aug 20, 2007 
 * 
 * An object capable of computing recall, precision and f-measure
 * of an annotation regarding a corresponding gold standard.
 **/

package de.julielab.segmentationEvaluator;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Evaluator {
	
    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

	/**
	 * @param gold The gold standard.
	 * @param prediction A prediction to be compared with the gold standard.
	 * @return An array of EvaluationResult object. The overall performance if
     *         found at index 0, the results for every single label follows.
	 */
	public static EvaluationResult[] evaluate(IOToken[] gold, IOToken[] prediction) {
		
		/*
		 * make some data integrity checks
		 */
		
		// check same length 
		if (gold.length!=prediction.length) {
			String info = "Different number of tokens for gold and prediction: " + gold.length + " <-> " + prediction.length;
			IllegalStateException ex = new IllegalStateException(info);
			LOGGER.error(info,ex);
			throw ex;
		}
		
		// check that tokens are the same
		for (int i = 0; i < prediction.length; i++) {
			String predToken = prediction[i].getText();
			String goldToken = gold[i].getText();
			if (!predToken.equals(goldToken)) {
				String info = "Tokens in gold file differnt from tokens in prediction: " + predToken + " <-> " + goldToken;
				IllegalStateException ex = new IllegalStateException(info);
				LOGGER.error(info,ex);
				throw ex;
			}
		}
		
        LOGGER.info("Beginning evaluation");
      
        LOGGER.info("Computing spans of annotation: gold");
		HashMap<String, HashMap<String, String>> goldSpans = getAnnotationSpans(gold);
        LOGGER.info("Computing spans of annotation: prediction");
		HashMap<String, HashMap<String, String>> predSpans = getAnnotationSpans(prediction);
		
		EvaluationResult[] results = new EvaluationResult[goldSpans.keySet().size()+1];
		
		// evaluation for each single label
		int i = 1;
		for (Iterator iter = goldSpans.keySet().iterator(); iter.hasNext();) {
			
			EvaluationResult evalRes = null;
			String label = (String) iter.next();
			if (predSpans.get(label) == null) {
        
				evalRes = new EvaluationResult(0, goldSpans.get(label).size(), 0, label);
			}
			else {
                LOGGER.debug("Single evaluation of label \""+label+"\"");
				int[] evalValues = evaluateSingle(goldSpans.get(label), predSpans.get(label));
				evalRes = new EvaluationResult(evalValues[0], evalValues[1], evalValues[2], label);
			}
			results[i] = evalRes;
			++i;
		}
		
		// overall evaluation
		int tpOver = 0;
		int fpOver = 0;
		int fnOver = 0;
		
		for (i=1; i<results.length; i++) {
			tpOver += results[i].getTp();
			fpOver += results[i].getFp();
			fnOver += results[i].getFn();
		}
		results[0] = new EvaluationResult(tpOver, fnOver, fpOver);
		
		return results;
	}
	
	
	/**
     * @param gold The gold standard.
     * @param prediction A prediction to be compared with the gold standard.
	 * @param label The label of interest.
	 * @return The EvaluationResult object related to the label "label".
	 */
	public static EvaluationResult evaluate(IOToken[] gold, IOToken[] prediction, String label) {
		
		EvaluationResult result = null;
		EvaluationResult[] allResults = null;
		
		allResults = evaluate(gold, prediction);
		
		for (int i=0; i<allResults.length; i++) {
			if (allResults[i].getEvalLabel().equals(label)) {
				result = allResults[i];
				break;
			}
		}
		
		return result;
		
	}
    /**
     * @param gold The gold standard.
     * @param prediction A prediction to be compared with the gold standard.
     * @param labels An array of labels of interest.
     * @return An array of EvaluationResult objects related to the labels
     *         in "labels".
     */
    
	public static EvaluationResult[] evaluate(IOToken[] gold, IOToken[] prediction, String[] labels) {
		
		EvaluationResult[] results = new EvaluationResult[labels.length];
		EvaluationResult[] allResults = null;
		
		allResults = evaluate(gold, prediction);
		
		for (int j=0; j<labels.length; j++) {
			for (int i=0; i<allResults.length; i++) {
				if (allResults[i].getEvalLabel().equals(labels[j])) {
					results[j] = allResults[i];
				}
			}
		}
	
	return results;
	}
		
	private static int[] evaluateSingle(HashMap<String, String> goldSpan, HashMap<String, String> predSpan) {
      
		int tp = 0;
		int allFound = predSpan.size();
		int allExist = goldSpan.size();

		// now check the blocks
		for (Iterator iter = predSpan.keySet().iterator(); iter.hasNext();) {
			String offset = (String) iter.next();
			if (goldSpan.containsKey(offset)) {
				String labelsEval = (String) predSpan.get(offset);
				String labelsGold = (String) goldSpan.get(offset);
				if (labelsEval.equals(labelsGold)) {
					tp++;
				}
			}
		}
		
		int[] ret = new int[3];
		ret[0] = tp;
		ret[1] = allExist-tp;
		ret[2] = allFound-tp;
		
		return ret;
	}
	
	/**
	 * @param taglist An array of IOTokens corresponding to an IO or IOB document.
	 * @return A HashMap whose keys are the labels found in the taglist.
     *         The values are HashMaps themselves whose keys are String
     *         similar to "4,9" indicating that an annotation starts at the
     *         IOToken taglist[4] and reaches until taglist[5]. The values of
     *         the "inner" HashMaps are Strings that consist of the labels
     *         in the found interval split by # characters.
	 */
	public static HashMap<String, HashMap<String, String>> getAnnotationSpans(IOToken[] taglist) {
       
		int begin = -1;
		int end = -1;
		boolean inside = false;

		HashMap<String, HashMap<String, String>> spans = new HashMap<String, HashMap<String,String>>();

		String oldLabel = null;
		for (int i = 0; i < taglist.length; i++) {
			IOToken ioToken = taglist[i];
			String currLabel = ioToken.getLabel();
			String currIobmark = ioToken.getIobMark();

			if (!inside) {
				// not inside
				if (!currIobmark.equals("O")) {
					// was outside
					inside = true;
					begin = i;
					end = -1;
				}

			} else {
				// inside

				if ( currIobmark.equals("B") 
						|| currIobmark.equals("O")
						|| (ioToken instanceof IOToken && oldLabel != null && !oldLabel.equals(currLabel))) {
					// new chunk
					HashMap<String, String> span = null;
					
					end = i - 1;
					String labelSeq = "";
					for (int j = begin; j < end + 1; j++) {
						if (labelSeq.length() > 0)
							labelSeq += "#";
						labelSeq += taglist[j].getLabel();
					}
					if (spans.get(oldLabel) == null) {
						span = new HashMap<String, String>();
						spans.put(oldLabel, span);
					}
					spans.get(oldLabel).put(begin + "," + end, labelSeq);
					begin = i;
					end = -1;
					if (currIobmark.equals("O")) {
						// now outside again
						inside = false;
					}
				}
			}
			oldLabel = currLabel;
		}
		return spans;
	}
	

}
