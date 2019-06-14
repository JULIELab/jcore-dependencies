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

import edu.umass.cs.mallet.base.util.MalletLogger;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.Citation;

import java.util.*;
import java.util.logging.Logger;

/** Given two clusterings of related object, constrain one based on
 * the clustering of another. E.g., given a clustering of papers and a
 * clustering of venues, constrain the paper clusterings to respect
 * the venues, and vice-versa.*/
public class ConstrainedClusterer {

	private static Logger logger = MalletLogger.getLogger(ConstrainedClusterer.class.getName());

	/** Original cluster that will be constrained*/
	Collection ogc;
	
	public ConstrainedClusterer (Collection originalCluster) {
		this.ogc = originalCluster;
	}

	/** Constrain <code>ogc</code> such that nodes that resolve to the
	 * same paper are forced to have the same venue. Here, ogc is a
	 * cluster of venues */
	public Collection constrainByPapers (Collection paperCluster) {
		HashMap paperID2venueClusterID = createPaper2VenueHash (ogc);
		HashMap venueClusterID2venueCluster = createVenueClusterID2VenueClusterHash (ogc);
		// if papers coreferent, venues must be
		ArrayList venuesToMerge = new ArrayList();
		Iterator citer = paperCluster.iterator();
 		while (citer.hasNext()) { // for each paper cluster
	 		Collection cl = (Collection) citer.next();
		 	Iterator csubiter = cl.iterator();
			HashSet venueIDsThisCluster = new HashSet ();
		 	while (csubiter.hasNext()) {
		 		Citation cit = (Citation) csubiter.next();
			 	String paperID = cit.getField (Citation.paperID);
			 	String venueCID = (String) paperID2venueClusterID.get (paperID);
			 	if (venueCID == null) {
			 		logger.info("no venue id stored in hash for " + paperID + "\n" + cit);
				}
 				else {
				 	logger.info ("Found venue cluster for paper id " + paperID);
					venueIDsThisCluster.add (venueCID);
				}
			}
			// merge venues into one cluster
			mergeSets (venuesToMerge, venueIDsThisCluster);			
		}
		// create new clustering based on "venuesToMerge" clusters
 		ArrayList newClustering = new ArrayList ();
		Iterator viter = venuesToMerge.iterator();
		while (viter.hasNext()) {
			HashSet set = (HashSet) viter.next();
			ArrayList cluster = new ArrayList ();
			Iterator siter = set.iterator();
			while (siter.hasNext()) {
				ArrayList vcluster = (ArrayList) venueClusterID2venueCluster.get
														 ((String)siter.next());				
				if (vcluster == null)
					throw new IllegalArgumentException ("NO CLUSTER FOUND IN HASH\n");
				cluster.addAll (vcluster);
			}
			newClustering.add (cluster);
		}
		ogc = newClustering;
		return ogc;
	}


	/** Given a list of sets ("sets") and a set ("toMerge"), add the
	 * sets from "toMerge" to "sets" respecting transitive closure
	 * (e.g. if "toMerge"=[1 2], and "sets"=[2 3], [5 6] result is
	 * "sets"=[1 2 3] [5 6] */
	private void mergeSets(ArrayList sets, HashSet toMerge) {
		Iterator iter = toMerge.iterator();
		while (iter.hasNext()) {
			String clusterID = (String) iter.next();
			Iterator siter = sets.iterator();
			while (siter.hasNext()) {
				HashSet s = (HashSet) siter.next();
				if (s.contains (clusterID)) {
					s.addAll (toMerge);
					return;
				}
			}
		}
		sets.add (toMerge);
	}
	
	private HashMap createVenueClusterID2VenueClusterHash (Collection venues) {
		HashMap ret = new HashMap ();
		Iterator iter = venues.iterator();
		int ci = 1;
		while (iter.hasNext()) {
			Collection c = (Collection) iter.next();
			Iterator subiter = c.iterator();
			while (subiter.hasNext()) {
				ret.put (new String (String.valueOf(ci)), c);
			}
		}
		return ret;
	}
	
	/** Constrain <code>ogc<code> such that nodes with venues from
	 * different venue clusters will be in different clusters.*/
	public Collection constrainByVenues (Collection venueCluster) {
		String defaultVenueID = "-9999"; // for citations with no venue
		int [] confusion = new int [2]; // confusion matrix for splits
		                                // indx 0 = tn indx 1 = fn
		// build map from predicted venue cluster
		HashMap paperID2venueClusterID = createPaper2VenueHash (venueCluster);
		
		ArrayList newClustering = new ArrayList ();
		Iterator citer = ogc.iterator();
		while (citer.hasNext()) {
			Collection cl = (Collection) citer.next();
			Iterator csubiter = cl.iterator();
			HashMap splitClusters = new HashMap ();
			while (csubiter.hasNext()) {
				Citation cit = (Citation) csubiter.next();
				String paperID = cit.getField (Citation.paperID);
				String venueCID = (String) paperID2venueClusterID.get (paperID);
				if (venueCID == null) {
					logger.info("no venue id stored in hash for " + paperID + "\n" + cit);
					venueCID = defaultVenueID; 
				}
 				else
				 	logger.info ("Found venue cluster for paper id " + paperID);
				ArrayList a = (ArrayList) splitClusters.get (venueCID);
				if (a == null)
					a = new ArrayList();
				a.add (cit);
				splitClusters.put (venueCID, a);
			}
			if (splitClusters.size () > 1 ) {
				logger.info ("Splitting cluster into " + splitClusters.size() + " clusters:\n" + cl);			
				evaluateClustersSplitByVenue (splitClusters, confusion);
			}
			Iterator kiter = splitClusters.keySet().iterator();
			while (kiter.hasNext()) // add new clusters
				newClustering.add ((ArrayList)splitClusters.get ((String)kiter.next()));
		}
		logger.info ("Number correct splits: " + confusion[0] +
								 "\nNumber incorrect splits: " + confusion[1]);
		return newClustering;
	}

	/** Evaluate performance of splitting a cluster of papers based on
	 * non-coreferent venues */
	private int[] evaluateClustersSplitByVenue (HashMap h, int[] confusion) {
		String[] keys = (String[])h.keySet().toArray (new String[] {});
		for (int i=0; i < keys.length; i++) {
			ArrayList ai = (ArrayList) h.get (keys[i]);
			for (int j=i+1; j < keys.length; j++) {
				ArrayList aj = (ArrayList) h.get (keys[j]);
				for (int ii=0; ii < ai.size(); ii++) {
					for (int jj=ii+1; jj < aj.size(); jj++) {
						Citation ci = (Citation) ai.get (ii);
						Citation cj = (Citation) aj.get(jj);
						if (ci.getField (Citation.paperCluster).equals
								(cj.getField(Citation.paperCluster))) {
							logger.info ("Should NOT have split venues " +
													 ci.getField(Citation.venue) + " AND " +
													 cj.getField(Citation.venue)+ "\nCi: " + ci + "\nCj:"+cj);
							confusion[1]++;
						}
						else {
							logger.info ("CORRECTLY split venues " +
													 ci.getField(Citation.venue) + " AND " +
													 cj.getField(Citation.venue)+ "\nCi: " + ci + "\nCj:"+cj);
							confusion[0]++;
						}						
					}
				}
			}				
		}			
		return confusion;
	}
	
	private HashMap createPaper2VenueHash (Collection venueCluster) {
		HashMap paperID2venueClusterID = new HashMap ();
		Iterator viter = venueCluster.iterator ();
		int ci = 1;
		while (viter.hasNext()) {
			Collection cl = (Collection) viter.next();
			Iterator vsubiter = cl.iterator();
			while (vsubiter.hasNext()) {
				Citation cit = (Citation) vsubiter.next();
				String paperID = cit.getField (Citation.paperID);
				if (paperID.equals (""))
					throw new IllegalArgumentException ("No paper id in " + cit);
				paperID2venueClusterID.put (paperID, String.valueOf(ci));
				System.err.println (paperID + " --> " + (ci));
			}
			ci++;
		}
		return paperID2venueClusterID;
	}
}

