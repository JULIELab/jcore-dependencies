/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
   @author Aron Culotta <a href="mailto:culotta@cs.umass.edu">culotta@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.base.pipe.tests;

import edu.umass.cs.mallet.base.pipe.Input2CharSequence;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.SGML2TokenSequence;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.iterator.ArrayIterator;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.TokenSequence;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestSGML2TokenSequence extends TestCase
{
	public TestSGML2TokenSequence (String name) {
		super (name);
	}

	String[] dataWithTags = new String[] {
		"zeroth test string",
		"<tag>first</tag> test string",
		"second <tag>test</tag> string",
		"third test <tag>string</tag>",
	};

	String[] data = new String[] {
		"zeroth test string",
		"first test string",
		"second test string",
		"third test string",
	};
	
	String[] tags = new String[] {
		"O O O",
		"tag O O ",
		"O tag O",
		"O O tag",
	};

	public static class Array2ArrayIterator extends Pipe
	{
		public Instance pipe (Instance carrier) {
			carrier.setData(new ArrayIterator ((Object[])carrier.getData()));
			return carrier;
		}
	}
	
	public void testOne ()
	{
		Pipe p = new SerialPipes (new Pipe[] {
			new Input2CharSequence (),
			new SGML2TokenSequence()
		});
		for (int i=0; i < dataWithTags.length; i++) {
			Instance inst = new Instance (dataWithTags[i], null, null, null, p);
			TokenSequence input = (TokenSequence)inst.getData();
			TokenSequence target = (TokenSequence)inst.getTarget();
			String[] oginput = data[i].split("\\s+");
			String[] ogtags = tags[i].split("\\s+");
			assert (input.size() == target.size());
			assert (input.size() == oginput.length);
			for (int j=0; j < oginput.length; j++) {
				assert (oginput[j].equals (input.getToken(j).getText()));
				assert (ogtags[j].equals (target.getToken(j).getText()));
			}
		}
	}
	
	public static Test suite ()
	{
		return new TestSuite (TestSGML2TokenSequence.class);
	}

	protected void setUp ()
	{
	}

	public static void main (String[] args)
	{
		junit.textui.TestRunner.run (suite());
	}
	
}
