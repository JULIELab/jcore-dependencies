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
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;
import salvo.jesus.graph.*;
import java.util.Comparator;

public class CompareVertices implements Comparator
{
    public CompareVertices ()
    {

    }

    public int compare (Object o1, Object o2)
    {
	VertexImpl v1 = (VertexImpl)o1;
	VertexImpl v2 = (VertexImpl)o2;
	if ((v1.getObject()) instanceof Comparable)
	    return (((Comparable)v1.getObject()).compareTo(((Comparable)v2.getObject())));
	else
	    return 0;
    }
}
