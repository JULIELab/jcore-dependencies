/* Copyright (C) 2002 Dept. of Computer Science, Univ. of Massachusetts, Amherst

   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet

   This program toolkit free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  For more
   details see the GNU General Public License and the file README-LEGAL.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
   02111-1307, USA. */


/**
	 @author Ben Wellner
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.anaphora;

import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class TUI
{
    public static void main (String[] args)
    {
	String trainingDataPath;
	String testDataPath;
	if (args.length != 2) {
	    // System.exit(-1);
	    //trainingDataPath = new String ("/odin.mitre.org/tmp/treebank/xml-bigger/train");
	    //testDataPath = new String ("/odin.mitre.org/tmp/treebank/xml-bigger/train");
	    trainingDataPath = new String("c:/JavaDevel/data/toy");
	    testDataPath = new String("c:/JavaDevel/data/toy");
	} else {
	    trainingDataPath = args[0];
	    testDataPath = args[1];
	}
	
	// This iterator takes a directory and iterates over the files contained
	// in it
	
	XMLFileFilter filter = new XMLFileFilter(".*xml");
	
	FileIterator fileIterator = new FileIterator (new File(trainingDataPath), (FileFilter)filter);
	FileIterator testFileIterator = new FileIterator (new File(testDataPath), (FileFilter)filter);
	
	// This iterator takes an iterator over files, and iterates over all (relevant)
	// pairs of DOM nodes in each file
	MentionPairIterator pairIterator = new MentionPairIterator (fileIterator, "TB");
	MentionPairIterator testPairIterator = new MentionPairIterator
	    (testFileIterator, "TB");
	// This pipeline takes individual pairs as input and produces a feature vector
	Pipe instancePipe = new SerialPipes (new Pipe[] {
	    new Target2Label(),
	    new AffixOfMentionPair (),
	    new MentionPairSentenceDistance(),
	    new PartOfSpeechMentionPair(),
	    new HobbsDistanceMentionPair(),
	    new MentionPairAntecedentPosition(),
	    new NullAntecedentFeatureExtractor(),
	    new ModifierWordFeatures(),
	    new MentionPair2FeatureVector ()
	});
	InstanceList ilist = new InstanceList (instancePipe);
	ilist.add (pairIterator);
	
	//InstanceList[] ilists = ilist.split (new double[] {.7, .3});
	Classifier classifier = new MaxEntTrainer().train (ilist);
	System.out.println ("Training Accuracy on \"yes\" = "+
			    new Trial (classifier, ilist).labelF1("yes"));
	/*
	  System.out.println ("Testing Accuracy on \"yes\" = "
	  + new Trial (classifier, ilists[1]).labelF1("yes")); */
	System.out.println ("Training Accuracy on \"no\" = "+
			    new Trial (classifier, ilist).labelF1("no"));
	/*		System.out.println ("Testing Accuracy on \"no\" = "
			+ new Trial (classifier, ilists[1]).labelF1("no"));*/
	/*
	  Iterator it1 = ilist.iterator();
	  while (it1.hasNext())
	  {
	  Instance inst = (Instance)it1.next();
	  System.out.println("-------");
	  System.out.println(((FeatureVector)inst.getData(instancePipe)).toString());
	  System.out.println("---");
	  System.out.println(inst.getLabeling().toString());
	  }
	*/
	/*
	  Pipe testInstancePipe = new SerialPipes (new Pipe[] {
	  //new CharSequence2TokenSequence(" ")
	  new Target2Label(),
	  new AffixOfMentionPair (),
	  new MentionPairSentenceDistance(),
	  new PartOfSpeechMentionPair(),
	  new HobbsDistanceMentionPair(),
	  new MentionPairAntecedentPosition(),
	  new NullAntecedentFeatureExtractor(),
	  new ModifierWordFeatures(),			
	  new MentionPair2FeatureVector ()
	  });
	*/
	int curRefIndex = -1;
	int candRefIndex;
	double curBestValue = 0.0;
	Instance curInst;
	MentionPair candidatePair;
	MentionPair bestPair = null;
	Mention antecedent = null;
	Mention referent = null;
	Mention curKeyRef = null;
	
	LinkedHashSet allCoreferentials = new LinkedHashSet();
	LinkedHashSet keyCoreferentials = new LinkedHashSet();
	LinkedHashSet responseCoreferentials = new LinkedHashSet();
	
	int numAntecedents = 0;
	int numReferents = 0;
	int entityId = 0;
	int nullAntecedents = 0;
	
	int numCorrect = 0;
	
	while (testPairIterator.hasNext()) {
	    Instance carrier = (Instance)testPairIterator.next();
	    candidatePair = (MentionPair)carrier.getData();
	    curInst = new Instance (candidatePair, null, null, null);
	    curInst.getData(instancePipe); // runs instance through the pipeline
	    //collect actual labels
	    if ((carrier.getTarget().toString().equals("yes")) &&
		(!candidatePair.nullPair()) &&
		candidatePair.getReferent() != curKeyRef)
		// last condition ensures that we only add the first antecedent
		// to the key - we have to evaluate pairwise like this
		{
		      System.out.print(candidatePair.getReferentIndex() + "::");
		      if (candidatePair.getAntecedent() != null)
			  System.out.println("key: " + candidatePair.getAntecedent().getString()
					     + " & " +
					     candidatePair.getReferent().getString());
		      else
			  System.out.println("key: " + "NULL"
					     + " & " + candidatePair.getReferent().getString());

		    keyCoreferentials.add(candidatePair);
		    curKeyRef = candidatePair.getReferent();
		}
	    if (candidatePair.getReferent() != referent) {
		numReferents++;
		// condition checks if we have a new referent (i.e. anaphor) remember
		// that we're iterating through PAIRS of mentions
		if ((referent != null)) {
		    if (antecedent != null) {
			if (Integer.decode(antecedent.getEntityId()).intValue() == 0) {
			    entityId++;
			    antecedent.setEntityId(new Integer(entityId).toString());
			}
			allCoreferentials.add(antecedent);  // a "unique" add
			//System.out.println("++ Selected " + antecedent.getString());
		    }
		    else {
			entityId++;
			//System.out.println("++ Selected NULL");
		    }
		    referent.setEntityId(new Integer(entityId).toString());
		    allCoreferentials.add(referent);
		    if (antecedent != null) // only add non NULL ones now
			responseCoreferentials.add(bestPair);  // response coreffed
		    // pair
		}
		referent = candidatePair.getReferent();  // set new referent
		//System.out.print("\nNew referent: " + referent.getString());
		curRefIndex = candidatePair.getReferentIndex();
		curBestValue = 0.0;
		antecedent = candidatePair.getAntecedent();
		numAntecedents = 0;
		// do stuff for a new referent
	    } 
	    Classification cl = classifier.classify(curInst);
	    LabelVector classDistribution = cl.getLabelVector();
	    double value = 0.0;
	    for (int k=0; k < classDistribution.singleSize(); k++) {
		if (classDistribution.labelAtLocation(k).toString().equals("yes"))
		    value = classDistribution.valueAtLocation(k);
	    }
	    /*		
		if (candidatePair.getAntecedent() != null)
		System.out.print(" (" + candidatePair.getAntecedent().getString() + ","
		+ value + ")");
		else
		System.out.print(" (null," + value + ")"); */
	    if (value > curBestValue) {
		curBestValue = value;
		antecedent = candidatePair.getAntecedent();
		bestPair = candidatePair;
	    }
	    numAntecedents++;
	}

	Iterator i2 = responseCoreferentials.iterator();
	while (i2.hasNext()) {
	    MentionPair pair2 = (MentionPair)i2.next();
	    if (!pair2.nullPair())
		System.out.println(pair2.getAntecedent().getString() + " AND " +
				   pair2.getReferent().getString() + " with " +
				   pair2.getAntecedent().getEntityId() + "," + 
				   pair2.getReferent().getEntityId());
	    else
		System.out.println("NULL AND " +
				   pair2.getReferent().getString() + " with " +
				   pair2.getReferent().getEntityId());

	}

	PairWiseEvaluator evaluator = new PairWiseEvaluator (keyCoreferentials,
							     responseCoreferentials);
	//CorpusOutputter corpusOutputter = new CorpusOutputter (allCoreferentials);
	//corpusOutputter.begin();
	evaluator.evaluate();
	System.out.println("Number of referents is: " + numReferents);
	System.out.println("Number of null antecedents: " + nullAntecedents);
	System.out.println("Score is : " + (double)evaluator.score());
    }
}
