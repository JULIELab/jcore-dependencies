package edu.umass.cs.mallet.share.upenn.ner;

import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.util.*;

import java.util.*;
import gnu.trove.*;

/**
 * Adds all features of tokens in the window to the center token.
 */
public class FeatureWindow extends Pipe implements java.io.Serializable {

    int left, right;
    public FeatureWindow (int left, int right) {
        assert (left >= 0 && right >= 0);
        this.left = left;
        this.right = right;
    }

    public Instance pipe (Instance carrier) {
        TokenSequence seq = (TokenSequence)carrier.getData();
        TObjectDoubleHashMap[] original = new TObjectDoubleHashMap[seq.size()];
        for (int i=0; i<seq.size(); i++) {
            Token t = seq.getToken(i);
            original[i] = new TObjectDoubleHashMap();
            PropertyList.Iterator pl = t.getFeatures().iterator();
            while (pl.hasNext()) {
                pl.nextProperty();
                original[i].put(pl.getKey(), pl.getNumericValue());
            }
        }
        
        for (int i=0; i<original.length; i++) { // add to features of token i...
            for (int j = -1 * left; j <= right; j++) {
                int index = i + j; //...the features of token index
                String append = (j < 0) ? "/"+j : "/+"+j; 
                if (index<0 || index==i || index>=original.length) continue;
                
                Token t = seq.getToken(i);
                Object[] features = original[index].keys();
                for (int k=0; k<features.length; k++)
                    t.setFeatureValue((String)features[k]+append, 
                                      original[index].get(features[k]));
            }
        }
        return carrier;
    }
}
