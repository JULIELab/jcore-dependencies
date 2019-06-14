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
	 @author Aron Culotta
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types;

import edu.umass.cs.mallet.base.util.PropertyList;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.PaperCitation;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.VenueCitation;

import java.util.Collection;

/** Stores a Paper, its Venue, a potential paper cluster, and a
 * potential venue cluster. Label is true iff Paper belongs in Paper
 * cluster AND Venue belongs in Venue cluster.*/
public class VenuePaperCluster {
	
	PaperCitation paper;
	VenueCitation venue;
	Collection paperCluster;
	Collection venueCluster;
	boolean label;
	
	PropertyList features = null;
	
	public VenuePaperCluster (PaperCitation paper, VenueCitation venue,
														Collection paperCluster, Collection venueCluster) {
		this.paper = paper;
		this.venue = venue;
		this.paperCluster = paperCluster;
		this.venueCluster = venueCluster;		
	}
	
	public VenuePaperCluster (PaperCitation paper, VenueCitation venue,
														Collection paperCluster, Collection venueCluster,  boolean label) {
		super ();
		this.label = label;
	}
	
	public void setLabel (boolean label) {
		this.label = label;
	}
	
	public boolean getLabel () {
		return label;
	}
	
	public PaperCitation getPaper () {
		return paper;
	}
	
	public VenueCitation getVenue () {
		return venue;
	}

	public Collection getPaperCluster () {
		return paperCluster;
	}
	
	public Collection getVenueCluster () {
		return venueCluster;
	}

	public void setFeatureValue (String key, double value)
	{
		if (features ==null) {
			features = PropertyList.add (key, value, features);
		} else {
			assert(!features.hasProperty(key));
			features = PropertyList.add (key, value, features);
		}
	}
	
	public double getFeatureValue(String key)
	{
		if (features != null)
	    return features.lookupNumber(key);
		else
	    return 0.0;
	}
	
	public PropertyList getFeatures () {
		return features;
	}	
}

