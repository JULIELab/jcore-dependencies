package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.StringDistance;
import com.wcohen.secondstring.NeedlemanWunsch;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.Citation;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.NodePair;

/**
 * SplitFieldStringDistancePipe
 * User: culotta
 * Email: culota@cs.umass.edu
 */

/** Uses a StringDistance on two perversions of the original
 * strings. (1) split the strings into two sections, based on the
 * delimiter given by "splitPattern". Calculate separate edit
 * distances for each. (2) If one string is longer than the other,
 * truncate the longer one from the end, and then from the beginning,
 * and calculate the edit distances on the two resulting strings */
public class SplitFieldStringDistancePipe extends Pipe {
	StringDistance distanceMeasure;
	private String[] fields;
	private String featureName;
	private String splitPattern;
	
	public SplitFieldStringDistancePipe(StringDistance distanceMeasure,
																 String[] fields, String featureName) {
		this.distanceMeasure = distanceMeasure;
		this.fields = fields;
		this.featureName = featureName;
	}
	
	public SplitFieldStringDistancePipe(StringDistance distanceMeasure,
																 String[] fields, String featureName, String splitPattern ) {
		this.distanceMeasure = distanceMeasure;
		this.fields = fields;
		this.featureName = featureName;
		this.splitPattern = splitPattern;
	}

	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();
		Citation c1 = (Citation)pair.getObject1();
		Citation c2 = (Citation)pair.getObject2();
		
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i];
			String f1 = c1.getField(fieldName);
			String f2 = c2.getField(fieldName);
			if (f1.length() > 0 && f2.length() > 0) {

				// split by pattern 
				if (splitPattern != null) { 
					// need original string to split on punctuation, since
					// normalizer removes punct
					String og1 = c1.getOrigString();
					String og2 = c2.getOrigString();
					f1 = SGMLStringOperation.locateAndConcatFields( fieldName, og1 );
					f2 = SGMLStringOperation.locateAndConcatFields( fieldName, og2 );
					String[] split1 = f1.split( splitPattern, 2 );
					String[] split2 = f2.split( splitPattern, 2 );
					// only apply if pattern matches at least one
					if (split1.length != 1 || split2.length != 1) {
						double dist = distanceMeasure.score( split1[0], split2[0] );
						pair.setFeatureValue( featureName+"_"+fieldName+"_FirstSplit"+getFeatureNameFromScore( dist ), 1.0 );

						if (split1.length == 2 && split2.length == 2 ){
							dist = distanceMeasure.score( split1[1], split2[1] );
							pair.setFeatureValue( featureName+"_"+fieldName+"_SecondSplit"+getFeatureNameFromScore( dist ), 1.0 );
						}							
					}
				}
				// split by length
				else {
					if (f1.length() != f2.length()) {
						String longer = f1;
						String shorter = f2;
						if (longer.length() < shorter.length()) {
							longer = f2;
							shorter = f1;							
						}
						// first get score when truncating from end
						int difference = longer.length() - shorter.length();
						String truncated = longer.substring( 0, longer.length()-difference );
						double dist = distanceMeasure.score( truncated, shorter );
						pair.setFeatureValue( featureName+"_"+fieldName+"_TruncatedEnd"+getFeatureNameFromScore( dist ), 1.0 );
						// now get score when truncating from start
						truncated = longer.substring( difference );
						dist = distanceMeasure.score( truncated, shorter );
						pair.setFeatureValue( featureName+"_"+fieldName+"_TruncatedBegin"+getFeatureNameFromScore( dist ), 1.0 );
					}
				}
			}
		}
		return carrier;
	}

	private String getFeatureNameFromScore (double dist) {
		if (dist >= 0.9)
			return "HIGH";
		else if (dist > 0.75)
			return "MED";
		else if (dist > 0.5)
			return "WEAK";
		else if (dist > 0.3)
			return "MIN";
		else 
			return "NONE";
	}
}
