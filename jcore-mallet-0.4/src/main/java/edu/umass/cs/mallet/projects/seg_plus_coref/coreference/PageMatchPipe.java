package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

import java.util.List;

/**
 * FieldStringDistancePipe
 * User: mhay
 * Email: mhay@cs.umass.edu
 * Date: Feb 18, 2004 4:40:24 PM
 */
public class PageMatchPipe extends Pipe {

    public Instance pipe (Instance carrier) {
        NodePair pair = (NodePair)carrier.getData();
        Citation c1 = (Citation)pair.getObject1();
        Citation c2 = (Citation)pair.getObject2();

        List p1 = c1.getFieldTokens(Citation.pages);
        List p2 = c2.getFieldTokens(Citation.pages);

        if (p1.size() <= 0 || p1.size() > 2 ||
                p2.size() <= 0 || p2.size() > 2) {
            return carrier;
        }

        String s1 = new String();
        String s2 = new String();

        for (int i=0; i<p1.size(); i++) {
            s1 += p1.get(i);
        }
        for (int i=0; i<p2.size(); i++) {
            s2 += p2.get(i);
        }
        //System.out.println("s1=" + s1 + " s2=" + s2);
        //System.out.println("p1=" + p1.toString() + " p2=" + p2.toString());
        if (s1.equals(s2)) {
            pair.setFeatureValue("ConcatPagesMatch", 1.0);
        } else {
					//pair.setFeatureValue("ConcatPagesMatch", 0.0);
        }

        if (s1.indexOf(s2) >= 0 || s2.indexOf(s1) >= 0) {
            pair.setFeatureValue("ConcatPagesSubstr", 1.0);
        } else {
					//pair.setFeatureValue("ConcatPagesSubstr", 0.0);
        }

        return carrier;
    }
}
