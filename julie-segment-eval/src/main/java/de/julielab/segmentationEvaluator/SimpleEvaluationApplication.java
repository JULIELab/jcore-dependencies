/** 
 * SimpleEvaluationApplication.java
 * 
 * Copyright (c) 2006, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: faessler
 * 
 * Current version: 1.0.3
 * Since version:   1.0.3
 *
 * Creation date: Aug 20, 2007 
 * 
 * just outputs the overall recall, precision, f-score (tab separated)
 **/

package de.julielab.segmentationEvaluator;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class SimpleEvaluationApplication {

	public static void main(String args[]) {

		DecimalFormat df = (DecimalFormat) NumberFormat
				.getNumberInstance(Locale.US);
		df.setMinimumFractionDigits(5);

		if (args.length != 3) {
			System.out
					.println("Usage: SimpleEvaluationApplication <goldFile> <predFile> <IOB|IO>");
			System.exit(0);
		}

		IOToken[] gold = null;
		IOToken[] pred = null;
		EvaluationResult[] result = null;
		Converter conv = null;

		int mode = 0;
		if (args[2].equals("io") || args[2].equals("IO")) {
			mode = Converter.TYPE_IO;
		} else if (args[2].equals("iob") || args[2].equals("IOB")) {
			mode = Converter.TYPE_IOB;
		}

		try {
			conv = new Converter(mode);
		} catch (UnknownFormatException e) {
			System.err.println(e);
		}

		try {
			gold = conv.textToIOTokens(new File(args[0]));
			pred = conv.textToIOTokens(new File(args[1]));
		} catch (FileNotFoundException e) {
			System.err.println("Gold file or pred file does not exist.");
			System.exit(0);
		}

		result = Evaluator.evaluate(gold, pred);

		if (result.length > 0) {
			System.out.println(df.format(result[0].getRecall()) + "\t"
					+ df.format(result[0].getPrecision()) + "\t"
					+ df.format(result[0].getFscore()));
		} else {
			System.out.println("no results!");
		}

	}

}
