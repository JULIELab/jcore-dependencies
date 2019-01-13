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

import salvo.jesus.graph.*;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.types.*;
import java.io.*;
import java.util.*;

public class PairEvaluate
{

	Collection keyClusters;
	Collection responseClusters;
	HashMap responseClustersTotal;
	double  recall;
	double  precision;

	public PairEvaluate (Collection keyClusters, Collection responseClusters)
	{
		this.keyClusters = copyCollection(keyClusters);
		this.responseClusters = copyCollection(responseClusters);
		responseClustersTotal   = new HashMap();
		computeResponseTotals();
	}

	private void computeResponseTotals()
	{
		Iterator resI = responseClusters.iterator();
		while (resI.hasNext()) {
	    Collection cl1 = (Collection)resI.next();
	    int pairSize = triangle(cl1.size());
	    //System.out.println("Setting total pairs of " + cl1 + " to " + pairSize);
	    responseClustersTotal.put(cl1,new Integer(pairSize));
		}   
	}

	private int triangle (int startInt)
	{
		int i = 0;
		for (int j=0;j < startInt; j++) {
	    for (int k=j+1; k < startInt; k++) {
				i++;
	    }
		}
		return i;
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

	public void evaluate ()
	{
		Iterator keyI = keyClusters.iterator();
		int allPairs = 0;
		int matchingPairs = 0;
		int allSingletons = 0;
		int matchingSingletons = 0;
		while (keyI.hasNext()) {
	    Collection cl1 = (Collection)keyI.next();
	    if (cl1.size() == 1) {
				allSingletons++;
				Iterator i1 = cl1.iterator();
				if (singletonCluster (i1.next()))
					matchingSingletons++;
	    }
	    else {
				Object clArray[] = cl1.toArray();
				for (int i=0; i<cl1.size(); i++) {
					for (int j=i+1; j< cl1.size(); j++) {
						if (inSameResponseCluster (clArray[i],clArray[j])) {
							matchingPairs++;
						}
						allPairs++;
					}
				}
	    }
		}
		if ((allPairs + allSingletons) > 0) {
	    recall = ((double)matchingPairs + (double)matchingSingletons)/ ((double)allPairs + (double)allSingletons);
	    //System.out.println("Pairwise Recall: " + recall);
		}
		else {
	    //System.out.println("Setting recall to zero because allPairs is empty");
	    recall = 0.0;
		}
		Iterator i2 = responseClusters.iterator();
		int numExtra = 0;
		while (i2.hasNext()) {
	    Collection s1 = (Collection)i2.next();
	    //System.out.println("Pair size of " + s1 + " is " + (Integer)responseClustersTotal.get(s1));
	    numExtra += ((Integer)responseClustersTotal.get(s1)).intValue();
		}
	
		if ((matchingPairs + numExtra + matchingSingletons) > 0) {
	    precision = ((double)matchingPairs + matchingSingletons) / 
									(double)(matchingPairs + matchingSingletons + numExtra);
	    //System.out.println("Pairwise Precision: " + precision);
		}
		else {
	    //System.out.println("Setting precision to zero because matchingPairs+numExtra is zero");
	    precision = 0.0;
		}
	

	}

	public double getF1 ()
	{
		if ((precision+recall) > 0)
	    return (2.0 * precision * recall) / (precision + recall);
		else 
	    return 0.0;
	}

	public double getRecall ()
	{
		return recall;
	}

	public double getPrecision ()
	{
		return precision;
	}

	public boolean singletonCluster (Object o1)
	{
		Iterator resI = responseClusters.iterator();
		while (resI.hasNext()) {
	    Collection cl1 = (Collection)resI.next();
	    if ((cl1.contains(o1)) && (cl1.size() == 1))
				return true;
		}
		return false;
	}

	public boolean inSameResponseCluster (Object o1, Object o2)
	{
		Iterator resI = responseClusters.iterator();
		while (resI.hasNext()) {
	    Collection cl1 = (Collection)resI.next();
	    if ((cl1.contains(o1)) && (cl1.contains(o2))) {
				Integer curCnt = (Integer)responseClustersTotal.get(cl1);
				int cnt = curCnt.intValue();
				int newCnt = cnt - 1;
				responseClustersTotal.put(cl1,new Integer(newCnt));
				return true;
	    }
		}
		return false;
	}


}
