/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/**
	 Add the token text as a feature with value 1.0.

   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.base.pipe.tsf;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Token;
import edu.umass.cs.mallet.base.types.TokenSequence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TokenText extends Pipe implements Serializable
{
	String prefix;

	public TokenText (String prefix)
	{
		this.prefix=prefix;
	}
	
	public TokenText ()
	{
	}

	public Instance pipe (Instance carrier)
	{
		TokenSequence ts = (TokenSequence) carrier.getData();
		for (int i = 0; i < ts.size(); i++) {
			Token t = ts.getToken(i);
			t.setFeatureValue (prefix == null ? t.getText().intern() : (prefix+t.getText()).intern(), 1.0);
		}
		return carrier;
	}
	
	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt (CURRENT_SERIAL_VERSION);
		out.writeObject (prefix);
	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();
		prefix = (String) in.readObject ();
	}


}
