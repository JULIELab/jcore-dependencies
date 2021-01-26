package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Heuristically guesses acronym of two venues. Binary feature is
 * true if acronyms exactly match.*/

public class VenueAcronymPipe extends Pipe
{
	final static Pattern startsCaps = Pattern.compile ("^[A-Z].*");
	final static Pattern allCaps = Pattern.compile ("^[A-Z]+$");
	final static String[] stop = {"of", "proceedings", "proc", "in", "the", "on", "and", "a", "an", "for"};

	public VenueAcronymPipe() 
	{
	}

	public Instance pipe (Instance carrier) 
	{
		NodePair pair = (NodePair)carrier.getData();
		Citation s1 = (Citation)pair.getObject1();
		Citation s2 = (Citation)pair.getObject2();

		String ss1 = SGMLStringOperation.locateField(Citation.booktitle,s1.getOrigString());
		String ss2 = SGMLStringOperation.locateField(Citation.booktitle,s2.getOrigString());		
		if (ss1.equals(""))
			ss1 = SGMLStringOperation.locateField(Citation.journal,s1.getOrigString());
		if (ss2.equals(""))
			ss2 = SGMLStringOperation.locateField(Citation.journal,s2.getOrigString());

		if(!ss1.equals("") && !ss2.equals("")){
			String ac1 = getAcronym (ss1);
			String ac2 = getAcronym (ss2);
			if (ac1.equals (ac2)) {
				pair.setFeatureValue("VenueAcronymMatch", 1.0);
				//System.err.println ("Word1: " + ss1 + " (" + ac1 + ") : Word2: " + ss2 + " (" + ac2 + ")");
			}
		}		
		return carrier;
	}

	private String getAcronym (String s) {
		String[] wds = s.split ("[\\s+\\p{Punct}]");
		String acr = "";
		for (int i=0; i < wds.length; i++) {
			if (isAllCaps (wds[i]) && wds[i].length() > 1) // acronym in string
				return wds[i]; 
			if (startsWithCaps(wds[i]) && !stopWord (wds[i])) {
				acr += wds[i].charAt(0);
		 	}
		}
		return acr;
	}

	private boolean startsWithCaps (String s) {
		Matcher m = startsCaps.matcher (s);
		return m.matches();
	}
	
	private boolean isAllCaps (String s) {
		Matcher m = allCaps.matcher (s);
		return m.matches();
	}
	
	private boolean stopWord (String s) {
		for (int i=0; i < stop.length; i++) {
			if (s.equalsIgnoreCase(stop[i]))
				return true;
		}
		return false;
	}

}
