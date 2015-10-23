package relations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import parser.Parser;
import utils.DBUtils;

/**
 *
 * @author Chinh
 * @Date: Nov 26, 2010
 */
public class Eval {

    /**
     * Main class for extracting event from text
     *
     */
    public Eval() {
        int i = 0;
        for (String s : trigger_type) {
            hashType.put(s, new Integer(i));
            allTrgs[i] = new ArrayList<Word>();
            i++;
        }
    }

    public Eval(SenSimplifier simp) {
        sim = simp;
        int i = 0;
        for (String s : trigger_type) {
            hashType.put(s, new Integer(i));
            allTrgs[i] = new ArrayList<Word>();
            i++;
        }
    }
    SenSimplifier sim;
    FileWriter writer;
    List<Word>[] allTrgs = new ArrayList[9];
    Map<String, String> proIDMap = new HashMap<String, String>();
    Map<String, TData> mprotein = new HashMap<String, TData>();
    Map<String, TData> mtrigger = new HashMap<String, TData>();
    Map<String, EData> mevent = new HashMap<String, EData>();
    List<TData> plist, tlist; // protein list, trigger list
    List<EData> elist; // event list
    String simp, simpsen[], pname, tg_name, longsen[];
    TData tgr, prt;
    int[] full_pos;// starting position of the sentence related to the abstract
    int trg_id = 0;
    int ev_id = 0;
    public Map<PData, String> evtMap = new HashMap<PData, String>(); // candidate/ extracted event
    public Map<Word, PData> proMap = new HashMap<Word, PData>(); // map protein -> event contains this protein
    public Map<Word, String> trgIDMap = new HashMap<Word, String>(); // Keep track of trigger being used.

    public int findPos(int[] loc, Word[] w) {
        int i = 0;
        while (i < w.length) {
            if (loc[0] == w[i].loc || loc[1] == w[i].loc + w[i].word.length()) {
                return w[i].pos;
            } else {
                i++;
            }
        }
        return -1;
    }
    /**
     * Helper method to analyze data
     */
    int event_count = 0; // count number of compound trigger
    List<EData> events[];

    public void printEvents(String id) {
        sim.loadDict(null);
        initAbstract(id);
        List<EData> levents[] = splitEvents(elist);
        Parser analyzer = new Parser();
        try {
            for (int i = 0; i < levents.length; i++) {
                List<EData> list = levents[i];
                System.out.println(simpsen[i]);
                analyzer.printChunk(simpsen[i]);
                for (EData eData : list) {
                    System.out.println(eData.getTxt(0));
                }
                System.out.println("");
            }
        } catch (Exception ex) {
            System.out.println(ex.getCause());
        }
    }

   

    public void countLevel() {
        DBUtils db = new DBUtils();
        Map<Integer, Counter> map = new HashMap<Integer, Counter>();
        sim.loadDict(db);
        List<String> pmids = sim.loadPMIDs();
        int k;
        Counter ct;
        for (String st : pmids) {
            initAbstract(st);
            for (EData edt : elist) {
                k = edt.getLevel(0);
                if (k >= 4) {
                    System.out.println(edt.getTxt(0));
                }
                if (map.containsKey(k)) {
                    map.get(k).inc();
                } else {
                    ct = new Counter();
                    map.put(k, ct);
                }
            }
        }
        for (Integer i : map.keySet()) {
            System.out.println("Level: " + i + "  " + map.get(i).count);
        }
    }


    public void printEventbyType(int type,String trg) {
        DBUtils db = new DBUtils();
        String dbpaht ="D:/DataNLP/Dev2011/Data";
        db.openDB(dbpaht);
        sim = new SenSimplifier(db);
        TData tg;
        int total = 0, match = 0, detected = 0, count = 0;
        List<String> pmids = sim.loadPMIDs();
        boolean print = false;
        int loop;
        try {
            for (String id : pmids) {
                if (initAbstract(id)) {
                    List<EData> levents[] = splitEvents(elist);
                    loop = simpsen.length;
                    for (int i = 0; i < loop; i++) {
                        print = false;
                        for (EData ed : levents[i]) {
                            int ev_type = sim.hashType.get(ed.type);
                            if (ev_type != type) {
                                continue;
                            }
                            tg= ed.trgdata;
                            if(tg.name.toLowerCase().contains(trg)){
                                if (!print) {
                                    System.out.println(id+" "+ i +" "+simpsen[i]);
                                    print = true;
                                }
                                System.out.println(ed.getTxt(0));
                                total++;
                            }
                        }
                        if (print) {
                            System.out.println("");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
        System.out.println("Total event found: "+total);
    }

    public void printEventType(int type) {
        DBUtils db = new DBUtils();
        String dbpaht ="D:/DataNLP/Dev2011/Data";
        db.openDB(dbpaht);
        sim = new SenSimplifier(db);
        TData tg;
        int total = 0, match = 0, detected = 0, count = 0;
        List<String> pmids = sim.loadPMIDs();
        boolean print = false;
        int loop;
        int begin,end;
        String key=" dependent";
        String key2=" ";
        int sen_count=0;
        try {
            for (String id : pmids) {
                if (initAbstract(id)) {
                    List<EData> levents[] = splitEvents(elist);
                    loop = simpsen.length;
                    for (int i = 0; i < loop; i++) {
                        if(!simpsen[i].contains(key)||!simpsen[i].contains(key2)){
                            continue;
                        }
                        sen_count++;
                        begin = full_pos[i];
                        end =begin+longsen[i].length();
                        print = false;
                        for (EData ed : levents[i]) {
                            int ev_type = SenSimplifier.hashType.get(ed.type);
                            if (ev_type != type) {
                                //continue;
                            }
                            tg = ed.trgdata ;
                            if(inSentence(begin,end,ed)&& tg.name.toLowerCase().equals("dependent")){
                                if (!print) {
                                    System.out.println(id+" "+ i +" "+simpsen[i]);
                                    print = true;
                                }
                                System.out.println(ed.getTxt(0));
                                total++;
                            }
                        }
                        if (print) {
                            System.out.println("");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
        System.out.println("Total event found: "+total+ " number of sentences: "+sen_count);
    }
    
    public void printFile(String path, String dest) {
        File file = new File(path);
        BufferedReader reader = null;
        String st[];
        try {
            reader = new BufferedReader(new FileReader(file));
            FileWriter fw = new FileWriter(dest);
            String text ;
            // repeat until all lines is read
            while ((text = reader.readLine()) != null) {
                if (text.contains("verexpression")) {
                    fw.append(text);
                    fw.append("\n");
                }
            }
            fw.close();
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getCause());
        } catch (IOException e) {
            System.out.println(e.getCause());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println(e.getCause());
            }
        }
    }

    public void evalSimple() {
        TData tg = null;
        int total = 0;
        String sr_path = "D:/DataNLP/Mix2011/Data";
        String dest_path = "D:/DataNLP/Dev2011/Data";
        DBUtils db_sr = new DBUtils();
        db_sr.openDB(sr_path);
        DBUtils dbdest = new DBUtils();
        dbdest.openDB(dest_path);
        double pc, rc, fc;
        boolean print, found;
        int smiss = 0;
        List<EData> emiss = new ArrayList<EData>();
        EventExtraction ex = new EventExtraction(db_sr, dbdest);
        sim = ex.simp;
        List<String> pmids = sim.loadPMIDs();
        System.out.println("Loading done: " + pmids.size());
        //pmids.clear();
        //pmids.add("PMC-1134658-08-Discussion");
        Word trg = null;
        int match[] = new int[9];
        int detected[] = new int[9];
        int totals[] = new int[9];
        int start, end;
        TData pro1, pro2;
        Set<EData> miss_list = new HashSet<EData>();
        Set<PData> dlist = new HashSet<PData>();
        Set<PData> allpair = new HashSet<PData>();
        int ttype = 0;
        String tgname ="binds";
        boolean check;
        try {
            ex.init();
            for (String id : pmids) {
                initAbstract(id);
                ex.initSentence(id);
                List<EData>[] elists = splitEvents(elist);
                for (int i = 0; i < simpsen.length; i++) {
                    ex.extractedSet.clear();
                    ex.extractedMap.clear();
                    ex.extractSentence(i);
                    start = full_pos[i];
                    end = start + longsen[i].length();
                    miss_list.clear();
                    dlist.clear();
                    allpair.clear();
                    for (EData edt : elists[i]) {
                        int etype = SenSimplifier.hashType.get(edt.type);
                        if (etype != ttype) {
                            continue;
                        }
                        // Check whether pro1/pro2 in the same sentence
                        totals[etype]++;
                        if(!inSentence(start,end,edt)){
                            continue ;
                        }
                        if(edt.trgdata.name.toLowerCase().equals(tgname)){
                            check =true ;
                        }else {
                            check =true ;
                        }
                        if (!edt.trgdata.equals(tg)) { // new trg
                            tg = edt.trgdata;
                            trg = null;
                            for (Word w : ex.extractedMap.keySet()) {
                                if (w.locs[0] == tg.list[0] || w.locs[1] == tg.list[1]) {
                                    trg = w;
                                    break;
                                }
                            }
                        }
                        // find Trg
                        if (check&& trg != null) {
                            List<PData> list = ex.extractedMap.get(trg);
                            found = false;
                            for (PData pdt : list) {
                                found = compareEvent(pdt, edt);
                                if (found) {
                                    match[ttype]++;
                                    dlist.add(pdt);
                                    break;
                                }
                            }
                            if (!found) {
                                miss_list.add(edt);
                            }
                        } else if (ex.dic.containsKey(tg.name.toLowerCase())&& check) {
                            miss_list.add(edt);
                        }
                    }
                    for (PData pdt : ex.extractedSet) {
                        if (SenSimplifier.hashType.get(pdt.evt_type) == ttype && (pdt.trg.word.equals(tgname)||true)) {
                            allpair.add(pdt);
                            detected[ttype]++;
                        }
                    }
                    allpair.removeAll(dlist);
                    if (miss_list.size() > 0 || allpair.size() > 0) {
                        System.out.println(id + " " + i + ": " + simpsen[i]);
                        ex.printChunkList();
                        if (miss_list.size() > 0) {
                            System.out.println("----miss-------> ");
                            for (EData edt : miss_list) {
                                System.out.println(edt.getTxt(0));
                            }
                        }
                        if (allpair.size() > 0) {
                            System.out.println("--------------------- false positive --------");
                            for (PData pdt : allpair) {
                                System.out.println(" --> " + pdt.getText());
                            }
                        }
                        System.out.println("");

                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
        for (int i = ttype; i <= ttype; i++) {
            System.out.println("match: " + match[i] + " | Detected: " + detected[i]);
            rc = (1f * match[i] / totals[i]);
            pc = (1f * match[i] / detected[i]);
            fc = (rc * pc * 2 / (rc + pc));
            System.out.println("Type: " + i + " PC: " + pc + "   RC: " + rc + "  FSCORE: " + fc);
        }
        db_sr.closeDB();
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
    public void evalReg() {
        TData tg = null;
        int total = 0;
        String sr_path = "D:/DataNLP/Train2011/Data";
        String dest_path = "D:/DataNLP/Dev2011/Data";
        DBUtils db_sr = new DBUtils();
        db_sr.openDB(sr_path);
        DBUtils dbdest = new DBUtils();
        dbdest.openDB(dest_path);
        double pc, rc, fc;
        boolean print, found;
        int skip_count = 0;
        List<EData> emiss = new ArrayList<EData>();
        EventExtraction ex = new EventExtraction(db_sr, dbdest);
        sim = ex.simp;
        List<String> pmids = sim.loadPMIDs();
        //pmids.clear();
        //pmids.add("PMC-1134658-00-TIAB");
        System.out.println("Loading done: " + pmids.size());
        Word trg = null;
        int match[] = new int[9];
        int detected[] = new int[9];
        int totals[] = new int[9];
        int start, end;
        Set<EData> miss_list = new HashSet<EData>();
        Set<PData> dlist = new HashSet<PData>();
        Set<PData> allpair = new HashSet<PData>();
        int ttype = 7;
        int level, level_2 = 0;
        int tg_all = 0, tgcount = 0;
        Map<String, Counter> counter = new HashMap<String, Counter>();
        String tgname="effect";
        try {
            ex.init();
            for (String id : pmids) {
                initAbstract(id);
                ex.initSentence(id);
                List<EData>[] elists = splitEvents(elist);
                for (int i = 0; i < simpsen.length; i++) {
                    ex.extractedSet.clear();
                    ex.extractedMap.clear();
                    ex.extractSentence(i);
                    start = full_pos[i];
                    end = start + longsen[i].length();
                    miss_list.clear();
                    dlist.clear();
                    allpair.clear();
                    print =false ;
                    //System.out.println(simpsen[i]);
                    for (EData edt : elists[i]) {
                        int etype = SenSimplifier.hashType.get(edt.type);
                        if(!inSentence(start,end,edt)){
                            continue;
                        }
                        if (etype != ttype) {
                            continue;
                        }
                        totals[etype]++;
                        level = edt.getLevel(0);
                        if (level > 2) {
                            level_2++;
                            continue;
                        }
                        // Check whether pro1/pro2 in the same sentence
                        if (!edt.trgdata.equals(tg)) { // new trg
                            tg = edt.trgdata;
                            tg_all++;
                            trg = null;
                            for (Word w : ex.extractedMap.keySet()) {
                                if (w.locs[0] == tg.list[0] || w.locs[1] == tg.list[1]) {
                                    trg = w;
                                    tgcount++;
                                    break;
                                }
                            }
                        }
                        String trgkey =tg.name.toLowerCase();
                        // find Trg
                        found = false;
                        if (trg != null) {
                            List<PData> list = ex.extractedMap.get(trg);
                            for (PData pdt : list) {
                                found = compareRegEvent(pdt, edt);
                                if (found) {
                                    match[etype]++;
                                    dlist.add(pdt);
                                    break;
                                }

                            }
                            if (!found && trgkey.equals(tgname)) {
                                miss_list.add(edt);
                            }
                        } else if (ex.dic.containsKey(trgkey) && trgkey.equals(tgname)) {
                            miss_list.add(edt);
                        }
                        
                        if (ex.dic.containsKey(trgkey)&& (!found || trg == null)) {
                            Counter ct = counter.get(trgkey);
                            if (ct == null) {
                                ct = new Counter(1);
                                counter.put(trgkey, ct);
                            } else {
                                ct.inc();
                            }
                        }
                    }
                    for (PData pdt : ex.extractedSet) {
                        if (SenSimplifier.hashType.get(pdt.evt_type) == ttype) {
                            allpair.add(pdt);
                            detected[ttype]++;
                        }
                    }
                    allpair.removeAll(dlist);
                    if (miss_list.size() > 0  ) {
                        if(!print){
                            System.out.println(id + " " + i + ": " + simpsen[i]);
                            ex.printChunkList();
                            print =true;
                        }
                        System.out.println("------------- mis events: ------------------>>");
                        for (EData edt : miss_list) {
                            System.out.println(edt.getTxt(0));
                        }
                    }
                    if (allpair.size() >0) {
                        for (PData pdt : allpair) {
                            if (!dlist.contains(pdt)&& pdt.trg.word.equals(tgname)) {
                                if (!print) {
                                    System.out.println(id + " " + i + ": " + simpsen[i]);
                                    ex.printChunkList();
                                    System.out.println("---------------------false positive ------------------");
                                    print =true ;
                                 }
                                System.out.println("-->"+pdt.getText());
                            }
                        }
                    }
                    if(print){
                        System.out.println("");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
        System.out.println("-------------------------------------------------------------");
        int tg_count =0;
        for(String s:counter.keySet()){
            Counter ct = counter.get(s);
            tg_count+=ct.count;
            System.out.println(s+ ":"+ct.count);
        }
        System.out.println("Total known tg miss: "+tg_count);
        System.out.println("-------------------------------------------------------------");
        System.out.println("TG recall:"+(tgcount*1f/tg_all)+" total known tg: "+tg_all);
        System.out.println("Level >=2 "+level_2);
        System.out.println("Skip due to not in the same sentence "+skip_count);
        for (int i = ttype; i <= ttype; i++) {
            System.out.println("Total:" + totals[i] + "  | match: " + match[i] + " | Detected: " + detected[i]);
            rc = (1f * match[i] / totals[i]);
            pc = (1f * match[i] / detected[i]);
            fc = (rc * pc * 2 / (rc + pc));
            System.out.println("Type: " + i + " PC: " + pc + "   RC: " + rc + "  FSCORE: " + fc);
        }
        db_sr.closeDB();
    }

    
    private boolean compareRegEvent(PData pdt, EData ev) {
        boolean theme = false, cause = false;
        if (ev.type.equals(pdt.evt_type)) { // same type
            TData tg = ev.trgdata;
            Word w = pdt.trg;
            if (w.locs[0] == tg.list[0] || w.locs[1] == tg.list[1]) { // same trigger
                //check theme
                if (ev.data1 instanceof TData) {
                    TData pr1 = (TData) ev.data1;
                    if (pdt.pro1 != null && pdt.pro1.TID.equals(pr1.tid)) {
                        theme = true;
                    }
                } else {
                    if (pdt.pdata1 != null) {
                        EData ev1 = (EData) ev.data1;
                        theme = compareRegEvent(pdt.pdata1, ev1);
                    }
                }
                // check cause
                if (ev.ecause != null) {
                    if (ev.ecause instanceof EData) {
                        if (pdt.pdata2 != null) {
                            cause = compareRegEvent(pdt.pdata2, (EData) ev.ecause);
                        }
                    } else {
                        TData pr2 = (TData) ev.ecause;
                        if (pdt.pro2 != null && pdt.pro2.TID.equals(pr2.tid)) {
                            cause = true;
                        }
                    }
                } else {
                    cause = true;
                }
                if (theme && cause) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean compareEvent(PData pdt, EData ev) {
        boolean theme = false, cause = false;
        if (ev.type.equals(pdt.evt_type)) { // same type
            TData tg = ev.trgdata;
            Word w = pdt.trg;
            if (w.locs[0] == tg.list[0] || w.locs[1] == tg.list[1]) { // same trigger
                //check theme
                TData pr1 = (TData) ev.data1;
                if (pdt.pro1 != null && pdt.pro1.TID.equals(pr1.tid)) {
                    theme = true;
                }
                // check cause
                if (ev.data2 != null) {
                    TData pr2 = ev.data2;
                    if (pdt.pro2 != null && pdt.pro2.TID.equals(pr2.tid)) {
                        cause = true;
                    }
                } else {
                    cause = true;
                }
                if (theme && cause) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Evaluating dictionary
     * @param type 
     */
    public void DictEval() {
        TData tg;
        List<String> pmids = sim.loadPMIDs();
        sim.loadDict(null);
        Map<String, KeyData> dic = sim.sharedDic;
        int idx;
        int load[] = new int[9], found[] = new int[9];
        try {
            for (String id : pmids) {
                if (initAbstract(id)) {
                    for (EData ev : elist) {
                        idx = sim.hashType.get(ev.type);
                        if (dic.containsKey(ev.trgdata.name.toLowerCase())) {
                            found[idx]++;
                        }
                        load[idx]++;
                    }
                }

            }
            for (int i = 0; i < 9; i++) {
                System.out.println("Type: " + this.trigger_type[i] + " Recall: " + 1f * found[i] / load[i]);
            }
        } catch (Exception e) {
            System.out.println(e.getCause());
        }


    }

    public void printAllSentence(int type) {
        List<String> pmids = sim.loadPMIDs();
        int loop;
        Parser analyzer = new Parser();
        ChunkAnalyzer operator = new ChunkAnalyzer();
        int count = 0, total = 0;
        String txt;
        List<Chunk> ls;
        Map<String, String> map = new HashMap<String, String>();
        Map<String, String> map1, map2;
        String tokens[];
        Chunk c, next, prev;
        try {
            for (String id : pmids) {
                txt = sim.loadSimplified(id);
                simpsen = sim.abs2Sen(txt, "\n");
                loop = simpsen.length;
                for (int i = 0; i < loop; i++) {
                    analyzer.id = id;
                    analyzer.senid = i;
                    tokens = analyzer.splitWord(simpsen[i]);
                    ls = analyzer.parse(tokens);
                    total++;
                    for (int j = 1; j < ls.size() - 1; j++) {
                        prev = ls.get(j);
                        if (prev.type.equals("VP")) {
                            c = ls.get(j - 1);
                            if (c.txt.equals(",")) {
                                System.out.println("--> " + prev.getValues());
                                printChunk(ls);
                                count++;
                                break;
                            }
                        }
                    }
                }
                if (count > 1000) {
                    break;
                }
            }
            System.out.println("--total sentences found-- " + count + "  over " + total);

        } catch (Exception e) {
            System.out.println(e.getCause());
        }

    }

    private void printChunk(List<Chunk> ls) {
        for (Chunk c : ls) {
            System.out.print("[" + c.type + " " + c.txt + "] ");
        }
        System.out.println("");
    }

    
    public void printCompoundEvent(int type) {
        TData pro;
        int scount = 0, ecount = 0;
        List<String> pmids = sim.loadPMIDs();
        int loop;
        boolean print = false;
        try {
            for (String id : pmids) {
                if (initAbstract(id)) {
                    List<EData> levents[] = splitEvents(elist);
                    loop = simpsen.length;
                    for (int i = 0; i < loop; i++) {
                        print = false;
                        for (EData ed : levents[i]) {
                            pro = ed.getPro();
                            if (simpsen[i].contains(pro.new_name + "/")) {
                                if (!print) {
                                    print = true;
                                    System.out.println(simpsen[i]);
                                }
                                scount++;
                                System.out.println(ed.getTxt(0));
                            } else if (simpsen[i].contains("/" + pro.new_name)) {
                                if (!print) {
                                    print = true;
                                    System.out.println(simpsen[i]);
                                }
                                ecount++;
                                System.out.println(ed.getTxt(0));
                            }
                        }
                        if (print) {
                            System.out.println("");
                        }

                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
        System.out.println("Found: " + scount + " total: " + ecount);
        if (compMap.size() > 0) {
            for (String s : compMap.keySet()) {
                System.out.println(s);
            }
        }
    }
    Map<String, String> compMap = new HashMap<String, String>();
    boolean run_1 = false;

    /**
     * Map a given trigger to a detected trigger
     * @param loc: relative location of given trigger
     * @return: Word (location)
     */
    private Word findTrigger(int loc[], List<Word> relWord) {
        for (Word w : relWord) {
            if (w.locs[0] >= loc[0] && w.locs[1] <= loc[1]) {
                return w;
            }
        }
        return null;
    }
    /**
     * Find location of a given protein
     * @param pr: protein name
     * @return: Word (location)
     */
    StringBuilder sbtxt = new StringBuilder();

    /**
     * Load all data (protein, trigger, event) belong to this abtract/paragraph
     * Split abstract/paragraph into sentences
     * @param pid
     * @return 
     */
    public boolean initAbstract(String pid) {
        senText = sim.loadSentence(pid);
        if (senText == null) {
            System.out.println("PMID: " + pid + " -> no text");
            return false;
        }
        plist = sim.loadProtein(pid); //load Protein list
        tlist = sim.loadTrigger(pid); // load Trigger list based on PMID
        elist = sim.loadEvent(pid); // load Event list
        simp = sim.loadSimplified(pid); // simplified sentence
        simpsen = sim.abs2Sen(simp, "\n");// array of sentences
        longsen = sim.abs2Sen(senText, "\n");// array of sentences
        full_pos = new int[longsen.length];
        if (simpsen.length != longsen.length) {
            System.out.println("Skip ---> " + pid);
            return false;
        }
        sbtxt = new StringBuilder(senText);
        /**
         * Init starting sentences
         */
        for (int i = 1; i < simpsen.length; i++) {
            full_pos[i] = senText.indexOf(longsen[i], full_pos[i - 1]);
        }

        // preparing Protein Map
        mprotein.clear();
        for (TData dt : plist) {
            mprotein.put(dt.tid, dt); // map <id, protein>
        }
        // preparing trigger map
        mtrigger.clear();
        for (TData dt : tlist) {
            mtrigger.put(dt.tid, dt); // map <trigger id , trigger>
        }
        // preparing event map
        mevent.clear();
        for (EData edt : elist) {
            mevent.put(edt.eid, edt); // map <event id , event>
        }
        for (EData ed : elist) {
            ed.init(mprotein, mtrigger, mevent);
        }
        return true;
    }

    public void printCompoundTrigger(TData tg, List<Word> ls) {
        int pos = tg.list[0] - 1;
        String sub;
        if (pos >= 0) {
            if (sbtxt.charAt(pos) == '-') {
                sub = sbtxt.substring(tg.list[0], tg.list[1]);
                for (Word w : ls) {
                    if (w.compound) {
                        if (w.word.equals(sub) && tg.list[0] <= w.locs[0] && tg.list[1] >= w.locs[1]) {
                            System.out.println("TG: " + sub + " pos: " + tg.list[0] + "-" + tg.list[1] + "  Detected: " + w.fullword + "  pos: " + w.locs[0] + " " + w.locs[1]);
                        }
                    }
                }
                event_count++;
            }
        }
    }

    /**
     * Spliting event per sentence
     * @param list: list of event
     * @return: array list of event per sentence
     */
    public List<EData>[] splitEvents(List<EData> list) {
        List<EData> dlist[] = new List[simpsen.length];
        int pos, idx;
        TData tg;
        for (int i = 0; i < simpsen.length; i++) {
            dlist[i] = new ArrayList<EData>();
        }
        for (EData dt : list) {
            tg = mtrigger.get(dt.trigID);
            pos = tg.list[0];
            idx = pos2sen(full_pos, pos);
            dlist[idx].add(dt);
        }
        return dlist;
    }

    /**
     * Split list of Data (Trigger/Protein) based on number of sentences
     * @param list: trigger/protein
     * @return: Array of list
     */
    public List<TData>[] splitData(List<TData> list) {
        List<TData> dlist[] = new List[simpsen.length];
        for (int i = 0; i < simpsen.length; i++) {
            dlist[i] = new ArrayList<TData>();
        }
        int pos, idx;
        for (TData dt : list) {
            pos = dt.list[0];
            idx = pos2sen(full_pos, pos);
            dlist[idx].add(dt);
        }
        return dlist;
    }

    public List<Word>[] splitTrg(List<Word> list) {
        List<Word> dlist[] = new List[simpsen.length];
        for (int i = 0; i < simpsen.length; i++) {
            dlist[i] = new ArrayList<Word>();
        }
        int pos, idx;
        for (Word dt : list) {
            pos = dt.locs[0];
            idx = pos2sen(full_pos, pos);
            dlist[idx].add(dt);
        }
        return dlist;
    }

    public void test() {
        String sr_path = "D:/DataNLP/Mix2011/Data";
        DBUtils sr = new DBUtils();
        sr.openDB(sr_path);
        Map<String, Rules> rule1[] = sim.loadPatterns(sr);
        sim.loadDict(sr);
        KeyData kdt = sim.sharedDic.get("induction");
        kdt = kdt.getDefault();
        Rules rules = rule1[6].get("induction");
        List<RuleData> ls = rules.getEvalRules("NNNP");
        int count = 0, miss = 0;
        if (ls != null) {
            for (RuleData dt : ls) {
                for (String s : dt.childMap) {
                    if (!kdt.child.contains(s)) {
                        System.out.println("Miss: " + s);
                        miss++;
                    } else {
                        count++;
                    }
                }
            }
        }
        System.out.println("Found: " + count + "  miss: " + miss);
        sr.closeDB();
    }

    public static void main(String[] args) {
        Eval xt = new Eval();
        //xt.printFile("D:/Output/test/out.txt", "D:/Output/out.txt");
        xt.evalReg();
        //xt.printEventbyType(1, "mrna");
    }

    public void comparePatterns() {
        String sr_path = "D:/DataNLP/Mix2011/Data";
        DBUtils sr = new DBUtils();
        sr.openDB(sr_path);
        Map<String, RuleSet> rules = sim.loadRuleSet(sr);
        System.out.println("Number of rules: " + rules.size());
        sr.closeDB();
    }

    private int pos2sen(int[] senpos, int pos) {
        for (int i = senpos.length - 1; i > 0; i--) {
            if (pos >= senpos[i]) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Convert event type to index
     * @param type: event type
     * @return: index of that event type
     */
    private int toType(String type) {
        return hashType.get(type);

    }
    String[] trigger_type = {"Gene_expression", "Transcription", "Protein_catabolism","Phosphorylation", "Localization",
         "Binding", "Positive_regulation", "Regulation", "Negative_regulation"};
    Map<String, Integer> hashType = new HashMap<String, Integer>();
    String senText;
}
