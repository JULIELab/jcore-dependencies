/* Copyright (C) 2006 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package cc.mallet.grmm.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import cc.mallet.grmm.types.AbstractTableFactor;
import cc.mallet.grmm.types.Assignment;
import cc.mallet.grmm.types.Factor;
import cc.mallet.grmm.types.HashVarSet;
import cc.mallet.grmm.types.PottsTableFactor;
import cc.mallet.grmm.types.TableFactor;
import cc.mallet.grmm.types.VarSet;
import cc.mallet.grmm.types.Variable;

/**
 * $Id: TestPottsFactor.java,v 1.1 2007/10/22 21:37:41 mccallum Exp $
 */
public class TestPottsFactor {

    private static PottsTableFactor factor;
    private static Variable alpha;
    private static VarSet vars;

    // public TestPottsFactor (String name)
    // {
    // super (name);
    //
    //
    // }

    /**
     * @return a <code>TestSuite</code>
     */
    // public static TestSuite suite ()
    // {
    // return new TestSuite (TestPottsFactor.class);
    // }

    @BeforeClass
    public static void setUp() throws Exception {
        alpha = new Variable(Variable.CONTINUOUS);
        Variable v1 = new Variable(2);
        Variable v2 = new Variable(2);
        vars = new HashVarSet(new Variable[] { v1, v2 });
        factor = new PottsTableFactor(vars, alpha);
    }

    @Test
    public void testSlice() {
        Assignment assn = new Assignment(alpha, 1.0);
        Factor sliced = factor.slice(assn);

        assertTrue(sliced instanceof AbstractTableFactor);
        assertTrue(sliced.varSet().equals(vars));

        TableFactor expected = new TableFactor(vars, new double[] { 1.0, Math.exp(-1), Math.exp(-1), 1.0 });
        assertTrue(sliced.almostEquals(expected));
    }

    @Test
    @Ignore
    public void testSumGradLog() {
        Assignment alphaAssn = new Assignment(alpha, 1.0);

        double[] values = new double[] { 0.4, 0.1, 0.3, 0.2 };
        Factor q = new TableFactor(vars, values);

        double grad = factor.sumGradLog(q, alpha, alphaAssn);
        assertEquals(-0.4, grad, 1e-5);
    }

    @Test
    public void testSumGradLog2() {
        Assignment alphaAssn = new Assignment(alpha, 1.0);

        double[] values = new double[] { 0.4, 0.1, 0.3, 0.2 };
        Factor q1 = new TableFactor(vars, values);
        Factor q2 = new TableFactor(new Variable(2), new double[] { 0.7, 0.3 });
        Factor q = q1.multiply(q2);

        double grad = factor.sumGradLog(q, alpha, alphaAssn);
        assertEquals(-0.4, grad, 1e-5);
    }

    // public static void main (String[] args)
    // {
    // TestSuite theSuite;
    // if (args.length > 0) {
    // theSuite = new TestSuite ();
    // for (int i = 0; i < args.length; i++) {
    // theSuite.addTest (new TestPottsFactor (args[i]));
    // }
    // } else {
    // theSuite = suite ();
    // }
    //
    // junit.textui.TestRunner.run (theSuite);
    // }

}
