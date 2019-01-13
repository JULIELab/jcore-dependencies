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

import java.util.Iterator;
import java.util.List;

import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.PropertyList;
import org.jdom.*;

public class MentionPairSentenceDistance extends Pipe
{
	public MentionPairSentenceDistance ()
	{
	}

	public class SentenceCounter
	{
		private int numSentences;
		private boolean startCount;
		
		public SentenceCounter ()
		{
			numSentences = 0;
			startCount = false;
		}
		
		public int lengthOfSentencesBetween (Element parent, Element node1, Element node2)
		{
			count (parent, node1, node2);
			return numSentences;
		}

		private void count (Element parent, Element node1, Element node2)
		{
			List children = parent.getContent();
			Iterator iterator = children.iterator();
			Element el;
			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (o instanceof Element)
				{
					el = (Element)o;
					if (startCount && (el.getName().equals("S") || el.getName().equals("s")))
						numSentences++;
					if (el == node1)
						startCount = true;
					if (el == node2) {
						startCount = false;
						break;              // we've reached the second node, and can therefore stop
					}
					count (el, node1, node2);
				}
			}
		}
	}

	private int calcSentenceDistance (Element node1, Element node2)
	{
		Element mutualParent = findLeastCommonParent (node1, node2);
		int     numChars = 0;
		if (mutualParent != null) {
			SentenceCounter counter = new SentenceCounter();
			numChars = counter.lengthOfSentencesBetween (mutualParent, node1, node2);
			return numChars;
		} else { return 0; }
	}
		
	private Element findLeastCommonParent (Element node1, Element node2)
	{
		Element parent1 = node1;
		Element parent2 = node2;
		while ((parent1 != parent2) && (parent1 != null) && (parent2 != null)) {
			while ((parent2 != parent1) && parent2 != null)
				parent2 = (Element) parent2.getParent();
			if (parent2 != parent1) {
				parent1 = (Element) parent1.getParent();  // increment to next parent
				parent2 = node2;               // resent to orignal node
			}
		}
		if ((parent1 == null) || (parent2 == null))
			return null;
		else
			return parent1;
	}

	public Instance pipe (Instance carrier)
	{
		MentionPair pair = (MentionPair)carrier.getData();
		if (!pair.nullPair()) {
			Integer distance = new Integer
				(pair.getReferent().getMalletPhrase().getSentence().getIndex() -
				 pair.getAntecedent().getMalletPhrase().getSentence().getIndex());
		    String featureName = new String("SentenceDistance").concat(distance.toString()).concat("Pronoun").concat(pair.getReferent().getString().toUpperCase());
		    pair.setFeatureValue (featureName, 1);
		}
		return carrier;
	}

}
