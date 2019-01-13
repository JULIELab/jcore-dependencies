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

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.*;
import java.net.URI;

import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;

/*
	This class is the start of the core data structures used for co-reference
	* and other tasks that require obtaining data from disparate parts of a
	* document.  Traversing the JDOM tree is clumsy and inefficient as well as
	* error-prone.
 */
public class MalletDocument extends MalletDocumentElement
{

	LinkedHashSet  sentences;
	LinkedHashSet    phrases;
	Document        document;
	String         sourceType;

	public MalletDocument (Document doc, String sourceType)
	{
		sentences = constructSentences(doc, sourceType);
		phrases = collectPhrases(sentences);
		document = doc;
		this.sourceType = sourceType;
	}

	private LinkedHashSet collectPhrases (LinkedHashSet sents)
	{
		LinkedHashSet allPhrases = new LinkedHashSet();
		Iterator si = sents.iterator();
		while (si.hasNext())
		{
			MalletSentence ms = (MalletSentence)si.next();
			MalletPhrase ph = ms.getPhrases();
			while (ph != null) {
				allPhrases.add(ph);
				ph = ph.getNext();
			}
		}
		return allPhrases;
	}

	public LinkedHashSet getPhrases ()
	{
		return phrases;
	}
    private LinkedHashSet filterSubSentences (LinkedHashSet sents)
    {
	boolean addThis = true;
	LinkedHashSet filtered = new LinkedHashSet ();
	Iterator iter = sents.iterator();
	while (iter.hasNext()) {
	    
	    Element e = (Element)iter.next();
	    Element p = (Element) e.getParent();
	    // search through ancestors for sentence annotations, if found, don't add this sent as a sentence
	    while (p != null) {
		if ((p.getName().equals("S")) || (p.getName().equals("s"))) {
		    addThis = false;
		}
		p = (Element) p.getParent();
	    }
	    if (addThis)
		filtered.add(e);
	    addThis = true;
	}
	return filtered;
    }

	private LinkedHashSet constructSentences (Document doc, String sourceType)
	{
		int sentIndex = 0;
		LinkedHashSet sents = new LinkedHashSet();
		LinkedHashSet l = new LinkedHashSet();
		LinkedHashSet sentElements = null;
		l.add(Pattern.compile("S"));  // this is usually the tag for sents
		l.add(Pattern.compile("s"));
		sentElements = collectElementsWithinDocument (doc, l, null);
		sentElements = filterSubSentences(sentElements);

		MalletSentence prev = null;
		MalletSentence sent = null;

		Iterator sentIterator = sentElements.iterator();
		while (sentIterator.hasNext())
		{
			sent = new MalletSentence ((Element)sentIterator.next(),
						   sentIndex, sourceType);
			sents.add(sent);
			if (prev != null)
				prev.setNext(sent);
			
			if (sentIndex == 0) {
				sent.setPrev(null);
			}
			else
				sent.setPrev(prev);
			prev = sent;
			sentIndex++;
		}
		sent.setNext(null); // last sentence has next value of NULL
		return sents;
	}
	
	public LinkedHashSet getSentences()
	{
		return sentences;
	}

}
