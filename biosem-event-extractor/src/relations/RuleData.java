package relations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Chinh
 * @Date: Sep 13, 2011
 */
public final class RuleData implements Comparable {

    public RuleData() {
    }

    public RuleData(int verb_type, String pos, String ctype, boolean pos1, String prep, boolean inchunk, int ccount, String childTrg) {
        POS = pos;
        this.verb_type = verb_type;
        chunk_type = ctype;
        theme_pos = pos1;
        prep1 = prep;
        in_chunk = inchunk;
        count = 1;
        theme = childTrg ;
        
        if (dist1 < ccount) {
            dist1 = ccount;
        }
        if (chunk_type.equals("NP") || chunk_type.equals("CP")) {
            key = chunk_type + POS + in_chunk + theme_pos + prep1;
        } else {
            key = chunk_type + POS + verb_type + theme_pos + prep1;
        }
        setChildTrigger(childTrg); // themse
    }

    public RuleData(int verb_type, String pos, String ctype, boolean pos1, boolean pos2, boolean order,
            String pr1, String pr2, boolean has_theme2, boolean inchunk, int ccount1, int ccount2, String childTrg) {

        POS = pos;
        this.verb_type = verb_type;
        chunk_type = ctype;
        theme_pos = pos1;
        cause_pos = pos2;
        prep_order = order;
        prep1 = pr1;
        prep2 = pr2;
        in_chunk = inchunk;
        has_cause = has_theme2;
        count = 1;
        dist1 = ccount1;
        dist2 = ccount2;
        theme = childTrg ;
        setChildTrigger(childTrg); // themse
        if (chunk_type.equals("NP") || chunk_type.equals("CP")) {
            key = chunk_type + POS + in_chunk + has_theme2 + theme_pos + prep1 + cause_pos + prep2 + prep_order;
        } else {
            key = chunk_type + POS + verb_type + has_theme2 + theme_pos + prep1 + cause_pos + prep2 + prep_order;
        }
    }

    public RuleData(int verbtype, String pos, String ctype, boolean pos1, boolean pos2, boolean order,
            String pr1, String pr2, boolean hastheme2, boolean inchunk, int ccount1, int ccount2, boolean pro1, boolean pro2, String trg1, String trg2) {
        POS = pos;
        verb_type = verbtype;
        chunk_type = ctype;
        theme_pos = pos1;
        cause_pos = pos2;
        prep_order = order;
        prep1 = pr1;
        prep2 = pr2;
        in_chunk = inchunk;
        has_cause = hastheme2;
        //for regulatory event
        event1 = pro1;
        event2 = pro2;
        this.theme = trg1;
        this.cause = trg2;
        count = 1;
        dist1 = ccount1;
        dist2 = ccount2;
        // TODO: Generate key here
        if (chunk_type.equals("NP") || chunk_type.equals("CP")) {
            key = chunk_type + POS + in_chunk + has_cause + theme_pos + prep1 + cause_pos + prep2 + prep_order + event1 + event2;
        } else {
            key = chunk_type + POS + verb_type + has_cause + theme_pos + prep1 + cause_pos + prep2 + prep_order + event1 + event2;
        }
        setChildTrigger(trg1); // themse
        setParentTrigger(trg2); // cause
    }

    public void setChildTrigger(String tg) {
        if(tg.isEmpty()){
            return ;
        }
        String st[] = tg.split(",");
        childMap.addAll(Arrays.asList(st));
    }

    public void setParentTrigger(String tg) {
        if(tg.isEmpty()){
            return ;
        }
        String st[] = tg.split(",");
        parentMap.addAll(Arrays.asList(st));
    }

    public String mapToString(Set<String> map) {
        String txt = null;
        for (String s : map) {
            if (txt == null) {
                txt = s;
            } else {
                txt = txt + "," + s;
            }
        }
        if (txt == null) {
            return "";
        } else {
            return txt;
        }
    }
    /**
     * Variables
     */
    int count = 0; // total events that satisfy this pattern
    int verb_type = 0;
    String POS = ""; // POS of trigger
    String chunk_type = ""; // chunk contains trigger // NP ; VB (include ADJP)
    boolean theme_pos = false;// behind trigger; false: in front of trigger
    boolean cause_pos = false;// behind trigger; false: in front of trigger // use for binding and reg events
    boolean prep_order = false;// if(theme1_pos && theme2_pos): same side -> true if theme1 behinds theme2 (position)
    String prep1 = "";// of, on, in, to, between...
    boolean has_cause = false;// whether has theme2 (binding) or cause (regulatory)
    String prep2 = "";// cause or theme2: with, and, or, ....
    //In some cases, NP chunk can have subject as cause; in normal case, this is applied for verb chunk
    boolean in_chunk = false;
    int dist1 = 0; // distance from trg to theme
    int dist2 = 0;// distance from trg to cause/ theme2
    //for regulatory event
    boolean event1 = false; // theme  as event
    boolean event2 = false;// cause as event
    String theme = ""; // trigger of event that acts as theme / valid only if event1 is true
    String cause = ""; // trigger of event that acts as cause / valid only if event2 is true
    String key = "";
    public int extracted = 0; // number of event extracted by using this pattern
    public Set<String> childMap = new HashSet<String>();
    public Set<String> parentMap = new HashSet<String>();
    int detected =0;
    public String getKey() {
        return key;
    }

    public String getSubKey() {
        return POS + chunk_type;
    }

    public String getKeyInfo() {
        String txt;
        if (chunk_type.equals("NP") || chunk_type.equals("CP")) {
            txt = chunk_type + " | Pos: " + POS + " | InChunk: " + in_chunk + " | Has theme2: " + has_cause + " | Prep1 pos: " + theme_pos + " | Prep1: " + prep1 + " | Prep2 pos: " + cause_pos + " | Prep2: " + prep2 + " Count: " + count;
            txt = txt + " -> " + mapToString(childMap) + " ->> " + mapToString(parentMap);
        } else {
            txt = chunk_type + " | Pos: " + POS + " | Verb type: " + verb_type + " | Has theme2: " + has_cause + " | Prep1 pos: " + theme_pos + " | Prep1: " + prep1 + " | Prep2 pos: " + cause_pos + " | Prep2: " + prep2 + " Count: " + count;
            txt = txt + " -> " + mapToString(childMap) + " ->> " + mapToString(parentMap);
        }
        return txt;
    }

    @Override
    public int compareTo(Object ob) {
        return ((RuleData) ob).count - count;
    }
}
