/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */


/**
 @author Ben Wellner
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.StringWrapper;
import edu.umass.cs.mallet.base.fst.CRF;
import edu.umass.cs.mallet.base.fst.Transducer;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Sequence;
import edu.umass.cs.mallet.base.types.TokenSequence;
import edu.umass.cs.mallet.projects.seg_plus_coref.ie.IEInterface;

import java.util.*;

//import edu.umass.cs.mallet.users.hay.canopy.Util;
//import edu.umass.cs.mallet.users.hay.canopy.NGramAnalyzer;
//import edu.umass.cs.mallet.users.hay.canopy.DateAnalyzer;

/**
 Objects of this class represent citations (in the citation
 domain).  This object essentially maintains all of field values
 for the citation in separate field slots.  This will make feature
 extraction easier an much more efficient.  Previsouly, we were essentially
 re-parsing the citation in each stage of the pipeline.
 */
public class Citation {


    // segmented fields
    public final static String author = "author";
    public final static String title = "title";
    public final static String booktitle = "booktitle";
    public final static String date = "date";
    public final static String pages = "pages";
    public final static String publisher = "publisher";
    public final static String address = "address";
    public final static String journal = "journal";
    public final static String conference = "conference";
    public final static String volume = "volume";
    public final static String paperID = "reference_no";
    public final static String paperCluster = "cluster_no";
    public final static String venueID = "venue_no";
    public final static String venueCluster = "venue_cluster";
    public final static String venueVolume = "venue_vol";
    public final static String tech = "tech";
    public final static String note = "note";
    public final static String institution = "institution";
    public final static String other = "O";
    public final static String editor = "editor";

    // *new* fields for coreference
    public final static String type = "type";  // booktitle and/or journal
    public final static String citation = "citation";
    public final static String authors = "authors";  // list of authors
    public final static String venue = "venue";  // booktitle and/or journal

    // list of valid fields that Pipes should use
    private static List corefFieldsList;
    public final static String[] corefFields = new String[] {
			address,author,authors,booktitle,citation,conference,date,
			editor,institution,journal,note,other,
			pages,publisher,
			title,tech,type,
			venue,venueVolume,volume
		};

    private final static String [] POSSIBLE_FIELDS =
            new String[]{address,author,authors,booktitle,citation,conference,date,
												 editor,institution,journal,note,other,
												 pages,paperCluster,paperID,publisher,
												 title,tech,type,
												 venue,venueCluster,venueID,venueVolume,volume};
	
    private Map fields;  // key: fieldname -> value: String field value
    private Map fieldTokens;
    private Map fieldTokensAsSets;
    private static CitationNormalizer normalizer;
    private String rawstring;
    private String origstring;
    private String underlyingString;
    	
    private ArrayList normalizedAuthors;
    private Object label;
    private int index;
    private ArrayList nBest;

    private double viterbiScore; // score of the selected viterbi path
    private double confidenceScore;

    public String getRawstring() {
        return rawstring;
    }

    public String getUnderlyingString () {
	return underlyingString;
    }
	
	public Citation (String s) {
        rawstring = s.toLowerCase();
        origstring = s;
				// xxx removes abstract from underlying string
				String noabstract = s.replaceAll("<abstract>[^<]+<\\/abstract>", " ");
				underlyingString = SGMLStringOperation.removeSGMLTags (noabstract);
				corefFieldsList = Arrays.asList(corefFields);
        normalizer = new CitationNormalizer();
        fields = new HashMap();
        fieldTokens = new HashMap();
        fieldTokensAsSets = new HashMap();
        parseCitation(s);
    }

    public Citation (String s, Object label, int index) {
        this(s);
        this.label = label;
        this.index = index;
    }

    public Citation (String s, Object label, int index, IEInterface ieInterface, int n) {
        this (s, label, index, ieInterface, n, 0);
    }

    public Citation (String s, Object label, int index, IEInterface ieInterface) {
        this (s, label, index, ieInterface, 1);
    }

    public Citation (String s, Object label, int index, IEInterface ieInterface, int n, int nthToUse) {
        this(s, label, index);
        //assert(n >= 1);
        // use the CRF to parse the string
        Instance instance;
        instance = new Instance(s, null, new Integer(index), null, ieInterface.pipe);

        Transducer.ViterbiPath_NBest viterbiP_NBest;
        CRF crf = ieInterface.crf;
        viterbiP_NBest = crf.viterbiPath_NBest((Sequence) instance.getData(), n);//n-best list

        //double [] confidenceScores = viterbiP_NBest.confidenceNBest();

        Sequence[] sequence_temp = viterbiP_NBest.outputNBest();
        TokenSequence tokenSequence = (TokenSequence) (instance.getSource());

        this.nBest = new ArrayList();

        for (int i=0; i < n; i++) {
            String crfStr = ieInterface.printResultInFormat(true, sequence_temp[i], tokenSequence);
            if (i == nthToUse) {  // this MAY not be the top viterbi path for
                // certain experiments

                viterbiScore = ieInterface.InstanceAccuracy (sequence_temp[i],
                        (Sequence)instance.getTarget(),
                        instance);

                crfStr = crfStr.toLowerCase(); // downcase now
                this.rawstring = crfStr;
                parseCitation(crfStr);

                //System.out.println("score on " + i + " path is: " + viterbiScore);
            }
            Citation none = new Citation(crfStr, label, index);
            none.setConfidenceScore(1.0);
            //none.setConfidenceScore (confidenceScores[i]);
            nBest.add(none);
        }
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore (double s) {
        this.confidenceScore = s;
    }

    public double getScore() {
        return viterbiScore;
    }

    private void parseCitation(String s) {

        // set fields
        setField(address, normalizer.norm(SGMLStringOperation.locateAndConcatFields(address, s)));
        setField(author, normalizer.norm(normalizer.getAlphaOnly(SGMLStringOperation.locateAndConcatFields(author, s))));
        setField(authors, normalizer.norm(normalizer.getAlphaOnly(SGMLStringOperation.locateAndConcatFields(authors, s))));
        setField(booktitle, normalizer.norm(SGMLStringOperation.locateAndConcatFields(booktitle, s)));
//        setField(citation, normalizer.norm(SGMLStringOperation.removeSGMLTags(s)));
        setField(citation, normalizer.norm(s));
        setField(conference, normalizer.norm(SGMLStringOperation.locateAndConcatFields(conference, s)));
        setField(date, normalizer.getFourDigitString(SGMLStringOperation.locateAndConcatFields(date, s)));
        setField(editor, normalizer.norm(normalizer.getAlphaOnly(SGMLStringOperation.locateAndConcatFields(editor, s))));
        setField(institution, normalizer.norm(SGMLStringOperation.locateAndConcatFields(institution, s)));
        setField(journal, normalizer.norm(SGMLStringOperation.locateAndConcatFields(journal, s)));
        setField(note, normalizer.norm(SGMLStringOperation.locateAndConcatFields(note, s)));
        setField(other, normalizer.norm(SGMLStringOperation.locateAndConcatFields(other, s)));
        setField(pages, normalizer.getNumericOnly(SGMLStringOperation.locateAndConcatFields(pages, s)));
        setField(publisher, normalizer.norm(SGMLStringOperation.locateAndConcatFields(publisher, s)));
        setField(title, normalizer.norm(SGMLStringOperation.locateAndConcatFields(title, s)));
        setField(tech, normalizer.norm(SGMLStringOperation.locateAndConcatFields(tech, s)));
        setField(volume, normalizer.getNumericOnly(SGMLStringOperation.locateAndConcatFields(volume, s)));

        setAuthors(); // List of authors
        setVenue();   // = journal and/or booktitle
        setType();

				HashMap clusterAttributes = SGMLStringOperation.locateAttributes ("meta", s);
				String pcid = (String)clusterAttributes.get (paperCluster);
				String pid = (String)clusterAttributes.get (paperID);
				if (pcid == null)
					throw new IllegalArgumentException ("Paper has no cluster: " + s);
				if (pid == null)
					throw new IllegalArgumentException ("Paper has no id: " + s);
				setField (paperCluster, pcid);
				setField (paperID, pid);			
    }

    public String getField(String fieldName) {
			//assert (corefFieldsList.contains(fieldName));
        String s = (String)fields.get(fieldName);
				return (s==null) ? "" : s;
    }

    public List getFieldTokens(String fieldName) {
        assert (corefFieldsList.contains(fieldName));
        return (List)fieldTokens.get(fieldName);
    }

    public boolean hasField(String fieldName) {
        assert (corefFieldsList.contains(fieldName));
        String value = getField(fieldName);
        if (value == null) {
            return false;
        } else if (value.length() == 0) {
            return false;
        }
        return true;
    }


    private void setField(String fieldName, String fieldValue) {
        //assert (corefFieldsList.contains(fieldName));
        fields.put(fieldName, fieldValue);
        fieldTokens.put(fieldName, normalizer.getTokens(fieldValue));
        fieldTokensAsSets.put(fieldName,normalizer.getTokensAsSet(fieldValue));
    }

    public Set getFieldTokensAsSet(String fieldName) {
        assert (corefFieldsList.contains(fieldName));
        return (Set)fieldTokensAsSets.get(fieldName);
    }

    private void setAuthors() {
        normalizedAuthors = new ArrayList();
        List authors = SGMLStringOperation.locateFields(author, rawstring);
        for (int i = 0; i < authors.size(); i++) {
            String authorStr = (String) authors.get(i);
            normalizedAuthors.add(normalizer.authorNorm(authorStr));
        }
    }

    public List getAuthors() {
        return normalizedAuthors;
    }

    public int getNumAuthors() {
        return normalizedAuthors.size();
    }

    private void setVenue() {
        String booktitleStr = null;
        String journalStr = null;
        String techStr = null;
        booktitleStr = normalizer.norm(SGMLStringOperation.locateAndConcatFields(booktitle, rawstring));
        journalStr = normalizer.norm(SGMLStringOperation.locateAndConcatFields(journal, rawstring));
        techStr = normalizer.norm(SGMLStringOperation.locateAndConcatFields(tech,rawstring));
        String confStr = normalizer.norm(SGMLStringOperation.locateAndConcatFields(conference,rawstring));
				HashMap venueAttributes = new HashMap();
        if (booktitleStr.equals("") && techStr.equals("") && confStr.equals("")) {
					venueAttributes = SGMLStringOperation.locateAttributes (journal, rawstring);
					fields.put(venue, journalStr);
        } else if (journalStr.equals("") && techStr.equals("") && confStr.equals("")) {
					fields.put(venue, booktitleStr);
					venueAttributes = SGMLStringOperation.locateAttributes (booktitle, rawstring);
        } else if (booktitleStr.equals("") && journalStr.equals("") && confStr.equals("")) {
					fields.put(venue, techStr);
        } else if (booktitleStr.equals("") && techStr.equals("") && journalStr.equals("")) {
					fields.put(venue, confStr);
				}
				else {
            fields.put(venue, journalStr + booktitleStr + techStr + confStr);
        }				
				if (venueAttributes.size() != 0) {
					fields.put (venueCluster, venueAttributes.get (venueCluster).toString());
					fields.put (venueVolume, venueAttributes.get (venueVolume).toString());
					fields.put (venueID, venueAttributes.get (venueID).toString());
				}
    }

    public Object getLabel() {
        return label;
    }

    public int getIndex() {
        return index;
    }

    public List getAllStringsWrapped() {
        return getAllStringsWrapped(corefFields);
    }

    public List getAllStringsWrapped(String[] fields) {
        ArrayList allStrings = new ArrayList();
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i];
            allStrings.add(new StringWrapper(getField(fieldName)));
        }
        return allStrings;
    }

    public List getNBest() {
        return nBest;
    }

    public Citation getNthBest(int i) {
        if (nBest.size() < (i-1))
            return null;
        return (Citation)nBest.get(i);
    }
    public Citation() {}

    public String getOrigString() {
        return origstring;
    }

    public String getString() {
        return rawstring;
    }

    public void setString (String s) {
        this.rawstring = s;
    }

    public Map getFields() {
        return fields;
    }
    public void setField(Object key, Object val) {
        fields.put(key, val);
    }

    public String print () {
        Integer i = new Integer(getIndex());
        return i.toString();
    }

    public String[] getPossibleFields() {
        return POSSIBLE_FIELDS;
    }
	


    public boolean isConferencePaper () {
        String booktitle = getField("booktitle");
        if (booktitle.matches(".*proc\\..*") ||
                booktitle.matches(".*proceedings.*") ||
                booktitle.matches(".*workshop.*") ||
                booktitle.matches(".*conference.*"))
            return true;
        return false;
    }

    public boolean isJournalPaper() {
        String journal = getField("journal");
        if (journal.length() > 0)
            return true;
        String volume = getField("volume");
        if (volume.length() > 0)
            return true;
        return false;
    }

    public boolean isTechPaper() {
        String journal = getField("journal");
        if (journal.length() > 0)
            return false;
        String volume = getField("volume");
        if (volume.length() > 0)
            return false;
        String tech = getField("tech");
        if (tech.length() > 0)
            return true;
        return false;
    }

	private void setType() {
		String btitle = null;
		String jnl = null;
		String tch = null;
		btitle = (String)fields.get(this.booktitle);
		jnl = (String)fields.get(this.journal);
		tch = (String)fields.get(this.tech);
		assert(btitle != null);
		assert(jnl != null);
		assert(tch != null);
		String type = "";
		
		if (isConferencePaper()) {
			type = "conference";
		} else if (isJournalPaper()) {
			type = "journal";
		} else if (isTechPaper()) {
			type = "tech";
		} else {
					type = "uncertain";
		}
		setField(this.type, type);
	}
	
	public String toString () {
		List alls = getAllStringsWrapped();
		String ret = "Original string: " + origstring + "-->Fields: " + fields;
		return ret;
	}


	private static final String delimiter = "###";

	/** Construct a new Citation object from a serialized String of fields. */
	public static Citation deserializeFromString (String s) {
		int size, index;
	 	String[] toks = s.split (delimiter);
		Citation ret = new Citation ();
		index = 0;
		size = Integer.parseInt (toks[index++]);
		ret.fields = new HashMap();
		index = string2HashMap (toks, index, size, ret.fields); // inclusive

		size = Integer.parseInt (toks[index++]);
		ret.fieldTokens = new HashMap ();
		index = string2HashMapOfArrayLists (toks, index, size, ret.fieldTokens);

		size = Integer.parseInt (toks[index++]);
		ret.fieldTokensAsSets = new HashMap();
		index = string2HashMapOfSets (toks, index, size, ret.fieldTokensAsSets);

		ret.rawstring = toks[index++];
		ret.origstring = toks[index++];
		ret.underlyingString = toks[index++];
		
		size = Integer.parseInt (toks[index++]);
		ret.normalizedAuthors = new ArrayList ();
		index = string2ArrayList (toks, index, size, ret.normalizedAuthors);
		//unserialized: index, label, nBest, viterbiScore, confidenceScore
		if (index != toks.length)
			throw new IllegalArgumentException ("There are additional fields remaining to be deserialized in string " + s);
		return ret;
	}

	private static int string2ArrayList (String[] toks, int start, int size, List ret) {
		for (int i=start; i < start+size; i++)
			ret.add (toks[i]);
		return start+size;
	}
	
	private static int string2HashSet (String[] toks, int start, int size, Set ret) {
		for (int i=start; i < start + size; i++)
			ret.add (toks[i]);
		return start+size;
	}

	private static int string2HashMap (String[] toks, int start, int size, Map ret) {
		for (int i=start; i < start+size*2; i+=2) {
			ret.put (toks[i], toks[i+1]);
		}
		return start+size*2;
	}
	
	private static int string2HashMapOfArrayLists (String[] toks, int start, int size, Map ret) {
		int index = start;
		int numKeys = 0;
		while (numKeys < size) {
			String key = toks[index++];
			numKeys++;
			int csize = Integer.parseInt (toks[index++]);
			ArrayList list = new ArrayList();
			index = string2ArrayList (toks, index, csize, list);
			ret.put (key, list);
		}
		return index;
	}

	private static int string2HashMapOfSets (String[] toks, int start, int size, Map ret) {
		int index = start;
		int numKeys = 0;
		while (numKeys < size) {
			String key = toks[index++];
			numKeys++;
			int csize = Integer.parseInt (toks[index++]);
			Set set = new HashSet ();
			index = string2HashSet (toks, index, csize, set);
			ret.put (key, set);
		}
		return index;
	}

	/** Serializes attributes to a String, to be deserialized by
	 * <code>readString</code>*/
 	public String serializeToString () {
	 	String ret = "";
		ret += fields.size() + delimiter;
		ret += map2String (fields);
		ret += fieldTokens.size() + delimiter;
		ret += mapOfLists2String (fieldTokens);
		ret += fieldTokensAsSets.size() + delimiter;
		ret += mapOfSets2String (fieldTokensAsSets);
		ret += rawstring + delimiter;
		ret += origstring + delimiter;
		ret += underlyingString + delimiter;
		ret += normalizedAuthors.size() + delimiter;
		ret += list2String (normalizedAuthors);
		//unserialized: index, label, nBest, viterbiScore, confidenceScore
		return ret;
	}

	private String list2String (List l) {
		String ret = "";
		Iterator iter = l.iterator();
		while (iter.hasNext())
			ret += iter.next().toString() + delimiter;
		return ret;
	}
	
	private String map2String (Map m) {
		String ret = "";
		Iterator keyIter = m.keySet().iterator();
		while (keyIter.hasNext()) {
			Object key = keyIter.next();
			Object val = m.get(key);
			ret += key.toString() + delimiter + val.toString() + delimiter;
		}
		return ret;
	}

	private String set2String (Set s) {
		String ret = "";
		Iterator iter = s.iterator();
		while (iter.hasNext()) {
			Object val = iter.next();
			ret += val.toString() + delimiter;
		}
		return ret;
	}

	private String mapOfLists2String (Map m) {
		String ret = "";
		Iterator keyIter = m.keySet().iterator();
		while (keyIter.hasNext()) {
			Object key = keyIter.next();
			List list = (List)m.get(key);
			ret += key.toString() + delimiter + list.size() + delimiter + list2String(list);
		}
		return ret;		
	}

	private String mapOfSets2String (Map m) {
		String ret = "";
		Iterator keyIter = m.keySet().iterator();
		while (keyIter.hasNext()) {
			Object key = keyIter.next();
			Set set = (Set)m.get(key);
			ret += key.toString() + delimiter + set.size() + delimiter + set2String(set);
		}
		return ret;		
	}

}


