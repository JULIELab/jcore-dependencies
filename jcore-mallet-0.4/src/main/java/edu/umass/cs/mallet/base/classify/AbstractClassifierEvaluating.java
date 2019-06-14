/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.base.classify;

/**
 * Created: Apr 13, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: AbstractClassifierEvaluating.java,v 1.1 2005/04/19 17:44:36 casutton Exp $
 */
public abstract class AbstractClassifierEvaluating implements ClassifierEvaluating {

  private int numIterToWait = 0;
  private int numIterToSkip = 10;
  private boolean alwaysEvaluateWhenFinished = true;

  public void setNumIterToWait (int numIterToWait)
  {
    this.numIterToWait = numIterToWait;
  }

  public void setNumIterToSkip (int numIterToSkip)
  {
    this.numIterToSkip = numIterToSkip;
  }

  public void setAlwaysEvaluateWhenFinished (boolean alwaysEvaluateWhenFinished)
  {
    this.alwaysEvaluateWhenFinished = alwaysEvaluateWhenFinished;
  }

  protected boolean shouldDoEvaluate (int iter, boolean finished)
  {
    if (alwaysEvaluateWhenFinished && finished) return true;
    return ((iter > numIterToWait) && (iter % numIterToSkip == 0));
  }
}
