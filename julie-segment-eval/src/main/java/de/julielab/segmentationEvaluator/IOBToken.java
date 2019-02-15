/** 
 * IOBToken.java
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
 * Creation date: Aug 20, 2007 
 * 
 * This object represents a token and its corresponding label and IOB mark. It
 * is used as input for an Evaluator object.
 **/

package de.julielab.segmentationEvaluator;

public class IOBToken extends IOToken {

	/**
	 * @param text 	The characters of the token.
	 * @param label The IOBlabel of the token, expected to be of form "[IOBmark]_[label]", e.g. "B_NN".
	 * 				
	 */
	public IOBToken(String text, String label) {
		this.text = text;
		this.iobMark = label.substring(0, 1);
		if (this.iobMark.equals("O")
			|| this.iobMark.equals("0")) {
			iobMark = "O";
			label = "";
		}
		else {
			this.label = label.substring(2);
		}
	}

	public IOBToken(String text, String label, String pos) {
		this(text, label);
		this.pos = pos;
	}
    
    public String toString() {
        String ret = text+"\t\t\t"+iobMark;
        if (!iobMark.equals("O")) {
            ret += "_"+label; 
        }
        return ret;
    }
    
}
