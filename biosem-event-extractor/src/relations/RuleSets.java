package relations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dec 12, 2011 
 * @author Chinh
 *  
 */
public class RuleSets {

    Set<RuleData> rules[] = new HashSet[8]; // list of pattern belong to each group
    int count[] = new int[8]; // count total events belong to each group
    int values[][] = new int[8][4]; // Pro - pro ; pro - event ; event - pro ; event - event
    int detected = 0;
    int total = 0;
    Set<String> frontPrep = new HashSet<String>();
    Set<String> prep = new HashSet<String>();
    Map<String, Set<String>> prepPair = new HashMap<String, Set<String>>();
    int dist1 = 0; // after TG
    int dist2 = 0; // for group 3 only
    int fdist = 0; // front TG ; the distance is interchangeable depending on the group type
    Set<String> order = new HashSet<String>();
    Set<String> causePrep = new HashSet<String>(); // only used for reg events
    boolean passive_order = true; // theme - tg - cause (group 5)

    /**
     * group0 : in-chunk -> PRO-TG         | POS: NN/JJ
     * group1 : in-chunk + cause/theme2: PRO-TG PRO/EVT  |POS: NN/JJ
     * group2 : TG prep PRO/EVT (theme) | POS: NN/JJ/VB (active)
     * group3 : TG prep PRO/EVT prep PRO/EVT   |POS: NN/JJ/VB
     * group4 : PRO/EVT (theme) TG  | POS: NN/VB (passive)(binding)
     * group5 : PRO/EVT (theme) (prep) TG (prep) PRO/EVT (cause) | POS: NN/VB(passive)
     * group6 : PRO/EVT (cause) (prep) TG (prep) PRO/EVT (theme) | POS: NN/VB(active)
     * group7 : PRO - PRO - TG
     * For NP chunk -> POS: NNx/JJ/VBx
     * With NNx -> possible group: G0,G1,G2,G3,G4,G5,G6,G7
     * -->Reg: G0,G1,G2(has prep or dist=0),G3, can skip G4,G6 
     * -->Binding:G0,G1,G2(has prep or dist=0),G3, can skip G4, no G6 
     * -->Simple:G0, G2
     * With JJ -> Possible group: G0,G1,G2, skip the other group
     * With VBx -> Possible group: G2,G4,G5,G6
     * -->Reg: G2, G3(by), G5 (by); can skip G4,G0,G1
     * For VP chunk -> POS:JJ/VBx
     * Possible group (Reg event): G2,G6 (active); G4,G5 (passive) ; 
     * For binding, no distinction between passive and active
     * For simple event: G2 and G4
     */
    public RuleSets() {
        for (int i = 0; i < 8; i++) {
            rules[i] = new HashSet<RuleData>();
            for (int j = 0; j < 4; j++) {
                values[i][j] = 0;
            }
            count[i] = 0;
        }
    }

    /**
     * Split of pattern into defined group
     * @param ls: list of pattern 
     */
    public RuleSets createRule(List<RuleData> ls) {
        RuleSets rl = new RuleSets();
        int count1 = 0, count2 = 0;
        int idx;
        for (RuleData dt : ls) {
            idx = getIdx(dt);
            rl.rules[idx].add(dt);
            countValues(dt, rl.values[idx]);
            rl.count[idx] += dt.count;
            rl.total += dt.count;
            rl.detected = dt.detected;
            if (dt.theme_pos) {
                if (dt.cause_pos && !dt.prep2.isEmpty()&& !dt.prep2.equals(dt.prep1)) { // group 3: tg - theme - cause / or tg - cause - theme (order =false)
                    Set<String> set = rl.prepPair.get(dt.prep_order ? dt.prep2 : dt.prep1);
                    if (set == null) {
                        set = new HashSet<String>();
                        if(dt.prep_order){
                            rl.prepPair.put(dt.prep2, set);
                        }else {
                            rl.prepPair.put(dt.prep1, set);
                        }
                    }
                    if (dt.prep_order) {
                        rl.order.add(dt.prep2 + dt.prep1); // mark to switch role
                        set.add(dt.prep1);
                        rl.prep.add(dt.prep2);
                    } else { // normal case
                        set.add(dt.prep2);
                        rl.prep.add(dt.prep1);
                    }
                }
                if (!dt.cause_pos && !dt.prep2.isEmpty()) {
                    rl.frontPrep.add(dt.prep2);
                }
                if (!dt.prep1.isEmpty()) {
                    rl.prep.add(dt.prep1);
                }
            } else if (!dt.theme_pos) { // group 5: theme - tg - cause 
                if (!dt.prep1.isEmpty()) {
                    rl.frontPrep.add(dt.prep1);
                }
                if (dt.cause_pos && !dt.prep2.isEmpty()) { // 
                    rl.prep.add(dt.prep2);
                }
            }
            if (idx == 1 || idx == 2 || idx == 3 || idx == 6) { // for binding: idx ==1 -> theme2 distance
                rl.dist1 = Math.max(rl.dist1, dt.dist1);
                if (idx == 3) {
                    rl.dist2 = Math.max(rl.dist2, dt.dist2);
                }
            } else if (idx == 4 || idx == 5 || idx == 7) {
                rl.fdist = Math.max(rl.fdist, dt.dist1);
                if (idx == 5) {
                    rl.dist1 = Math.max(rl.dist1, dt.dist2);
                }
            }
            if (idx == 5 && !dt.prep2.isEmpty()) { // cause - tg - theme
                rl.causePrep.add(dt.prep2);
            }
            // determine order for passive case
            if (dt.has_cause && dt.verb_type == 1 && dt.POS.equals("VBN")) {
                if (dt.cause_pos) { // theme -tg-cause (g5)
                    count1 += dt.count;
                } else {
                    count2 += dt.count; // cause -tg- theme
                }
            }
        }
        if (count2 > count1) { // determine whether to switch the default order of passive
            rl.passive_order = false;
        }
        return rl;
    }

    /**
     * Count number of pros and events of theme/cause/theme2
     * @param dt
     * @param val: Theme(pro,event) Cause (pro, event) [0,1,2,3]
     */
    private void countValues(RuleData dt, int val[]) {
        if (dt.event1) {
            val[1] += dt.count;
        } else {
            val[0] += dt.count;
        }
        if (dt.has_cause) {
            if (dt.event2) {
                val[3] += dt.count;
            } else {
                val[2] += dt.count;
            }
        }
    }

    /**
     * Determine group this rule belongs to
     * @param rule: 
     * @return: group index 
     */
    private int getIdx(RuleData rule) {
        int idx;
        if (rule.in_chunk) { // Pos: JJ/NN
            if (rule.has_cause) {
                idx = 1; // PRO-TG PRO
            } else {
                idx = 0; // PRO-TG
            }
        } else {
            if (rule.theme_pos) { // TG  PRO (theme)
                if (rule.has_cause) { // Reg only since Binding has theme_pos = false
                    if (rule.cause_pos) { // TG - PRO1 (theme)- PRO2 (cause) (some if order=true then switch theme and cause)
                        idx = 3; // Pos: NN with two prep; can skip JJ and VB
                    } else {
                        idx = 6; // PRO (cause)- TG - PRO (theme) ; Pos: NN/VB but mainly applied for VB
                    }
                } else { // no cause / theme2
                    idx = 2; // Reg/Binding ; Pos: NN/JJ/VB
                }
            } else { // PRO (theme) TG
                if (rule.has_cause) {
                    if (rule.cause_pos) { // PRO (theme) - TG - PRO (cause/theme2) (for Reg/Binding)
                        idx = 5; // Pos: VBN passive/or binding ; NN with prep: through, by, via
                    } else { // PRO - PRO - TG: binding (and some reg)
                        idx = 7; // Mostly for binding events
                    }
                } else {
                    idx = 4;
                }
            }
        }
        return idx;
    }

    /**
     * Check whether to skip this pattern
     * @return 
     */
    public boolean isSkipped() {
        if (total * 1f / detected < 0.1) {
            return true;
        }
        return false;
    }

    /**
     * Check whether to skip this pattern
     * @return 
     */
    public boolean isSkipped(int type) {
        double score =total * 1f / detected ;
        if (type <5 && score < 0.1) {
            return true;
        }else if(type >=5 && score <0.12){
            return true ;
        }
        return false;
    }
    
    /**
     * Check whether this pattern has inchunk form
     * @return 
     */
    public boolean inChunk() {
        if ((count[0] + count[1]) * 1f / total >= 0.1) {
            return true;
        }
        return false;
    }
    /*
     * Get score of an in-chunk event having cause 
     */

    public double getInchunkCause() {
        return count[1] * 1f / (count[0] + count[1]);
    }

    /*
     * Get score of an in-chunk event having cause 
     */
    public double getInChunkEvtScore() {
        return values[1][1] * 1f / (values[1][0] + values[1][1]);
    }
    
    /**
     * Score of event having theme infront of TG
     * @return 
     */
    public double getFrontScore() {
        return count[4] * 1f / total;
    }

    /**
     * Score of event having theme behind TG ; for simple event only;
     * TODO: for other events need to take into account group 6
     * @return 
     */
    public double getBehindScore() {
        return count[2] * 1f / total;
    }

    /**
     * Get score of an event having theme as an event
     * @param idx: group type
     * @return: score 
     */
    public double getEvtScore(int idx) {
        int value[] = values[idx];
        return value[1] * 1f / (value[0] + value[1]);
    }

     /**
     * Get score of an event having theme as an pro
     * @param idx: group type
     * @return: score 
     */
    public double getProScore(int idx) {
        int value[] = values[idx];
        return value[0] * 1f / (value[0] + value[1]);
    }
    
    /**
     * Get score of an event having cause as an event
     * @param idx: group type
     * @return: score 
     */
    public double getEvtCause(int idx) {
        int value[] = values[idx];
        return value[3] * 1f / (value[2] + value[3]);
    }

    /**
     * Get score of an event having cause as an pro
     * @param idx: group type
     * @return: score 
     */
    public double getProCause(int idx) {
        int value[] = values[idx];
        return value[2] * 1f / (value[2] + value[3]);
    }
    
    /**
     * Get score of an event having cause/theme2. This score does not take into account in-chunk cause
     * @param idx: group type
     * @return: score 
     */
    public double getCauseScore() {
        return (count[3] + count[5] + count[6] + count[7]) * 1f / (total - count[0] - count[1]);
    }
}
