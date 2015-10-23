/** 
 * EvaluationApplication.java
 * 
 * Copyright (c) 2006, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: faessler
 * 
 * Current version: 1.0.5
 * Since version:   1.0
 *
 * Creation date: Aug 20, 2007 
 * 
 * A generic evaluation package for annotations which define a
 * segmentation of the underlying text.
 **/

package de.julielab.segmentationEvaluator;

import java.io.File;
import java.io.FileNotFoundException;


public class EvaluationApplication {

	public static void main(String args[]) {
		
		if (args.length != 3) {
			System.out.println("Usage: EvaluationApplication <goldFile> <predFile> <IOB|IO>");
			System.exit(0);
		}
		
		IOToken[] gold = null;
		IOToken[] pred = null;
		EvaluationResult[] result = null;
		Converter conv = null;
        
        int mode = 0;
        if (args[2].equals("io") || args[2].equals("IO")) {
          mode = Converter.TYPE_IO;
        }
        else if (args[2].equals("iob") || args[2].equals("IOB")) {
          mode = Converter.TYPE_IOB;
        }
        
		try {
			conv = new Converter(mode);
		}
		catch (UnknownFormatException e) {
			System.err.println(e);
		}
		
		try {
			gold = conv.textToIOTokens(new File(args[0]));
			pred = conv.textToIOTokens(new File(args[1]));
		} catch (FileNotFoundException e) {
			System.err.println("Error: Gold file or pred file does not exist.");
			System.exit(-1);
		}
		
	
		result = Evaluator.evaluate(gold, pred);
		
		for (int i=0; i<result.length; i++) {
			System.out.println(result[i]);
		}
	}
	
}
