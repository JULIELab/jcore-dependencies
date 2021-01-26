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

import edu.umass.cs.mallet.base.classify.Classification;
import edu.umass.cs.mallet.base.classify.Classifier;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.types.LabelVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class AccuracyCoverage
{
    ArrayList classifications;
    Random randGen;

    public AccuracyCoverage (Classifier classifier, InstanceList instances)
    {
	this (classifier.classify (instances));
    }
    public AccuracyCoverage (ArrayList classifications)
    {
	this.classifications = classifications;
	this.randGen = new Random();
    }

    public void sortByConfidence ()
    {
	int size = classifications.size();
	Object []classArray = classifications.toArray();
	insertSort (classArray, size);
	ArrayList newArray = new ArrayList ();
	for (int i=0; i < size; i++) {
	    newArray.add((Classification)classArray[i]);
	}
	classifications = newArray;
    }

    private double getYesVal (Classification cl)
    {
	LabelVector classDistribution = cl.getLabelVector();
	double value = 0.0;
	for (int k=0; k < classDistribution.singleSize(); k++) {
	    if (classDistribution.labelAtLocation(k).toString().equals("yes"))
		value = classDistribution.valueAtLocation(k);
	}
	return value;
    }

    private void insertSort (Object[] array, int size)
    {
	double curVal = 0.0;
	double candVal = 0.0;
	double newVal = 0.0;
	int len = size;
	for (int i=0; i < len; i++) {
	    Classification curCl = (Classification)array[i];
	    int candPosition = i;
	    curVal = getYesVal(curCl);
	    candVal = 0.0; 
	    newVal = 0.0;
	    for (int j=i+1; j < len; j++) {
		Classification newCl = (Classification)array[j];
		newVal = getYesVal(newCl);
		if (newVal > candVal) {
		    candVal = newVal;
		    candPosition = j;
		}
	    }
	    if (candVal > curVal) { // in this case, swap
		Classification tmp = (Classification)array[i];
		array[i] = array[candPosition];
		array[candPosition] = tmp;
	    }
	}
    }

    public void printClassifications ()
    {
	Iterator i = classifications.iterator();
	while (i.hasNext()) {
	    Classification c = (Classification)i.next();
	    c.print();
	}
    }
    /*
      quicksort:
        
    */
    private void quicksort (Object [] array)
    {
	
    }
}
