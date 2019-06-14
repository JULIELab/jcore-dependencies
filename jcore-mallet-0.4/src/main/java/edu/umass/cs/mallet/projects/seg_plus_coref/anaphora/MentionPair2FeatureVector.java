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

public class MentionPair2FeatureVector extends Pipe
{
    public MentionPair2FeatureVector (Alphabet dataDict)
    {
	super (dataDict, null);
    }
    
    public MentionPair2FeatureVector ()
    {
	super (Alphabet.class, null);
    }
    
    public Instance pipe (Instance carrier)
    {
	MentionPair pair = (MentionPair)carrier.getData();
	carrier.setSource(pair); // set the source to be the pair from which it was created
	carrier.setData(new AugmentableFeatureVector
	    ((Alphabet)getDataAlphabet(), pair.getFeatures(), false));
	/*
	if (pair.getAntecedent() != null)
	    System.out.println("For: " + pair.getAntecedent().getString() + " " + pair.getReferent().getString());
	else
	System.out.println("For: " + "NULL" + " " + pair.getReferent().getString()); */
	//System.out.println("Vector: " + carrier.getData());
	return carrier;
    }
    
}
