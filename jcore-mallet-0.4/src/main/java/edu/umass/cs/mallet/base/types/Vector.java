/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.base.types;

import edu.umass.cs.mallet.base.types.Matrix;
import edu.umass.cs.mallet.base.types.Alphabet;
import edu.umass.cs.mallet.base.util.PropertyList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;

// Could also be called by convention "Matrix1"

public interface Vector extends ConstantMatrix
{
	public double value (int index);
	//public void setValue (int index, double value);
}
