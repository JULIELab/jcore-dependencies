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
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.*;
import org.jdom.output.XMLOutputter;


public class CorpusOutputter
{
	LinkedHashSet mentions;
	int          mentionId;
	
	public CorpusOutputter (LinkedHashSet m)
	{
		mentions = m;
	}

	public void begin ()
	{
		Vector   allDocuments  =  new Vector();
		Iterator iter = mentions.iterator();
		Document doc = null;
		File     targetFile = null;
		Mention  mention = null;
		Vector   ids = null;

		XMLOutputter outputter = new XMLOutputter();
		
		while (iter.hasNext()) {
			mention = (Mention)iter.next();
			if (mention.getDocument() != doc) {
				// new document				
				if (doc != null) {
					try {
						outputter.output (doc, new FileOutputStream (targetFile));
					} catch (IOException e) { e.printStackTrace(); }
				}
				mentionId = 10000;  // reset this
				allDocuments.add (mention.getDocument());
				ids = new Vector();
				doc = mention.getDocument();
				removeTestCorefAnnotations(doc); // remove coref annots to allow new
			}
			insertNewCorefAnnotation (mention, doc, ids);
			targetFile = mention.getDocPath();
			//System.out.println("Target File: " + targetFile.getAbsolutePath());
		}
		if (doc != null) {
			try {
				outputter.output (doc, new FileOutputStream (targetFile));
			} catch (IOException e) { e.printStackTrace(); }
		}
	}

	private void removeTestCorefAnnotations (Document doc)
	{
		List children = doc.getContent();
		Iterator childIterator = children.iterator();
		while (childIterator.hasNext()) {
			Object o = childIterator.next();
			if (o instanceof Element) {
				removeTestCorefAnnotations ((Element) o);
			}
		}
	}

	private void removeTestCorefAnnotations (Element el)
	{
		List children = el.getContent();
		Iterator childIterator = children.iterator();
		Vector toRemove = new Vector();
		while (childIterator.hasNext()) {
			Object o = childIterator.next();
			if (o instanceof Element) {
				if (((Element)o).getName().equals("COREF")) {
					toRemove.add(o);
				}
				removeTestCorefAnnotations ((Element) o);
			}
		}
		for (int i = 0; i < toRemove.size(); i++) {
			children.remove(toRemove.elementAt(i));
		}
	}

	private int findPosition (Element el, List list)
	{
		for (int i=0; i < list.size(); i++) {
			if (list.get(i) == el)
				return i;
		}
		return 0;
	}

	private void insertNewCorefAnnotation (Mention men, Document doc, Vector entityIds)
	{
		Element np = men.getElement();
		Element parent = (Element) np.getParent();
		List children = parent.getChildren();
		Element corefEl = new Element ("COREF");
		int position = findPosition (np, children);
		String sid = (String)men.getEntityId();
		int id = Integer.decode(sid).intValue();
		boolean newEntity = true;

		for (int i = 0; i < entityIds.size(); i++) {
		    if (id == ((Integer)entityIds.get(i)).intValue()) {
				newEntity = false;
				break;
			}
		}
		corefEl.addContent(np.detach());
		children.add(position, corefEl);
		if (newEntity) {
			entityIds.addElement(new Integer (id));
			Attribute attr = new Attribute ("ID", new Integer(id).toString());
			corefEl.setAttribute(attr);
		} else {
			Attribute attr1 = new Attribute ("ID", new Integer(mentionId++).toString());
			Attribute attr2 = new Attribute ("REF", new Integer(id).toString());
			Vector attrs = new Vector();
			attrs.add (attr1);
			attrs.add (attr2);
			corefEl.setAttributes(attrs);
		}				
		
	}
		

}
