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
import edu.umass.cs.mallet.base.types.Alphabet;
import edu.umass.cs.mallet.base.types.AugmentableFeatureVector;
import edu.umass.cs.mallet.base.types.Instance;

public class MentionPair2FeatureVectorFilter extends Pipe
{
    public MentionPair2FeatureVectorFilter (Alphabet dataDict)
    {
	super (dataDict, null);
    }

    public MentionPair2FeatureVectorFilter ()
    {
	super (Alphabet.class, null);
    }

    private boolean validPair (MentionPair pair)
    {
	Mention ref = pair.getReferent();
	Mention ant = pair.getReferent();
	if (((!MentionPairIterator.referentProperNoun(ref)) && (!MentionPairIterator.referentPronoun (ref))) ||
	    ((ant != null) && 
	     (!MentionPairIterator.referentProperNoun(ant)) && (!MentionPairIterator.referentPronoun (ant))))
	    return false;
	else
	    return true;
    }
    
    public Instance pipe (Instance carrier)
    {
	MentionPair pair = (MentionPair)carrier.getData();
	carrier.setSource(null); // set the source to be the pair from which it was created
	if (validPair (pair)) {
	    carrier.setData(new AugmentableFeatureVector
		((Alphabet)getDataAlphabet(), pair.getFeatures(), false));
	    carrier.setSource(pair);
	}
	else {
	    carrier.setSource(null);
	    carrier.setData(null);
	}
	return carrier;
    }
}
