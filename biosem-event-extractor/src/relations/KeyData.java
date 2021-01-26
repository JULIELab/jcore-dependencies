package relations;

import java.util.*;

/**
 *
 * @author Chinh
 * @Date: Oct 29, 2010
 * @Revision: August 22, 2011
 */
public class KeyData implements Comparable {

    String key;
    int freq = 0; // frequency ; -1: for shared/mix key
    int found = 0; // freq+ noise
    String type = null; // if type==null - > cannot determine event type (for shared/mix trigger)
    int keytype = 0; // { 1- single ; 2- shared ; 3- mixed }
    public double score = 0.0f; // confident score
    public boolean required = false; // indicate whether modifier must provide or not 

    public KeyData(String word, String tgtype, int fq, int ktype, int total) {
        key = word;
        type = tgtype;
        freq = fq;
        keytype = ktype;
        found = total;
    }

    /**
     * Get default event type (ignoring modifiers) 
     * In this case, use the highest frequency as default type
     * @return 
     */
    public KeyData getDefault() {
        if (keytype == 1) {
            return this;
        } else if (keytype == 2) { // shared trigger, pick first trigger
            return trgMap.get("Gene_expression"); // hard code -> only Gene_expression has shared trigger with Positive_regulation
        } else {
            KeyData tmkey = null;
            int tmfreq = 0;
            for (String s : trgMap.keySet()) {
                KeyData tmk = trgMap.get(s);
                if (tmk.freq > tmfreq) { // and not required modifier; otherwise, skip this key
                    tmkey = tmk;
                    tmfreq = tmk.freq;
                }
            }
            return tmkey;
        }
    }

    @Override
    public int compareTo(Object o) {
        return ((KeyData) o).freq - freq;
    }
    int pcount = 0;
    int ecount = 0;
    int pcause = 0;
    int ecause = 0;
    int t2count = 0;
    int itype = -1;
    public double escore = 0; // a ratio of having an event as argument
    public double cscore = 0; // a ratio of having cause argument
    public double t2score = 0; // a ratio of having second argument (binding event)
    Set<String> child = new HashSet<String>();
    Set<String> parent = new HashSet<String>();// parent for simple/binding event ; cause for regulatory event
    Set<String> modifier = new HashSet<String>();
    Map<String, KeyData> trgMap = new HashMap<String, KeyData>(); // store mix trigger
    public boolean init = false;

    public void initData(String l1, String l2) {
        score = (freq * 1f) / (found * 1f); // determine whether to skip this trigger
        t2score = t2count*1f/(pcount*1f);
        String[] ls = l1.split(",");
        for (String st : ls) {
            if (st.length() >= 1) {
                child.add(st);
            }
        }
        ls = l2.split(",");
        for (String st : ls) {
            if (st.length() >= 1) {
                parent.add(st);
            }
        }
        init = true;
    }

    /**
     * Add shared/mix trigger to map
     * @param dt: keydata 
     */
    public void addToMap(KeyData dt) {
        trgMap.put(dt.type, dt);
    }

    /**
     * get list of shared /mix trigger -> which has the same key
     * @return 
     */
    public Map<String, KeyData> getMap() {
        return trgMap;
    }

    /**
     * get KeyData for shared and mixed trigger
     * @param type: event type
     * @return: KeyData 
     */
    public KeyData getKeyData(String etype) {
        if (keytype == 1) {
            return this;
        } else {
            return trgMap.get(etype);
        }
    }

    /**
     * Set modifers for this trigger
     * @param ls 
     */
    public void setModifiers(String ls[]) {
        modifier.addAll(Arrays.asList(ls));

    }

    /**
     * Convert a map into string separeted by commar.
     * @param map
     * @return 
     */
    public String set2String(Set<String> map) {
        StringBuilder sb = new StringBuilder();
        for (String s : map) {
            sb.append(s);
            sb.append(',');
        }
        return sb.toString();
    }

    /**
     * Determine event type based on a given list of modifiers
     * @param tokens: modifiers
     * @return: event type 
     */
    public String getType(List<Word> tokens) {
        String s = null;
        KeyData tmkey;
        if (keytype == 1) { // single trigger
            if (!required) {
                return type;
            } else {
                for (Word w : tokens) {
                    if (modifier.contains(w.word)) {
                        return type; // found required modifier
                    }
                }
            }
        } else if (keytype == 2) { // shared trigger, pick first trigger
            return "Gene_expression"; // hard code -> only Gene_expression shares trigger with Positive_regulation
        } else { // mix trigger
            for (String etype : trgMap.keySet()) {
                tmkey = trgMap.get(etype);
                for (Word w : tokens) { // first loop try to check modifier
                    if (tmkey.modifier.contains(w.word)) {
                        return tmkey.type;
                    }
                }
            }
            // failed, now try without modifier
            tmkey = null;
            int tmfreq = 0;
            for (String w : trgMap.keySet()) {
                KeyData tmk = trgMap.get(w);
                if (!tmk.required && tmk.freq > tmfreq) { // and not required modifier; otherwise, skip this key
                    tmkey = tmk;
                    tmfreq = tmk.freq;
                }
            }
            if (tmkey != null) {
                return tmkey.type;
            }
        }
        return s; // null, no type
    }
}
