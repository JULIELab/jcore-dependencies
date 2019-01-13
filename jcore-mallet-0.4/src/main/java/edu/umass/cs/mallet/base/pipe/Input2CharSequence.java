/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.base.pipe;

import edu.umass.cs.mallet.base.util.CharSequenceLexer;
import edu.umass.cs.mallet.base.types.Instance;
import java.io.*;
import java.net.URI;

/**
 *  Pipe that can read from various kinds of text sources
 *   (either URI, File, or Reader) into a CharSequence
 *
 *  @version $Id: Input2CharSequence.java,v 1.6 2004/04/05 21:23:40 hough Exp $
 */
public class Input2CharSequence extends Pipe implements Serializable
{
	public Input2CharSequence ()
	{
	}

	public Instance pipe (Instance carrier)
	{
		try {
			if (carrier.getData() instanceof URI)
				carrier.setData(pipe ((URI)carrier.getData()));
			else if (carrier.getData() instanceof File)
				carrier.setData(pipe ((File)carrier.getData()));
			else if (carrier.getData() instanceof Reader)
				carrier.setData(pipe ((Reader)carrier.getData()));
			else if (carrier.getData() instanceof CharSequence)
				;																	// No conversion necessary
			else
				throw new IllegalArgumentException ("Does not handle class "+carrier.getData().getClass());

		} catch (java.io.IOException e) {
			throw new IllegalArgumentException ("IOException " + e);
		}

//		System.out.println(carrier.getData().toString());
		return carrier;
	}

	public CharSequence pipe (URI uri)
		throws java.io.FileNotFoundException, java.io.IOException
	{
		if (! uri.getScheme().equals("file"))
			throw new UnsupportedOperationException ("Only file: scheme implemented.");
		return pipe (new File (uri.getPath()));
	}
	
	public CharSequence pipe (File file)
		throws java.io.FileNotFoundException, java.io.IOException
	{
	        BufferedReader br = new BufferedReader (new FileReader (file));
		CharSequence cs = pipe(br);
		br.close();
		return cs;
	}

	public CharSequence pipe (Reader reader)
		throws java.io.IOException
	{
		final int BUFSIZE = 2048;
		char[] buf = new char[BUFSIZE];
		int count;
		StringBuffer sb = new StringBuffer (BUFSIZE);
		do {
			count = reader.read (buf, 0, BUFSIZE);
			if (count == -1)
				break;
			//System.out.println ("count="+count);
			sb.append (buf, 0, count);
		} while (count == BUFSIZE);
		return sb;
	}

	public CharSequence pipe (CharSequence cs)
	{
		return cs;
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
