/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.base.extract;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created: Nov 23, 2004
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: PunctuationIgnoringComparator.java,v 1.1 2004/11/24 19:01:32 casutton Exp $
 */
public class PunctuationIgnoringComparator implements FieldComparator {

  private Pattern punctuationPattern = Pattern.compile ("\\p{Punct}*$");

  public void setPunctuationPattern (Pattern punctuationPattern)
  {
    this.punctuationPattern = punctuationPattern;
  }

  public boolean matches (String fieldVal1, String fieldVal2)
  {
    String trim1 = doTrim (fieldVal1);
    String trim2 = doTrim (fieldVal2);
    return trim1.equals (trim2);
  }

  private String doTrim (String str)
  {
    return punctuationPattern.matcher (str).replaceAll ("");
  }

}
