/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package edu.umass.cs.mallet.base.maximize.tests;

import edu.umass.cs.mallet.base.maximize.GradientAscent;
import edu.umass.cs.mallet.base.maximize.LimitedMemoryBFGS;
import edu.umass.cs.mallet.base.maximize.Maximizable;
import edu.umass.cs.mallet.base.maximize.Maximizer;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *  Unit Test for class TestMaximizer.java
 *
 *
 * Created: Mon Apr 26 19:54:25 2004
 *
 * @author <a href="mailto:casutton@cs.umass.edu">Charles Sutton</a>
 * @version $Id: TestMaximizer.java,v 1.2 2004/05/31 17:38:17 casutton Exp $
 */
public class TestMaximizer extends TestCase {

	public TestMaximizer (String name){
		super(name);
	}

	// Maximizable for 3x^2 - 5x + 2
	static class SimplePoly implements Maximizable.ByGradient {

		double[] params = new double [1];
		
		public void getParameters(double[] doubleArray) {
			doubleArray [0] = params [0];
		}

		public int getNumParameters() { return 1; }

		public double getParameter(int n) { return params [0]; };

		public void setParameters(double[] doubleArray) {
			params [0] = doubleArray [0];
		}
		public void setParameter(int n, double d) { params[n] = d; }

		public double getValue () {
			System.out.println("param = "+params [0]+" value = "+( -3*params[0]*params[0] + 5 * params[0] - 2));
			
			return - 3*params[0]*params[0] + 5 * params[0] - 2;
		}

		public void getValueGradient (double[] buffer)
		{
			buffer [0] = - 6*params [0] + 5;
		}
	}

/*
	public void testBoldDriver ()
	{
		SimplePoly poly = new SimplePoly ();
		Maximizer.ByGradient bold = new BoldDriver ();
		bold.maximize (poly);
		assertEquals (5.0/6.0, poly.params [0], 1e-3);
	}
*/

	public void testGradientAscent ()
	{
		SimplePoly poly = new SimplePoly ();
		Maximizer.ByGradient gd = new GradientAscent ();
		gd.maximize (poly);
		assertEquals (5.0/6.0, poly.params [0], 1e-3);
	}

  public void testLinearLBFGS ()
  {
    SimplePoly poly = new SimplePoly ();
    Maximizer.ByGradient bfgs = new LimitedMemoryBFGS ();
    bfgs.maximize (poly);
    assertEquals (5.0/6.0, poly.params [0], 1e-3);
  }


/**
 * @return a <code>TestSuite</code>
 */
	public static TestSuite suite(){
		return new TestSuite (TestMaximizer.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}// TestMaximizer
