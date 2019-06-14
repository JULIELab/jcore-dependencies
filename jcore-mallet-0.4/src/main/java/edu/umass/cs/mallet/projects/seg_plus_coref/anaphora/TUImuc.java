package edu.umass.cs.mallet.projects.seg_plus_coref.anaphora;

import edu.umass.cs.mallet.base.classify.Classification;
import edu.umass.cs.mallet.base.classify.Classifier;
import edu.umass.cs.mallet.base.classify.MaxEntTrainer;
import edu.umass.cs.mallet.base.classify.Trial;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.Target2Label;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.types.LabelVector;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashSet;

public class TUImuc
{
	public static void main (String[] args)
	{
	    String trainingDataPath;
	    String testDataPath;
		if (args.length != 2) {
			// System.exit(-1);
		    trainingDataPath = new String ("/odin.mitre.org/tmp/muc/tr/all-docs/training");
		    testDataPath = new String ("/odin.mitre.org/tmp/muc/tr/all-docs/test-annotated");
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
		MentionPairIterator pairIterator = new MentionPairIterator (fileIterator,
																																"MUC", true);
		MentionPairIterator testPairIterator = new MentionPairIterator
			(testFileIterator, "MUC", true);
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
		//InstanceList testList = new InstanceList (instancePipe);
		//testList.add (testPairIterator);
		//System.out.println ("Testing Accuracy on \"yes\" = "+
		//										new Trial (classifier, testList).labelF1("yes"));
		//System.out.println ("Testing Accuracy on \"no\" = "+
		//											new Trial (classifier, testList).labelF1("no"));
		int curRefIndex = -1;
		int candRefIndex;
		double curBestValue = 0.0;
		Instance curInst;
		MentionPair candidatePair;
		MentionPair bestPair = null;
		Mention antecedent = null;
		Mention referent = null;

		LinkedHashSet allCoreferentials = new LinkedHashSet();
		LinkedHashSet keyCoreferentials = new LinkedHashSet();
		LinkedHashSet responseCoreferentials = new LinkedHashSet();

		int numAntecedents = 0;
		int numReferents = 0;
		int entityId = 0;
		int nullAntecedents = 0;

		int numCorrect = 0;

		int numTotalInstances = 0;

		// try on training data - check for bugs
		while (testPairIterator.hasNext()) {
			numTotalInstances++;
			Instance carrier = (Instance)testPairIterator.next();
			candidatePair = (MentionPair)carrier.getData();
			curInst = new Instance (candidatePair, null, null, null);
			curInst.getData(instancePipe); // runs instance through the pipeline
			//collect actual labels
			if ((carrier.getTarget().toString().equals("yes")))
			{
				keyCoreferentials.add(candidatePair);
			}
			
			if (candidatePair.getReferentIndex() != curRefIndex) {
				numReferents++;
				if ((referent != null)) {
					if (antecedent != null) {
						if (antecedent.getEntityId() == null) {
							entityId++;
							antecedent.setEntityId(new Integer(entityId).toString());
						}
						allCoreferentials.add(antecedent);  // a "unique" add
						System.out.println("++ Selected " + antecedent.getString());						
					}
					else {
						System.out.println("++ Selected NULL");
						entityId++;
					}
					referent.setEntityId(new Integer(entityId).toString());
					allCoreferentials.add(referent);
					responseCoreferentials.add(bestPair);  // response coreffed
					// pair
				}
				referent = candidatePair.getReferent();
				curRefIndex = candidatePair.getReferentIndex();
				curBestValue = 0.0;
				antecedent = null;
				numAntecedents = 0;
				// do stuff for a new referent
			} else {
        // condition indicates that we're looking at possible antecedent still
				Classification cl = classifier.classify(curInst);
				LabelVector classDistribution = cl.getLabelVector();
				double value = 0.0;
				for (int k=0; k < classDistribution.singleSize(); k++) {
					if (classDistribution.labelAtLocation(k).toString().equals("yes"))
						value = classDistribution.valueAtLocation(k);
				}
				if (value > curBestValue) {
					curBestValue = value;
					antecedent = candidatePair.getAntecedent();
					bestPair = candidatePair;
				}
				numAntecedents++;
			}
		}
		System.out.println("Total instances inference: " + numTotalInstances);
		System.out.println("Total instances training: " + ilist.size());
		PairWiseEvaluator evaluator = new PairWiseEvaluator (keyCoreferentials,
																												 responseCoreferentials);
		evaluator.evaluate();
		//CorpusOutputter corpusOutputter = new CorpusOutputter (allCoreferentials);
		//corpusOutputter.begin();
		System.out.println("Number of referents is: " + numReferents);
		System.out.println("Number of null antecedents: " + nullAntecedents);
		System.out.println("Score is : " + (double)evaluator.score());

	}
}
