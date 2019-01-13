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

import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.fst.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.pipe.tsf.*;
import edu.umass.cs.mallet.base.util.*;

import junit.framework.*;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.*;
import java.io.*;
import java.util.logging.*;
import java.util.ArrayList;


public class IEInterface{
	String seperator = "";

	private static Logger logger = Logger.getLogger(IEInterface.class.getName());

	private File crfFile;
	public CRF crf = null;
	public SerialPipes pipe;

	private TokenSequence tokenSequence;
	private Sequence viterbiSequence;
	private double confidence;
	private Transducer.ViterbiPath viterbiP;
	private Transducer.ViterbiPath_NBest viterbiP_NBest;

	private int instance_error_num = 0;
	private int instance_size = 0;
	private double instance_accuracy;
	private double[] instance_accuracy_nbest;

	static boolean printFont = true;

        String PUNT = "[,\\.;:?!()*]";
        Pattern puntPattern = Pattern.compile(PUNT);
	boolean ignorePunct = true;

	public IEInterface()
	{
		this.crfFile = null;
	}

	public IEInterface(File crfFile)
	{
		assert(crfFile != null);
		this.crfFile = crfFile;
	}

	public void setPipe(SerialPipes pipe)
	{
		this.pipe = pipe;
	}

	public void replacePipe(int index, Pipe p)
	{
		assert(index < pipe.size());
		pipe.replacePipe(index,p);
	}

	public void printPipe()
	{
                ArrayList pipes1 = (this.pipe).getPipes();
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

	}

	// load in CRF and its pipe from a trained crfFile
	public boolean loadCRF()
	{
		return loadCRF(crfFile);
	}

	
	public boolean loadCRF(File crfFile)
	{

		assert(crfFile != null);

		CRF crf = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream( crfFile ));
			crf = (CRF) ois.readObject();
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
//		printPipe();

//		System.out.println("================= start of CRF ============");
//		crf.print();
//		System.out.println("==================end of crf ==============");

		//xxx

		logger.log(Level.INFO, "Load CRF successfully\n");

		return true;
	}

	public boolean loadCRF(CRF crf)
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
		return printResultInFormat(sgml, viterbiSequence, tokenSequence);
	}
	public static String printResultInFormat(boolean sgml, Sequence viterbiSequence, TokenSequence tokenSequence)
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


	public double InstanceAccuracy(Sequence viterbiSequence, Instance instance)
	{
		return InstanceAccuracy(viterbiSequence, (Sequence)instance.getTarget(), instance);
	}

	public double InstanceAccuracy(Sequence viterbiSequence, Sequence targetSequence)
	{
		return InstanceAccuracy(viterbiSequence, targetSequence, null);
	}

	public double InstanceAccuracy(Sequence viterbiSequence, Sequence targetSequence, Instance instance)
	{
		assert(viterbiSequence.size() == targetSequence.size());
		instance_size = viterbiSequence.size();
		instance_error_num = 0;

       //         String PUNT = "[,\\.;:?!()*]";
//                Pattern puntPattern = Pattern.compile(PUNT);

		if(instance != null)
			tokenSequence = (TokenSequence)instance.getSource();

		int totalNum = 0;
		for(int i=0; i<instance_size; i++){
			
			if(instance != null){
                	        String tokenStr = tokenSequence.getToken(i).getText();
        	                if(puntPattern.matcher(tokenStr).matches() && ignorePunct ){//ignore punct;
	                             continue;
                        	}
			}

			totalNum ++;
			String predO = viterbiSequence.get(i).toString();
			String trueO = targetSequence.get(i).toString();

//			System.out.println(i + " " + predO + " " + trueO + "\n");
			if(!predO.equals(trueO)){
				instance_error_num ++;
			}	
		}
	
		double accuracy = 1- (double)instance_error_num/totalNum;
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
	
		instance_accuracy= InstanceAccuracy(viterbiSequence, (Sequence)instance.getTarget(), instance);

		tokenSequence = (TokenSequence)instance.getSource(); 
		assert(viterbiSequence.size() == tokenSequence.size());

		return printResultInFormat(sgml);

	}

	public String viterbiCRFInstance_NBest(Instance instance, boolean sgml, int N )
	{

		String str = "";
		assert(crf != null);
	        tokenSequence = (TokenSequence)instance.getSource();
                assert(viterbiSequence.size() == tokenSequence.size());
	
		instance_accuracy_nbest = new double[N];

		viterbiP_NBest = crf.viterbiPath_NBest((Sequence)instance.getData(), N);//n-best list
		Sequence[] nbestlist = viterbiP_NBest.outputNBest();

		// print all N candidates
		for(int i=0; i<nbestlist.length; i++)	{
			viterbiSequence = nbestlist[i];
			str += "\n" + i + ": cost=" + (viterbiP_NBest.costNBest())[i] + " : viterbicost=" + viterbiP_NBest.getCost() + " ";

//			double tempW = viterbiP_NBest.costNBest()[i] - viterbiP_NBest.costNBest()[0];
//			double weight = Math.exp(-tempW);

			double confidence = viterbiP_NBest.confidenceNBest()[i];
			str += " confidene=" + confidence + " ";
                        instance_accuracy_nbest[i]= InstanceAccuracy(viterbiSequence, (Sequence)instance.getTarget(), instance);
//			System.out.println(instance_accuracy_nbest[i]);
			str += instance_accuracy_nbest[i] + "\n";
			str += printResultInFormat(sgml);
		}

		// print only the Nth candidate
//		viterbiSequence = nbestlist[N-1];
//		str += printResultInFormat(sgml);

             // use the combined results
       //         viterbiSequence = crf.combineNBest_fieldLevel(instance, viterbiP_NBest, null, null);
//		viterbiSequence = crf.combineNBest_fieldLevel2(instance, N, 99);
//		viterbiSequence = crf.combineNBest_fieldLevel3(instance, N, 99);

//	         str += "\ncombined result\n";
 //               str += InstanceAccuracy(viterbiSequence, (Sequence)instance.getTarget(), instance) + " \n";
   //             str += printResultInFormat(sgml);

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
				taggedOut.println(" instance accuracy= " 
					+ instance_error_num + "/" + instance_size + "=" + instance_accuracy);
				taggedOut.println(crfStr);
				viterbiStr += crfStr;



				//N-best tagging
				int N = 10;
				crfStr = viterbiCRFInstance_NBest(instance,sgml, N);
				taggedOut.println("N-best result:");
				taggedOut.println(seperator);
				taggedOut.println(crfStr);

				viterbiStr += crfStr;
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
        public void cumulativeEvaluate_InstanceLevel(File inputFile, String seperator, int N)
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

//                String PUNT = "[,\\.;:?!()*]";
//                Pattern puntPattern = Pattern.compile(PUNT);

                for(int i=0; i<instancelist.size(); i++){
                                Instance instance = instancelist.getInstance(i);

                                //N-best tagging
                                viterbiP_NBest = crf.viterbiPath_NBest((Sequence)instance.getData(), N);//n-best list
                                Sequence[] nbestlist = viterbiP_NBest.outputNBest();
				instance_accuracy_nbest = new double[N];
//				System.out.println(nbestlist.length);
		                for(int k=0; k<nbestlist.length; k++)   {
                	        	Sequence tempViterbiSequence = nbestlist[k];
                       		 	instance_accuracy_nbest[k]= InstanceAccuracy(tempViterbiSequence, (Sequence)instance.getTarget(), instance);
               			}
				
				int optimalIndex = 0;
				for(int k=1; k<nbestlist.length; k++){
//					System.out.println(i + " : " + k + " : " +  instance_accuracy_nbest[k]);
					if(instance_accuracy_nbest[k] > instance_accuracy_nbest[optimalIndex]) {
						optimalIndex = k;
		//				System.out.println(optimalIndex + " : " +  instance_accuracy_nbest[k]);
					}
				}

//				System.out.println(optimalIndex + "/" + nbestlist.length + " : " +  instance_accuracy_nbest[optimalIndex]);

                                boolean wholeInstanceCorrect = true;
                                Sequence trueSequence = (Sequence)instance.getTarget();

                                tokenSequence = (TokenSequence)instance.getSource();


                                for (int j = 0; j < trueSequence.size(); j++) {

                                        String tokenStr = tokenSequence.getToken(j).getText();
                                        if(puntPattern.matcher(tokenStr).matches() && ignorePunct ){//ignore punct;
                                                continue;
                                        }

                                        totalTokens ++;

                                        Object trueO = trueSequence.get(j);
				//	String trueO = trueSequence.get(j).toString();
                                        int trueIndex = targets.lookupIndex(trueO);
                                        numTrueSegments[trueIndex] ++;

                                        int predIndex = 0;
                                        Object predO = nbestlist[optimalIndex].get(j);
				//	String predO = nbestlist[optimalIndex].get(j).toString();
                                        predIndex = targets.lookupIndex(predO);

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


                System.out.println("\n\nAlways select the best instance evalutation results: N = " + N);

		double macro_average_p=0;
		double macro_average_r=0;
		double macro_average_f=0;
		double micro_average_p=0;
		double micro_average_r=0;
		double micro_average_f=0;
		int micro_numCorrectSegments = 0;
		int micro_numPredictedSegments = 0;
		int micro_numTrueSegments = 0;
		int classNum=0;
                for(int t=0; t<targets.size(); t++){
                        double precision = numPredictedSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numPredictedSegments[t];
                        double recall = numTrueSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numTrueSegments[t];
                        double f1 = recall+precision == 0.0 ? 0.0 : (2.0 * recall * precision) / (recall + precision);
                        double accuracy_individual = (double)(totalTokens-numPredictedSegments[t]-numTrueSegments[t] + 2*numCorrectSegments[t] )/totalTokens;

                        System.out.println (targets.lookupObject(t) + " precision="+precision+" recall="+recall+" f1="+f1 + " accuracy=" + accuracy_individual);
                        System.out.println ("segments true="+numTrueSegments[t]+" pred="+numPredictedSegments[t]+" correct="+numCorrectSegments[t]+" misses="+(numTrueSegments[t]-numCorrectSegments[t])+" alarms="+(numPredictedSegments[t]-numCorrectSegments[t]) + "\n");

			if(!targets.lookupObject(t).equals("O")){
				classNum++;
				
				macro_average_p += precision;
				macro_average_r += recall;
				macro_average_f += f1;

				micro_numCorrectSegments += numCorrectSegments[t];
				micro_numPredictedSegments += numPredictedSegments[t];
				micro_numTrueSegments +=  numTrueSegments[t];
			}
                }

		micro_average_p = (double)micro_numCorrectSegments/micro_numPredictedSegments;
		micro_average_r = (double)micro_numCorrectSegments/micro_numTrueSegments;
		micro_average_f =  micro_average_r + micro_average_p == 0.0 ? 0.0 : (2.0 * micro_average_r * micro_average_p) / (micro_average_r + micro_average_p);

		macro_average_p /= classNum;
		macro_average_r /= classNum;
		macro_average_f /= classNum;

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

		// print out the overall performance
                double accuracy = (double)numCorrectTokens/totalTokens;
                System.out.println ("\n" +" accuracy=" + numCorrectTokens +"/"+ totalTokens + " = " +accuracy);

                double wholeInstanceAccuracy = (double)numCorrectWholeInstance/instancelist.size();
                System.out.println ("Whole instance accuracy = " + numCorrectWholeInstance + "/" + instancelist.size() + " = " + wholeInstanceAccuracy);

		System.out.println("\nMacro Average");
		System.out.println("macro precision : " + macro_average_p);
		System.out.println("macro recall: " + macro_average_r);
		System.out.println("macro f : " + macro_average_f);

		System.out.println("\nMicro Average");
		System.out.println("micro precision : " + micro_average_p);
		System.out.println("micro recall: " + micro_average_r);
		System.out.println("micro f : " + micro_average_f);


/*                double accuracy = (double)numCorrectTokens/totalTokens;
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


                System.out.println();

                for(int t=0; t< targets.size(); t++){
                        System.out.print(targets.lookupObject(t)+"\t");
                        for(int tt=0; tt<targets.size(); tt++){
                                System.out.print(matrixEntry[t][tt] + "\t");
                        }
                        System.out.println();
                }
*/

        }

	
	// cumulative evaluation for N-best list 
	public void cumulativeEvaluate_TokenLevel(File inputFile, String seperator, int N)
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


//        	String PUNT = "[,\\.;:?!()*]";
//	        Pattern puntPattern = Pattern.compile(PUNT);

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
					if(puntPattern.matcher(tokenStr).matches() &&  ignorePunct ){//ignore punct;
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


		System.out.println("\n\ncumulative at token level evalutation results: N = " + N);

		double macro_average_p=0;
		double macro_average_r=0;
		double macro_average_f=0;
		double micro_average_p=0;
		double micro_average_r=0;
		double micro_average_f=0;
		int micro_numCorrectSegments = 0;
		int micro_numPredictedSegments = 0;
		int micro_numTrueSegments = 0;
		int classNum=0;
                for(int t=0; t<targets.size(); t++){
                        double precision = numPredictedSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numPredictedSegments[t];
                        double recall = numTrueSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numTrueSegments[t];
                        double f1 = recall+precision == 0.0 ? 0.0 : (2.0 * recall * precision) / (recall + precision);
                        double accuracy_individual = (double)(totalTokens-numPredictedSegments[t]-numTrueSegments[t] + 2*numCorrectSegments[t] )/totalTokens;

                        System.out.println (targets.lookupObject(t) + " precision="+precision+" recall="+recall+" f1="+f1 + " accuracy=" + accuracy_individual);
                        System.out.println ("segments true="+numTrueSegments[t]+" pred="+numPredictedSegments[t]+" correct="+numCorrectSegments[t]+" misses="+(numTrueSegments[t]-numCorrectSegments[t])+" alarms="+(numPredictedSegments[t]-numCorrectSegments[t]) + "\n");

			if(!targets.lookupObject(t).equals("O")){
				classNum++;
				
				macro_average_p += precision;
				macro_average_r += recall;
				macro_average_f += f1;

				micro_numCorrectSegments += numCorrectSegments[t];
				micro_numPredictedSegments += numPredictedSegments[t];
				micro_numTrueSegments +=  numTrueSegments[t];
			}
                }

		micro_average_p = (double)micro_numCorrectSegments/micro_numPredictedSegments;
		micro_average_r = (double)micro_numCorrectSegments/micro_numTrueSegments;
		micro_average_f =  micro_average_r + micro_average_p == 0.0 ? 0.0 : (2.0 * micro_average_r * micro_average_p) / (micro_average_r + micro_average_p);

		macro_average_p /= classNum;
		macro_average_r /= classNum;
		macro_average_f /= classNum;

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

		// print out the overall performance
                double accuracy = (double)numCorrectTokens/totalTokens;
                System.out.println ("\n" +" accuracy=" + numCorrectTokens +"/"+ totalTokens + " = " +accuracy);

                double wholeInstanceAccuracy = (double)numCorrectWholeInstance/instancelist.size();
                System.out.println ("Whole instance accuracy = " + numCorrectWholeInstance + "/" + instancelist.size() + " = " + wholeInstanceAccuracy);

		System.out.println("\nMacro Average");
		System.out.println("macro precision : " + macro_average_p);
		System.out.println("macro recall: " + macro_average_r);
		System.out.println("macro f : " + macro_average_f);

		System.out.println("\nMicro Average");
		System.out.println("micro precision : " + micro_average_p);
		System.out.println("micro recall: " + micro_average_r);
		System.out.println("micro f : " + micro_average_f);

/*
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
*/

	}

	// offline test, inputFile is labeled
	public void offLineEvaluate(File inputFile, boolean sgml, String seperator, int N)
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

		System.out.println("target size: " +  targets.size());

		System.out.print ("State labels:");
		for (int i = 0; i < targets.size(); i++)
			System.out.print (" " + targets.lookupObject(i));
		System.out.println ("");

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


//        	String PUNT = "[,\\.;:?!()*]";
//	        Pattern puntPattern = Pattern.compile(PUNT);

		String viterbiStr = "";
//		taggedOut.println("testing instance number: " + instancelist.size() );
		for(int i=0; i<instancelist.size(); i++){
//				taggedOut.println("\ntesting instance " + i);
//				System.out.println("\ntesting instance " + i);

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
				String crfStr = viterbiCRFInstance_NBest(instance,sgml, N);
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
					if(puntPattern.matcher(tokenStr).matches() && ignorePunct){//ignore punct;
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


		double macro_average_p=0;
		double macro_average_r=0;
		double macro_average_f=0;
		double micro_average_p=0;
		double micro_average_r=0;
		double micro_average_f=0;
		int micro_numCorrectSegments = 0;
		int micro_numPredictedSegments = 0;
		int micro_numTrueSegments = 0;
		int classNum=0;
                for(int t=0; t<targets.size(); t++){
                        double precision = numPredictedSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numPredictedSegments[t];
                        double recall = numTrueSegments[t] == 0 ? 1 : ((double)numCorrectSegments[t]) / numTrueSegments[t];
                        double f1 = recall+precision == 0.0 ? 0.0 : (2.0 * recall * precision) / (recall + precision);
                        double accuracy_individual = (double)(totalTokens-numPredictedSegments[t]-numTrueSegments[t] + 2*numCorrectSegments[t] )/totalTokens;

                        System.out.println (targets.lookupObject(t) + " precision="+precision+" recall="+recall+" f1="+f1 + " accuracy=" + accuracy_individual);
                        System.out.println ("segments true="+numTrueSegments[t]+" pred="+numPredictedSegments[t]+" correct="+numCorrectSegments[t]+" misses="+(numTrueSegments[t]-numCorrectSegments[t])+" alarms="+(numPredictedSegments[t]-numCorrectSegments[t]) + "\n");

			if(!targets.lookupObject(t).equals("O")){
				classNum++;
				
				macro_average_p += precision;
				macro_average_r += recall;
				macro_average_f += f1;

				micro_numCorrectSegments += numCorrectSegments[t];
				micro_numPredictedSegments += numPredictedSegments[t];
				micro_numTrueSegments +=  numTrueSegments[t];
			}
                }

		micro_average_p = (double)micro_numCorrectSegments/micro_numPredictedSegments;
		micro_average_r = (double)micro_numCorrectSegments/micro_numTrueSegments;
		micro_average_f =  micro_average_r + micro_average_p == 0.0 ? 0.0 : (2.0 * micro_average_r * micro_average_p) / (micro_average_r + micro_average_p);

		macro_average_p /= classNum;
		macro_average_r /= classNum;
		macro_average_f /= classNum;

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

		// print out the overall performance
                double accuracy = (double)numCorrectTokens/totalTokens;
                System.out.println ("\n" +" accuracy=" + numCorrectTokens +"/"+ totalTokens + " = " +accuracy);

                double wholeInstanceAccuracy = (double)numCorrectWholeInstance/instancelist.size();
                System.out.println ("Whole instance accuracy = " + numCorrectWholeInstance + "/" + instancelist.size() + " = " + wholeInstanceAccuracy);

		System.out.println("\nMacro Average");
		System.out.println("macro precision : " + macro_average_p);
		System.out.println("macro recall: " + macro_average_r);
		System.out.println("macro f : " + macro_average_f);

		System.out.println("\nMicro Average");
		System.out.println("micro precision : " + micro_average_p);
		System.out.println("micro recall: " + micro_average_r);
		System.out.println("micro f : " + micro_average_f);


/*

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

*/

		if(taggedOut != System.out){
			taggedOut.close();
		}

	}	

}



