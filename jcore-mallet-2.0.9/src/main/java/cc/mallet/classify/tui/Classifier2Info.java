/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */





package cc.mallet.classify.tui;

import cc.mallet.classify.Classifier;
import cc.mallet.util.CommandOption;
import cc.mallet.util.MalletLogger;

import java.io.*;
import java.util.logging.Logger;
/**
 * Diagnostic facilities for a classifier.
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

public class Classifier2Info
{
	private static Logger logger = MalletLogger.getLogger(Classifier2Info.class.getName());

	static CommandOption.File classifierFile = new CommandOption.File
	(Classifier2Info.class, "classifier", "FILE", true, new File("-"),
	 "Read the saved classifier from this file.", null);

	public static void main (String[] args) throws FileNotFoundException, IOException
	{
		// Process the command-line options
		CommandOption.setSummary (Classifier2Info.class,
		"A tool for printing information about saved classifiers.");
		CommandOption.process (Classifier2Info.class, args);

		// Print some helpful messages for error cases
		if (args.length == 0) {
			CommandOption.getList(Classifier2Info.class).printUsage(false);
			System.exit (-1);
		}

		// Read in the classifier
		Classifier classifier;
		try {
			ObjectInputStream ois = new ObjectInputStream (new FileInputStream (classifierFile.value));
			classifier = (Classifier) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException ("Couldn't read classifier "+classifierFile.value);
		}

		classifier.print ();
	}


}
