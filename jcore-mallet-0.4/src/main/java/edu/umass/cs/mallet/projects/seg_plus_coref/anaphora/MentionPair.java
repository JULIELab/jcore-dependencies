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
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.anaphora;

import edu.umass.cs.mallet.base.util.PropertyList;

public class MentionPair
{

	Mention node1, node2;
	PropertyList features = null;
	int refIndex;
    String entityReference = null;

	public MentionPair (Mention n1, Mention n2)
	{
		node1 = n1; node2 = n2;
		refIndex = 0;
		if ((n1 != null) && (n2 != null) && (n1.getEntityId() != null)) {
		    if ((n1.getUniqueEntityIndex() == null) && (n1.getEntityRef() == null)) {
			n1.setUniqueEntityIndex(n1.getEntityId());
		    }
		    if ((n1.getEntityId().equals(n2.getEntityRef())) ||
			((n1.getUniqueEntityIndex() != null) && (n2.getUniqueEntityIndex() != null) &&
			 (n1.getUniqueEntityIndex().equals(n2.getUniqueEntityIndex())))) {
			if (n1.getUniqueEntityIndex() != null) {
			    entityReference = n1.getUniqueEntityIndex();
			    n2.setUniqueEntityIndex(n1.getUniqueEntityIndex());
			}
		    }			
		}
		/*
		if ((n1 != null) && (entityReference != null)) {
		    System.out.println("MENTION PAIR: " + n1.getString() + " :: " + n2.getString()
				       + " -- " + entityReference + " +++ " +
				       n1 + " " + n2);
		}  else {
		    if ((n1 != null) && (n2 != null))
		    System.out.println("MENTION PAIR: " + " -- " + entityReference + " +++ " +
				       n1.getString() + " " + n2.getString());
				       }*/


	}

	public int getReferentIndex ()
	{
		return refIndex;
	}

	public void setReferentIndex (int val)
	{
		refIndex = val;
	}

    public void setEntityReference (String ref)
    {
	entityReference = ref;
    }

    public String getEntityReference ()
    {
	return entityReference;
    }

    public boolean nullPair ()
    {
	if (node1 == null)
	    return true;
	else
	    return false;
    }

	public Mention getAntecedent () { return node1; }
	public Mention getReferent () { return node2; }

	public void setFeatureValue (String key, double value)
	{
		features = PropertyList.add (key, value, features);
	}

	public double getFeatureValue (String key)
	{
		return (features == null ? 0.0 : features.lookupNumber (key));
	}

	public PropertyList getFeatures ()
	{
		return features;
	}

	public void setFeatures (PropertyList pl)
	{
		features = pl;
	}

}
