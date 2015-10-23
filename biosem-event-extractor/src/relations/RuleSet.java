
package relations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Chinh
 * Date: Nov 9, 2011
 */
public class RuleSet {
    // for all events
    boolean in_chunk =false;
    int dist1=0;
    int dist2=0;
    // for binging and regulatory
    Set<String> inchunk_prep = new HashSet<String>(); //if in_chunk =true ; extension for theme2 ; for reg: this prep is for theme ; in_chunk is cause
    Set<String> prep = new HashSet<String>(); //
    Set<String> frontprep = new HashSet<String>(); //
    Map<String, Set<String>> prep2 = new HashMap<String, Set<String>>(); // for NP, prep2 is the same side with prep1
    Set<String> prep_2 = new HashSet<String>(); //
    Set<String> prep_1 = new HashSet<String>(); //
    // for regulatory event
    boolean order =true ; // in normal NP case: (same side) theme /cause, but there are case, this order must be switched
    // for VP case: swich the role of subject (cause) to object (theme)
    int pcount=0;
    int ecount =0;
    int pcause =0;
    int ecause =0;
    int t2count=0;
    int in_front=0; // use for binding (in case tg has VBz tag in NP) or special case like by/through
    int detected =0;
    int inchunk_count=0;
    int apply=0;
    /**
     * Ratio of event as theme
     * @return 
     */
    public double getEvtScore(){
        return (ecount*1f/(pcount+ecount));
    }
    
    /**
     * Ratio of event has cause
     * @return 
     */
    public double getCauseScore(){
        return ((pcause+ecause)*1f/(pcount+ecount));
    }
    /**
     * Ratio of event as cause
     * @return 
     */
    public double getEv2Score(){
        return (ecause*1f/(ecause+pcause));
    }
    /**
     * Theme 2 score
     * @return 
     */
    public double getT2Score(){
        return (t2count*1f/pcount);
    }
    /**
     * Count frequency of this rule, should not use alone, use getScore instead.
     * @return 
     */
    public int getFreq(){
        return pcount+ecount;
    }
    /**
     * For binding only; this value is used to determine whether to use pro in front of TG as theme/ or use t2 score
     * @return 
     */
    public double getT1Score(){
        return (in_front*1f/pcount);
    }
    
    /** Confident score for this rule, if this value is low, skip this rule
     * @return : score
     */
    public double getScore(){
        if(detected==0){
            return 0;
        }
        return (pcount+ecount)*1f/detected;
    }
    /**
     * Percentage of event has in chunk form, if this value is low, then skip in-chunk event
     * @return 
     */
    public double  inchunkScore(){
        if(detected==0){
            return 0;
        }
        return inchunk_count*1f/detected;
    }
    public double applyScore(){
        return apply*1f/(pcount+ecount);
    }
}
