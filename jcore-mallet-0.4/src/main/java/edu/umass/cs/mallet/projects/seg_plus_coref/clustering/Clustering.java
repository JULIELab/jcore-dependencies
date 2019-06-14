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

package edu.umass.cs.mallet.projects.seg_plus_coref.clustering;

import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.Mention;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Clustering extends LinkedHashSet
{

    List selectVertices;

    public Clustering ()
    {
	super();
    }

    public void print ()
    {
	Iterator i = this.iterator();
	while (i.hasNext()) {
	    Set c = (Set)i.next();
	    System.out.println("Cluster: ");
	    System.out.println(c);
	    System.out.println("-----------------------\n");
	}
    }

    public void setSelectVertices (List vertices) 
    {
	selectVertices = vertices;
    }

    public List getSelectVertices ()
    {
	return selectVertices;
    }

    public void printDetailed ()
    {
	Iterator i = this.iterator();
	while (i.hasNext()) {
	    Set c = (Set)i.next();
	    System.out.println("Cluster: ");
	    Iterator i2 = c.iterator();
	    while (i2.hasNext()) {
		Mention m = (Mention)i2.next();
		if (m != null)
		    System.out.println(" " + m.getString());
	    }
	    System.out.println("-----------------------\n");
	}
    }

    public boolean inSameCluster (Object o1, Object o2)
    {
	Iterator i = this.iterator();
	while (i.hasNext()) {
	    Set cl = (Set)i.next();
	    if (cl.contains(o1) && cl.contains(o2)) {
		//		System.out.println("In SAME CLUSTER");
		return true;
	    }
	}
	return false;
    }

    public void printClustering ()
    {
	System.out.println(this);
    }

}
