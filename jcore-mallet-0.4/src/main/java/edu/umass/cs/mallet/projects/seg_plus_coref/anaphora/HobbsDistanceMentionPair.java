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

public class HobbsDistanceMentionPair extends Pipe
{
	public HobbsDistanceMentionPair ()
	{
	}

	private int calcHobbsDistance (Mention ant, Mention ref)
	{
		MalletPhrase ant_ph = ant.getMalletPhrase();
		MalletPhrase ref_ph = ref.getMalletPhrase();
		MalletSentence ant_sent = ant_ph.getSentence();
		MalletSentence ref_sent = ref_ph.getSentence();
		
		boolean startCounting = false;
		int     distance = 0;

		MalletSentence curSent = ant_sent;

		distance += countNPsFromLeft (curSent,ant_ph);
		while (curSent != ref_sent) {
			distance += countNPsFromLeft (curSent,null);
			curSent = curSent.getNext();
		}
		return distance;
	}

	private int countNPsFromLeft (MalletSentence s, MalletPhrase ph)
	{
		MalletPhrase curPhrase = s.getPhrases(); // get first element of linked list
		int phCount = 0;
		
		while ((curPhrase != null) && (curPhrase != ph))
		{
			phCount++;
			curPhrase = curPhrase.getNext();
		}
		return phCount++;
	}

	public Instance pipe (Instance carrier)
	{
		MentionPair pair = (MentionPair)carrier.getData();
		if (pair.nullPair())
		    return carrier;
		Integer distance = new Integer(calcHobbsDistance (pair.getAntecedent(), pair.getReferent()));
		String hobbsFeature = new String("HobbsDistance").concat(distance.toString()).concat("PronounGender").concat(pair.getReferent().getGender());
		pair.setFeatureValue (hobbsFeature,1);
		return carrier;
	}
	
}
