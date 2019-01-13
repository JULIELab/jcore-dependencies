package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

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
public class ExactFieldMatchPipe extends Pipe {

    String[] fields;

    public ExactFieldMatchPipe(String[] fields) {
        this.fields = fields;
    }

    public Instance pipe (Instance carrier) {
        NodePair pair = (NodePair)carrier.getData();
        Citation c1 = (Citation)pair.getObject1();
        Citation c2 = (Citation)pair.getObject2();

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
						// we're already handling author - this is confusing things
						if (!field.equals("author"))
							setFeatures(pair, field, c1.getField(field), c2.getField(field));
        }

        return carrier;
    }

    private void setFeatures(NodePair pair, String name, String s1, String s2) {
        //System.out.println("field=" + name + " s1=" + s1 + " s2=" + s2);
/*
        if (s1.length() == 0 && s2.length() == 0) {
            pair.setFeatureValue("BothAreMissing_"+name, 1.0);
        } if (s1.length() == 0 || s2.length() == 0) {
            pair.setFeatureValue("OneIsMissing_"+name, 1.0);
            pair.setFeatureValue("Same_"+name, 0.0);             // todo: is this appropriate?
            pair.setFeatureValue("Substr_"+name, 0.0);
						} else {*/
					//pair.setFeatureValue("OneIsMissing_"+name, 0.0);
			if (s1.equals(s2) && s1.length() > 0 && s2.length() > 0) {
				pair.setFeatureValue("Same_"+name, 1.0);
			} else {
				//pair.setFeatureValue("Same_"+name, 0.0);
			}
			/*
			if (s1.indexOf(s2) >= 0 || s2.indexOf(s1) >= 0) {
				pair.setFeatureValue("Substr_"+name, 1.0);
			} else {
				pair.setFeatureValue("Substr_"+name, 0.0);
				}*/
    }

}
