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

package edu.umass.cs.mallet.projects.seg_plus_coref.graphs;

import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.CompareMentions;
import salvo.jesus.graph.VertexImpl;

import java.util.Comparator;

// NOTE: This comparator imposes orderings inconsistent with equals
public class CompareMentionVertices implements Comparator
{

    CompareMentions mentionComparator;
    public CompareMentionVertices ()
    {
	mentionComparator = new CompareMentions();
    }
    
    public int compare (Object v1, Object v2)
    {
	return mentionComparator.compare (((VertexImpl)v1).getObject(),((VertexImpl)v2).getObject());
    }
}
