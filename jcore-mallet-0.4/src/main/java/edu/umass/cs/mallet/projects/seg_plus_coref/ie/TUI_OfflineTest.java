/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet

   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
	Fuchun Peng, July 2003
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.ie;

import edu.umass.cs.mallet.base.util.CommandOption;

import java.io.FileNotFoundException;


public class TUI_OfflineTest
{
	private static String[] SEPERATOR = new String[] {"<NEW_HEADER>", "<NEWREFERENCE>"};
//	private static String[] SEPERATOR = new String[] {"<NEW_HEADER>", "^$"};

	static CommandOption.File crfInputFileOption = new CommandOption.File
	(TUI_OfflineTest.class, "crf-input-file", "FILENAME", true, null,
	 "The name of the file to read the trained CRF for testing.", null);

	static CommandOption.File testFileOption = new CommandOption.File
	(TUI_OfflineTest.class, "test-file", "FILENAME", true, null,
	 "The name of the file containing the testing data.", null);


	static final CommandOption.List commandOptions =
	new CommandOption.List (
		"Training, testing and running information extraction on paper header or reference.",
		new CommandOption[] {
			crfInputFileOption,
			testFileOption,
		});


	public static void main (String[] args) throws FileNotFoundException
	{
		commandOptions.process (args);

		IEInterface ieInterface = new IEInterface(crfInputFileOption.value);
//		IEInterface3 ieInterface = new IEInterface3(crfInputFileOption.value);


		ieInterface.loadCRF();
		long timeStart = System.currentTimeMillis();

		int N= 1;
		ieInterface.offLineEvaluate(testFileOption.value, true, SEPERATOR[1], N);

		long timeEnd = System.currentTimeMillis();
		double timeElapse = (timeEnd - timeStart)/(1000.000);
		System.out.println("Time elapses " + timeElapse + " seconds for testing.");
	}
	
}
