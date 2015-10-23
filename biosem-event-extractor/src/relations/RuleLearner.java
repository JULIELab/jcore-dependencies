/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package relations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import utils.DBUtils;

/**
 *
 * @author Chinh
 */
public class RuleLearner {

    SenAnalyzer analyzer; // use default database
    SenSimplifier simp;

    public RuleLearner(){
        
    }
    public RuleLearner(DBUtils sr, DBUtils dest) {
        analyzer = new SenAnalyzer(sr, dest);
        simp = analyzer.simp;

    }

    public static void main(String[] args) {
        DBUtils db1 = new DBUtils();
        db1.openDB("D:/DataNLP/Dev2011/Data");
        RuleLearner learner = new RuleLearner(db1, db1);
        learner.LearnData();
        db1.shutdownDB();
    }
    String current_txt = "";
    String curr_pmid = "";
    int curr_sen_id = 0;
    List<Word> detectedTrgs = null;
    Map<String, Map<String, Counter>> TGCount = new HashMap<String, Map<String, Counter>>();
    Map<String, Map<String, Counter>> subTG = new HashMap<String, Map<String, Counter>>();
    Map<String, Map<String, Counter>> sharedTG = new HashMap<String, Map<String, Counter>>();
    Map<String, Counter> sameChunk = new HashMap<String, Counter>();
    String tokens[];
    Set<Word> validTG = new HashSet<Word>(); // detected tg match with annotated TG
    Map<TData, Word> matchTG = new HashMap<TData, Word>(); // annotated TG <-> detectedTG
    public void LearnData() {
        int total = 0, skip_pro = 0, miss_pro = 0, skip_dic = 0, miss_vb = 0;
        analyzer.init();
        Set<TData> usedTG = new HashSet<TData>();
        List<String> ids = simp.loadPMIDs();
        //ids.clear();
        //ids.add("PMC-1920263-13-RESULTS-05");
        System.out.println("Total abstract: " + ids.size());
        List<EData> elist;
        List<TData> trgList;
        Map<String, TData> TGmap = new HashMap<String, TData>();
        Map<String, EData> EVmap = new HashMap<String, EData>();
        Map<String, Word> protMap = new HashMap<String, Word>();
        ChunkAnalyzer op = new ChunkAnalyzer();
        Word tg, pr, pr2;
        TData pro;
        List<Chunk>[] out;
        List<Word> prep;
        Map<String, Counter> mtrg[] = new HashMap[SenSimplifier.trigger_type.length];
        Map<String, Rules> rules[] = new Map[SenSimplifier.trigger_type.length];
        boolean add;
        for (int x = 0; x < SenSimplifier.trigger_type.length; x++) {
            rules[x] = new HashMap<String, Rules>();
            mtrg[x] = new HashMap<String, Counter>();
        }
        int miss_events = 0, skip_trg = 0, success = 0, unknown = 0;
        int ev_type;
        boolean print = false;
        KeyData kdata;
        int counter[] = new int[SenSimplifier.trigger_type.length];
        int totals[] = new int[SenSimplifier.trigger_type.length];
        int map_count[] = new int[SenSimplifier.trigger_type.length];
        int mis_count[] = new int[SenSimplifier.trigger_type.length];
        int mis_trg[] = new int[SenSimplifier.trigger_type.length];
        int total_sen = 0, s_events = 0, skip_sen =0;
        boolean ev1, ev2;
        int lv1 = 0, lv2 = 0, lv3 = 0, lv0 = 0;
        int sen_begin, sen_end;
        for (String id : ids) {
            EVmap.clear();
            TGmap.clear();
            out = analyzer.analyze(id);
            elist = simp.loadEvent(id);
            trgList = simp.loadTrigger(id);
            for (TData dt : trgList) { // prepare hash for trigger
                TGmap.put(dt.tid, dt);
            }
            for (EData ev : elist) {
                EVmap.put(ev.eid, ev);
            }
            for (EData ed : elist) {
                ed.init(analyzer.proMap, TGmap, EVmap);
            }
            curr_pmid = id;
            List<EData> events[] = analyzer.splitEvents(elist);
            for (int i = 0; i < analyzer.senpos.length; i++) {
                usedTG.clear();
                validTG.clear();
                matchTG.clear();
                curr_sen_id = i;
                detectedTrgs = analyzer.detectedTrg[i];
                total += events[i].size(); // count events
                print = false;
                if (out[i] == null) {
                    s_events += events[i].size(); // miss due to no trg/pro
                    for (EData ed : events[i]) {
                   		ev_type = SenSimplifier.hashType.get(ed.type);
                        mis_count[ev_type]++;
                    }
                    if(events[i].size()>0){
                        skip_sen++;
                    }
                    continue;
                }
                total_sen++; // sen has event
                sen_begin = analyzer.senpos[i];
                sen_end = analyzer.senpos[i] + analyzer.longsen[i].length();
                tokens = analyzer.tokenList.get(i);
                prep = analyzer.getPreps(tokens);
                protMap.clear();
                for (Word w : analyzer.detectedPro[i]) {
                    protMap.put(w.word, w); // name -> word
                }
                op.curr_text = analyzer.shortsen[i]; // for debuging
                current_txt = analyzer.shortsen[i];
                op.analyzeChunk(out[i], analyzer.tagList.get(i), tokens); // split chunks into clauses
                countTrg(op);
                // map trg into detetced tg, put true detected trg to valid list
                for(EData ev:events[i]){
                    tg = analyzer.findTrigger(ev.trgdata.list, analyzer.detectedTrg[i]);
                    if(tg!=null){
                        matchTG.put(ev.trgdata, tg);
                        validTG.add(tg);
                    }
                }
                for (EData ev : events[i]) { // only simple events
                    curr_event = ev;
                    pr2 = null;
                    ev1 = false;
                    ev2 = false;
                    ev_type = SenSimplifier.hashType.get(ev.type);
                    totals[ev_type]++; // count event per type
                    if (!inSentence(sen_begin, sen_end, ev)) {
                        skip_pro++;
                        continue; // Skip: pro belongs to the other sentence
                    }
                    if(!usedTG.contains(ev.trgdata)){
                        add=true;
                        usedTG.add(ev.trgdata);
                    }else{
                        add =false ;
                    }
                    tg = matchTG.get(ev.trgdata);
                    if (tg == null) {
                        skip_trg++;
                        mis_trg[ev_type]++;
                        Counter ct = mtrg[ev_type].get(ev.trgdata.name.toLowerCase());
                        if (ct != null) {
                            ct.inc();
                        } else {
                            ct = new Counter();
                            mtrg[ev_type].put(ev.trgdata.name.toLowerCase(), ct);
                        }
                        continue;
                    }
                    kdata = simp.sharedDic.get(tg.word);
                    if ((kdata.keytype == 1 && !kdata.type.equals(ev.type))) {
                        skip_dic++;
                        continue; // wrong type
                    }
                    // determining theme type and theme2/cause type
                    if (ev.data1 instanceof TData) { // pro
                        pro = (TData) ev.data1;
                        pr = protMap.get(pro.new_name); // TData -> Word
                    } else {
                        pro = ((EData) ev.data1).trgdata;
                        ev1 = true;
                        pr = matchTG.get(pro);
                        if (pr == null) {
                            skip_trg++;
                            continue;
                        }
                    }
                    if (ev.data2 != null) { // binding event
                        TData pro2 = ev.data2;
                        pr2 = protMap.get(pro2.new_name);
                    } else if (ev.ecause != null) { // regulatory event
                        if (ev.ecause instanceof TData) { // cause as pro
                            TData pro2 = (TData) ev.ecause;
                            pr2 = protMap.get(pro2.new_name);
                        } else { // cause as event
                            ev2 = true;
                            TData pro2 = ((EData) ev.ecause).trgdata;
                            pr2 = matchTG.get(pro2);
                            if (pr2 == null) {
                                skip_trg++;
                                continue;
                            }
                        }
                    }
                    // now find chunk containing trigger and pro
                    boolean found = false;
                    // Map event to BasicChunk or VerbChunk
                    for (BasicChunk bs : op.bsList) {
                        if (bs.belongTO(tg, pr, pr2)) {
                            found = eventToNP(bs, tg, pr, pr2, prep, rules, ev_type, ev1, ev2,add);
                            break;
                        }
                    }
                    if (!found) {
                        for (VerbChunk vc : op.verbList) {
                            if (vc.belongTO(tg, pr, pr2)) {
                                found = eventToVP(vc, tg, pr, pr2, prep, rules, ev_type, ev1, ev2,add);
                                break;
                            }
                        }
                    }
                    if (found) {
                        success++;
                        map_count[ev_type]++;
                        switch (ev.getLevel(0)) {
                            case 0:
                                lv0++;
                                break;
                            case 1:
                                lv1++;
                                break;
                            case 2:
                                lv2++;
                                break;
                            default:
                                lv3++;
                                break;
                        }
                    } else {
                        counter[ev_type]++;
                        miss_events++;
                        if(!print){
                            //System.out.println(curr_pmid+" "+curr_sen_id+ current_txt);
                            print =true;
                        }
                        //System.out.println("--->" +ev.getTxt(0));
                    }

                }// event
            if(print){
                unknown++;
                //System.out.println("");
            }    
            }// sentence
            
        }// id

        /**
         * Debuging information ; just comment if not needed
         */
        System.out.println("Sub trg list:-------------------------------------------------------------------------------------");
        for(String s: subTG.keySet()){
            Map<String,Counter> ct = subTG.get(s);
            Map<String,Counter> ct2 = sharedTG.get(s);
            if(ct2!=null){
                for(String w: ct2.keySet()){
                    ct.remove(w);
                }
            }
            System.out.println(s+ " number of sub tg: "+ct.size());
            for(String w: ct.keySet()){
                System.out.println("            --> "+w+"  "+ct.get(w).count);
            }
            System.out.println("");
        }
        System.out.println("----------------------------------------------------------------------  end sub tg list ----------");
        
        System.out.println("----------------------------------------------------------------------  end sub tg list ----------");
        
        System.out.println("---Number of sentence with miss events "+unknown+" Total: "+total_sen+" Skip sen: "+
                skip_sen+" Rc1:(miss) "+unknown*1f/total_sen+" Rc2:(skip) "+skip_sen*1f/total_sen);
        System.out.println("-----------------------------------------------------------------------------------------------------------------------\n");
        for (int k = 0; k < 9; k++) {
            System.out.println(SenSimplifier.trigger_type[k] + " : Total: " + totals[k] + " map events: " + map_count[k] + " |  miss map: " + counter[k] + "  ->> recall: " + (1f * map_count[k] / totals[k]));
            System.out.println("      Miss due to no trg/pro: " + mis_count[k] + " | miss due to dict: " + mis_trg[k]);
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------------\n");
        System.out.println("Total events skip due to no trg/pro: " + s_events + " mis pro: " + miss_pro);
        System.out.println("    Total events:" + total + " | events map: " + success + " -> Recall: " + success * 1f / total + "  | skip trg: " + skip_trg + " | Skip dic: " + skip_dic + " | skip pro: " + skip_pro + " | miss: " + miss_events);
        System.out.println("Number of trig as VB is missed " + miss_vb);
        System.out.println("");
        System.out.println("Level 0: " + lv0 + "  | Level 1: " + lv1 + " | Level 2: " + lv2 + " Level 3: " + lv3);
        System.out.println("Noun count: " + nppt + "  Verb count: " + vppt + " | Number of event with level >=2 " + vppt2);

        System.out.println("----------------missing trgger for each event type-------------------------------");
        for (int i = 0; i <= 8; i++) {
            System.out.println(SenSimplifier.trigger_type[i] + " total " + mtrg[i].size());
            for (String s : mtrg[i].keySet()) {
                // System.out.println(s+" --> "+mtrg[i].get(s).count);
            }
            System.out.println("");
        }
        System.out.println("--------------------------------------------------------------same chunk , tg > pro------------: "+sameChunk.size());
        for(String s: sameChunk.keySet()){
            System.out.println(s+ " "+sameChunk.get(s).count);
        }
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("Combining rules....");
        //Map<String, RuleSet> rset = combiningRules(rules);
        System.out.println("---> Storing patterns.....");
        storePatterns(rules, analyzer.db);
        //System.out.println("Storing rules .....");
        //storeRuleSet(rset, analyzer.db);
        System.out.println("");
    }

    private void countTrg(ChunkAnalyzer ac) {
        Set<BasicChunk> bc = new HashSet<BasicChunk>();
        List<Chunk> verb = new ArrayList<Chunk>();
        Set<Word> used = new HashSet<Word>();
        for (BasicChunk bs : ac.bsList) {
            bc.add(bs);
        }
        for (VerbChunk vc : ac.verbList) {
            bc.add(vc.subject);
            bc.add(vc.object);
            verb.add(vc.verb);
        }
        for (BasicChunk bs : bc) {
            for (Chunk c : bs.chunkList) {
                for (Word w : c.trigs) {
                    if (!used.contains(w)) {
                        add2Map(w.pos_tag, w.word, "NP");
                        used.add(w);
                    }
                }
            }
        }
        for (Chunk c : verb) {
            for (Word w : c.trigs) {
                if (!used.contains(w)) {
                    add2Map(w.pos_tag, w.word, "VP");
                    used.add(w);
                }
            }
        }
    }

    private void add2Map(String pos, String tg, String type) {
        Map<String, Counter> ct = TGCount.get(tg);
        if (ct == null) {
            ct = new HashMap<String, Counter>();
            TGCount.put(tg, ct);
        }
        String key = pos + type;
        Counter c = ct.get(key);
        if (c == null) {
            c = new Counter(1);
            ct.put(key, c);
        } else {
            c.inc();
        }
    }

    private boolean inSentence(int begin, int end, EData ev) {
        boolean theme = false, cause = false;
        TData tg = ev.trgdata;

        if (tg.list[0] >= begin && tg.list[1] <= end) { // same trigger
            //check theme
            if (ev.data1 instanceof TData) {
                TData pr1 = (TData) ev.data1;
                if (pr1.list[0] >= begin && pr1.list[1] <= end) {
                    theme = true;
                }
            } else {
                EData ev1 = (EData) ev.data1;
                theme = inSentence(begin, end, ev1);
            }
            // check cause
            if (ev.ecause != null) {
                if (ev.ecause instanceof EData) {

                    cause = inSentence(begin, end, (EData) ev.ecause);

                } else {
                    TData pr2 = (TData) ev.ecause;
                    if (pr2.list[0] >= begin && pr2.list[1] <= end) {
                        cause = true;
                    }
                }
            } else if (ev.data2 != null) { // theme2
                TData pr2 = (TData) ev.data2;
                if (pr2.list[0] >= begin && pr2.list[1] <= end) {
                    cause = true;
                }
            } else {
                cause = true;
            }
            if (theme && cause) {
                return true;
            }
        }

        return false;
    }
    /**
     * Map event into BasicChunk
     *
     * @param bs: chunk
     * @param tg: trigger
     * @param pr: protein 1
     * @param pr2: theme2
     * @param words: preposition list
     * @param rules: array of map of each event type
     * @param ev_type: event type
     */
    int in_distance = 0;
    int in_total = 0;
    int has_trg = 0;
    int in_count = 0;
    int prep_order_count = 0;
    boolean debug = false;

    private boolean eventToNP(BasicChunk bs, Word tg, Word pr, Word pr2, List<Word> prep, Map<String, Rules> rules[], 
            int ev_type, boolean evt1, boolean evt2, boolean add) {
        boolean prep1_pos, prep2_pos = false, prep_order = false, in_chunk = false;
        String prep1 = "", prep2 = "", ctype, pos_type;
        int count1, count2 = 0;// for theme2/cause
        int verb_type;
        boolean has_theme2 = false;
        String themeTrg = "", causeTrg = "";
        bs.mergeNP();
        if (bs.is_merged) {
            ctype = "CP";
        } else {
            ctype = "NP";
        }
        if (evt1) { // theme
            themeTrg = pr.word;
        }
        if (evt2) { //cause
            causeTrg = pr2.word;
        }
        pos_type = tg.pos_tag;
        prep1_pos = pr.pos > tg.pos ? true : false; // prep 1 position
        nppt++; // NP pattern
        if (pr2 == null) { // only theme
            if(bs.isSameChunk(tg, pr) && !evt1){
                Counter ct = sameChunk.get(tg.word);
                if(ct==null){
                    ct = new Counter(1);
                    sameChunk.put(tg.word, ct);
                }else{
                    ct.inc();
                }
            }
            count1 = bs.countChunks(tg, pr);
            if (prep1_pos) { // TG - PRO
                prep1 = getPrep(tg, pr, bs);
                if(ev_type<=5){
                    Word sub_tg = findTrg(tg,pr.pos, bs);
                    if(sub_tg!=null){ // found sub-trg
                        String key = tg.word+tg.pos_tag;
                        Map<String, Counter> ct = subTG.get(key);
                        if(ct==null){
                            ct = new HashMap<String,Counter>();
                            subTG.put(key, ct);
                        }
                        Counter c = ct.get(sub_tg.word+sub_tg.pos_tag);
                        if(c==null){
                            c = new Counter(1);
                            ct.put(sub_tg.word+sub_tg.pos_tag, c);
                        }else {
                            c.inc();
                        }
                    }
                }
            } else { // PRO - TG
                if (!evt1 && bs.inChunk(tg, pr)) {
                    in_chunk = true;
                    count1 = 0;
                } else if (evt1&& tg.pos == pr.pos) {
                    in_chunk =true ;
                    count1 = 0;
                }
            }
            if(in_chunk && !evt1){
                Chunk tgc = bs.getChunk(tg.pos);
                if(tgc.is_inChunk(tg,tokens)&& tgc.trigs.size()==2){
                    //System.out.println("---->"+ tgc.getText()+"\n"+curr_pmid+" "+curr_sen_id);
                    //bs.printChunk();
                    //System.out.println("");
                }
            }
        } else {
            has_theme2 = true; // has cause/theme2
            prep2_pos = pr2.pos > tg.pos ? true : false;
            count1 = bs.countChunks(tg, pr);
            count2 = bs.countChunks(tg, pr2);
            if (prep1_pos && prep2_pos) { // both are behind trig
                if (pr.pos > pr2.pos) { // reg only: TG - cause - theme
                    prep1 = getPrep2(pr2, pr, bs); // theme
                    prep2 = getPrep(tg, pr2, bs); // cause
                    prep_order = true; // need to swich order
                } else { // binding  & reg : TG- theme - cause/theme2
                    prep2 = getPrep2(pr, pr2, bs); //cause
                    prep1 = getPrep(tg, pr, bs); // theme
                }
            } else if (prep1_pos && !prep2_pos) { // cause - tg - theme -> reg only
                prep1 = getPrep(tg, pr, bs);
                prep2 = getPrepFront(tg, pr2, bs);
            } else if (!prep1_pos && prep2_pos) { // theme - tg - cause | theme1 - tg - theme2
                prep1 = getPrepFront(tg, pr, bs);
                prep2 = getPrep(tg, pr2, bs);
            } else { // both are in front of tg
                //binding & reg
                if (pr.pos > pr2.pos) { // cause/theme2 - theme -TG: should skip this
                   prep1 = getPrepFront(tg, pr, bs);
                   prep2 = getPrep2(pr2, pr, bs);
                   prep_order =true;
                } else { // Reg only : theme - cause/theme2 - TG
                    prep1 = getPrep2(pr, pr2, bs);
                    prep2 = getPrepFront(tg, pr2, bs);
                }
            }
            if (ev_type == 5) {
                in_chunk = bs.inChunk(tg, pr);
                // count1 = 0;
            } else if (ev_type > 5 && !evt2 && !prep2_pos) { // cause must be pro
                in_chunk = bs.inChunk(tg, pr2);
                // count2 = 0;
            }
        }// pr2 condition
        verb_type = 0;
        Rules rl = rules[ev_type].get(tg.word);
        if (rl == null) {
            rl = new Rules(ev_type, tg.word);
            rules[ev_type].put(tg.word, rl);
        }
        if (ev_type < 5) {
            rl.addPattern(verb_type, pos_type, ctype, prep1_pos, prep1, in_chunk, count1, themeTrg,add);
        } else if (ev_type == 5) {
            rl.addPattern(verb_type, pos_type, ctype, prep1_pos, prep2_pos, prep_order, prep1, prep2, has_theme2, in_chunk, count1, count2, themeTrg,add);
        } else {// regulatory events
            rl.addPattern(verb_type, pos_type, ctype, prep1_pos, prep2_pos, prep_order, prep1, prep2, has_theme2, in_chunk, count1, count2, evt1, evt2, themeTrg, causeTrg,add);
        }
        return true;
    }

    private boolean eventToVP(VerbChunk vc, Word tg, Word pr, Word pr2, List<Word> prep, Map<String, Rules> rules[], 
            int ev_type, boolean evt1, boolean evt2,boolean add) {
        boolean prep1_pos, prep2_pos = false, prep_order = false, in_chunk = false;
        String prep1 = "", prep2 = "", ctype, pos_type;
        int count1 = 0, count2 = 0;// count2 -> for theme2/cause
        boolean has_theme2 = false;
        String childTrg = "", parentTrg = "";
        if (vc.subject.belongTO(tg, pr, pr2)) {
            eventToNP(vc.subject, tg, pr, pr2, prep, rules, ev_type, evt1, evt2,add);
        } else if (vc.object.belongTO(tg, pr, pr2)) {
            eventToNP(vc.object, tg, pr, pr2, prep, rules, ev_type, evt1, evt2,add);
        } else if (vc.verb.contains(tg)) { // verb contains trigger
            vppt++;
            if (curr_event.getLevel(0) >= 2) {
                vppt2++;
            }
            ctype = "VP";
            pos_type = tg.pos_tag;
            prep1_pos = tg.pos < pr.pos ? true : false;
            if (pr2 == null) {
                if (vc.subject.containsKey(pr)) {
                    count1 = vc.subject.getChunkPos(pr.pos);
                    // or relative clause
                    prep1 = getPrepFront(tg, pr, vc.subject);
                } else if (vc.object.containsKey(pr)) {
                    count1 = vc.object.getChunkPos(pr.pos);
                    prep1 = getPrep(tg, pr, vc.object);
                }
                if (evt1) {
                    childTrg = pr.word;
                }
            } else { // Pr2!=null
                //for both binding and regulatory events
                has_theme2 = true;
                prep2_pos = tg.pos < pr2.pos ? true : false;
                if (prep1_pos && prep2_pos) { // both are behind trig
                    count1 = vc.object.getChunkPos(pr.pos);
                    count2 = vc.object.getChunkPos(pr2.pos);
                    if(pr.pos< pr2.pos){
                        prep1 = getPrep(tg, pr, vc.object);
                        prep2 = getPrep2(pr, pr2, vc.object);
                    }else {
                        prep2 = getPrep(tg, pr2, vc.object);
                        prep1 = getPrep2(pr2, pr, vc.object);
                    }
                } else if (prep1_pos && !prep2_pos) { // cause - tg - theme
                    prep1 = getPrep(tg, pr, vc.object);
                    prep2 = getPrepFront(tg, pr2, vc.subject);
                    count1 = vc.object.getChunkPos(pr.pos);
                    count2 = vc.subject.getChunkPos(pr2.pos);
                } else if (!prep1_pos && prep2_pos) { // theme - tg - cause
                    prep1 = getPrepFront(tg, pr, vc.subject);
                    prep2 = getPrep(tg, pr2, vc.object);
                    count2 = vc.object.getChunkPos(pr2.pos);
                    count1 = vc.subject.getChunkPos(pr.pos);
                } else if (!prep1_pos && !prep2_pos) { // both are in front of tg
                    // reg event: few cases, can skip
                    prep1 = getPrep2(pr, pr2, vc.subject);
                    prep2 = getPrepFront(tg, pr2, vc.subject);
                    count1 = vc.subject.getChunkPos(pr.pos);
                    count2 = vc.subject.getChunkPos(pr2.pos);
                    if (ev_type > 5) {
                        return false;   // skip this case
                    }
                }
                if (ev_type > 5) { // for binding events: the order of proteins always are PR1-PR2 if they are in the same side
                    if (evt1) {
                        childTrg = pr.word;
                    }
                    if (evt2) {
                        parentTrg = pr2.word;
                    }
                }
            }
            Rules rl = rules[ev_type].get(tg.word);
            if (rl == null) {
                rl = new Rules(ev_type, tg.word);
                rules[ev_type].put(tg.word, rl);
            }
            if (ev_type < 5) {
                rl.addPattern(vc.verb_type, pos_type, ctype, prep1_pos, prep1, in_chunk, count1, childTrg,add);
            } else if (ev_type == 5) {
                rl.addPattern(vc.verb_type, pos_type, ctype, prep1_pos, prep2_pos, prep_order, prep1, prep2, has_theme2, in_chunk, count1, count2, childTrg,add);
            } else {
                rl.addPattern(vc.verb_type, pos_type, ctype, prep1_pos, prep2_pos, prep_order, prep1, prep2, has_theme2, in_chunk, count1, count2, evt1, evt2, childTrg, parentTrg,add);
            }
        } else { // merge subject and subject
            BasicChunk new_ch = new BasicChunk();
            new_ch.addChunk(vc.subject);
            new_ch.addChunk(vc.verb);
            new_ch.addChunk(vc.object);
            new_ch.is_merged = true;
            return eventToNP(new_ch, tg, pr, pr2, prep, rules, ev_type, evt1, evt2,add);
        }
        return true;
    }

    public String getPrepFront(Word tg, Word pr, BasicChunk bs) {
        int st1, st2;
        st1 = bs.getChunkPos(tg.pos);
        st2 = bs.getChunkPos(pr.pos);
        if (Math.min(st1, st2) >= 0 && st1 > st2) { // both in bs and PRO - TG
            Chunk c = bs.chunkList.get(st1 - 1);
            if (c.type.endsWith("PP") && preps.contains(c.txt)) {
                return c.txt;
            }
        } else if (st1 < 0 && st2 > 0) { // PRO
            Chunk c = bs.chunkList.get(bs.chunkList.size() - 1);
            if (c.type.endsWith("PP")) {
                return c.txt;
            }
        }
        return "";
    }

    public String getPrep(Word tg, Word pr, BasicChunk bs) {
        int pos1 = bs.getChunkPos(tg.pos);
        int pos2 = bs.getChunkPos(pr.pos);
        if (Math.min(pos1, pos2) >= 0 && pos1 < pos2) { // NP
            Chunk c = bs.chunkList.get(pos1 + 1);
            if (c.type.endsWith("PP") && prepmap.contains(c.txt)) {
                return c.txt;
            }
        } else if (pos1 < 0 && pos2 > 0) { // VP
            Chunk c = bs.chunkList.get(0);
            if (c.type.endsWith("PP")&& prepmap.contains(c.txt)) {
                return c.txt;
            }
        }
        return "";
    }

    public String getPrep2(Word pr1, Word pr2, BasicChunk bs) {
        int pos1 = bs.getChunkPos(pr1.pos);
        int pos2 = bs.getChunkPos(pr2.pos);
        if (Math.min(pos1, pos2) >= 0 && pos1 < pos2) { // NP
            int i= pos1+1;
            Chunk c;
            while(i< pos2){
                c = bs.chunkList.get(i);
                if (c.type.endsWith("PP")&& prepmap.contains(c.txt)) {
                    return c.txt;
                }
                i++ ;
            }
        } 
        return "";
    }
    
    private Word findTrg(Word tg, int pos2, BasicChunk bs) {
        int pos1 = tg.pos ;
        if(pos2-pos1>10){
            return null;
        }
        Chunk c1 = bs.getChunk(pos1);
        Chunk c2 = bs.getChunk(pos2);
        int begin = c1.begin;
        int end = c2.end;
        for(Chunk c: bs.chunkList){
            if(c.begin>=begin && c.end<=end){
                for(Word w: c.trigs){
                    if(!validTG.contains(w) && w.pos > pos1){
                        return w ;
                    }else if(validTG.contains(w)&& w.pos > pos1){
                        String key = tg.word+tg.pos_tag;
                        Map<String, Counter> ct = sharedTG.get(key);
                        if(ct==null){
                            ct = new HashMap<String,Counter>();
                            sharedTG.put(key, ct);
                        }
                        Counter count = ct.get(w.word+w.pos_tag);
                        if(count==null){
                            count = new Counter(1);
                            ct.put(w.word+w.pos_tag, count);
                        }else {
                            count.inc();
                        }
                    }
                }
            }
        }
        return null;
    }

    public void storePatterns(Map<String, Rules>[] map, DBUtils db) {
        DBUtils dbs;
        dbs = db;
        Connection cons;
        Statement stms;
        PreparedStatement ps;
        try {
            System.out.println("----> Storing patterns.....");
            dbs.dropTable("Patterns");
            cons = dbs.getConnection();
            stms = cons.createStatement();
            String sql = "CREATE CACHED TABLE PATTERNS(TRGKEY VARCHAR(80), TYPE VARCHAR(25), verb_type int, POS varchar(5), "
                    + "chunk_type varchar(5), pos1 boolean, pos2 boolean, prep1 varchar(10), prep2 varchar(10), prep_order boolean,"
                    + "has_theme2 boolean,in_chunk boolean, chunk1 int, chunk2 int, event1 boolean, event2 boolean,"
                    + "trg1 varchar(2000), trg2 varchar(2000), pcount int, detected int)";
            stms.executeUpdate(sql);
            ps = cons.prepareStatement("INSERT INTO Patterns(trgkey,type,verb_type, pos, chunk_type,pos1, pos2,"
                    + "prep1,prep2,prep_order,has_theme2,in_chunk,chunk1,chunk2,event1,event2,trg1,trg2,pcount,detected) VALUES(?,?,?,?,?,?,?,?,?,?,?"
                    + ",?,?,?,?,?,?,?,?,?)");
            Map<String, Rules> m;
            String type;
            Rules rule;
            RuleData p;
            for (int i = 0; i < map.length; i++) {
                m = map[i];
                type = SenSimplifier.trigger_type[i];
                for (String s : m.keySet()) {
                    rule = m.get(s);
                    for (String st : rule.map.keySet()) {
                        p = rule.map.get(st);
                        ps.setString(1, s);
                        ps.setString(2, type);
                        ps.setInt(3, p.verb_type);
                        ps.setString(4, p.POS);
                        ps.setString(5, p.chunk_type);
                        ps.setBoolean(6, p.theme_pos);
                        ps.setBoolean(7, p.cause_pos);
                        ps.setString(8, p.prep1);
                        ps.setString(9, p.prep2);
                        ps.setBoolean(10, p.prep_order);
                        ps.setBoolean(11, p.has_cause);
                        ps.setBoolean(12, p.in_chunk);
                        ps.setInt(13, p.dist1);
                        ps.setInt(14, p.dist2);
                        ps.setBoolean(15, p.event1);
                        ps.setBoolean(16, p.event2);
                        ps.setString(17, p.mapToString(p.childMap));
                        ps.setString(18, p.mapToString(p.parentMap));
                        ps.setInt(19, p.count);
                        p.detected = getCountDetectedTG(s,p.POS+p.chunk_type);
                        ps.setInt(20, p.detected);
                        ps.executeUpdate();
                    }
                }
            }
            ps.close();
            System.out.println("---DONE---> Saving patterns");
        } catch (Exception e) {
            System.out.println("ERORR here ");
            System.out.println(e);
        }
    }

    private void combiningNP(Map<String, Rules> rl, Map<String, RuleSet> ruleset, int i) {
        RuleSet rs;
        String rkey;
        int order1, order2;
        Rules rule;
        List<RuleData> ls;
        String keys[] = {"NNNP", "VBNP", "JJNP", "NNCP", "VBCP", "JJCP",}; // noun, adj, vb in noun phrase
        for (String s : rl.keySet()) {
            rule = rl.get(s);
            rule.initMap();
            // process NP chunk
            // cases to considered: PRO-TG (PRO)*
            //TG prep1 PRO1 (prep2 PRO2)*
            for (String subkey : keys) {
                ls = rule.getEvalRules(subkey); // all NP pattern of current trg
                if (ls == null) {
                    continue;
                }
                rkey = s + i + subkey; // trg + type + pos(first two letters) + chunk type
                // has NP patterns
                rs = new RuleSet();
                order1 = 0;
                order2 = 0;
                for (RuleData dt : ls) {
                    if (dt.in_chunk) { // in chunk case
                        rs.in_chunk = true;
                        rs.inchunk_count += dt.count; // count number of inchunk event
                        if (!dt.has_cause) { // all event type without theme2/cause
                            if (dt.event1) {
                                rs.ecount += dt.count;
                            } else {
                                rs.pcount += dt.count;
                            }
                        } else if (i == 5 && dt.has_cause) { // theme2 PRO-TG PRO
                            rs.t2count += dt.count;
                            rs.pcount += dt.count;
                            rs.dist2 = Math.max(rs.dist2, dt.dist2);
                        } else if (i > 5 && dt.theme_pos && dt.has_cause) { // has cause
                            rs.pcause += dt.count; // assume only pro is cause : PRO - TG Theme (Pro/Evt)
                            if (dt.event1) {
                                rs.ecount += dt.count;
                            } else {
                                rs.pcount += dt.count;
                            }
                            rs.dist1 = Math.max(rs.dist1, dt.dist1);
                        }
                    } else if (dt.theme_pos) { // for all POS : TG - prep - PRO
                        if ((dt.POS.startsWith("NN") && !dt.prep1.isEmpty()) || !dt.POS.startsWith("NN")) {// (NN && prep) or (VB/JJ)
                            if (!dt.has_cause) { // all event type, no theme2 / cause
                                if (i <= 5) {
                                    rs.pcount += dt.count;
                                } else {
                                    if (dt.event1) {
                                        rs.ecount += dt.count;
                                    } else {
                                        rs.pcount += dt.count;
                                    }
                                }
                                rs.dist1 = Math.max(rs.dist1, dt.dist1);
                            } else if (dt.cause_pos && !dt.prep2.isEmpty() && dt.POS.startsWith("NN")) { // TG-prep1-PRO1-prep2-PRO2 ; only NNx
                                if (i == 5) {
                                    rs.t2count += dt.count;
                                    rs.pcount += dt.count;
                                } else {
                                    if (dt.event1) {
                                        rs.ecount += dt.count;
                                    } else {
                                        rs.pcount += dt.count;
                                    }
                                    if (dt.event2) {
                                        rs.ecause += dt.count;
                                    } else {
                                        rs.pcause += dt.count;
                                    }
                                }
                                rs.dist1 = Math.max(rs.dist1, dt.dist1);
                                rs.dist2 = Math.max(rs.dist2, dt.dist2);
                            }
                        }
                    } else if (i == 5 && !dt.theme_pos && ((dt.has_cause && dt.cause_pos) || !dt.POS.startsWith("NN"))) { // Binding: PRO1 - TG - PRO2
                        rs.in_front += dt.count;
                        if (!dt.prep2.isEmpty()) {
                            rs.prep_2.add(dt.prep2);
                        }
                        if (!dt.prep1.isEmpty()) {
                            rs.prep_1.add(dt.prep1);
                        }
                        rs.dist1 = Math.max(rs.dist1, dt.dist1);
                        rs.dist2 = Math.max(rs.dist2, dt.dist2);
                        rs.pcount += dt.count;
                        rs.dist2 = Math.max(rs.dist2, dt.dist2);
                    }
                }
                rs.detected = getCountDetectedTG(s, subkey);
                if (order2 > order1) {
                    rs.order = false;
                }
                if (rs.getFreq() >= 2) {
                    ruleset.put(rkey, rs);
                }
            }
        }
    }

    /**
     * Get number of detected tg based on POS and chunk type
     *
     * @param tg: trigger
     * @param subkey: POS + chunk type which contains tg
     * @return
     */
    private int getCountDetectedTG(String tg, String subkey) {
        Map<String, Counter> ct = TGCount.get(tg);
        if (ct != null) {
            if (subkey.endsWith("CP")) { // CP->NP
                subkey = subkey.substring(0, subkey.length()-2) + "NP";
            }
            Counter c = ct.get(subkey); //subkey: NNNP;VBNP;JJNP;VBVP;JJVP
            return c != null ? c.count : 0;
        }
        return 0;
    }

    private void combiningVP(Map<String, Rules> rl, Map<String, RuleSet> ruleset, int i) {
        RuleSet rs;
        String rkey;
        int order1;
        Rules rule;
        List<RuleData> ls;
        String keys[] = {"VBVP", "JJVP"}; // noun, adj, vb in noun phrase
        for (String s : rl.keySet()) {
            rule = rl.get(s);
            rule.initMap();
            //** VP JJ
            for (String subkey : keys) {
                ls = rule.getEvalRules(subkey); // all NP pattern of current trg
                if (ls == null) {
                    continue;
                }
                rkey = s + i + subkey;
                rs = new RuleSet();
                order1 = 0;
                for (RuleData dt : ls) {
                    if (dt.count < 2 && i < 5) {
                        //continue;
                    }
                    if (!dt.has_cause) {
                        if (i <= 5) {
                            rs.pcount += dt.count;
                        } else {
                            if (dt.event1) {
                                rs.ecount += dt.count;
                            } else {
                                rs.pcount += dt.count;
                            }
                        }
                    } else { // has theme2/cause
                        if (i == 5) {
                            rs.t2count += dt.count;
                            rs.pcount += dt.count;
                        } else {
                            if (dt.event1) {
                                rs.ecount += dt.count;
                            } else {
                                rs.pcount += dt.count;
                            }
                            if (dt.event2) {
                                rs.ecause += dt.count;
                            } else {
                                rs.pcause += dt.count;
                            }
                        }
                    }
                    if (dt.verb_type == 1 && dt.POS.equals("VBN") && dt.theme_pos) { // 
                        order1 += dt.count;
                    } else if (dt.verb_type == 1 && dt.POS.equals("VBN") && !dt.theme_pos) {
                        order1 -= dt.count;
                    }
                    rs.dist1 = Math.max(rs.dist1, dt.dist1);
                    rs.dist2 = Math.max(rs.dist2, dt.dist2);
                }
                rs.detected = getCountDetectedTG(s,subkey);
                if (order1 > 0) {
                    rs.order = false;
                }
                if (rs.getFreq() >= 2) {
                    ruleset.put(rkey, rs);
                }
            }
        }
    }

    public Map<String, RuleSet> combiningRules(Map<String, Rules>[] rules) {
        Map<String, RuleSet> ruleset = new HashMap<String, RuleSet>();
        Map<String, Rules> rl;
        for (int i = 0; i < 9; i++) {
            rl = rules[i];
            combiningNP(rl, ruleset, i);
            combiningVP(rl, ruleset, i);
        }
        return ruleset;
    }

    public String setToStr(Set<String> set) {
        String txt = "";
        for (String s : set) {
            txt += s + " ";
        }
        return txt;
    }

    public String mapToStr(Map<String, Set<String>> map) {
        String txt = "";
        for (String s : map.keySet()) {
            txt += s + ":";
            for (String key : map.get(s)) {
                txt += key + " ";
            }
            txt += "|";
        }
        return txt;
    }

    
    
    public void storeRuleSet(Map<String, RuleSet> map, DBUtils db) {
        DBUtils dbs;
        dbs = db;
        Connection cons;
        Statement stms;
        PreparedStatement ps;
        RuleSet rs;
        try {
            System.out.println("----> Storing rulesets.....");
            dbs.dropTable("RuleSet");
            cons = dbs.getConnection();
            stms = cons.createStatement();
            String sql = "CREATE CACHED TABLE RULESET(KEY VARCHAR(80), INCHUNK BOOLEAN, DIST1 INT, DIST2 INT, PREP VARCHAR(100), "
                    + "PREP2 VARCHAR(300), T_ORDER BOOLEAN, PCOUNT INT, ECOUNT INT, T2COUNT INT, PCAUSE INT, ECAUSE INT, "
                    + "INPREP VARCHAR(100), in_front int, prep_1 varchar(100), prep_2 varchar(100), detected int, inchunk_count int, apply int)";
            stms.executeUpdate(sql);
            ps = cons.prepareStatement("INSERT INTO RULESET(key,inchunk,dist1,dist2,prep,prep2,t_order,pcount,ecount,t2count,pcause,"
                    + "ecause,INPREP, in_front, prep_1,prep_2, detected, inchunk_count,apply) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            for (String s : map.keySet()) {
                rs = map.get(s);
                ps.setString(1, s);
                ps.setBoolean(2, rs.in_chunk);
                ps.setInt(3, rs.dist1);
                ps.setInt(4, rs.dist2);
                ps.setString(5, setToStr(rs.prep));
                ps.setString(6, mapToStr(rs.prep2));
                ps.setBoolean(7, rs.order);
                ps.setInt(8, rs.pcount);
                ps.setInt(9, rs.ecount);
                ps.setInt(10, rs.t2count);
                ps.setInt(11, rs.pcause);
                ps.setInt(12, rs.ecause);
                ps.setString(13, setToStr(rs.inchunk_prep));
                ps.setInt(14, rs.in_front);
                ps.setString(15, setToStr(rs.prep_1));
                ps.setString(16, setToStr(rs.prep_2));
                ps.setInt(17,rs.detected);
                ps.setInt(18,rs.inchunk_count);
                ps.setInt(19,rs.apply);
                ps.executeUpdate();
            }
            ps.close();
            System.out.println("---DONE---> Saving patterns");
        } catch (Exception e) {
            System.out.println("ERORR here ");
            System.out.println(e.getLocalizedMessage());
        }
    }
    public final static Set<String> preps = new HashSet<String>();
    public final static Set<String> prepmap = SenSimplifier.prepmap ;
    static {
        preps.add("by");
        preps.add("through");
        preps.add("after");
    }
    int nppt = 0, vppt = 0, vppt2 = 0;
    EData curr_event = null;
}
