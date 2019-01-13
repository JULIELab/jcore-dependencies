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
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.projects.seg_plus_coref.anaphora.*;


public class MentionPairIterator extends AbstractPipeInputIterator
{
    public static final int NP_LOOKBACK = 20000;  // need to change this to look at sentences
    
    FileIterator fileIterator;
    public static final String [] pronouns = new String[] {"He", "he", "Him",
							   "him",
							   "His", "his",
							   "She", "she", "Her",
							   "her", "hers", "it",
							   "It",
							   "its",
							   "Its", "itself",
							   "himself",
							   "herself"};
    
    /*
      public static final String [] pronouns = new String[] {"He", "he", "Him",
      "him", "They",
      "they", "I", "We",
      "we", "Our",
      "our","His", "his",
      "She", "she", "Her",
      "her", "hers", "it",
      "their", "It",
      "Their", "its",
      "Its", "itself",
      "himself", "herself",
      "ourselves",
      "themselves"};
    */
    public static final String [] malePronouns = new String[] {"He", "he", "Him", "him", "His", "his", "himself"};
    public static final String [] femalePronouns = new String[] {"She","she","Her","her","hers","herself"};
    public static final int pronounsSize = 18;
    public static final int numMalePronouns = 7;
    public static final int numFemalePronouns = 6;
    
    
    int        refIndex = 0;
    SAXBuilder builder = null;
    Document currentDocument;  // JDOM structure
    MalletDocument malletDocument; // Mallet document structure, optimized for access
    DocumentMentionPairIterator docNodePairIterator;
    java.util.Vector    allDocuments;
    File       targetDocPath;
    boolean   positiveAntecedent = false;
    String    sourceType = "";
    boolean   addNullAntecedent;
    int       numberOfReferents = 0;
    boolean   includeProperNouns = false;
    boolean   includeEverything = false;
    List      filters;
    ArrayList    nodePairArray;
    
    public MentionPairIterator (FileIterator fi, String sourceType)
    {
	this (fi, sourceType, true);
    }

    public MentionPairIterator (FileIterator fi, String sourceType, boolean addNullAntecedent,
				boolean includeNNPs, boolean includeAll, List filters)
    {
	this.includeProperNouns = includeNNPs;
	this.sourceType = sourceType;
	this.addNullAntecedent = addNullAntecedent;
	this.includeEverything = includeAll;
	this.filters = filters;
	constructAux(fi, sourceType);
    }

    public MentionPairIterator (FileIterator fi, String sourceType, boolean addNullAntecedent,
				boolean includeNNPs)
    {
	this.includeProperNouns = includeNNPs;
	this.sourceType = sourceType;
	this.addNullAntecedent = addNullAntecedent;
	constructAux(fi, sourceType);
    }
    
    public MentionPairIterator (FileIterator fi, String sourceType, boolean addNullAntecedent)
    {
	this.sourceType = sourceType;
	this.addNullAntecedent = addNullAntecedent;
	constructAux(fi, sourceType);
    }

    private void constructAux (FileIterator fi, String sourceType)
    {
	fileIterator = fi;
	builder = new SAXBuilder();
	allDocuments = new java.util.Vector();
	if ((currentDocument == null) && fileIterator.hasNext()) {
	    try {
		File openDocPath = (File)fileIterator.nextInstance().getData();
		targetDocPath = new
		    File(((String)openDocPath.getAbsolutePath()).concat(".sys"));
		currentDocument = builder.build(openDocPath);
		malletDocument = new MalletDocument(currentDocument, sourceType);
		allDocuments.add(currentDocument);
		docNodePairIterator = new DocumentMentionPairIterator();
	    } catch (JDOMException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
	}
    }

    
    public int getNumReferents ()
    {
	return numberOfReferents;
    }
    
    public java.util.Vector getAllDocuments ()
    {
	return allDocuments;
    }
    
    public MentionPair getNextMentionPairFromDocument()
    {
	if (docNodePairIterator.hasNext())
	    return (MentionPair)docNodePairIterator.next();
	else
	    return null;
    }		
    
    public MentionPair getNextMentionPair ()
    {
	MentionPair pair = null;
	File openDocPath = null;
	
	while (((pair = (MentionPair)docNodePairIterator.next()) == null) && currentDocument != null)
	    {
		if (fileIterator.hasNext()) {
		    try {
			openDocPath = (File)fileIterator.nextInstance().getData();
			targetDocPath = new
			    File(((String)openDocPath.getAbsolutePath()).concat(".sys"));					
			currentDocument = builder.build(openDocPath);
			malletDocument = new MalletDocument (currentDocument, sourceType);
		    } catch (JDOMException e) { e.printStackTrace(); }
          catch (IOException e) { e.printStackTrace(); }
		    docNodePairIterator = new DocumentMentionPairIterator();
		}
	    }
	return pair;
    }
    
    public Instance nextInstance()
    {
	MentionPair pair = null;
	String    target = null;
	URI      nodePairURI = null;
	Mention     referent = null;
	
	if (this.hasNext()) {
	    pair = (MentionPair)getNextMentionPair();
	    if (pair.nullPair()) {
		if (!positiveAntecedent) {
		    target = "yes";
		}
		else {
		    target = "no";
		}
		positiveAntecedent = false;
	    } else {
		String tval = null;
		tval = docNodePairIterator.getTargetValue(pair);
		if (tval != null) {
		    target = new String("yes");
		    pair.setEntityReference(tval);
		} else {
		    pair.setEntityReference(null);
		    target = new String("no");
		}
		if (target.equals("yes")) {
		    positiveAntecedent = true;
		}
	    }
	    try {
		nodePairURI = new URI("nodePairURI");
	    } catch (Exception e) { e.printStackTrace(); }
	}
	// include pair as the "source" of this pipe object because we want this to carry
	// into the feature vector
	return new Instance (pair, target, nodePairURI, null);
    }
    
    // Iterator methods
    public boolean hasNext() { return (docNodePairIterator.hasNext() || fileIterator.hasNext()); }
    public void remove () { throw new UnsupportedOperationException(); }
    
    public class DocumentMentionPairIterator implements Iterator
    {

	int              pairCount;
	int           currentIndex;
	
	public DocumentMentionPairIterator ()
	{
	    java.util.Vector candPatterns = new java.util.Vector();
	    nodePairArray = new ArrayList ();
	    //should parameterize this:
	    //candPatterns.add(Pattern.compile("[A-Z]*NAME[A-Z]*"));
	    //candPatterns.add(Pattern.compile("PRO[A-Z]*"));
	    if (sourceType.equals("TB")) {
		candPatterns.add(Pattern.compile("NP.*")); // for TreeBank
		candPatterns.add(Pattern.compile("lex"));
	    } else if (sourceType.equals("MUC")) {
		candPatterns.add(Pattern.compile("NG")); // for the MUC data
		candPatterns.add(Pattern.compile("COREF")); // for the MUC data
	    }
	    
	    fillNodePairArray (candPatterns);
	    nodePairArray = filterPairs();
	    pairCount = nodePairArray.size();
	    System.out.println("Pair array size = " + pairCount);
	    currentIndex = 0;
	}

	private ArrayList filterPairs ()
	{
	    ArrayList newList = new ArrayList();
	    Iterator i = filters.iterator();
	    while (i.hasNext()) {
		Filter filter = (Filter)i.next();
		Iterator i2 = nodePairArray.iterator();
		while (i2.hasNext()) {
		    MentionPair pair = (MentionPair)i2.next();
		    if (!filter.filters(pair)) {
			/*
			if (pair.getAntecedent() != null)
			    System.out.println("Pair: " + pair.getAntecedent().getString() + " " + pair.getReferent().getString());
			else
			System.out.println("Pair: " + "NULL" + " " + pair.getReferent().getString()); */
			newList.add(pair);
		    }
		}
	    }
	    return newList;
	}
	
	private boolean compatible (Element n, java.util.Vector patterns)
	{
	    if (sourceType.equals("TB")) {
		List children = n.getChildren();
		Iterator iter = children.iterator();
		Pattern p1 = Pattern.compile("NP.*");
		while (iter.hasNext()) {
		    Element e1 = (Element)iter.next();
		    Matcher m = p1.matcher(e1.getName());
		    if (m.matches())
			return false;
		}
	    }
	    for (int i=0; i < patterns.size(); i++) {
		Matcher m = ((Pattern)patterns.elementAt(i)).matcher(n.getName()); // matches against Element Name
		if (m.matches()) {
		    return true;
		}
	    }
	    return false;
	}

	private void printMentions (java.util.Vector mentions)
	{
	    for (int i=0; i < mentions.size(); i++) {
		Mention m = (Mention)mentions.elementAt(i);
		System.out.println("Mention: " + m.getString() + " -- " + m);
	    }
	}
	
	private int fillNodePairArray(java.util.Vector patterns)
	{
	    java.util.Vector nodes = new java.util.Vector();
	    java.util.Vector mentions = new java.util.Vector();
	    Element outsideNode;
	    Element insideNode;
	    getAllNodes(malletDocument,nodes,patterns);
	    mentions = convertToMentions(nodes);
	    //printMentions(mentions);
	    int pairCount = 0;
	    Mention ant,ref;
	    
	    for (int i=0; i < mentions.size(); i++) {
		ref = (Mention)mentions.elementAt(i);
		outsideNode = ref.getElement();
		if ((compatible (outsideNode, patterns)) && validReferent(ref))
		    {
			for (int j=i-1; (j > -1 && (i - j) < NP_LOOKBACK) ; j--)
			    {
				ant = (Mention)mentions.elementAt(j);
				insideNode = ant.getElement();
				// added check to ensure antecedent isn't empty (can happen with Treebank)
				if ((ant.getString().length() > 0) && (compatible (insideNode, patterns))) {
				    MentionPair pair = new MentionPair(ant,ref);
				    ant.setSentence(getSentenceParent(ant.getElement()));
				    ref.setSentence(getSentenceParent(ref.getElement()));
				    ant.setGender(findGender(ant.getString()));
				    ref.setGender(findGender(ref.getString()));
				    //pair.setFeatureValue("NPDistance", i - j); // feature set here (more efficient)
				    pair.setReferentIndex(i);
				    nodePairArray.add((Object)pair);
				    pairCount++;
				}
			    }
			// only add null antecedent if we're supposed to
			if (addNullAntecedent) {
			    MentionPair nullPair = new MentionPair(null,ref);
			    nullPair.setReferentIndex(i);
			    nodePairArray.add((Object)nullPair); // the null pair HAS to be
			}
			// the last pair of a particular referent in order for the code
			// that determines whether a null pair is a positive instance or
			// not to work
		    }
	    }
	    return pairCount;
	}
	
	public java.util.Vector convertToMentions(java.util.Vector nodes)
	{
	    int curId = 1;
	    Iterator iterator = nodes.iterator();
	    java.util.Vector mentions = new java.util.Vector();
	    while (iterator.hasNext()) {
		mentions.add(new Mention((MalletPhrase)iterator.next(),
					 targetDocPath, currentDocument, curId, sourceType));
		curId++; // give unique ids to mentions now
	    }
	    return mentions;
	}
	
	private String findGender (String string)
	{
	    for (int i=0; i < numMalePronouns; i++) {
		if (((String)malePronouns[i]).equals(string))
		    return "male";
	    }
	    for (int i=0; i < numFemalePronouns; i++) {
		if (((String)femalePronouns[i]).equals(string))
		    return "female";
	    }
	    return "unknown";
	}
	
	private Element getSentenceParent (Element node)
	{
	    if (node != null) {
		if ((node.getName().equals("S")) || (node.getName().equals("s"))) {
		    return node;
		}
		return getSentenceParent((Element) node.getParent());
	    }
	    return null;
	}

	private boolean validReferent (Mention referent)
	{
	    if (includeEverything) {
		if (sourceType.equals("MUC"))
		    return true;
		else {
		    if (referent.getElement().getName().equals("lex")) {
			if (MentionPairIterator.referentPronoun (referent)) {
			    return true;
			} else {
			    return false;
			}
		    } else {
			return true;
		    }
		}
	    }		
	    else if (includeProperNouns)
		return (MentionPairIterator.referentProperNoun (referent) || 
			MentionPairIterator.referentPronoun (referent));
	    else
		return MentionPairIterator.referentPronoun(referent);
	}
	
	private void getAllNodes (MalletDocument malletDoc, java.util.Vector allNodes,
				  java.util.Vector patterns)
	{
	    //System.out.println("Nodes from malletDoc construction: " );
	    //printAllNodesInContext(malletDoc.getPhrases());
	    Iterator phraseIter = malletDoc.getPhrases().iterator();
	    while (phraseIter.hasNext())
		{
		    MalletPhrase ph = (MalletPhrase)phraseIter.next();
		    if ((compatible (ph.getElement(),patterns)) ||
			((ph.getElement().getName().equals("lex") &&
			  argumentsIncludeCoreferentialInfo(ph)))) {
			allNodes.add(ph);
		    }
		}
	    //System.out.println("Actual nodes: ");
	    //printAllNodesInContext(allNodes); // XXXX Debugging
	}

	public void printAllNodesInContext (java.util.Vector nodes)
	{
	    for (int i = 0; i < nodes.size(); i++) {
		MalletPhrase ph = (MalletPhrase)nodes.elementAt(i);
		if (ph.getPreceedingPreTerm() != null)
		    System.out.print(ph.getPreceedingPreTerm().getString() + " ");
		else
		    System.out.print("NULL ");
		ph.printPreTerms();
		if (ph.getFollowingPreTerm() != null)
		    System.out.println(" " + ph.getFollowingPreTerm().getString());
		else
		    System.out.println(" NULL");
	    }
	}

	public void printAllNodesInContext (Set nodes)
	{
	    Iterator iter = nodes.iterator();
	    while (iter.hasNext()) {
		MalletPhrase ph = (MalletPhrase)iter.next();
		if (ph.getPreceedingPreTerm() != null)
		    System.out.print(ph.getPreceedingPreTerm().getString() + " ");
		else
		    System.out.print("NULL ");
		ph.printPreTerms();
		if (ph.getFollowingPreTerm() != null)
		    System.out.println(" " + ph.getFollowingPreTerm().getString());
		else
		    System.out.println(" NULL");
	    }
	}
	
	private boolean argumentsIncludeCoreferentialInfo (MalletPhrase ph)
	{
	    Pattern p = Pattern.compile(".*OBJREF-.*");
	    List attrs = ph.getElement().getAttributes();
	    Iterator i1 = attrs.iterator();
	    while (i1.hasNext()) {
		Attribute a = (Attribute)i1.next();
		Matcher m = p.matcher(a.getName());
		if (m.matches())
		    return true;
	    }
	    return false;
	}

	private String getTargetValue (MentionPair pair)
	{
	    return pair.getEntityReference();
	}
	
	private void updateMentionFeatures (MentionPair pair)
	{
	    Mention ant = pair.getAntecedent();
	    Mention ref = pair.getReferent();
	    ref.setAntecedentCount (ant.getAntecedentCount() + 1); // update the antecedent count
	}
	
	private boolean hasNextNodePair()
	{
	    return (currentIndex < pairCount);
	}
	
	private MentionPair nextNodePair()
	{
	    int index;
	    if (currentIndex < pairCount) {
		index = currentIndex;
		currentIndex++;
		MentionPair curPair = (MentionPair)nodePairArray.get(index);
		return curPair;
	    } else { return null; }
	    
	}
	
	public boolean hasNext() { return hasNextNodePair(); }
	public Object next() { return nextNodePair(); }
	public void remove () { throw new UnsupportedOperationException(); }
    }
    
    public static Set partitionIntoDocumentInstances (InstanceList allInstances)
    {
	Set      setOfDocInstances = new LinkedHashSet();
	Iterator instIterator = allInstances.iterator();
	Document  curDoc = null;
	ArrayList curList = null;
	while (instIterator.hasNext()) {
	    Instance inst = (Instance)instIterator.next();
	    Document doc = ((MentionPair)inst.getSource()).getReferent().getDocument();
	    if (curDoc != doc) { // we have something from a new document
		if (curList != null)
		    setOfDocInstances.add(curList);
		curList = new ArrayList();
		curDoc = doc; // update curDoc to new doc
	    }
	    curList.add(inst);
	}
	setOfDocInstances.add(curList);
	return setOfDocInstances;
    }


	// NOTE: we could have issues where there are part of speech errors here
	public static boolean referentProperNoun (Mention referent)
	{
	    MalletPhrase ph = referent.getMalletPhrase();
	    MalletPreTerm pt = ph.getHeadPreTerm();

	    if ((pt.getPartOfSpeech() != null) && (pt.getPartOfSpeech().equals("NNP")))
		return true;
	    else
		return false;
	}
	
	public static boolean referentPronoun (Mention referent)
	{
	    String refString = referent.getString();
	    for (int i=0; i < pronounsSize; i++) {
		if (((String)pronouns[i]).equals(refString)) {
		    return true;
		}
	    }
	    return false;
	}

    
    public static void main(String[] args)
    {
	if (args.length != 1) {
	    System.err.println ("Usage: "+TUI.class.getName()+" <directory of ACE files>");
	    System.exit(-1);
	}
	
	// This iterator takes a directory and iterates over the files contained in it
	FileIterator fileIterator = new FileIterator (new File(args[0]));
	MentionPairIterator pairIterator = new MentionPairIterator (fileIterator, "TB");
	while (pairIterator.hasNext()) {
	    pairIterator.next();
	}
    }
}
