/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.base.pipe.iterator;

import edu.umass.cs.mallet.base.types.Instance;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class FileUriIterator extends FileIterator
{
	public FileUriIterator (File[] directories, FileFilter filter, Pattern targetPattern)
	{
		super (directories, filter, targetPattern);
	}

	public FileUriIterator (File directory, FileFilter filter, Pattern targetPattern)
	{
		super (directory, filter, targetPattern);
	}
	
	public FileUriIterator (File[] directories, Pattern targetPattern)
	{
		super (directories, null, targetPattern);
	}

	public FileUriIterator (File directory, Pattern targetPattern)
	{
		super (directory, null, targetPattern);
	}

	public Instance nextInstance ()
	{
		Instance carrier = super.nextInstance();
		carrier.setData(((File)carrier.getData()).toURI());
		return carrier;
	}
	
}
