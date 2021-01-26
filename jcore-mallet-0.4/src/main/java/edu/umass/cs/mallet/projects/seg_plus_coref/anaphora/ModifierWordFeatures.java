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

import java.util.Iterator;
import java.util.LinkedHashSet;

public class ModifierWordFeatures extends Pipe
{


	private LinkedHashSet getModifierFeatureStrings (MalletPhrase ant, String gender)
	{
		MalletPreTerm pt1 = ant.getHeadPreTerm();
		LinkedHashSet hs = new LinkedHashSet(); // set of features
		
		while ((pt1 != null) && (pt1.getMalletPhrase() == ant))
		{
			pt1 = pt1.getPrev();

			if (pt1 != null) {
				String pos = pt1.getPartOfSpeech();
				if (pos == null)
					pos = "NULL";
				hs.add(new
					String("ModifierWordsPOSGen").concat(pt1.getString().toUpperCase()).concat(pos).concat(gender));
				hs.add(new
					String("ModifierPOSGen").concat(pos).concat(gender));
			}
		}
		return hs;
	}

	public Instance pipe (Instance carrier)
	{
		MentionPair pair = (MentionPair)carrier.getData();
		if (pair.nullPair())
			return carrier;

		LinkedHashSet features = getModifierFeatureStrings
								(pair.getAntecedent().getMalletPhrase(),
								 pair.getReferent().getGender());
		Iterator i = features.iterator();
		while (i.hasNext())
		{
			pair.setFeatureValue((String)i.next(),1);
		}
		return carrier;
	}


}
