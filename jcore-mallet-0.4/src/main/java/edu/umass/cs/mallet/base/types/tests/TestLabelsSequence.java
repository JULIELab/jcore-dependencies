/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.base.types.tests;

import edu.umass.cs.mallet.base.types.Label;
import edu.umass.cs.mallet.base.types.LabelAlphabet;
import edu.umass.cs.mallet.base.types.Labels;
import edu.umass.cs.mallet.base.types.LabelsSequence;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * Created: Sep 21, 2004
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: TestLabelsSequence.java,v 1.1 2004/09/22 02:19:50 casutton Exp $
 */
public class TestLabelsSequence extends TestCase {

  public TestLabelsSequence (String name)
  {
    super (name);
  }

  public void testSerializable () throws IOException, ClassNotFoundException
  {
    LabelAlphabet dict = new LabelAlphabet ();
    Labels lbls1 = new Labels (new Label[] {
      dict.lookupLabel ("A"),
      dict.lookupLabel ("B"),
    });
    Labels lbls2 = new Labels (new Label[] {
      dict.lookupLabel ("C"),
      dict.lookupLabel ("A"),
    });
    LabelsSequence lblseq  = new LabelsSequence (new Labels[] { lbls1, lbls2 });
    LabelsSequence lblseq2 = (LabelsSequence) TestSerializable.cloneViaSerialization (lblseq);
    assertEquals (lblseq.size(), lblseq2.size());
    assertEquals (lblseq.getLabels(0).toString(), lblseq2.getLabels(0).toString ());
    assertEquals (lblseq.getLabels(1).toString(), lblseq2.getLabels(1).toString ());
  }
  
  public static Test suite ()
  {
    return new TestSuite (TestLabelsSequence.class);
  }

  public static void main (String[] args) throws Throwable
  {
    TestSuite theSuite;
    if (args.length > 0) {
      theSuite = new TestSuite ();
      for (int i = 0; i < args.length; i++) {
        theSuite.addTest (new TestLabelsSequence (args[i]));
      }
    } else {
      theSuite = (TestSuite) suite ();
    }

    junit.textui.TestRunner.run (theSuite);
  }

}
