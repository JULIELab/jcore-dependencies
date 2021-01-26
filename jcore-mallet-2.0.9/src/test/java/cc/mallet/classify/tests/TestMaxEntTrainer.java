/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package cc.mallet.classify.tests;

import cc.mallet.classify.MaxEnt;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.optimize.Optimizable;
import cc.mallet.optimize.tests.TestOptimizable;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestMaxEntTrainer extends TestCase
{
	public TestMaxEntTrainer (String name)
	{
		super (name);
	}

	private static Alphabet dictOfSize (int size)
	{
		Alphabet ret = new Alphabet ();
		for (int i = 0; i < size; i++)
			ret.lookupIndex ("feature"+i);
		return ret;
	}

	public void testSetGetParameters ()
	{
 		MaxEntTrainer trainer = new MaxEntTrainer();
		Alphabet fd = dictOfSize (6);
		String[] classNames = new String[] {"class0", "class1", "class2"};
		InstanceList ilist = new InstanceList (new Randoms(1), fd, classNames, 20);
		Optimizable.ByGradientValue maxable = trainer.getOptimizable (ilist);
		TestOptimizable.testGetSetParameters (maxable);
	}

	public void testRandomMaximizable ()
	{
		MaxEntTrainer trainer = new MaxEntTrainer();
		Alphabet fd = dictOfSize (6);
		String[] classNames = new String[] {"class0", "class1"};
		InstanceList ilist = new InstanceList (new Randoms(1), fd, classNames, 20);
		Optimizable.ByGradientValue maxable = trainer.getOptimizable (ilist);
		TestOptimizable.testValueAndGradient (maxable);
	}
	
	// TODO This doesn't pass, but it didn't in the old MALLET either.  Why?? -akm 1/08
	public void testTrainedMaximizable ()
	{
		MaxEntTrainer trainer = new MaxEntTrainer();
		Alphabet fd = dictOfSize (6);
		String[] classNames = new String[] {"class0", "class1"};
		InstanceList ilist = new InstanceList (new Randoms(1), fd, classNames, 20);
		MaxEnt me = (MaxEnt)trainer.train(ilist);
		Optimizable.ByGradientValue maxable = trainer.getOptimizable (ilist, me);
		TestOptimizable.testValueAndGradientCurrentParameters (maxable);
	}

	public static Test suite ()
	{
		return new TestSuite (TestMaxEntTrainer.class);
	}

	protected void setUp ()
	{
	}

	public static void main (String[] args)
	{
		junit.textui.TestRunner.run (suite());
	}
	
}
		
