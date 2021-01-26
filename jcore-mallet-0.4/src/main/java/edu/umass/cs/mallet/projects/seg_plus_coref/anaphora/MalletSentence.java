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

import org.jdom.Element;

public class MalletSentence extends MalletDocumentElement
{

	MalletPreTerm preTerms;   // note: MalletSentence stores these as linked lists
    MalletPhrase  phrases;  // linked list as well but only WITHIN A SENTENCE, document holds as a Set
	Element      sentElement;
	int           index;
	MalletSentence prevSent;
	MalletSentence nextSent;

	public MalletSentence (Element s, int i, String sourceType)
	{
		preTerms = constructPreTerms (s, this); // should parameterize by source
		phrases = constructPhrases (s, preTerms, this, sourceType);
		sentElement = s;
		index  = i;
	}

	public void printPhrases ()
	{
		MalletPhrase cur = phrases;
		System.out.println("\nSent phrases: ");
		while (cur != null) {
		    cur.printPreTerms();
		    System.out.print("::");
		    cur = cur.getNext();
		}
	}
	
	public void printPreTerms ()
	{
		MalletPreTerm cur = preTerms;
		System.out.println("\nSent preTerms: ");
		while (cur != null) {
		    System.out.print(cur.getString() + " ");
		    cur = cur.getNext();
		}
	}

	public MalletPhrase getPhrases ()
	{
		return phrases;
	}

	public void setNext (MalletSentence s)
	{
		nextSent = s;
	}

	public void setPrev (MalletSentence p)
	{
		prevSent = p;
	}

	public MalletSentence getNext ()
	{
		return nextSent;
	}

	public MalletSentence getPrev ()
	{
		return prevSent;
	}

	public int getIndex ()
	{
		return index;
	}
																	 
	
}
