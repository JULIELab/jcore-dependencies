/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

public class Node
{

	Object object; // the object wrapped by this node

	String string; // string representation for the Node
	Object label;// labels, added by Fuchun Peng
	int	index;    // index for node - remember Nodes can now be citations or publications

	// in this case, Node is a citation
	public Node (Citation s) {
		this.object = s;
		this.string = s.getString();
		this.label = s.getLabel();
		this.index = s.getIndex();
	}

	// in this case, Node is a publication
	public Node (Publication s) {
		this.object = s;
		//this.string = s.getString();
		//this.label = s.getLabel(); //added by Fuchun Peng
	}

	// return the object that this node encapsulates
	public Object getObject () {
		return object;
	}

	public String getString () {
		return string;
	}

	// added by Fuchun Peng
	public Object getLabel()
	{
		return label;
	}

	public int getIndex(){return index;}

	public void setIndex(int index)
	{
		this.index = index;
	}
	
}
