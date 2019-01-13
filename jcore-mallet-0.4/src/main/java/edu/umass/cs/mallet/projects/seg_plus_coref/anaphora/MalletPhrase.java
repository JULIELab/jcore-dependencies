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

import org.jdom.*;
import java.util.*;

import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;

public class MalletPhrase extends MalletDocumentElement
{
	LinkedHashSet   preTerms;
	Element         phrase;
    Element         corefElement; // this is the coref element coresponding to this phrase
	MalletSentence  sentence;

	MalletPhrase  prevPhrase;
	MalletPhrase  nextPhrase;
    
	public MalletPhrase (Element e, Element corefElement, MalletPreTerm sentPreTerms, MalletSentence sent)
	{
		phrase = e;
		this.corefElement = corefElement;
		preTerms = findPreTerms (e, sentPreTerms);
		/*
		System.out.print("MalletPhrase: ");
		printPreTerms();
		if (corefElement != null)
		    System.out.println("Coref element: " + corefElement.getName() + " " + corefElement.getAttributeValue("ID") +
		    " " + corefElement.getAttributeValue("REF")); */
		sentence = sent;
	}

    public Element getCorefElement ()
    {
	return corefElement;
    }


	public void printPreTerms (MalletPreTerm pt)
	{
		MalletPreTerm cur = pt;
		if (pt == null)
			System.out.println("NULL");
		while (cur != null) {
			System.out.print(cur.getString() + " ");
			cur = cur.getNext();
		}
	}

    public void printPreTerms ()
    {
	Iterator i = preTerms.iterator();
	while (i.hasNext()) {
	    MalletPreTerm p = (MalletPreTerm)i.next();
	    System.out.print(p.getString() + " ");
	}
    }

	private LinkedHashSet findPreTerms (Element e,
					     MalletPreTerm sentPreTerms)
	{
		LinkedHashSet pts = new LinkedHashSet();
		MalletPreTerm curTerm = sentPreTerms;
		
		while (curTerm != null) {
			if ((curTerm.getElement() != null) &&
			    ((e.isAncestor(curTerm.getElement())) ||
			     (curTerm.getElement().isAncestor(e)) ||
			     (curTerm.getElement() == e))) {
			    pts.add(curTerm);
			    curTerm.setMalletPhrase(this); // set back-pointer to MalletPhrase
			}
			curTerm = curTerm.getNext();
		}
		return pts;
	}

    public LinkedHashSet getPreTerms()
    {
	return preTerms;
    }

	public MalletPreTerm getHeadPreTerm ()
	{
		if (preTerms != null) {
			Iterator i = preTerms.iterator();
			MalletPreTerm pt = null;
			while (i.hasNext()) {
				pt = (MalletPreTerm)i.next();
			}
			return pt; // return last PreTerm
		} else
			return null;
	}

	public MalletPreTerm getPreceedingPreTerm ()
	{
		if (preTerms != null) {
			Iterator i = preTerms.iterator();
			if (i.hasNext()) {
				MalletPreTerm pt = (MalletPreTerm)i.next();
				return pt.getPrev();
			} else
				return null;
		} else
			return null;
	}

	public MalletPreTerm getFollowingPreTerm ()
	{
		if (preTerms != null) {
			Iterator i = preTerms.iterator();
			MalletPreTerm pt = null;
			while (i.hasNext()) {
				pt = (MalletPreTerm)i.next();
				if (!i.hasNext())
					return pt.getNext();
			}
		}
		return null;
	}

	public Element getElement ()
	{
		return phrase;
	}

	public MalletSentence getSentence ()
	{
		return sentence;
	}
	
	public void setNext (MalletPhrase ph)
	{
		nextPhrase = ph;
	}

	public void setPrev (MalletPhrase ph)
	{
		prevPhrase = ph;
	}

	public MalletPhrase getNext ()
	{
		return nextPhrase;
	}

	public MalletPhrase getPrev ()
	{
		return prevPhrase;
	}
}
