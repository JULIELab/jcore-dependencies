package edu.umass.cs.mallet.projects.seg_plus_coref.anaphora;

import edu.umass.cs.mallet.base.classify.Classifier;
import edu.umass.cs.mallet.base.classify.MaxEntTrainer;
import edu.umass.cs.mallet.base.classify.Trial;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.Target2Label;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.InstanceList;
import org.jdom.Document;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

public class TUI2
{
	public static void main (String[] args)
	{
	    String trainingDataPath;
	    String testDataPath;
		if (args.length != 2) {
			// System.exit(-1);
		    trainingDataPath = new String ("c:/JavaDevel/data/xml-bigger/train");
		    testDataPath = new String ("c:/JavaDevel/data/xml-bigger/test");
		    //trainingDataPath = new String ("c:/JavaDevel/data/toy");
		    //testDataPath = new String ("c:/JavaDevel/data/toy");
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
		MentionPairIterator pairIterator = new MentionPairIterator (fileIterator, "TB", true, true);
		MentionPairIterator testPairIterator = new MentionPairIterator
		    (testFileIterator, "TB", true, true);
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
		printOutReferentInfo (ilist);

		//InstanceList[] ilists = ilist.split (new double[] {.7, .3});
		Classifier classifier = new MaxEntTrainer().train (ilist);

		System.out.println("Number of referents: " + pairIterator.getNumReferents());

		System.out.println ("Training Accuracy on \"yes\" = "+
				    new Trial (classifier, ilist).labelF1("yes"));
		/*
		System.out.println ("Testing Accuracy on \"yes\" = "
		+ new Trial (classifier, ilists[1]).labelF1("yes")); */
		System.out.println ("Training Accuracy on \"no\" = "+
				    new Trial (classifier, ilist).labelF1("no"));
/*		System.out.println ("Testing Accuracy on \"no\" = "
			+ new Trial (classifier, ilists[1]).labelF1("no"));*/

		InstanceList testList = new InstanceList (instancePipe);
		testList.add (testPairIterator);
		
		System.out.println("Number of referents: " + testPairIterator.getNumReferents());		
		System.out.println ("Testing Accuracy on \"yes\" = "+
				    new Trial (classifier, testList).labelF1("yes"));
		System.out.println ("Testing Accuracy on \"no\" = "+
				    new Trial (classifier, testList).labelF1("no"));

	}
    private static void printOutReferentInfo (InstanceList ilist)
    {
	Iterator iter = ilist.iterator();
	Mention curRef = null;
	Document curDoc = null;
	int numRefs = 0;

	while (iter.hasNext()) {
	    MentionPair pair = (MentionPair)((Instance)iter.next()).getSource();
	    Mention ref = pair.getReferent();
	    if (ref.getDocument() != curDoc) {
		System.out.println("New Document: " + ref.getDocPath().toString());
		curDoc = ref.getDocument();
	    }
	    if (ref != curRef) {
		System.out.println("     Referent: " + ref.getString() + " : " + ref.getElement().getName());
		if ((ref.getElement().getName().equals("lex")) ||(ref.getElement().getName().equals("LEX")))
		    System.out.println("     :  " + ref.getElement().getAttributeValue("pos"));
		curRef = ref;
		numRefs++;
	    }
	}
	System.out.println("Number of refs via TUI2: " + numRefs);
    }
}
