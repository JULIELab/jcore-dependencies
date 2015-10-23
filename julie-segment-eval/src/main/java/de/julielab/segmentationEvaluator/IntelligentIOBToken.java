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

/**
 * entscheidet selbst ob IO oder IOB
 * 
 * @author tomanek
 * 
 */
public class IntelligentIOBToken extends IOToken {

	private final static String CONCATENATOR = "-";

	private final static String BEGIN_STRING = "B" + CONCATENATOR;

	private final static String INTERN_STRING = "I" + CONCATENATOR;

	/**
	 * @param text
	 *            The characters of the token.
	 * @param label
	 *            The IOBlabel of the token, expected to be of form
	 *            "[IOBmark]_[label]", e.g. "B_NN".
	 * 
	 */
	public IntelligentIOBToken(String text, String label) {
		this.text = text;

		if (label.equals("O") || label.equals("0")) {
			this.iobMark = "O";
			this.label = "";
		} else if (label.startsWith(BEGIN_STRING)
				|| label.startsWith(INTERN_STRING)) {
			// go for IOB style
			this.iobMark = label.substring(0, 1);
			this.label = label.substring(2);
		} else {
			this.label = label;
			this.iobMark = "I";
		}

	}

	public String toString() {
		String ret = text + "\t\t\t" + iobMark + CONCATENATOR + label;
		// if (!iobMark.equals("O")) {
		// ret += "_"+label;
		// }
		return ret;
	}

}
