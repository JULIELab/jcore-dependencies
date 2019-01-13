package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.*;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.classify.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.*;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;
import java.util.logging.*;
import java.lang.*;
import java.io.*;


/** features about regex matches in a field */
public class RegexPipe extends Pipe {

	private static Logger logger = MalletLogger.getLogger(RegexPipe.class.getName());

	String[] fields;
	String regex, featureName;
	
	public RegexPipe (String featureName, String[] fields, String regex) {
		this.featureName = featureName;
		this.fields = fields;
		this.regex = regex;
	}


	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();
		Citation c1 = (Citation)pair.getObject1();
		Citation c2 = (Citation)pair.getObject2();

		for (int i=0; i < fields.length; i++) {
			boolean m1 = c1.getField( fields[i] ).matches( this.regex );
			boolean m2 = c2.getField( fields[i] ).matches( this.regex );
			if (m1 && m2)
				pair.setFeatureValue( featureName+"_"+fields[i]+"_MatchesBoth_", 1.0 );
			else if (m1 || m2) {
				pair.setFeatureValue( featureName+"_"+fields[i]+"_MatchesOne_", 1.0 );
			}
		}

		return carrier;		
	}	
	
}
																
