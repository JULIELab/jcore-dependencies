/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */




package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.AbstractStringDistance;
import com.wcohen.secondstring.Jaccard;
import edu.umass.cs.mallet.base.fst.CRF;
import edu.umass.cs.mallet.base.fst.Transducer;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.iterator.FileIterator;
import edu.umass.cs.mallet.base.pipe.iterator.LineGroupIterator;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.IEInterface;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ComputeUpperBound1 {



	String seperator = "";

	private static Logger logger = Logger.getLogger(ComputeUpperBound1.class.getName());

	private File crfFile;
	private CRF crf = null;
	private SerialPipes pipe;

	private TokenSequence tokenSequence;
	private Sequence viterbiSequence;
	private double confidence;
	private Transducer.ViterbiPath viterbiP;
	private Transducer.ViterbiPath_NBest viterbiP_NBest;

	private int instance_error_num = 0;
	private int instance_size = 0;
	private double instance_accuracy;
	private double[] instance_accuracy_nbest;

	boolean printFont = true;

	IEInterface ieInterface;

	InstanceList instancelist;
	ArrayList optimalViterbi;

	AbstractStringDistance nw;
	double default_Max_Dist = 0;
	double default_Ignore_Dist = 0;


	String[] startTags = new String[]
	{"<author>", "<title>", "<booktitle>", "<publisher>", "<journal>","<date>", "<location>", "<pages>",
	"<note>", "<institution>", "<editor>",  "<volume>", "<tech>"};

	String[] endTags = new String[]
	{"</author>", "</title>", "</booktitle>", "</publisher>", "</journal>", "</date>", "</location>", "</pages>",
	"</note>", "</institution>", "</editor>",  "</volume>", "</tech>"};

	double[] tagWeight = new double[]{1.0, 10.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};


	public ComputeUpperBound1()
	{
		this.crfFile = null;
	}

	public ComputeUpperBound1(File crfFile)
	{
		assert(crfFile != null);
		this.crfFile = crfFile;

	}

	// load in CRF and its pipe from a trained crfFile
	public boolean loadCRF()
	{
		ieInterface = new IEInterface(this.crfFile);
		boolean flag = ieInterface.loadCRF(crfFile);

		this.crf = ieInterface.crf;
		this.pipe = ieInterface.pipe;


//		nw = new NeedlemanWunsch(); // for edit distance
//		nw = new JaroWinkler();
//		nw = new CharJaccard();
		nw = new Jaccard();		
//		nw = new JelinekMercerJS();
//		nw = new TFIDF(); // x
//		nw = new Mixture(); //x
//		nw = new DirichletJS();//x


		return flag;	
	}

	
	//given an input file, label it, and output in the format of inline SGML
	public void viterbiCRF(File inputFile, boolean sgml, String seperator, int N)
	{
		instancelist = new InstanceList (pipe);

		Reader reader;
		try {
			reader = new FileReader (inputFile);
		} catch (Exception e) {
			throw new IllegalArgumentException ("Can't read file "+inputFile);
		}
		instancelist.add (new LineGroupIterator (reader, Pattern.compile(seperator), true));

		ArrayList nbestlists = new ArrayList(instancelist.size());
		for(int i=0; i<instancelist.size(); i++){
				Instance instance = instancelist.getInstance(i);

				//N-best tagging
				viterbiP_NBest = crf.viterbiPath_NBest((Sequence)instance.getData(), N);//n-best list
				nbestlists.add(i, (Sequence[]) viterbiP_NBest.outputNBest());
		}

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

		System.out.print( nbestlists.size() + ": ");

		// using approximation 
		//int[] indexList = indexListSearch_approximate(instancelist, nbestlists);

		// using exaustive search
		int[] indexList = indexListSearch_exaustive(instancelist, nbestlists, N);


	}


	protected int[] indexListSearch_exaustive(InstanceList instancelist, ArrayList nbestlists, int N)
	{
		int[] indexList = new int[instancelist.size()];

		for(int i=0; i<indexList.length; i++){
			indexList[i] = 0;
		}

		int[] optimalIndexList = (int[])indexList.clone();
		double highestWeight = weightOfConfig(indexList, instancelist, nbestlists);

		while( hasNextIndexList(indexList, N) ){
			indexList = nextIndexList(indexList, N);

//			System.out.print(num + ": " + nbestlists.size() + ": ");
//			for(int j=0; j<indexList.length; j++){
//				System.out.print(optimalIndexList[j]);
//			}
//			System.out.println();

			double weight = weightOfConfig(indexList, instancelist, nbestlists);
			if( weight > highestWeight ){
				highestWeight = weight;
				optimalIndexList = (int[])indexList.clone();
			}

		}

//		System.out.println(instancelist.size());
	
		return optimalIndexList;
	}

	protected double weightOfConfig(int[] indexList, InstanceList instancelist, ArrayList nbestlists)
	{
		double weight = 0;

		for(int i=0; i<indexList.length; i++){
                        Sequence[] lists1 = (Sequence[]) nbestlists.get(i);

			for(int j=i+1; j<indexList.length; j++){
	                        Sequence[] lists2 = (Sequence[]) nbestlists.get(j);

                                double sim = PairSimilarity(lists1[indexList[i]], lists2[indexList[j]],
                                           instancelist.getInstance(i), instancelist.getInstance(j));

				weight += sim;					
			}
		}

		return weight;
	}

	protected boolean hasNextIndexList(int[] indexList, int N)
	{
		for(int i=0; i<indexList.length; i++){
			if(indexList[i] < N-1) return true;
		}

		return false;
	}

	protected int[] nextIndexList(int[] indexList, int N)
	{
		for(int i=indexList.length-1; i>=0; i--){
			if(indexList[i] <= N-2){
				indexList[i] ++;

				for(int j=i+1; j<=indexList.length-1;j++){
					indexList[j] = 0;
				}

				break;
			}
		}
		
		return indexList;
	}

	protected int[] indexListSearch_approximate(InstanceList instancelist, ArrayList nbestlists)
	{
		int[] indexList = new int[instancelist.size()];

		System.out.println(instancelist.size());
	
		if(instancelist.size() == 1){
			indexList[0] = 0;
		}
		else if(instancelist.size() == 2){

			Sequence[] lists1 = (Sequence[]) nbestlists.get(0);
			Sequence[] lists2 = (Sequence[]) nbestlists.get(1);
			double highestSimilarity = Double.NEGATIVE_INFINITY;
			indexList[0] = indexList[1] = 0;
			for(int i=0; i<lists1.length; i++){
				for(int j=0; j<lists2.length; j++){
					double sim = PairSimilarity(lists1[i], lists2[j], 
							instancelist.getInstance(0), instancelist.getInstance(1));
					
					if(sim > highestSimilarity){
						highestSimilarity = sim;
						indexList[0] = i;
						indexList[1] = j;
					}
				}
			}
		}
		else {
			//process the first two citations
			Sequence[] lists1 = (Sequence[]) nbestlists.get(0);
			Sequence[] lists2 = (Sequence[]) nbestlists.get(1);
			double highestSimilarity = Double.NEGATIVE_INFINITY;
			indexList[0] = indexList[1] = 0;
			for(int i=0; i<lists1.length; i++){
				for(int j=0; j<lists2.length; j++){
					double sim = PairSimilarity(lists1[i], lists2[j],
							instancelist.getInstance(0), instancelist.getInstance(1) );
					
					if(sim > highestSimilarity){
						highestSimilarity = sim;
						indexList[0] = i;
						indexList[1] = j;
					}
				}
			}

			//dynamically process the rest citations
			for(int i=2; i<instancelist.size(); i++){
				indexList[i] = 0;
				Sequence[] sequence_prev = (Sequence[]) nbestlists.get(i-1);
				Sequence[] sequence_current = (Sequence[]) nbestlists.get(i);
				highestSimilarity = PairSimilarity(sequence_prev[indexList[i-1]], sequence_current[0],
							instancelist.getInstance(i-1), instancelist.getInstance(i) );
				for(int j=1; j<sequence_current.length; j++){
					double sim = PairSimilarity(sequence_prev[indexList[i-1]], sequence_current[j],
							instancelist.getInstance(i-1), instancelist.getInstance(i) );	
					if(sim > highestSimilarity){
						indexList[i] = j;
					}
				}
			}
		}

		return indexList;

	}

	protected double computeSGMLObjDistance(String string1, String string2)
	{

		double dist = 0.0;
		double distTemp;
		int usedNumFields = 0;

		int NumFields = startTags.length;

		double totalWeight = 0;
//		System.out.println(string1 + "\n" + string2);
		for(int i=0; i<NumFields; i++){
			String[] strs1 = locateFields(startTags[i], endTags[i], string1);
			String[] strs2 = locateFields(startTags[i], endTags[i], string2);

/*			if( startTags[i].equals("<author>") ){//only use last names

				if(strs1 != null)
				for(int k=0; k<strs1.length; k++){

					ArrayList namelist1 = LastName(strs1[k]);
					String tempStr = "";
					for(int j=0; j<namelist1.size(); j++){
						tempStr += (String)namelist1.get(j);
						if( j<namelist1.size()-1){
							tempStr += " ";
						}
					}
					strs1[k] = tempStr;
				}
				
				if(strs2 != null)
				for(int k=0; k<strs2.length; k++){	

					ArrayList namelist2 = LastName(strs2[k]);
					String tempStr = "";
					for(int j=0; j<namelist2.size(); j++){
						tempStr += (String)namelist2.get(j);
						if( j<namelist2.size()-1){
							tempStr += " ";
						}
					}
					strs2[k] = tempStr;
					
				}
			}
*/
			String str1 = "";
			if(strs1 != null)
				for(int j=0; j<strs1.length; j++){
					str1 += strs1[j];
	
					if( j<strs1.length - 1 )
						str1 += " ";
				}

			String str2 = "";
			if(strs2 != null)
				for(int j=0; j<strs2.length; j++){
					str2 += strs2[j];
		
					if( j<strs2.length - 1 )
						str2 += " ";
				}

			distTemp = tagWeight[i] * computeStringDistance(str1, str2);
//			distTemp = Math.abs(distTemp);
			totalWeight += tagWeight[i];
			dist += distTemp;
			if(distTemp != 0){
				usedNumFields ++;
			}

//                      if(startTags[i].equals("<author>"))
                                System.out.println(startTags[i] + ": " + str1 + " : " + str2 + " : " + distTemp + " : " + usedNumFields + " : " + dist);
		}

//		dist /= usedNumFields;
//		dist /= totalWeight;

		System.out.println(dist + " : " + usedNumFields);

//		dist = Math.exp(dist);

		return dist;
	}

	protected ArrayList LastName(String ss)
	{
//		System.out.println("ss=" + ss);

		ArrayList names = new ArrayList();

		if(ss == null){
			return names;
		}

		ss = ss.replaceAll(" \\w\\.", "");
		ss = ss.replaceAll("\\s\\w\\s\\.", "");

		ss = ss.replaceAll("^\\w\\.", "");
		ss = ss.replaceAll("^\\w\\s\\.", "");

		ss = ss.replaceAll(" and", " ,");

		ss = ss.replaceAll("\\.$", "");


//		System.out.println(ss);

		String[] authors = ss.split(",");
		String last_name;
		for(int i=0; i<authors.length; i++){
			String author = authors[i];
			author = author.replaceAll("^\\s+|\\s+$", "");
			String[] first_last_name = author.split(" ");
			if(first_last_name.length == 2){
				last_name = first_last_name[1];
			}
			else if(first_last_name.length == 1){
				last_name = first_last_name[0];
			}
			else {
//				System.out.println(ss);
//				throw new UnsupportedOperationException(author);
				last_name = first_last_name[first_last_name.length-1];
			}
					
			if(!last_name.equals("")){
//				System.out.println(i+": \""+last_name+"\"");
				names.add(last_name);
			}
		}
		
		return names;
	}


//	protected String locateFields(String startTag, String endTag, String string)
	protected String[] locateFields(String startTag, String endTag, String string)
	{
		int indexStart = string.indexOf(startTag);
		int indexEnd   = string.indexOf(endTag, indexStart);
		
		if(indexStart == -1 || indexEnd == -1){
			return null;
		}
		else{
			ArrayList strlist = new ArrayList();
			while(indexStart != -1 && indexEnd != -1){
				String str = string.substring(indexStart+startTag.length(), indexEnd-1);
				strlist.add(str);

				indexStart = string.indexOf(startTag, indexEnd);
				indexEnd = string.indexOf(endTag, indexStart);
			}

			String[] strs = new String[strlist.size()];
			for(int i=0; i<strlist.size(); i++){
				strs[i] = (String)strlist.get(i);
			}

			return strs;
		}
	} 

	protected double computeStringDistance(String str1, String str2)
	{
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();

		if(str1.length() > 0 && str2.length() > 0 ){
			return nw.score(str1, str2);
		}
		else if(str1.length() == 0 && str2.length() == 0){
			return default_Ignore_Dist;
		}	
		else{
			return default_Max_Dist;
		}


//		return nw.score(str1, str2);

	}

	public double PairSimilarity(Sequence sequence1, Sequence sequence2, 
			Instance instance1, Instance instance2)
	{
		
		TokenSequence tokenSequence1 = (TokenSequence)(instance1.getSource());	
		TokenSequence tokenSequence2 = (TokenSequence)(instance2.getSource());

		String str1 = ieInterface.printResultInFormat(true, sequence1, tokenSequence1);
		String str2 = ieInterface.printResultInFormat(true, sequence2, tokenSequence2);

		double sim1 =  computeSGMLObjDistance(str1, str2);
//		double sim2 =  computeSGMLObjDistance(str2, str1);
//		double sim = (sim1+sim2)/2;

		double sim = sim1;		
		System.out.println(str1 + "\n" + str2 + " : " + sim + "\n");

		return sim;

	}

	//viterbi for all files under a given directory, 
	//if the given directory is a plain file, viterbi for this file
	public void viterbiCRF(String inputDir, boolean sgml, String seperator, int N)
	{

		// variables for performance measurement
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

                String PUNT = "[,\\.;:?!()*]";
                Pattern puntPattern = Pattern.compile(PUNT);
		boolean ignorePunct = true;

		// if inputDir is a plain file
		instancelist = new InstanceList (pipe);
		optimalViterbi = new ArrayList();
	
		int totalInstanceNum = 0;

		System.out.println(inputDir); 
		File file = new File(inputDir);
		if( file.isFile() ){
			viterbiCRF(file, sgml, seperator, N);
		}
		else{
			// continue if it is a directory
			FileIterator fileIter = new FileIterator (inputDir);	
			ArrayList fileList = fileIter.getFileArray();

			for(int i=0; i<fileList.size(); i++){
				file = (File) fileList.get(i);
				viterbiCRF(file, sgml, seperator, N);

				totalInstanceNum += instancelist.size();
         		        for(int k=0; k<instancelist.size(); k++){
                         	        Instance instance = instancelist.getInstance(k);

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
                                        	int trueIndex = targets.lookupIndex(trueO);
                        		        numTrueSegments[trueIndex] ++;

                                		Object predO = ((Sequence)optimalViterbi.get(k)).get(j);
                                    		int predIndex = targets.lookupIndex(predO);

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
			}
		}


		// print out the performance
                double accuracy = (double)numCorrectTokens/totalTokens;
                System.out.println ("\n" +" accuracy=" + numCorrectTokens +"/"+ totalTokens + " = " +accuracy);

                double wholeInstanceAccuracy = (double)numCorrectWholeInstance/totalInstanceNum;
                System.out.println ("Whole instance accuracy = " + numCorrectWholeInstance + "/" + totalInstanceNum + " = " + wholeInstanceAccuracy);

		System.out.println("targets size = " + targets.size());
		System.out.print ("State labels:");
		for (int i = 0; i < targets.size(); i++)
			System.out.print (" " + targets.lookupObject(i));
		System.out.println ("");

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

	public void viterbiCRF(String inputDir, int N)
	{
		viterbiCRF(inputDir, true, N);
	}

	public void viterbiCRF(String inputDir, boolean sgml, int N)
	{
		viterbiCRF(inputDir, sgml, seperator, N);
	}

	public static void main (String[] args) {

		File f = new File ("/tmp/wellner/crfs/CRF_face");
		ComputeUpperBound1 c = new ComputeUpperBound1 (f);
		c.loadCRF();

	}


}
