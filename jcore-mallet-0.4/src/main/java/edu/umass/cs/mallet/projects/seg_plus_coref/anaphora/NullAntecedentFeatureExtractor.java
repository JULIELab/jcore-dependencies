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

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

public class NullAntecedentFeatureExtractor extends Pipe
{

	public NullAntecedentFeatureExtractor ()
	{

	}

	public Instance pipe (Instance carrier)
	{
		MentionPair pair = (MentionPair)carrier.getData();
		if (pair.nullPair())
		{
			String preTermString, prePOS, postTermString, postPOS;			
			Mention referent = pair.getReferent();
			MalletPhrase refPH = referent.getMalletPhrase();

			MalletPreTerm preLexPT = refPH.getPreceedingPreTerm();
			MalletPreTerm postLexPT = refPH.getFollowingPreTerm();
			
			if (preLexPT != null)
			{
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
			postPOS = postLexPT.getPartOfSpeech();
			if (postPOS != null)
				postPOS = postPOS.toUpperCase();
			else
				postPOS = "NULL";
			} else {
				postTermString = "NULL";
				postPOS  = "NULL";
			}
			pair.setFeatureValue (new
				String("PronounContext").concat(preTermString).concat(prePOS).concat(postTermString).concat(postPOS),1);
			pair.setFeatureValue (new
				String("pronoun").concat(refPH.getHeadPreTerm().getString().toUpperCase()), 1);
		}
		return carrier;
	}
}

