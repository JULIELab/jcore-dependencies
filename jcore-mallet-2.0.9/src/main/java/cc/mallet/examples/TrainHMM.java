package cc.mallet.examples;

import cc.mallet.fst.HMM;
import cc.mallet.fst.HMMTrainerByLikelihood;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.InstanceList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class TrainHMM {
	
	public TrainHMM(String trainingFilename, String testingFilename) throws IOException {
		
		ArrayList<Pipe> pipes = new ArrayList<Pipe>();

		pipes.add(new SimpleTaggerSentence2TokenSequence());
		pipes.add(new TokenSequence2FeatureSequence());

		Pipe pipe = new SerialPipes(pipes);

		InstanceList trainingInstances = new InstanceList(pipe);
		InstanceList testingInstances = new InstanceList(pipe);

		trainingInstances.addThruPipe(new LineGroupIterator(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(trainingFilename)))), Pattern.compile("^\\s*$"), true));
		testingInstances.addThruPipe(new LineGroupIterator(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(testingFilename)))), Pattern.compile("^\\s*$"), true));
		
		HMM hmm = new HMM(pipe, null);
		hmm.addStatesForLabelsConnectedAsIn(trainingInstances);
		//hmm.addStatesForBiLabelsConnectedAsIn(trainingInstances);

		HMMTrainerByLikelihood trainer = 
			new HMMTrainerByLikelihood(hmm);
		TransducerEvaluator trainingEvaluator = 
			new PerClassAccuracyEvaluator(trainingInstances, "training");
		TransducerEvaluator testingEvaluator = 
			new PerClassAccuracyEvaluator(testingInstances, "testing");
		trainer.train(trainingInstances, 10);
		
		trainingEvaluator.evaluate(trainer);
		testingEvaluator.evaluate(trainer);
	}

	public static void main (String[] args) throws Exception {
		TrainHMM trainer = new TrainHMM(args[0], args[1]);

	}

}