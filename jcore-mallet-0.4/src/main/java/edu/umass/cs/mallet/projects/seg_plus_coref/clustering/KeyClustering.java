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

import java.util.Iterator;

public class KeyClustering extends Clustering
{
    public KeyClustering ()
    {
	super();
    }

    public void print ()
    {
	Iterator i = this.iterator();
	while (i.hasNext()) {
	    KeyCluster c = (KeyCluster)i.next();
	    System.out.println("Cluster: " + c.getId());
	    System.out.println(c);
	    System.out.println("-----------------------\n");
	}
    }

    public void addToClustering (String id, Object item)
    {
	boolean added = false;
	Iterator i = this.iterator();
	while (i.hasNext()) {
	    KeyCluster cluster = (KeyCluster)i.next();
	    if (id.equals(cluster.getId())) {
		cluster.add(item);
		added = true;
	    }
	}
	if (!added) {
	    KeyCluster newCluster = new KeyCluster(id);
	    newCluster.add(item);
	    add(newCluster);
	}
    }
}
