/* Copyright (C) 2002 Dept. of Computer Science, Univ. of Massachusetts, Amherst

   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet

   This program toolkit free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  For more
   details see the GNU General Public License and the file README-LEGAL.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
   02111-1307, USA. */


/**
	 @author Ben Wellner
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.anaphora;

import org.jdom.input.*;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.*;
import org.jdom.output.XMLOutputter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.*;
import java.net.URI;

import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;

public class MalletPreTerm extends MalletDocumentElement
{
	Element preTermEl;
	MalletSentence sentence;
	MalletPreTerm  prevPreTerm;
	MalletPreTerm  nextPreTerm;
	MalletPhrase   phrase;
	String  partOfSpeech;
	String     term;  // actual string associated with this PreTerm
	
	public MalletPreTerm (Element pt, MalletSentence s)
	{
		sentence = s;
		preTermEl = pt;
		partOfSpeech = pt.getAttributeValue("pos"); // XXXX might have to filter
		// crud out of here for Treebank data
		term = getTextWithinElement (pt);
	}

	public void setMalletPhrase (MalletPhrase ph)
	{
		phrase = ph;
	}

	public MalletPhrase getMalletPhrase ()
	{
		return phrase;
	}

	public String getString ()
	{
		return term;
	}

	public String getPartOfSpeech ()
	{
		return partOfSpeech;
	}

	public Element getElement ()
	{
		return preTermEl;
	}

	public void setNext (MalletPreTerm pt)
	{
	  nextPreTerm = pt;
	}
	
	public void setPrev (MalletPreTerm pt)
	{
	  prevPreTerm = pt;
	}

	public MalletPreTerm getNext ()
	{
		return nextPreTerm;
	}
	
	public MalletPreTerm getPrev ()
	{
		return prevPreTerm;
	}
}
