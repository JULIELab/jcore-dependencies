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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionPairSubstring extends Pipe
{
    public MentionPairSubstring ()
    {
    }

    public Instance pipe (Instance carrier)
    {
	MentionPair pair = (MentionPair)carrier.getData();
	Mention ant = pair.getAntecedent();
	Mention ref = pair.getReferent();
	if (ant != null) {
	    String antString = ant.getString();
	    String header = new String(".*");
	    String refString = ref.getString();
	    Pattern antPat = Pattern.compile(header.concat(antString).concat(new String(".*")));
	    Pattern refPat = Pattern.compile(header.concat(refString).concat(new String(".*")));
	    Matcher m1 = antPat.matcher(refString);
	    Matcher m2 = refPat.matcher(antString);
	    if ((m1.matches()) || (m2.matches())) {
		pair.setFeatureValue(new String("Substring"), 1.0);
	    }
	}
	return carrier;
    }


}
