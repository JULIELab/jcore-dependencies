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

public class MentionPairAntecedentPosition extends Pipe
{
	public MentionPairAntecedentPosition ()
	{
	}

	private int calcNPPosition (MalletPhrase m)
	{
		int position = 1;
		while (m != null) {
			m = m.getPrev();
			position++;			
		}
		return position;
	}
	
	public Instance pipe (Instance carrier)
	{
		MentionPair pair = (MentionPair)carrier.getData();
		if (pair.nullPair())
		    return carrier;
		Integer position = new Integer (calcNPPosition (pair.getAntecedent().getMalletPhrase()));
		pair.setFeatureValue (new String("AntecedentPosition").concat(position.toString()), 1);
		return carrier;
	}
}
