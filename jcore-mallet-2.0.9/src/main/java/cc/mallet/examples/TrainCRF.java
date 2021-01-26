package cc.mallet.examples;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.pipe.tsf.TokenFirstPosition;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;
import cc.mallet.types.InstanceList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class TrainCRF {
	
	public TrainCRF(String trainingFilename, String testingFilename) throws IOException {
		
		ArrayList<Pipe> pipes = new ArrayList<Pipe>();

		int[][] conjunctions = new int[2][];
		conjunctions[0] = new int[] { -1 };
		conjunctions[1] = new int[] { 1 };

		pipes.add(new SimpleTaggerSentence2TokenSequence());
		pipes.add(new OffsetConjunctions(conjunctions));
		//pipes.add(new FeaturesInWindow("PREV-", -1, 1));
		pipes.add(new TokenTextCharSuffix("C1=", 1));
		pipes.add(new TokenTextCharSuffix("C2=", 2));
		pipes.add(new TokenTextCharSuffix("C3=", 3));
		pipes.add(new RegexMatches("CAPITALIZED", Pattern.compile("^\\p{Lu}.*")));
		pipes.add(new RegexMatches("STARTSNUMBER", Pattern.compile("^[0-9].*")));
		pipes.add(new RegexMatches("HYPHENATED", Pattern.compile(".*\\-.*")));
		pipes.add(new RegexMatches("DOLLARSIGN", Pattern.compile(".*\\$.*")));
		pipes.add(new TokenFirstPosition("FIRSTTOKEN"));
		pipes.add(new TokenSequence2FeatureVectorSequence());

		Pipe pipe = new SerialPipes(pipes);

		InstanceList trainingInstances = new InstanceList(pipe);
		InstanceList testingInstances = new InstanceList(pipe);

		trainingInstances.addThruPipe(new LineGroupIterator(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(trainingFilename)))), Pattern.compile("^\\s*$"), true));
		testingInstances.addThruPipe(new LineGroupIterator(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(testingFilename)))), Pattern.compile("^\\s*$"), true));
		
		CRF crf = new CRF(pipe, null);
		//crf.addStatesForLabelsConnectedAsIn(trainingInstances);
		crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
		crf.addStartState();

		CRFTrainerByLabelLikelihood trainer = 
			new CRFTrainerByLabelLikelihood(crf);
		trainer.setGaussianPriorVariance(10.0);

		//CRFTrainerByStochasticGradient trainer = 
		//new CRFTrainerByStochasticGradient(crf, 1.0);

		//CRFTrainerByL1LabelLikelihood trainer = 
		//	new CRFTrainerByL1LabelLikelihood(crf, 0.75);

		//trainer.addEvaluator(new PerClassAccuracyEvaluator(trainingInstances, "training"));
		trainer.addEvaluator(new PerClassAccuracyEvaluator(testingInstances, "testing"));
		trainer.addEvaluator(new TokenAccuracyEvaluator(testingInstances, "testing"));
		trainer.train(trainingInstances);
		
	}

	public static void main (String[] args) throws Exception {
		TrainCRF trainer = new TrainCRF(args[0], args[1]);

	}

}