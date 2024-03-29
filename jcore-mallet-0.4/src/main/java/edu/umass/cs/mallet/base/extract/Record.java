/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.base.extract;

import edu.umass.cs.mallet.base.types.Label;
import gnu.trove.THashMap;

import java.util.Iterator;

/**
 * Created: Oct 12, 2004
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: Record.java,v 1.5 2005/03/04 01:06:29 casutton Exp $
 */
public class Record {

  private THashMap fieldMap;
  private String name;

  public Record (String name, LabeledSpans spans) {
    this.name = name;
    fieldMap = new THashMap ();
    for (int i = 0; i < spans.size(); i++) {
      LabeledSpan span = spans.getLabeledSpan (i);
      if (!span.isBackground()) {
        Label tag = span.getLabel ();
        Field field = (Field) fieldMap.get (tag);
        if (field == null) {
          field = new Field (span);
          fieldMap.put (tag, field);
        } else {
          field.addFiller (span);
        }
      }
    }
  }

  public String getName ()
  {
    return name;
  }

  public Field getField (Label name)
  {
    return (Field) fieldMap.get (name);
  }

  public Iterator fieldsIterator ()
  {
    return fieldMap.values ().iterator ();
  }

}
