/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.projects.seg_plus_coref;

import edu.umass.cs.mallet.base.fst.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.util.CommandOption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.*;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.List;

import bsh.EvalError;

/**
 * Methods that are useful for CRF TUIs.
 *
 * @author Charles Sutton
 * @version $Id: BaseTUICRF.java,v 1.2 2004/06/15 14:39:58 casutton Exp $
 */
public class BaseTUICRF {

  protected static CommandOption.File outputPrefix = new CommandOption.File
	(BaseTUICRF.class, "output-prefix", "FILENAME", true, null,
	 "The name of the file to write the CRF after training.", null);

  protected static CommandOption.Integer randomSeedOption = new CommandOption.Integer
	(BaseTUICRF.class, "random-seed", "INTEGER", true, 0,
	 "The random seed for randomly selecting a proportion of the instance list for training", null);

  protected static CommandOption.Double trainingPct = new CommandOption.Double
	(BaseTUICRF.class, "training-pct", "0..1", false, 0.2,
	 "Percentage of available training data to use.", null);

  protected static CommandOption.Double testingPct = new CommandOption.Double
	(BaseTUICRF.class, "testing-pct", "0..1", false, 0.5,
	 "Percentage of available testing data to use.", null);

  protected static CommandOption.String offsetsOption = new CommandOption.String
   (BaseTUICRF.class, "offsets", "e.g. {{0,0},{1}}", true, "{{-2},{-1},{1},{2}}",
     "Offset conjunctions", null);


/*
  protected static void saveCrf (Transducer crf, String id)
  {
    File f = new File (outputPrefix.value, "crf-saved"+id+".dat");
    FileUtils.writeObject(f, crf);
  }
*/
  
  protected static void writeCrf (Transducer crf) throws IOException
  {
    writeCrf (crf, "");
  }


  protected static void writeCrf (Transducer crf, String id)
		throws IOException
	{
		PrintStream oldOut = System.out;
		File crfFile = new File (outputPrefix.value, "crf"+id+".txt");
		System.setOut (new PrintStream (new FileOutputStream (crfFile)));
		crf.print ();
		System.setOut (oldOut);
	}

  protected static void writeOutput (Transducer crf, InstanceList testing) throws IOException
  {
    writeOutput (crf, testing, "");
  }


  protected static void writeOutput (Transducer crf, InstanceList testing, String num)
		throws IOException
	{
		File resultsFile = new File (outputPrefix.value, "correct"+num+".txt");
		File failuresFile = new File (outputPrefix.value, "failures"+num+".txt");
		PrintStream out = new PrintStream (new FileOutputStream (resultsFile));
		PrintStream outf = new PrintStream (new FileOutputStream (failuresFile));

		for (int i = 0; i < testing.size(); i++) {
			Instance instance = testing.getInstance(i);
			Sequence input = (Sequence) instance.getData();
			Sequence trueOutput = (Sequence) instance.getTarget();
			assert (input.size() == trueOutput.size());
			Sequence predOutput = crf.viterbiPath(input).output();
			assert (predOutput.size() == trueOutput.size());
		  boolean hadError = false;

			for (int j = 0; j < input.size(); j++) {
				if (predOutput.get (j).toString() != trueOutput.get(j).toString ()) {
					hadError = true;
          break;
				}
			}

			if (hadError) {
        printSequence (outf, instance.getName().toString(), input, trueOutput, predOutput);
      } else {
        printSequence (out, instance.getName().toString(), input, trueOutput, predOutput);
      }

			out.println();
		}

		out.close ();
	}

  
  protected static void printSequence (PrintStream outf, String name, Sequence input, Sequence trueOutput, Sequence predOutput)
	{
		outf.println (name);

		// Print the sentence, one line per token, with the true/pred labels, the features, and the word
		for (int j = 0; j < input.size(); j++) {
			FeatureVector fv = (FeatureVector) input.get(j);
			char errIndicator = trueOutput.get(j).equals(predOutput.get(j)) ? ':' : '*';
			outf.print (trueOutput.get(j).toString() + "/" + predOutput.get(j).toString() + errIndicator + ' '
				);

			String word = null;
			for (int k = 0; k < fv.numLocations(); k++) {
				String featureName = fv.getAlphabet().lookupObject(fv.indexAtLocation(k)).toString();
				if (Pattern.matches ("^W=[^@]+$", featureName)) {
					word = featureName;
				}
			}
			if (word != null)
				outf.print ('['+word.substring(word.indexOf('=')+1)+']'+errIndicator+" ");

			for (int k = 0; k < fv.numLocations(); k++) {
				String featureName = fv.getAlphabet().lookupObject(fv.indexAtLocation(k)).toString();
				outf.print (featureName + " ");
			}

			outf.println ("");
		}
		outf.println();
	}


  protected static void initOutputDirectory() throws IOException
  {
    Logger.getLogger("").addHandler(new FileHandler
            (new File
                    (outputPrefix.value, "java.log").toString()));
  }

  protected static int[][] getOffsets() throws EvalError {
    return (int[][]) CommandOption.getInterpreter().eval
            ("new int[][] " + offsetsOption.value);
  }

}
