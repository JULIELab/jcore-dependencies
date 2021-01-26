/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


/** 
   @author Fuchun Peng <a href="mailto:fuchun@cs.umass.edu">fuchun@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.ie;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Sequence;
import edu.umass.cs.mallet.base.types.Token;
import edu.umass.cs.mallet.base.types.TokenSequence;

import java.io.*;
import java.util.logging.Logger;


public class POSFeaturesPipe extends Pipe implements Serializable
{

	String POSModelFile = "/usr/col/scratch1/fuchun/mallet/src/edu/umass/cs/mallet/users/fuchun/crfpos/pos_fi/tempCRF";
	boolean doTag = false;
	boolean doTagBigram = true;
	boolean doTagTrigram = true;
	boolean doTagWord = false;
	boolean POS_On = false;
	IEInterface3 posExtracter;


        private static Logger logger =
        Logger.getLogger("edu.umass.cs.mallet.users.fuchun.ace.POSFeaturesPipe");

	public POSFeaturesPipe (String POSModelFile, boolean doTag, boolean doTagBigram, boolean doTagTrigram, boolean doTagWord)
	{
		this.doTag = doTag;
		this.doTagBigram = doTagBigram;
		this.doTagTrigram = doTagTrigram;
		this.doTagWord = doTagWord;

                this.posExtracter = new IEInterface3(new File(POSModelFile));
       	        this.POS_On = posExtracter.loadCRF();

		if(!this.POS_On) logger.warning("Load CRF failed, POS feature not enabled!");
	}

	public POSFeaturesPipe (boolean doTag, boolean doTagBigram, boolean doTagTrigram, boolean doTagWord)
	{
		this.doTag = doTag;
		this.doTagBigram = doTagBigram;
		this.doTagTrigram = doTagTrigram;
		this.doTagWord = doTagWord;

                this.posExtracter = new IEInterface3(new File(POSModelFile));
     	        this.POS_On = posExtracter.loadCRF();

		if(!this.POS_On) logger.warning("POS feature not enabled!");
		
	}


	public Instance pipe (Instance carrier)
	{

		if(!POS_On) return carrier;

		TokenSequence ts = (TokenSequence) carrier.getData();

		assert(posExtracter != null);
		assert(ts != null);
		Sequence posTS = posExtracter.viterbiCRFTokenSequence((TokenSequence)ts);
		
		// the not maching size could be introduced by not maching tokenization pattern
		// the default tokenization pattern in POS tagging is "\\w+-\\w+|\\w+|'s|``|''|\\S"
		// if size not matching, ignore POS features 
		if(ts.size() != posTS.size()){
			logger.warning("Size not maching, POS feature not enabled!");
			return carrier;
		}

		String wordp1, wordp2, tagp1, tagp2;
		wordp1 = wordp2 = tagp1 = tagp2 = "<START>";

		TokenSequence tokens = (TokenSequence) carrier.getData();
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.getToken(i);
			String word = token.getText();
			String tag = posTS.get(i).toString();

			// Tag unigrams
			if (doTag) token.setFeatureValue ("T1="+tag, 1);
		
			// Tag bigrams
			if (doTagBigram) token.setFeatureValue ("T0,T1="+tagp1+','+tag, 1);

			// Tag tri-gram
			if (doTagTrigram) token.setFeatureValue ("T-1,T0,T1="+tagp2+','+tagp1+','+tag, 1);

			// Word/Tag bigrams
			if (doTagWord) token.setFeatureValue ("W0,T1="+wordp1+','+tag, 1);

			tagp2 = tagp1;
			tagp1 = tag;
			wordp2 = wordp1;
			wordp1 = word;
		}


		return carrier;


	}

	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_SERIAL_VERSION);

		out.writeObject(POSModelFile);

		out.writeBoolean(doTag);
		out.writeBoolean(doTagBigram);
		out.writeBoolean(doTagTrigram);
		out.writeBoolean(doTagWord);

	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();
		
		POSModelFile = (String)in.readObject();

		doTag = in.readBoolean();
		doTagBigram = in.readBoolean();
		doTagTrigram = in.readBoolean();
		doTagWord = in.readBoolean();

		posExtracter = new IEInterface3(new File(POSModelFile));
       	        POS_On = posExtracter.loadCRF();

		if(!POS_On) logger.warning("Load CRF failed, POS feature not enabled!");
	}
}
