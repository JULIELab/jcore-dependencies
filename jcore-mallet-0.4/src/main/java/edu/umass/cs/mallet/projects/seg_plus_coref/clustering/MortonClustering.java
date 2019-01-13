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

import java.util.*;

public class MortonClustering extends Clustering
{
    public MortonClustering ()
    {
	super();
    }

    public void addToClustering (Object item)
    {
	boolean alreadyExists = false;
	Iterator i = this.iterator();
	while (i.hasNext()) {
	    Cluster cluster = (Cluster)i.next();
	    if (cluster.contains(item)) {
		alreadyExists = true;
		break;
	    }
	}
	if (!alreadyExists) {
	    Cluster newCluster = new Cluster();
	    newCluster.add(item);
	    this.add(newCluster);
	}
    }

    public void addToClustering (Object item1, Object item2)
    {
	boolean added = false;
	Iterator i = this.iterator();
	while (i.hasNext()) {
	    Cluster cluster = (Cluster)i.next();
	    if ((cluster.contains(item1)) ||
		(cluster.contains(item2))) {
		cluster.add(item1);
		cluster.add(item2);
		added = true;
	    }
	}
	if (!added) {
	    Cluster newCluster = new Cluster();
	    newCluster.add(item1);
	    newCluster.add(item2);
	    add(newCluster);
	}	
    }

}
