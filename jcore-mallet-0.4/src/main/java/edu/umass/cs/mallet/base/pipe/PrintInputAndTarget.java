/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */





package edu.umass.cs.mallet.base.pipe;

import edu.umass.cs.mallet.base.types.Instance;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
/**
 * Print the data and target fields of each instance.
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

public class PrintInputAndTarget extends Pipe implements Serializable
{
	String prefix = null;

	public PrintInputAndTarget (String prefix)
	{
		this.prefix = prefix;
	}

	public PrintInputAndTarget ()
	{
	}
	
	public Instance pipe (Instance carrier)
	{
		 if (prefix != null)
			 System.out.print (prefix);
    String targetString = "<null>";
    if (carrier.getTarget() != null)
     targetString = carrier.getTarget().toString();
		System.out.println ("name: "+carrier.getName()+"\ninput: "+carrier.getData().toString()+"\ntarget: "+targetString);
		return carrier;
	}
	
	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt (CURRENT_SERIAL_VERSION);
		out.writeObject(prefix);
	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();
		prefix = (String) in.readObject();
	}
	
}
