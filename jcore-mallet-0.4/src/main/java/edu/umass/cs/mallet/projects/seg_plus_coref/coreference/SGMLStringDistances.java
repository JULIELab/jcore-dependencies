/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.lang.*;
import java.io.*;

public class SGMLStringDistances extends Pipe
{
	AbstractStringDistance nw;
	String[] sgmlStartTags;
	String[] sgmlEndTags;

	public SGMLStringDistances (Alphabet dataDict)
	{
		super(dataDict, null);
	}

	public SGMLStringDistances ()
	{
		super(Alphabet.class, null);
		this.sgmlStartTags = this.sgmlEndTags = new String[]{};
	}
	
	public SGMLStringDistances (String[] sgmlStartTags, String[] sgmlEndTags)
	{
		super(Alphabet.class, null);
		this.sgmlStartTags = sgmlStartTags;
		this.sgmlEndTags = sgmlEndTags;

		assert(sgmlStartTags.length == sgmlEndTags.length);
	}

	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();
		String s1 = (String)((Node)pair.getObject1()).getString(); // assume nodes are strings
		String s2 = (String)((Node)pair.getObject2()).getString();

		nw = new NeedlemanWunsch();
//		nw = new CharJaccard();
//		nw = new Jaccard();

		double dist = 0.0;
		if(sgmlStartTags.length == 0){
			s1 = SGMLStringOperation.removeSGMLTags(s1);
			s2 = SGMLStringOperation.removeSGMLTags(s2);
	
	//		dist = nw.score(new StringWrapper(s1), new StringWrapper(s2));
			dist = nw.score(s1, s2);
			pair.setFeatureValue("editDistance", dist);

//			System.out.println("editDistance: " + dist + "\n" + s1 + "\n"+ s2);
		}
		else{
			double dist_all = 0.0;
			for(int i=0; i<sgmlStartTags.length; i++){
				String start_tag = sgmlStartTags[i];
				String end_tag   = sgmlEndTags[i];

				String ss1 = SGMLStringOperation.locateField(start_tag, end_tag, s1);
				String ss2 = SGMLStringOperation.locateField(start_tag, end_tag, s2);
				
				if(!ss1.equals("") && !ss2.equals("")){
					dist = nw.score(ss1, ss2);
					dist_all += dist;
					pair.setFeatureValue("editDistance_"+start_tag, dist);

		//			System.out.println("editDistance_"+start_tag + ":" + dist);
				}
			}

//			pair.setFeatureValue("editDistance_all", dist_all);
		}

		return carrier;
	}
	
}
