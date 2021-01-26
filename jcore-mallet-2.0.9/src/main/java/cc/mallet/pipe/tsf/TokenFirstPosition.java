/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

/**
   Add a feature that is true if the token is the first in the sequence.
   @author David Mimno
 */

package cc.mallet.pipe.tsf;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TokenFirstPosition extends Pipe implements Serializable {

	String featureName;

	public TokenFirstPosition (String featureName) {
		this.featureName = featureName;
	}
	
	public TokenFirstPosition () {
	}

	public Instance pipe (Instance instance) {

		TokenSequence sequence = (TokenSequence) instance.getData();

		Token token = sequence.get(0);
		token.setFeatureValue(featureName, 1.0);

		return instance;
	}
	
	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 1;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt (CURRENT_SERIAL_VERSION);
		out.writeObject (featureName);
	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();
		featureName = (String) in.readObject ();
	}


}
