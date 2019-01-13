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

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.io.*;

import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.util.PropertyList;
import org.jdom.*;

public class GenderMentionPair extends Pipe
{

	private HashMap femaleMap;
	private HashMap maleMap;

	public GenderMentionPair (File femaleWords, File maleWords)
	{
		femaleMap = fileToHash(femaleWords);
		maleMap   = fileToHash(maleWords);
	}

	private HashMap fileToHash(File file)
	{
		HashMap map = new HashMap();
		String line;
		try {
			BufferedReader reader = new BufferedReader (new FileReader (file));
			line = reader.readLine();
			while (line != null) {
				map.put((Object)line,"true");
				line = reader.readLine();
			}
		} catch (Exception e) { System.out.println (e.toString()); }
		return map;
	}

	private String getTextWithinNode (Element node)
	{
		StringBuffer substringBuf = new StringBuffer();
		List children = node.getContent();
		Iterator iterator = children.iterator();
		while (iterator.hasNext()) {
			Object o = iterator.next();
			if (o instanceof Element) {
				String intString = getTextWithinNode ((Element)o);
				substringBuf.append(intString);
			}
			else if (o instanceof Text) {
				substringBuf.append(((Text)o).getText());
			}
		}
		return substringBuf.toString();
	}

	private double calcFemaleness (Element node1)
	{
		String str = getTextWithinNode (node1);
		Object val;
		val = femaleMap.get(str);
		if (val != null)
			return 1.0;
		else {
			val = maleMap.get(str);
			if (val != null)
				return 0.0;
			else
				return 0.5;
		}
	}

	public Instance pipe (Instance carrier)
	{
		MentionPair pair = (MentionPair)carrier.getData();
		double femaleness1 = calcFemaleness (pair.getAntecedent().getElement());
		double femaleness2 = calcFemaleness (pair.getReferent().getElement());

		pair.setFeatureValue("GenderDifference", Math.abs(femaleness1 - femaleness2));
		return carrier;
	}
	
}

