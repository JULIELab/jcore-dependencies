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

import java.util.List;
import java.util.Iterator;

import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.util.PropertyList;
import org.jdom.*;

public class LinearDistanceMentionPair extends Pipe
{
	public LinearDistanceMentionPair ()
	{
	}


	public class CharacterCounter
	{
		private int numCharacters;
		private boolean startCount;

		public CharacterCounter ()
		{
			numCharacters = 0;
			startCount = false;
		}

		public int lengthOfCharactersBetween (Element parent, Element node1, Element node2)
		{
			count (parent, node1, node2);
			return numCharacters;
		}

		private void count (Element parent, Element node1, Element node2)
		{
			List children = parent.getContent();
			Iterator iterator = children.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (o instanceof Element) {
					if ((Element)o == node1) {
						startCount = true;
					}
					if ((Element)o == node2) {
						startCount = false;
						break;              // we've reached the second node, and can therefore stop
					}
					count ((Element) o, node1, node2);
				}
				else if ((o instanceof Text) && startCount)
					numCharacters += ((Text)o).getText().length();
			}
		}
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

	private int calcLinearDistance (Element node1, Element node2)
	{
		Element mutualParent = findLeastCommonParent (node1, node2);
		int     numChars = 0;
		if (mutualParent != null) {
			CharacterCounter counter = new CharacterCounter();
			numChars = counter.lengthOfCharactersBetween (mutualParent, node1, node2);
			return numChars;
		} else { return 0; }
	}

	public Instance pipe (Instance carrier)
	{
		MentionPair pair = (MentionPair)carrier.getData();
		int distance = calcLinearDistance (pair.getAntecedent().getElement(), pair.getReferent().getElement());
		pair.setFeatureValue ("LinearDistance", (double)distance);
		return carrier;
	}
}

	
																							 
