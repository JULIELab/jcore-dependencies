/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/**
	 If the new article contains a "header", like "SOCCER-", or "RUGBY LEAGUE-",
	 add an indicative feature to all Tokens.
	 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.share.mccallum.ner;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Token;
import edu.umass.cs.mallet.base.types.TokenSequence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TokenSequenceDocHeader extends Pipe implements Serializable
{
	
	public Instance pipe (Instance carrier)
	{
		TokenSequence ts = (TokenSequence) carrier.getData();
		if (ts.size() > 3
				&& (ts.getToken(2).getText().equals("-") || ts.getToken(3).getText().equals("-"))
				&& ts.getToken(1).getText().matches("[A-Z]+")) {
			String header = ts.getToken(1).getText();
			if (header.equals("PRESS"))				// Don't bother with "PRESS DIGEST" headers
				return carrier;
			String featureName = "HEADER="+header;
			for (int i = 0; i < ts.size(); i++) {
				Token t = ts.getToken(i);
				// Only apply this feature to capitalized words, because if we apply it to everything
				// we easily get an immense number of possible feature conjunctions, (e.g. every word
				// with each of these HEADER= features.
				if (t.getText().matches("^[A-Z].*"))
					t.setFeatureValue (featureName, 1.0);
			}
		}
		return carrier;
	}

	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt (CURRENT_SERIAL_VERSION);
	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();
	}

}
