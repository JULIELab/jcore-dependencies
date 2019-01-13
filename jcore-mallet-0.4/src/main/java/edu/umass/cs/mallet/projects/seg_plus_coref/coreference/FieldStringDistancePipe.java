package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import com.wcohen.secondstring.StringDistance;
import com.wcohen.secondstring.NeedlemanWunsch;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.Citation;
import edu.umass.cs.mallet.projects.seg_plus_coref.coreference.NodePair;

/**
 * FieldStringDistancePipe
 * User: mhay
 * Email: mhay@cs.umass.edu
 * Date: Feb 18, 2004 4:40:24 PM
 */

public class FieldStringDistancePipe extends Pipe {
	StringDistance distanceMeasure;
	private String[] fields;
	private String featureName;
	
	
	public FieldStringDistancePipe(StringDistance distanceMeasure,
																 String[] fields, String featureName) {
		this.distanceMeasure = distanceMeasure;
		this.fields = fields;
		this.featureName = featureName;
	}
	
	public Instance pipe (Instance carrier) {
		NodePair pair = (NodePair)carrier.getData();
		Citation c1 = (Citation)pair.getObject1();
		Citation c2 = (Citation)pair.getObject2();
		
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i];
			if (
					!fieldName.equals("venue") &&
					!fieldName.equals("tech") &&
					!fieldName.equals("journal") &&
					!fieldName.equals("conference") &&
					!fieldName.equals("author") &&
					!fieldName.equals("type") &&
					!fieldName.equals("pages") &&
					!fieldName.equals("year")) {
				
				
				String f1 = c1.getField(fieldName);
				String f2 = c2.getField(fieldName);
				
				if (f1.length() > 0 && f2.length() > 0) {
					double dist = 0.0;
					if (distanceMeasure instanceof NeedlemanWunsch) {
						dist = 1 - ((Math.abs(distanceMeasure.score(f1, f2)) /
												 (double)(f1.length() + f2.length())));
					} else
						dist = distanceMeasure.score(f1,f2);
					if (dist >= 0.9)
						pair.setFeatureValue(featureName+"_"+fieldName+"HIGH", 1.0);
					else if (dist > 0.75)
						pair.setFeatureValue(featureName+"_"+fieldName+"MED", 1.0);
					else if (dist > 0.5)
						pair.setFeatureValue(featureName+"_"+fieldName+"WEAK", 1.0);
					else if (dist > 0.3)
						pair.setFeatureValue(featureName+"_"+fieldName+"MIN", 1.0);
					else if (dist < 0.3)
						pair.setFeatureValue(featureName+"_"+fieldName+"NONE", 1.0);
				} else if ((f1.length() == 0 && f2.length() > 0) ||
									 (f1.length() > 0 && f2.length() == 0))
					pair.setFeatureValue("onlyOne"+fieldName, 1.0);
//				else
//					pair.setFeatureValue("neitherHave"+fieldName, 1.0);
			}
		}
		return carrier;
	}
}
