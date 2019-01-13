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

public class MalletDocumentElement
{
    
    public MalletDocumentElement ()
    {
	
    }
    
    private void printElements(Set elements)
    {
	Iterator i = elements.iterator();
	System.out.println("Elements: ");
	while (i.hasNext()) {
	    Element e = (Element)i.next();
	    System.out.println("  " + e);
	}
    }

    protected MalletPhrase constructPhrases (Element e, MalletPreTerm preTerms,
					     MalletSentence sent,
					     String sourceType)
    {
	LinkedHashSet l = new LinkedHashSet();
	LinkedHashSet a = null;
	LinkedHashSet phraseElements = null;
	MalletPhrase phrases = null;
	MalletPhrase phrase = null;
	MalletPhrase prev = null;
	if (sourceType.equals("TB")) {
	    a = new LinkedHashSet();
	    l.add((Pattern)Pattern.compile("NP.*")); /// XXXX horribly hard-coded
	    l.add((Pattern)Pattern.compile("lex"));
	    a.add((Pattern)Pattern.compile(".*-.*")); // if lex pos has a hyphen
	} else if (sourceType.equals("MUC")) {
	    l.add((Pattern)Pattern.compile("NG.*")); // don't need 'a' in this case
	    l.add((Pattern)Pattern.compile("COREF"));  // this is sort of "cheating"
	}
	
	phraseElements = collectElementsWithin (e, l, a);
	if (sourceType.equals("TB"))
	    phraseElements = filterPhraseElements (phraseElements, l, a);
	if (sourceType.equals("MUC"))
	    phraseElements = filterNGs (phraseElements);
	//printElements(phraseElements);
	int phraseIndex = 0;
	
	Iterator ptIterator = phraseElements.iterator();
	while (ptIterator.hasNext())
	    {
		Element element = (Element)ptIterator.next();
		Element corefElement = getCorefElement (element);
		phrase = new MalletPhrase (element, corefElement, preTerms, sent);
		if (prev != null)
		    prev.setNext(phrase);
		
		if (phraseIndex == 0) {
		    phrase.setPrev(null);				
		    phrases = phrase;
		} else
		    phrase.setPrev(prev);
		prev = phrase;
		phraseIndex++;
		if (!ptIterator.hasNext())
		    phrase.setNext(null); // last phrase has next value of NULL
	    }
	return phrases;
    }

    private Element getCorefElement (Element e)
    {
	if (e.getName().equals("COREF"))
	    return e;

	Iterator i = e.getContent().iterator();
	Element corefEl = null;
	while (i.hasNext()) {
	    Object obj = i.next();
	    if (obj instanceof Element) {
		Element ch = (Element)obj;
		if (ch.getName().equals("COREF"))
		    corefEl = ch;
	    }
	}
	if (corefEl != null)
	    return corefEl;
	else if (((Element)e.getParent()).getName().equals("COREF"))
	    return (Element) e.getParent();
	else
	    return null;

    }

    /*
      We need to remove coref elements that have NG parents or NG children
    */
    private LinkedHashSet filterNGs (Set phraseElements)
    {
	LinkedHashSet newPHs = new LinkedHashSet();
	Iterator iter = phraseElements.iterator();
	while (iter.hasNext()) {
	    Element e = (Element)iter.next();
	    Element par = (Element) e.getParent();
	    if ((e.getName().equals("COREF"))) {
		if ((!(par.getName().equals("NG"))) &&
		    (!(hasNGChild (e))))
		    newPHs.add (e);
	    } else {
		newPHs.add(e);
	    }
	}
	return newPHs;
    }		

    private boolean hasNGChild (Element e)
    {
	Iterator i = e.getContent().iterator();
	while (i.hasNext()) {
	    Object obj = i.next();
	    if (obj instanceof Element) {
		Element el = (Element)obj;
		if (el.getName().equals("NG"))
		    return true;
	    }
	}
	return false;
    }

    private boolean noCorefChildren (Element e)
    {
	List children = e.getContent();
	Iterator i = children.iterator();
	while (i.hasNext()) {
	    Object ch = i.next();
	    if (ch instanceof Element)
		if (((Element)ch).getName().equals("COREF"))
		    return false;
	}
	return true;
    }
    // this method is only applicable for TreeBank data
    private LinkedHashSet filterPhraseElements (Set phraseElements, 
						LinkedHashSet namePatterns, LinkedHashSet attributePatterns)
    {
	LinkedHashSet newPHs = new LinkedHashSet ();
	Iterator iter = phraseElements.iterator();
	while (iter.hasNext()) {
	    Element e = (Element)iter.next();
	    if ((compatible (e, namePatterns, attributePatterns)) && 
		(e.getName().equals("lex") || hasLexChild(e)))
		newPHs.add(e);
	}
	return newPHs;
    }

    private boolean hasLexChild (Element e)
    {
	Iterator iter = e.getChildren().iterator();
	while (iter.hasNext()) {
	    Element e1 = (Element)iter.next();
	    if ((e1.getName().equals("lex")) || (e1.getName().equals("LEX")))
		return true;
	}
	return false;
    }

    private boolean compatible (Element e, LinkedHashSet namePatterns, LinkedHashSet attributePatterns)
    {
	    
	LinkedHashSet within = collectElementsWithin (e, namePatterns, attributePatterns);
	Pattern p1 = (Pattern)Pattern.compile("NP.*");
	Matcher m1 = p1.matcher(e.getName());
	if (m1.matches()) {
	    Iterator iter = within.iterator();
	    while (iter.hasNext()) {
		Element e1 = (Element)iter.next();
		Matcher m2 = p1.matcher(e1.getName());
		if (m2.matches())
		    return false;
	    }
	}
	if (within.size() == 1)
	    return false;
	else
	    return true;
    }

    protected MalletPreTerm constructPreTerms (Element s, MalletSentence sent)
    {
	LinkedHashSet l = new LinkedHashSet();
	LinkedHashSet a = new LinkedHashSet();
	LinkedHashSet preTermElements = null;
	MalletPreTerm preTerms = null;
	MalletPreTerm preTerm = null;
	MalletPreTerm prev = null;
	l.add((Pattern)Pattern.compile("lex")); // xxxxx Hardcoded, parameterize this!
	l.add((Pattern)Pattern.compile("LEX")); // xxxxx Hardcoded, parameterize
	// this!
	a.add((Pattern)Pattern.compile(".*"));
	preTermElements = collectElementsWithin (s, l, a);
	int preTermIndex = 0;
	
	Iterator ptIterator = preTermElements.iterator();
	while (ptIterator.hasNext())
	    {
		Element item = (Element)ptIterator.next();
		preTerm = new MalletPreTerm (item, sent);
		    if (prev != null)
			prev.setNext(preTerm);
		    if (preTermIndex == 0) {
			preTerm.setPrev(null);
			preTerms = preTerm;
		    } else {
			preTerm.setPrev(prev);
		    }
		    prev = preTerm;
		    preTermIndex++;

	    }
	return preTerms;
    }
    
    public LinkedHashSet collectElementsWithinDocument (Document doc,
							LinkedHashSet types,
							LinkedHashSet attrTypes)
    {
	LinkedHashSet collection = new LinkedHashSet();
	collectElementsWithinDocumentAux (doc, types, attrTypes, collection);
	return collection;
    }
    
    public LinkedHashSet collectElementsWithin (Element e, LinkedHashSet types,
						LinkedHashSet attrTypes)
    {
	LinkedHashSet v = new LinkedHashSet();
	collectElementsWithinAux (e, types, attrTypes, v);
	return v;
    }
    

    private void collectElementsWithinAux (Element e,
						    LinkedHashSet types,
						    LinkedHashSet attrTypes,
						    LinkedHashSet collection)
    {
	List children = e.getContent();
	Iterator iterator = children.iterator();
	Element element;
	int i;
	
	while (iterator.hasNext()) {
	    Object obj = iterator.next();
	    if (obj instanceof Element) {
		element = (Element) obj;
		Iterator tIter = types.iterator();
		while (tIter.hasNext()) {
		    Pattern pattern = (Pattern)tIter.next();
		    Matcher m =
			pattern.matcher(element.getName().toUpperCase());
		    if ((pattern.pattern().equals("lex") ||
			 pattern.pattern().equals("LEX")) &&
			(element.getName().equals("lex") ||
			 element.getName().equals("LEX")))
			{
			    if (attrTypes != null)
				{
				    Iterator aIter = attrTypes.iterator();
				    while (aIter.hasNext())
					{
					    Pattern p1 = (Pattern)aIter.next();
					    Iterator attrIter = element.getAttributes().iterator();
					    while (attrIter.hasNext())
						{
						    Matcher m1 =
							p1.matcher(((Attribute)attrIter.next()).getValue());
						    if (m1.matches()) {
							collection.add(element);
						    }
						}
					}
				}
			} else {
			    if (m.matches()) {
				collection.add(element);
			    }
			}
		}
		collectElementsWithinAux (element, types, attrTypes, collection);
	    }
	}
    }
    
    private void collectElementsWithinDocumentAux (Document e,
							    LinkedHashSet types,
							    LinkedHashSet attrTypes,
							    LinkedHashSet collection)
    {
	List children = e.getContent();
	Iterator iterator = children.iterator();
	Element element;
	int i;
	
	while (iterator.hasNext()) {
	    Object obj = iterator.next();
	    if (obj instanceof Element) {
		element = (Element) obj;
		Iterator tIter = types.iterator();
		while (tIter.hasNext()) {
		    Pattern pattern = (Pattern)tIter.next();
		    Matcher m =
			pattern.matcher(element.getName().toUpperCase());
		    if ((pattern.pattern().equals("lex") ||
			 pattern.pattern().equals("LEX")))
			{
			    if (attrTypes != null)
				{
				    Iterator aIter = attrTypes.iterator();
				    while (aIter.hasNext())
					{
					    //System.out.println(" ++ Considering lex element");
					    Pattern p1 = (Pattern)aIter.next();							
					    Iterator attrIter = element.getAttributes().iterator();
					    while (attrIter.hasNext())
						{
						    Matcher m1 = p1.matcher(((Attribute)attrIter.next()).getValue());
						    if (m1.matches()) {
							//System.out.println("Adding phrase based on lex elemetnt");
							collection.add(element);
						    }
						}
					}
				}
			} else {
			    if (m.matches()) {
				collection.add(element);
			    }
			}
		}
		collectElementsWithinAux (element, types, attrTypes, collection);
	    }
	}
    }
    
    protected String getTextWithinElement (Element node)
    {
	StringBuffer substringBuf = new StringBuffer();
	List children = node.getContent();
	Iterator iterator = children.iterator();
	String intString;
	
	while (iterator.hasNext()) {
	    Object o = iterator.next();
	    if (o instanceof Element) {
		if (!((Element)o).getChildren().isEmpty ())
		    intString = getTextWithinElement ((Element)o);
		else {
		    intString = ((Element)o).getTextNormalize();
		}
		substringBuf.append(intString);
	    }
	    else if (o instanceof Text) {
		substringBuf.append(((Text)o).getText());
	    }
	}
	return substringBuf.toString();
    }
}
