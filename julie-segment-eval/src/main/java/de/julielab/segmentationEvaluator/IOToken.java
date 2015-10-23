/** 
 * IOToken.java
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
 * This object represents a token and its corresponding label. It
 * is used as input for an Evaluator object.
 **/

package de.julielab.segmentationEvaluator;

public class IOToken {
	protected String text;
	protected String iobMark;
	protected String label;
	
	/**
	 * @param text 	The characters of the token.
	 * @param label The label of the token, e.g. "NN".
	 * 				
	 */
	public IOToken(String text, String label) {
		this.text = text;
		if (label == null
			|| label.equals("")
			|| label.equals("O")
			|| label.equals("0")) {
			this.label = "";
			this.iobMark = "O";
		}
		else {
			this.label = label;
			this.iobMark = "I";
		}	
	}
	
	protected IOToken() {
		
	}
	
	public String toString() {
      String ret = text+"\t\t\t";
      if (iobMark.equals("O")) {
        ret += iobMark;
      }
      else {
        ret += label;
      }
      return ret;
	}
	
	public String getText() {
		return text;
	}
	
	public String getIobMark() {
		return iobMark;
	}
	
	public String getLabel() {
		return label;
	}
}
