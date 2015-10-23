package relations;

import java.util.*;

/**
 *
 * @author Chinh
 * @Date: Aug 22, 2011
 */
public class Rules {
    int event_type = 0;
    String trigger; // trigger 
    Map<String, RuleData> map = new HashMap<String, RuleData>();// all patterns
    Map<String, List<RuleData>> data = new HashMap<String, List<RuleData>>(); // Patterns group by sub-key
    boolean init = false;
    int rcount = 0; // total events (can map to patterns)
    double conf[] = {0.03, 0.03, 0.03, 0.03, 0.03, 0.02, 0.03, 0.03, 0.03};
    double skip_value = conf[event_type]; // %
    public Rules(int type, String trg) {
        event_type = type;
        trigger = trg;
    }

    public void initMap() {
        if(init){
            return ;
        }
        RuleData p;
        String key;
        for (String s : map.keySet()) {
            p = map.get(s);
            key = p.POS+p.chunk_type; // first two POS -> NN/JJ/VB
            List<RuleData> item = data.get(key);
            if (item == null) {
                item = new ArrayList<RuleData>();
                item.add(p);
                data.put(key, item);
            } else {
                item.add(p);
            }
        }
        for(String s: data.keySet()){
            List<RuleData> item = data.get(s);
            Collections.sort(item);
        }
        init = true;
    }

    /**
     * For simple events
     * 
     */
    public void addPattern(int verb_type, String pos, String ctype, boolean pos1, String pr1, boolean inchunk, int ccount, String childTrg, boolean add) {
        RuleData p = new RuleData(verb_type, pos, ctype, pos1, pr1, inchunk, ccount, childTrg);
        String txt = p.getKey();
        if (!map.containsKey(txt)) {
            map.put(p.getKey(), p);
        } else {
            p = map.get(txt);
            if(add){
                p.count++;
            }
            if (p.dist1 < ccount) {
                p.dist1 = ccount;
            }
        }
        rcount++;
        if(!childTrg.isEmpty() && childTrg.length()>=3){
           p.childMap.add(childTrg);
        }
    }

    /**
     * 
     * Binding events
     */
    public void addPattern(int verb_type, String pos, String ctype, boolean pos1, boolean pos2, boolean order,
            String pr1, String pr2, boolean has_theme2, boolean inchunk, int ccount1, int ccount2, String childTrg, boolean add) {

        RuleData p = new RuleData(verb_type, pos, ctype, pos1, pos2, order, pr1, pr2, has_theme2, inchunk, ccount1, ccount2,childTrg);
        String txt = p.getKey();
        if (!map.containsKey(txt)) {
            map.put(p.getKey(), p);
        } else {
            p = map.get(txt);
            if(add){
                p.count++;
            }
            if (p.dist1 < ccount1) {
                p.dist1 = ccount1;
            }
            if (p.dist2 < ccount2) {
                p.dist2 = ccount2;
            }
        }
        rcount++;
        if(!childTrg.isEmpty()&& childTrg.length()>=3){
            p.childMap.add(childTrg);
        }
    }

    /**
     * 
     * Regulatory events
     */
    public void addPattern(int verb_type, String pos, String ctype, boolean pos1, boolean pos2, boolean order,
            String pr1, String pr2, boolean has_theme2, boolean inchunk, int ccount1, int ccount2, boolean ev1, boolean ev2, String trg1, String trg2, boolean add) {
        RuleData p = new RuleData(verb_type, pos, ctype, pos1, pos2, order, pr1, pr2, has_theme2, inchunk, ccount1, ccount2, ev1, ev2, trg1, trg2);
        String txt = p.getKey();
        if (!map.containsKey(txt)) { // new pattern
            map.put(p.getKey(), p);
        } else { // existing pattern, add new feature
            p = map.get(txt);
            if(add){
                p.count++;
            }
            if (p.dist1 < ccount1) {
                p.dist1 = ccount1;
            }
            if (p.dist2 < ccount2) {
                p.dist2 = ccount2;
            }
            if(!trg1.isEmpty()){
                p.childMap.add(trg1);
            }
            if(!trg2.isEmpty()){
                p.parentMap.add(trg2);
            }
        }
        rcount++;
    }

    /**
     * Get list of patterns
     * @param key: POS + Chunk type
     * @return: List of RuleData 
     */
    public List<RuleData> getEvalRules(String key) {
        if (!init) {
            initMap();
        }
        List<RuleData> rs = data.get(key);
        return rs;
    }

    
    public void printInfo() {
        System.out.println("Trigger: " + trigger + " number of patterns: " + map.size() + " number of events: " + rcount);
        if (!init) {
            initMap();
        }
        for (String s : data.keySet()) {
            List<RuleData> p = data.get(s);
            if (p.size() > 0) { // print sub key ; these patterns have the same subkey
                for (RuleData dt : p) {
                    System.out.println(dt.getKeyInfo());
                }
            }
        }

    }

    @Override
    public String toString() {
        return trigger + " " + map.size();
    }
    
}
