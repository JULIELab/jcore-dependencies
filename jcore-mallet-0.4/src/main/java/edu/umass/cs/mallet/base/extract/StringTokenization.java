/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
    information, see the file `LICENSE' included with this distribution. */

/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.base.extract;

import edu.umass.cs.mallet.base.types.TokenSequence;
import edu.umass.cs.mallet.base.util.CharSequenceLexer;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class StringTokenization extends TokenSequence implements Tokenization
{

  private CharSequence document;

  /** Create an empty StringTokenization */
  public StringTokenization (CharSequence seq)
  {
    document = seq;
  }

  /**
   * Creates a tokenization of the given string.  Tokens are
   * added from all the matches of the given lexer.
   */
  public StringTokenization (CharSequence string, CharSequenceLexer lexer)
  {
    super();
    this.document = string;

    lexer.setCharSequence (string);
    while (lexer.hasNext()) {
      lexer.next ();
      this.add (new StringSpan (string, lexer.getStartOffset(), lexer.getEndOffset()));
    }
  }


  //xxx Refactor into AbstractTokenization
  public Span subspan (int firstToken, int lastToken)
  {
    StringSpan firstSpan = (StringSpan) get(firstToken);
    int startIdx = firstSpan.getStartIdx ();

    int endIdx;
    if (lastToken > size()) {
      endIdx = document.length ();
    } else {
      StringSpan lastSpan = (StringSpan) get(lastToken - 1);
      endIdx = lastSpan.getEndIdx ();
    }

    return new StringSpan (document, startIdx, endIdx);
  }


  public Span getSpan (int i) { return (Span) getToken (i); }

  public Object getDocument ()
  {
    return document;
  }

  // Serialization garbage

  private static final long serialVersionUID = 1;
  private static final int CURRENT_SERIAL_VERSION = 1;

  private void writeObject (ObjectOutputStream out) throws IOException
  {
    out.defaultWriteObject ();
    out.writeInt (CURRENT_SERIAL_VERSION);
  }


  private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject ();
    int version = in.readInt ();
  }

}
