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

import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.PropertyList;
import java.util.*;

/*
	This class obtains features related to part of speech
 */
public class PartOfSpeechMentionPair extends Pipe
{

	public PartOfSpeechMentionPair ()
	{
	}
	
	public Instance pipe (Instance carrier)
	{
		String preTermString, prePOS, postTermString, postPOS, antecedentPOS, ahead;
		MentionPair pair = (MentionPair)carrier.getData();
		if (pair.nullPair())  // Do nothing if antecedent is NULL
		    return carrier;  

		Mention antecedent = pair.getAntecedent();
		MalletPhrase antPH = antecedent.getMalletPhrase();
		Mention referent   = pair.getReferent();
		MalletPhrase refPH = referent.getMalletPhrase();
		MalletPreTerm preLexPT = antPH.getPreceedingPreTerm();
		MalletPreTerm postLexPT = antPH.getFollowingPreTerm();
		
		if (preLexPT != null) {
			preTermString = preLexPT.getString();
			prePOS  = preLexPT.getPartOfSpeech();
			if (prePOS != null)
				prePOS = prePOS.toUpperCase();
			else
				prePOS = "NULL";
		} else {
			preTermString = "NULL";
			prePOS  = "NULL";
		}
		if (postLexPT != null) {
			postTermString = postLexPT.getString();
			postPOS = postLexPT.getElement().getAttributeValue("pos");
			if (postPOS != null)
				postPOS = postPOS.toUpperCase();
			else
				postPOS = "NULL";
		} else {
			postTermString = "NULL";
			postPOS  = "NULL";
		}

		MalletPreTerm antHeadPT = antPH.getHeadPreTerm();
		String featureName =
			new String("AntecedentContext").concat(preTermString).concat(prePOS).concat(postTermString).concat(postPOS);
		//System.out.println(featureName);
		if (antHeadPT != null)
			ahead = antHeadPT.getString().toUpperCase();
		else
			ahead = "NULL";
		
		String fn2 = new
			String("AntecedentHead".concat(ahead).concat("PronounGender").concat(referent.getGender()));

		if (antHeadPT != null)
			antecedentPOS = antHeadPT.getElement().getAttributeValue("pos");
		else
			antecedentPOS = null;
		
		if (antecedentPOS != null)
			antecedentPOS = antecedentPOS.toUpperCase();
		else
			antecedentPOS = "NULL";
		String fn3 = new String("AntecedentPOS".concat(antecedentPOS).concat("PronounGender").concat(referent.getGender()));
		pair.setFeatureValue (featureName, 1);
		pair.setFeatureValue (fn2, 1);
		pair.setFeatureValue (fn3, 1);
		return carrier;
	}
}
