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

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class ClusterEvaluate
{
	Collection keyClusters;
	Collection responseClusters;

	Collection correctClusters;
	Collection missedClusters;
	Collection remainingClusters;

	public ClusterEvaluate (Collection responseClusters) {
		this.responseClusters = (Collection)copyCollection(responseClusters);
		System.out.println("Size of responses: " + responseClusters.size());
		this.keyClusters = null;
	}

	public ClusterEvaluate (Collection keyClusters, Collection responseClusters)
	{
		this.keyClusters = (Collection)copyCollection(keyClusters);
		this.responseClusters = (Collection)copyCollection(responseClusters);

		//System.out.println("Key clusters:");
		//ppCollection(keyClusters);
		//System.out.println("Key raw : " + keyClusters);
		//System.out.println("Size of key: " + keyClusters.size());
		//System.out.println("Response clusters:");
		//ppCollection(responseClusters);
		//System.out.println("Response raw : " + responseClusters);
		//System.out.println("Size of responses: " + responseClusters.size());

	}

	private Collection copyCollection (Collection s)
	{
		Collection newS = new LinkedHashSet ();
		Iterator i = s.iterator();
		while (i.hasNext()) {
	    newS.add(i.next());
		}
		return newS;	
	}


	private boolean equalCollections (Collection sp1, Collection sp2)
	{
		Collection s1 = copyCollection(sp1);
		Collection s2 = copyCollection(sp2); // don't want to destroy these

		Iterator i = s1.iterator();
		while (i.hasNext()) {
	    Object o = (Object)i.next();
	    if (!s2.remove(o))
				return false;
		}
		return s2.isEmpty();
	}

	public void ppCollection (Collection col) {
		ppCollection (col, System.out);
	}
	
	public void ppCollection (Collection col, PrintStream ps) {
		Iterator i1 = col.iterator();
		ps.println("------------------------------------------");
		ps.println("------------------------------------------");
		while (i1.hasNext()) {
			Collection c1 = (Collection)i1.next();
			Iterator i2 = c1.iterator();
			ps.println("------------------");
			while (i2.hasNext()) {
				//Citation n = (Citation)i2.next(); // xxx assume objects are nodes
				//ps.print("  " + n.getIndex());
				//ps.print("  " + n.getIndex());
				ps.println (" " + i2.next());
			}
			ps.println();
		}
	}


	public void evaluate ()
	{
		if ((keyClusters != null)  && (responseClusters != null)) {
			Collection correctClusters = new LinkedHashSet();
			Collection missedClusters = new LinkedHashSet();
			Collection remainingClusters = (Collection)copyCollection (responseClusters);

			Iterator keyI = keyClusters.iterator();
	
			while (keyI.hasNext()) {
				Collection kS = (Collection)keyI.next();
				Collection rS = null;
				boolean kSCorrect = false;
				Iterator responseI = responseClusters.iterator();
				while (responseI.hasNext()) {
					rS = (Collection)responseI.next();
					if (equalCollections (kS, rS)) {
						//System.out.println(kS);
						//System.out.println(" -- equal to -- ");
						//System.out.println(rS);
						kSCorrect = true;
						correctClusters.add(rS);
						remainingClusters.remove(rS);
					}
				}
				if (!kSCorrect) {
					missedClusters.add(kS);
				} 
			}
			this.correctClusters = correctClusters;
			this.missedClusters = missedClusters;
			this.remainingClusters = remainingClusters;
		} else {
			System.out.println("Error: No keyClusters have been provided to evaluate against");
		}
	}

    public double getPrecision ()
    {
        return ((double)correctClusters.size() / (double)(correctClusters.size() + remainingClusters.size()));
    }

    public double getRecall ()
    {
        return ((double)correctClusters.size() / (double)(correctClusters.size() + missedClusters.size()));
    }

	public void printResults () {
		System.out.println("Recall: " + getRecall());
		System.out.println("Precision: " + getPrecision());
		System.out.println("F1: " + getF1());
	}

	public int sizeOfClustering (Collection c) {

		int size = 0;
		Iterator i = c.iterator();
		while (i.hasNext()) {
			size += ((Collection)i.next()).size();
		}
		return size;
	}

	public void printVerbose () {
		printVerbose (System.out);
	}
	
	public void printVerbose (PrintStream outputStream) {
		int sumSize = 0;
		System.out.println("Response: ");
		ppCollection(responseClusters);
		outputStream.println("Number of elements in response: " + sizeOfClustering(responseClusters));
		sumSize += sizeOfClustering(correctClusters);
		sumSize += sizeOfClustering(missedClusters);
		sumSize += sizeOfClustering(remainingClusters);
		outputStream.println("Number of elements over correct/missed/remaining: " + sumSize);
		outputStream.println("Correct:--------------");
		ppCollection(correctClusters);
		outputStream.println("Missed:--------------");		
		ppCollection(missedClusters);
		outputStream.println("Remaining:--------------");				
		ppCollection(remainingClusters);
	}

	public double getF1 ()
	{
		//System.out.println("\n\ncorrect: " + correctClusters.size());
		//ppCollection(correctClusters);
		//System.out.println("\n\nmissed: " + missedClusters.size());
		//ppCollection(missedClusters);
		//System.out.println("\n\nremaining: " + remainingClusters.size());
		//ppCollection(remainingClusters);
		double recall = ((double)correctClusters.size() / (double)(correctClusters.size() + missedClusters.size()));
		double precision = ((double)correctClusters.size() / (double)(correctClusters.size() + remainingClusters.size()));
		if ((recall > 0.0) && (precision > 0.0))
	    return ((2 * recall * precision) / (recall + precision));
		else
	    return 0.0;
	}

	public void printErrors (boolean detailed)
	{
		Iterator missed = missedClusters.iterator();
		System.out.println("Missed Clusters: ---------");
		while (missed.hasNext()) {
	    Collection cl = (Collection)missed.next();
	    if (!detailed)
				System.out.println("cluster: " + cl);
	    else {
				Iterator i1 = cl.iterator();
				System.out.println("cluster: ");
				while (i1.hasNext()) {
					Mention m1 = (Mention)i1.next();
					System.out.println("  - " + m1.getString());
				}
	    }
		}
		Iterator spurious = remainingClusters.iterator();
		System.out.println("Spurious clusters: +++++++++");
		while (spurious.hasNext()) {
	    Collection cl = (Collection)spurious.next();
	    if (!detailed) 
				System.out.println("cluster: " + cl);
	    else {
				Iterator i2 = cl.iterator();
				System.out.println("cluster: ");
				while (i2.hasNext()) {
					Mention m2 = (Mention)i2.next();
					System.out.println("  - " + m2.getString());
				}
	    }
		}
	}

	public static void main (String[] args)
	{
		Cluster s1 = new Cluster ();
		Cluster s2 = new Cluster ();
		Cluster s3 = new Cluster ();
	
		Cluster r1 = new Cluster ();
		Cluster r2 = new Cluster ();
		Cluster r3 = new Cluster ();
	
		String str1 = new String("A");
		String str2 = new String("B");
		String str3 = new String("C");
		String str4 = new String("D");
	
		s1.add(str1);
		s1.add(str4);
		s2.add(str2);
		s3.add(str3);
	
		r1.add(str1);
		r1.add(str4);
		r2.add(str2);
		r3.add(str3);

		Clustering set1 = new Clustering();
		Clustering set2 = new Clustering();
	
		set1.add(s2);
		set1.add(s1);
	
		set2.add(r1);
		set2.add(r2);
	
		ClusterEvaluate eval = new ClusterEvaluate (set1, set2);
		eval.evaluate();
		double f1 = eval.getF1();
		System.out.println("F1: " + f1);
	}
}

