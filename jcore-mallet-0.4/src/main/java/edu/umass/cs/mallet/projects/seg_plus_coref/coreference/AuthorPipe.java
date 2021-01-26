package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.StringDistance;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.util.CharSequenceLexer;
import edu.umass.cs.mallet.base.util.MalletLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


public class AuthorPipe extends Pipe {

	private static Logger logger = MalletLogger.getLogger(AuthorPipe.class.getName());

	StringDistance distMetric;
	//Levenstein nw;
	
	public AuthorPipe (StringDistance distMetric) {
		this.distMetric = distMetric;
		//this.nw = new Levenstein();
	}

	//private double normNW (String s1, String s2) {
	//	return 1 - (Math.abs(nw.score(s1,s2))/(double)(s1.length() + s2.length()));
	//}

	private void listOfStringsIntersection (List l1, List l2, int type) {

		List toRemoveList = new ArrayList();
		for (int i=0; i < l1.size(); i++) {
			int toRemove = -1;
			int j = 0;
			String s = (String)l1.get(i);
			//StringWrapper sw = new StringWrapper(s);
			for (j=0; j < l2.size(); j++) {
				if (type == 1) {
					//if (normNW(s,(String)l2.get(j)) > 0.9) {
					if (s.equals((String)l2.get(j))) {
						toRemove = j;
						toRemoveList.add(new Integer(i));
						break;
					}
				} else if (type == 2) {
					if (s.startsWith((String)l2.get(j))) {
						toRemove = j;
						toRemoveList.add(new Integer(i));
						break;
					}
				}
			}
			if (toRemove > -1)
				l2.remove(toRemove);
		}
		for (int k=0; k < toRemoveList.size(); k++) {
			int rem = (int)((Integer)toRemoveList.get(k)).intValue();
			l1.remove(rem-k);
		}
			
	}

	private void removeStops (String s) {
		s.replaceAll("and"," ");
		s.replaceAll("&"," ");
	}

	private void parseAuthorName (String author, Collection longTokens, Collection shortTokens) {
		// first remove punctuation
		String s1;
		String a1 = new String (author);
		CharSequenceLexer l1 = new CharSequenceLexer ();
		a1.replaceAll("[,.;]"," "); // replace all punctuation with space

		l1.setCharSequence(a1);
		while (l1.hasNext()) {
			s1 = (String)l1.next();
			if (s1.length() > 1) {
				longTokens.add(s1);
			} else if (s1.length() > 0) {
				shortTokens.add(s1);
			}
		}
		//System.out.println(" author longs: " + longTokens);
		//System.out.println(" author shorts: " + shortTokens);
		
	}

	private boolean containsEtAl (List l) {

		if (l.size() == 2) {
			for (int i=0; i < l.size(); i++) {
				if (!(((String)l.get(i)).equals("et") ||
							((String)l.get(i)).equals("al")))
					return false;
			}
			return true;
		} else {
			return false;
		}

	}

	public void extraPipe (List a1, List a2) {

	}

	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();
		Citation c1 = (Citation)pair.getObject1();
		Citation c2 = (Citation)pair.getObject2();

		List a1 = c1.getAuthors();
		List a2 = c2.getAuthors();

		List longs1 = new ArrayList();
		List shorts1 = new ArrayList();

		List longs2 = new ArrayList();
		List shorts2 = new ArrayList();

		/*
		if (!(a1.size() == a2.size()))
			pair.setFeatureValue("DiffNumAuthors", 1.0);
		*/
		Iterator i1 = a1.iterator();
		while (i1.hasNext()) {
			parseAuthorName ((String)i1.next(), longs1, shorts1);
		}
		Iterator i2 = a2.iterator();
		while (i2.hasNext()) {
			parseAuthorName ((String)i2.next(), longs2, shorts2);
		}

		int initialsize = longs1.size()+longs2.size()+shorts1.size()+shorts2.size();

		//System.out.println(" l1: " + longs1 + " l2: " + longs2);

		//System.out.println ("longs BEFORE: " + longs1);
		//System.out.println ("longs BEFORE: " + longs2);
		listOfStringsIntersection (longs1, longs2, 1);
		listOfStringsIntersection (shorts1, shorts2, 1);
		listOfStringsIntersection (longs1, shorts2, 2);
		listOfStringsIntersection (longs2, shorts1, 2);

		// set some features based on how this all happened

		/*
		if (initialsize > 0 && longs2.isEmpty() && longs1.isEmpty()) {
			pair.setFeatureValue("AuthorLongNamesMatch", 1.0);
			if (shorts2.isEmpty() && shorts2.isEmpty()) {
				pair.setFeatureValue("AuthorNamesMatch", 1.0);
			}
		} else if (initialsize > 0 && shorts2.isEmpty() && shorts2.isEmpty()) {
			//pair.setFeatureValue("AuthorShortNamesMatch", 1.0);
		}
		*/

		// et al is all that remains in one of them
		if (containsEtAl(longs1) || containsEtAl(longs2)) {
			logger.fine(">>>> ET AL <<<");
			longs1 = new ArrayList(); // reset these in this case
			longs2 = new ArrayList();
		}

		// this is intersection/union
		//System.out.println("initial size: " + initialsize);
		int topLong = longs1.size() + longs2.size();
		int topShort =  shorts1.size() + shorts2.size();

		if (initialsize > 0) {
			double overlapLong = 1 - ((double)topLong/(double)initialsize);
			double overlapShort = 1 - ((double)topShort/(double)initialsize);
			if ( overlapLong > 0.7 )
				pair.setFeatureValue("AuthorOverlapLongHigh", 1.0 );
			if ( overlapShort > 0.7)
				pair.setFeatureValue("AuthorOverlapShortHigh", 1.0 );
			if ( overlapLong > 0.45 && overlapLong < 0.7)
				pair.setFeatureValue("AuthorOverlapLongLow", 1.0 );
			if ( overlapShort > 0.45 && overlapLong < 0.7)
				pair.setFeatureValue("AuthorOverlapShortLow", 1.0 );
			if ( overlapLong <= 0.45)
				pair.setFeatureValue("AuthorOverlapLongNone", 1.0 );
			if ( overlapShort <= 0.45)
				pair.setFeatureValue("AuthorOverlapShortNone", 1.0 );
		}
		
		return carrier;
		
	}
	
}
																
