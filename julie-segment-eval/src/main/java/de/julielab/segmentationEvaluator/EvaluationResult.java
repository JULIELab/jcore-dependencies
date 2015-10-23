/** 
 * EvaluationResult.java
 * 
 * Copyright (c) 2006, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: faessler
 * 
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: Aug 21, 2007 
 * 
 * An object holding information about an evaluation process. It is able to compute precision, recall and f-measure of its
 * corresponding evaluation.
 **/

package de.julielab.segmentationEvaluator;

public class EvaluationResult {
	
	private int tp;
	private int fn;
	private int fp;
	private String evalLabel;

	public EvaluationResult(int tp, int fn, int fp, String evalLabel) {
		this.tp = tp;
		this.fn = fn;
		this.fp = fp;
		this.evalLabel = evalLabel;
	}
	
	public EvaluationResult(int tp, int fn, int fp) {
		this(tp, fn, fp, "Overall");
	}

	public double getPrecision() {
		double ret;
		
		if (tp+fp > 0) {
			ret = (double)tp / (tp+fp);
		}
		else {
			ret = 0;
		}
		return ret;
	}
	
	public double getRecall() {
		double ret;
		
		if (tp+fn > 0) {
			ret = (double)tp / (tp+fn);
		}
		else {
			ret = 0;
		}
		return ret;
	}
	
	public double getFscore () {
		
		double precision = getPrecision();
		double recall = getRecall();
		double ret;
		
		if (precision + recall > 0) {
			ret = 2.0 * precision * recall / (precision + recall);
		}
		else {
			ret = 0;
		}
		return ret;
	}
	
	public int getFn() {
		return fn;
	}

	public int getFp() {
		return fp;
	}

	public int getTp() {
		return tp;
	}
	
	public String getEvalLabel() {
		return evalLabel;
	}
	
	public String toString() {
		return evalLabel+": Recall: "+getRecall()+", Precision: "+getPrecision()+", F-Score: "+getFscore()+" (tp: "+tp+", fn: "+fn+",fp :"+fp+")";
	}
}
