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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mention
{
	Element sentence;  // sentence containing this mention
	Element element;  // Dom Element corresponding to mention
	Document document;
	MalletPhrase malletPhrase;
	File docPath;
    int mentionId;     // this is used for keeping an ordering over mentions

	// the below are used at performance time, really
	Mention antecedent; // nearest antecedent
	double  antecedentValue; // probability value associated with current antecedent
    //int  entityId;    // Id corresponding to entity to which this mention belongs
	int  numAntecedents;
	String gender;
	String string;
    String entityId;
    String entityRef;
    String uniqueEntityIndex;

    int newIdCount = 0;
	
	public Mention (MalletPhrase ph, File targetDocPath, Document doc,
			int id, String sourceType)
	{
		malletPhrase = ph;
		mentionId = id;
		element = ph.getElement();
		antecedent = null;
		numAntecedents = 0;
		antecedentValue = 0.0;
		string = getTextWithinElement (element);
		setEntityId (sourceType);
		docPath = targetDocPath;
		document = doc;
		//System.out.println(element.getName() + "--Mention: " + string + " -- ID: " + entityId
		//	   + " REF: " + entityRef);
	}

    public void setUniqueEntityIndex (String index)
    {
	uniqueEntityIndex = index;
    }

    public String getUniqueEntityIndex ()
    {
	return uniqueEntityIndex;
    }

    public int getId ()
    {
	return mentionId;
    }
	public Document getDocument()
	{
		return document;
	}

	public File getDocPath ()
	{
		return docPath;
	}

	private String getTextWithinElement (Element node)
	{
		StringBuffer substringBuf = new StringBuffer();
		List children = node.getContent();
		Iterator iterator = children.iterator();
		String intString;
		
		while (iterator.hasNext()) {
			Object o = iterator.next();
			if (o instanceof Element) {
				if (!((Element)o).getChildren().isEmpty())
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

	public Element getSentence()
	{
		return sentence;
	}

	public void setSentence(Element sent)
	{
		sentence = sent;
	}

	public void setGender (String gen)
	{
		gender = gen;
	}

	public String getGender()
	{
		return gender;
	}

	public String getString()
	{
		return string;
	}

    // XXXX this should be fixed to use the PreTerm structure via MalletPhrase
    public String getHead()
    {
	return malletPhrase.getHeadPreTerm().getString();
    }

	public MalletPhrase getMalletPhrase ()
	{
		return malletPhrase;
	}

	public String getEntityId ()
	{
		return entityId;
	}

    public String getEntityRef()
    {
	return entityRef;
    }


    public void setEntityId (String sourceType)
    {
	if (sourceType.equals("TB"))
	    this.entityId = findEntityIdTB();
	else {
	    if (element.getName().equals("COREF")) {
		this.entityId = findEntityIdMUC(element);
		this.entityRef = findEntityRefMUC(element);
	    }
	    else if (malletPhrase.getCorefElement() != null) {
		this.entityId = findEntityIdMUC(malletPhrase.getCorefElement());
		this.entityRef = findEntityRefMUC(malletPhrase.getCorefElement());
	    }
	    else {
		generateNewId();
	    }
	}
    }

    private String findEntityIdMUC (Element element)
    {
	String id = null;
	if (element.getAttributeValue("ID") != null)
	    return element.getAttributeValue("ID");
	else return null;
    }

    private String findEntityRefMUC (Element element)
    {
	String id = null;
	if (element.getAttributeValue("REF") != null)
	    return element.getAttributeValue("REF");
	else return null;
    }

    private void generateNewId ()
    {
	entityId = null; // this should generate a unique id for the singletons later
    }

    // check parent and children
	private Element getCorefNode (Element n)
	{
	    Element parent = (Element) n.getParent();
	    if (parent.getName().equals("COREF"))
		return parent; // give parent priority
	    List children = n.getContent();
	    Iterator i1 = children.iterator();
	    while (i1.hasNext()) {
		Object o = i1.next();
		if ((o instanceof Element) && (((Element)o).getName().equals("COREF"))) {
		    return (Element)o;
		}
	    }
	    return n;
	}


    private String findEntityIdTB ()
    {
	return getAppropriateStringFromElement(element);
    }

    	private String getAppropriateStringFromElement (Element e)
	{
	    Pattern checkForNP = Pattern.compile("NP.*--.*");	    
	    Matcher mCheck = checkForNP.matcher(e.getName());
	    String argVal = e.getAttributeValue("pos");
	    if (argVal != null) {
		Pattern checkLex  = Pattern.compile(".*OBJREF-.*");
		Matcher mLex = checkLex.matcher(argVal);
		if (mLex.matches())
		    return getLexId (e.getAttributeValue("pos"));
		else
		    return null;
	    }
	    if (mCheck.matches())
		return getNPId (e.getName());
	    return null;
	}

	private String getLexId (String lex)
	{
	    Pattern p1 = Pattern.compile(".*OBJREF-");
	    Pattern p2 = Pattern.compile("-.*");
	    Matcher m1 = p1.matcher(lex);
	    Matcher m2 = p2.matcher(m1.replaceFirst(""));
	    return m2.replaceFirst("");
	}

    private String getNPId (String np)
	{
	    Pattern p1 = Pattern.compile(".*--");
	    Pattern p2 = Pattern.compile("-.*");
	    Matcher m1 = p1.matcher(np);

	    Matcher m2 = p2.matcher(m1.replaceFirst(""));
	    return m2.replaceFirst("");
	}
	

	public Element getElement ()
	{
		return element;
	}

	public Mention getAntecedent ()
	{
		return antecedent;
	}

	public double getAntecedentValue()
	{
		return antecedentValue;
	}

	public void setAntecedent (Mention ant)
	{
		antecedent = ant;
	}

	public void setAntecedentCount (int count)
	{
		numAntecedents = count;
	}

	public int getAntecedentCount ()
	{
		return numAntecedents;
	}
	
}
