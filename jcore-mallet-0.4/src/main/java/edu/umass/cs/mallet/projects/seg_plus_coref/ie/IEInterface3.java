/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet

   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
   @author Fuchun Peng <a href="mailto:fuchun@cs.umass.edu">fuchun@cs.umass.edu</a>
	July 2003

	This class provides information extraction interface to other applications
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.ie;

import edu.umass.cs.mallet.base.fst.CRF3;
import edu.umass.cs.mallet.base.fst.Transducer;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import edu.umass.cs.mallet.base.pipe.iterator.LineGroupIterator;
import edu.umass.cs.mallet.base.types.*;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class IEInterface3{
	String seperator = "";

	private static Logger logger = Logger.getLogger(IEInterface3.class.getName());

	private File crfFile;
	private CRF3 crf = null;
	private SerialPipes pipe;

	private TokenSequence tokenSequence;
	private Sequence viterbiSequence;
	private double confidence;
	private Transducer.ViterbiPath viterbiP;
	private Transducer.ViterbiPath_NBest viterbiP_NBest;

	private int instance_error_num = 0;
	private int instance_size = 0;
	private double instance_accuracy;

	boolean printFont = true;

	public IEInterface3()
	{
		this.crfFile = null;
	}

	public IEInterface3(File crfFile)
	{
		assert(crfFile != null);
		this.crfFile = crfFile;
	}

	public void setPipe(SerialPipes pipe)
	{
		this.pipe = pipe;
	}

	// load in CRF3 and its pipe from a trained crfFile
	public boolean loadCRF()
	{
		return loadCRF(crfFile);
	}

	
	public boolean loadCRF(File crfFile)
	{

		assert(crfFile != null);

		CRF3 crf = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream( crfFile ));
			crf = (CRF3) ois.readObject();
			ois.close();
		}
		catch (IOException e) {
			System.err.println("Exception reading crf file: " + e);
			crf= null;
		}
		catch (ClassNotFoundException cnfe) {
			System.err.println("Cound not find class reading in object: " + cnfe);
			crf= null;
		}


//		crf = CRFIO.readCRF(crfFile.toString());

		if(crf==null) {
			System.err.println("Read a null crf from file: " + crfFile);
			System.exit(1);
		}
		
		this.crf = crf;
		this.pipe = (SerialPipes) crf.getInputPipe();
		if (this.pipe == null) {
			System.err.println("Get a null pipe from CRF");
			System.exit(1);
		}


		//xxx print out the read-in pipes, just for debugging purpose
/*                ArrayList pipes1 = (this.pipe).getPipes();
                System.out.println("pipes1");
                for (int i = 0; i < pipes1.size(); i++) {
                        System.out.print("Pipe: " + i + ": ");
                        Pipe tempP = (Pipe) pipes1.get (i);
                        if (tempP == null) {
                                System.out.println("Pipe is null");
                        }
                        else {
                                String pipeName = tempP.getClass().getName();
                                System.out.println(pipeName);
				if(tempP instanceof SerialPipes){
					ArrayList pipes2 = ((SerialPipes)tempP).getPipes();
			
					for(int j=0; j<pipes2.size(); j++){
						System.out.print("	Pipe: " + j + ": ");
						Pipe tempP2 = (Pipe) pipes2.get(j);
						if(tempP2 == null){
							System.out.println("	Pipe is null");
						}
						else{
				                        String pipeName2 = tempP2.getClass().getName();
                                			System.out.println(pipeName2);
						}
					}
				}
                        }

                }
*/


//		System.out.println("================= start of CRF ============");
//		crf.print();
//		System.out.println("==================end of crf ==============");

		//xxx

		logger.log(Level.INFO, "Load CRF successfully\n");

		return true;
	}

	public boolean loadCRF(CRF3 crf)
	{
		this.crf = crf;
		this.pipe = (SerialPipes) crf.getInputPipe();

		if (this.pipe == null) {
			System.err.println("Get a null pipe from CRF");
			return false;
		}

		return true;
	}
	
	public String printResultInFormat(boolean sgml)
	{

		String viterbiStr = "";

		assert(tokenSequence != null);
		assert(viterbiSequence != null);
		assert(tokenSequence.size() == viterbiSequence.size());

		String font = "";
		String current_font = "";
		if(sgml){
			String old_tag = null;
			String startTag, endTag;
			for(int i=0; i<tokenSequence.size(); i++){
				Token token = (Token)tokenSequence.getToken(i);

				String word = token.getText();
				String tag = viterbiSequence.get(i).toString();

				if(tag != old_tag){
					if(old_tag != null){
						endTag = "</"+old_tag+">";
						viterbiStr += endTag;	
					}
			
					startTag = "<"+tag+">";	
					viterbiStr += startTag;

					old_tag = tag;
				}

				if(token.hasProperty("FONT")){
					current_font = (String)token.getProperty("FONT");
				}
			
				if(!current_font.equals(font) && printFont){	
					viterbiStr += "<font value=\""+current_font+"\" />";
					font = current_font;
				}

				viterbiStr += word;
				viterbiStr += " ";
				if(i == tokenSequence.size() - 1){
					endTag = "</"+tag+">";
					viterbiStr += endTag;
				}

				if(token.hasProperty("LINE_END")){
					viterbiStr += "\n";
				}

			}

		}
		else{

			for(int i=0; i<tokenSequence.size(); i++){
				viterbiStr += ((Token)tokenSequence.getToken(i)).getText();
				viterbiStr += ": ";
				viterbiStr += viterbiSequence.get(i).toString();
				viterbiStr += "\n";
			}
		}

		return viterbiStr;
		
	}

	//given an input string, label it, and output in the format of inline SGML
	public String viterbiCRFString(String line, boolean sgml)
	{
		Instance lineCarrier = new Instance(line, null, null, null, pipe);

		assert(pipe != null);

		Instance featureCarrier = pipe.pipe(lineCarrier, 0);

		assert(crf != null);		
		viterbiP = crf.viterbiPath((Sequence)featureCarrier.getData());
		viterbiSequence = viterbiP.output();
		//confidence = Math.exp(-viterbiP.getCost()/viterbiSequence.size());
		confidence = viterbiP.getCost();		

		tokenSequence = (TokenSequence)featureCarrier.getSource();
		assert(viterbiSequence.size() == tokenSequence.size());

		return printResultInFormat(sgml);
	}


	// to use this method successfully, tokenization should use "\\w+-\\w+|\\w+|'s|``|''|\\S" pattern
	// or change the wordPattern in WSJPOSSentence2TokenSequence to match your tokenization pattern.
	// 	
	public Sequence viterbiCRFTokenSequence(TokenSequence ts)
	{	
		assert(crf != null);

		String line = "";
		for(int i=0; i<ts.size(); i++){
			line += ts.getToken(i).getText()+" ";
//			System.out.println(i+": "+ts.getToken(i).getText());
		}
		
		assert(pipe != null);
		Instance lineCarrier = new Instance(line, null, null, null, pipe);

		viterbiP = crf.viterbiPath((Sequence)lineCarrier.getData());
		viterbiSequence = viterbiP.output();
		confidence = Math.exp(-viterbiP.getCost()/viterbiSequence.size());

//		viterbiSequence = crf.viterbiPath((Sequence)lineCarrier.getData()).output();

//		Sequence tempTS = (Sequence)lineCarrier.getData();
//		for(int i=0; i<tempTS.size(); i++){
//			System.out.println(i+": "+tempTS.get(i).toString() + "/" + viterbiSequence.get(i).toString());
//		}
		
		assert(viterbiSequence.size() == ts.size()): "ts.size=" + ts.size() + " " + "viterSequence.size=" + viterbiSequence.size();
		
		return viterbiSequence;
	}

	private double InstanceAccuracy(Sequence viterbiSequence, Sequence targetSequence)
	{
		assert(viterbiSequence.size() == targetSequence.size());
		instance_size = viterbiSequence.size();
		instance_error_num = 0;
		for(int i=0; i<instance_size; i++){
			String predO = viterbiSequence.get(i).toString();
			String trueO = targetSequence.get(i).toString();
			if(!predO.equals(trueO)){
				instance_error_num ++;
			}	
		}
	
		double accuracy = (double)instance_error_num/instance_size;
		return accuracy;
	}

	//viterbi for a piped instance
	public String viterbiCRFInstance(Instance instance, boolean sgml )
	{

		assert(crf != null);

		viterbiP = crf.viterbiPath((Sequence)instance.getData());// regular viterbi
		viterbiSequence = viterbiP.output();

//		confidence = Math.exp(viterbiP.getCost()/viterbiSequence.size());
//		confidence = viterbiP.getCost()/viterbiSequence.size();
	
//		viterbiSequence = crf.viterbiPath((Sequence)instance.getData()).output();
		instance_accuracy= InstanceAccuracy(viterbiSequence, (Sequence)instance.getTarget());

		tokenSequence = (TokenSequence)instance.getSource(); 
		assert(viterbiSequence.size() == tokenSequence.size());

		return printResultInFormat(sgml);

	}

	public String viterbiCRFInstance_NBest(Instance instance, boolean sgml )
	{

		String str = "";
		assert(crf != null);
	        tokenSequence = (TokenSequence)instance.getSource();
                assert(viterbiSequence.size() == tokenSequence.size());

		int N = 1;
		viterbiP_NBest = crf.viterbiPath_NBest((Sequence)instance.getData(), N);//n-best list
		Sequence[] nbestlist = viterbiP_NBest.outputNBest();
/*
		for(int i=0; i<nbestlist.length; i++)	{
			viterbiSequence = nbestlist[i];
	//		viterbiSequence = viterbiP_NBest.output();
			str += "\n" + i + ": " + (viterbiP_NBest.costNBest())[i] + " : " + viterbiP_NBest.getCost() + "\n";
			str += printResultInFormat(sgml);

		}
*/
		viterbiSequence = nbestlist[N-1];
		str += printResultInFormat(sgml);

		return str;
	}



	//given an input file, label it, and output in the format of inline SGML
	public void viterbiCRF(File inputFile, boolean sgml, String seperator)
	{

		assert(pipe!= null);
		InstanceList instancelist = new InstanceList (pipe);

		Reader reader;
		try {
			reader = new FileReader (inputFile);
		} catch (Exception e) {
			throw new IllegalArgumentException ("Can't read file "+inputFile);
		}
		
		instancelist.add (new LineGroupIterator (reader, Pattern.compile(seperator), true));

		String outputFileStr = inputFile.toString() + "_tagged";
		
		System.out.println(inputFile.toString() + " ---> " + outputFileStr);

		PrintStream taggedOut = null;
		try{
			FileOutputStream fos = new FileOutputStream (outputFileStr);
			taggedOut = new PrintStream (fos);
		} catch (IOException e) {
			logger.warning ("Couldn't open output file '"+ outputFileStr+"'");
		}
		
		if(taggedOut == null){
			taggedOut = System.out;
		}
			
		String viterbiStr = "";
//		taggedOut.println("testing instance number: " + instancelist.size() );
		for(int i=0; i<instancelist.size(); i++){
//				taggedOut.println("\ntesting instance " + i);
				Instance instance = instancelist.getInstance(i);
				String crfStr = viterbiCRFInstance(instance,sgml);
			
				taggedOut.println(seperator);
//				taggedOut.println("confidence = " + confidence + " instance accuracy= " 
//					+ instance_error_num + "/" + instance_size + "=" + instance_accuracy);
				taggedOut.println(crfStr);
				viterbiStr += crfStr;


/*				//N-best tagging
				crfStr = viterbiCRFInstance_NBest(instance,sgml);
				taggedOut.println("N-best result:");
				taggedOut.println(seperator);
//				taggedOut.println("confidence = " + confidence + " instance accuracy= " 
//					+ instance_error_num + "/" + instance_size + "=" + instance_accuracy);
				taggedOut.println(crfStr);

				viterbiStr += crfStr;
*/

		}


		if(taggedOut != System.out){
			taggedOut.close();
		}

	}

	//viterbi for all files under a given directory, 
	//if the given directory is a plain file, viterbi for this file
	public void viterbiCRF(String inputDir, boolean sgml, String seperator)
	{

		// if inputDir is a plain file
		File file = new File(inputDir);
		if( file.isFile() ){
			viterbiCRF(file, sgml, seperator);
		}
		else{
			// continue if it is a directory
			FileIterator fileIter = new FileIterator (inputDir);	
			ArrayList fileList = fileIter.getFileArray();

			for(int i=0; i<fileList.size(); i++){
				file = (File) fileList.get(i);
				viterbiCRF(file, sgml, seperator);
			}
		}
	}

	public void viterbiCRF(String inputDir)
	{
		viterbiCRF(inputDir, true);
	}

	public void viterbiCRF(String inputDir, boolean sgml)
	{
		viterbiCRF(inputDir, sgml, seperator);
	}
	
	// cumulative evaluation for N-best list 
	public void cumulativeEvaluate(File inputFile, String seperator, int N)
	{
		assert(pipe!= null);
		InstanceList instancelist = new InstanceList (pipe);

		Reader reader;
		try {
			reader = new FileReader (inputFile);
		} catch (Exception e) {
			throw new IllegalArgumentException ("Can't read file "+inputFile);
		}
		
		instancelist.add (new LineGroupIterator (reader, Pattern.compile(seperator), true));

		Alphabet targets = (this.pipe).getTargetAlphabet();
                assert(targets != null);

		int numCorrectTokens = 0, totalTokens = 0;
		int[] numTrueSegments, numPredictedSegments, numCorrectSegments;
		int[] numCorrectSegmentsInVocabulary, numCorrectSegmentsOOV;
		int[] numIncorrectSegmentsInVocabulary, numIncorrectSegmentsOOV;
		int[][] matrixEntry;
		int numCorrectWholeInstance = 0;

		numTrueSegments = new int[targets.size()];
		numPredictedSegments = new int[targets.size()];
		numCorrectSegments = new int[targets.size()];
		matrixEntry = new int[targets.size()][targets.size()];


        	String PUNT = "[,\\.;:?!()*]";
	        Pattern puntPattern = Pattern.compile(PUNT);

		for(int i=0; i<instancelist.size(); i++){
				Instance instance = instancelist.getInstance(i);

				//N-best tagging
				viterbiP_NBest = crf.viterbiPath_NBest((Sequence)instance.getData(), N);//n-best list
				Sequence[] nbestlist = viterbiP_NBest.outputNBest();

				boolean wholeInstanceCorrect = true;
				Sequence trueSequence = (Sequence)instance.getTarget();

			        tokenSequence = (TokenSequence)instance.getSource();


				for (int j = 0; j < trueSequence.size(); j++) {

					String tokenStr = tokenSequence.getToken(j).getText();
					if(puntPattern.matcher(tokenStr).matches()){//ignore punct;
						continue;
					}

					totalTokens ++;

					Object trueO = trueSequence.get(j);
					int trueIndex = targets.lookupIndex(trueO);
					numTrueSegments[trueIndex] ++;
			
					int predIndex = 0;
					for(int k=0; k<nbestlist.length; k++){
						Object predO = nbestlist[k].get(j);
						predIndex = targets.lookupIndex(predO);
						if(predIndex == trueIndex) break;
					}

					numPredictedSegments[predIndex] ++;
					matrixEntry[trueIndex][predIndex] ++;

					if(predIndex == trueIndex){
						numCorrectTokens ++;
						numCorrectSegments[trueIndex] ++;
					}
					else{				
						wholeInstanceCorrect = false;
					}

	
				}

				if(wholeInstanceCorrect) numCorrectWholeInstance ++;
		}


		System.out.println("\n\ncumulative evalutation results: N = " + N);

		double accuracy = (double)numCorrectTokens/totalTokens;
                System.out.println ("\n" +" accuracy=" + numCorrectTokens +"/"+ totalTokens + " = " +accuracy);

		double wholeInstanceAccuracy = (double)numCorrectWholeInstance/instancelist.size();
		System.out.println ("Whole instance accuracy = " + numCorrectWholeInstance + "/" + instancelist.size() + " = " + wholeInstanceAccuracy);

		for(int t=0; t<targets.size(); t++){
			double precision = numPredictedSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numPredictedSegments[t];
			double recall = numTrueSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numTrueSegments[t];
			double f1 = recall+precision == 0.0 ? 0.0 : (2.0 * recall * precision) / (recall + precision);
			double accuracy_individual = (double)(totalTokens-numPredictedSegments[t]-numTrueSegments[t] + 2*numCorrectSegments[t] )/totalTokens;

                        System.out.println (targets.lookupObject(t) + " precision="+precision+" recall="+recall+" f1="+f1 + " accuracy=" + accuracy_individual);
			System.out.println ("segments true="+numTrueSegments[t]+" pred="+numPredictedSegments[t]+" correct="+numCorrectSegments[t]+" misses="+(numTrueSegments[t]-numCorrectSegments[t])+" alarms="+(numPredictedSegments[t]-numCorrectSegments[t]) + "\n");

		}


		System.out.println("\n Confusion Matrix (row: true label, col: predicted label)");
		System.out.print("\t");
		for(int t=0; t<targets.size(); t++){
			System.out.print(targets.lookupObject(t) + "\t");
		}
		System.out.println();

		for(int t=0; t< targets.size(); t++){
			System.out.print(targets.lookupObject(t)+"\t");
			for(int tt=0; tt<targets.size(); tt++){
				System.out.print(matrixEntry[t][tt] + "\t");
			}
			System.out.println();
		}

	}

	// offline test, inputFile is labeled
	public void offLineEvaluate(File inputFile, boolean sgml, String seperator)
	{
		assert(pipe!= null);
		InstanceList instancelist = new InstanceList (pipe);

		Reader reader;
		try {
			reader = new FileReader (inputFile);
		} catch (Exception e) {
			throw new IllegalArgumentException ("Can't read file "+inputFile);
		}
		
		instancelist.add (new LineGroupIterator (reader, Pattern.compile(seperator), true));

		String outputFileStr = inputFile.toString() + "_tagged";
		
		System.out.println(inputFile.toString() + " ---> " + outputFileStr);

		PrintStream taggedOut = null;
		try{
			FileOutputStream fos = new FileOutputStream (outputFileStr);
			taggedOut = new PrintStream (fos);
		} catch (IOException e) {
			logger.warning ("Couldn't open output file '"+ outputFileStr+"'");
		}
		
		if(taggedOut == null){
			taggedOut = System.out;
		}

		Alphabet targets = (this.pipe).getTargetAlphabet();
                assert(targets != null);

		int numCorrectTokens = 0, totalTokens = 0;
		int[] numTrueSegments, numPredictedSegments, numCorrectSegments;
		int[] numCorrectSegmentsInVocabulary, numCorrectSegmentsOOV;
		int[] numIncorrectSegmentsInVocabulary, numIncorrectSegmentsOOV;
		int[][] matrixEntry;
		int numCorrectWholeInstance = 0;

		numTrueSegments = new int[targets.size()];
		numPredictedSegments = new int[targets.size()];
		numCorrectSegments = new int[targets.size()];
		matrixEntry = new int[targets.size()][targets.size()];


        	String PUNT = "[,\\.;:?!()*]";
	        Pattern puntPattern = Pattern.compile(PUNT);

		String viterbiStr = "";
//		taggedOut.println("testing instance number: " + instancelist.size() );
		for(int i=0; i<instancelist.size(); i++){
//				taggedOut.println("\ntesting instance " + i);
				Instance instance = instancelist.getInstance(i);

				//viterbi decoding
/*				String crfStr = viterbiCRFInstance(instance,sgml);
				taggedOut.println(seperator);
//				taggedOut.println("confidence = " + confidence + " instance accuracy= " 
//					+ instance_error_num + "/" + instance_size + "=" + instance_accuracy);
				taggedOut.println(crfStr);
				viterbiStr += crfStr;
*/


				//N-best tagging
				String crfStr = viterbiCRFInstance_NBest(instance,sgml);
//				taggedOut.println("N-best result:");
				taggedOut.println(seperator);
//				taggedOut.println("confidence = " + confidence + " instance accuracy= " 
//					+ instance_error_num + "/" + instance_size + "=" + instance_accuracy);
				taggedOut.println(crfStr);

				viterbiStr += crfStr;


				boolean wholeInstanceCorrect = true;
				Sequence trueSequence = (Sequence)instance.getTarget();
				assert(trueSequence.size() == viterbiSequence.size() );

				for (int j = 0; j < trueSequence.size(); j++) {
				
					Object predO = viterbiSequence.get(j);
					Object trueO = trueSequence.get(j);
//					System.out.println(predO + "/" + trueO);
					int predIndex = targets.lookupIndex(predO);
					int trueIndex = targets.lookupIndex(trueO);
				
					String tokenStr = tokenSequence.getToken(j).getText();
					if(puntPattern.matcher(tokenStr).matches()){//ignore punct;
						continue;
					}

					totalTokens ++;
					numTrueSegments[trueIndex] ++;
					numPredictedSegments[predIndex] ++;

					matrixEntry[trueIndex][predIndex] ++;

					if(predIndex == trueIndex){
						numCorrectTokens ++;
						numCorrectSegments[trueIndex] ++;
					}
					else{				
						wholeInstanceCorrect = false;
					}

	
				}

				if(wholeInstanceCorrect) numCorrectWholeInstance ++;
		}



		double accuracy = (double)numCorrectTokens/totalTokens;
                System.out.println ("\n" +" accuracy=" + numCorrectTokens +"/"+ totalTokens + " = " +accuracy);

		double wholeInstanceAccuracy = (double)numCorrectWholeInstance/instancelist.size();
		System.out.println ("Whole instance accuracy = " + numCorrectWholeInstance + "/" + instancelist.size() + " = " + wholeInstanceAccuracy);

		for(int t=0; t<targets.size(); t++){
			double precision = numPredictedSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numPredictedSegments[t];
			double recall = numTrueSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numTrueSegments[t];
			double f1 = recall+precision == 0.0 ? 0.0 : (2.0 * recall * precision) / (recall + precision);
			double accuracy_individual = (double)(totalTokens-numPredictedSegments[t]-numTrueSegments[t] + 2*numCorrectSegments[t] )/totalTokens;

                        System.out.println (targets.lookupObject(t) + " precision="+precision+" recall="+recall+" f1="+f1 + " accuracy=" + accuracy_individual);
			System.out.println ("segments true="+numTrueSegments[t]+" pred="+numPredictedSegments[t]+" correct="+numCorrectSegments[t]+" misses="+(numTrueSegments[t]-numCorrectSegments[t])+" alarms="+(numPredictedSegments[t]-numCorrectSegments[t]) + "\n");

		}


		System.out.println("\n Confusion Matrix (row: true label, col: predicted label)");
		System.out.print("\t");
		for(int t=0; t<targets.size(); t++){
			System.out.print(targets.lookupObject(t) + "\t");
		}
		System.out.println();

		for(int t=0; t< targets.size(); t++){
			System.out.print(targets.lookupObject(t)+"\t");
			for(int tt=0; tt<targets.size(); tt++){
				System.out.print(matrixEntry[t][tt] + "\t");
			}
			System.out.println();
		}


		if(taggedOut != System.out){
			taggedOut.close();
		}

	}	
}



