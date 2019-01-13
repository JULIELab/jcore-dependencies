/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */


/**
 @author Ben Wellner
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import salvo.jesus.graph.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.lang.*;
import java.io.*;


/*	This is just a general structure for now
*/
public class NodePair
{

    Object o1;
    Object o2;
    boolean idRel;

    PropertyList features = null;

    public NodePair (Object o1, Object o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public NodePair (Object o1, Object o2, boolean idRel) {
        this(o1, o2);
        this.idRel = idRel;
    }

    public void setIdRel (boolean idRel) {
        this.idRel = idRel;
    }

    public boolean getIdRel () {
        return idRel;
    }

    public Object getObject1 () {
        return o1;
    }

    public Object getObject2 () {
        return o2;
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
