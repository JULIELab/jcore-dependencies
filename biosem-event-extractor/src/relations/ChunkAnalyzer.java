package relations;

import parser.Parser;

import java.util.*;

/**
 *
 * @author Chinh
 * @Date: Jun 16, 2011
 */
/**
 * 
 * Define all operations on Chunk such as forming NP, VP, PP, AP
 */
public class ChunkAnalyzer {

    public ChunkAnalyzer() {
        prepMap.addAll(Arrays.asList(prepList));
        breakMap.addAll(Arrays.asList(breakList));
        skipMap.addAll(Arrays.asList(skipList));
        ccMap.addAll(Arrays.asList(ccList));
        appoMap.addAll(Arrays.asList(appoList));
        beMap.addAll(Arrays.asList(to_be));
        allMap.addAll(ccMap);
        allMap.addAll(breakMap);
    }
    /**
     * input: list of chunks from single sentence
     * output: Basic chunks (PP, NP) and Verb chunks (subject/object)
     * Drop chunks: chunks that can not extract event (no Pro and no Trg)
     */
    List<BasicChunk> bsList = new ArrayList<BasicChunk>();
    List<VerbChunk> verbList = new ArrayList<VerbChunk>();
    boolean has_stop = false, shared_sub = false, reduced_clause = false;
    boolean has_breaker = false, next_clause = false;
    boolean has_comma = false;
    boolean use_prev_obj = false;
    String pos_tags[], stokens[];
    public String curr_text;
    
    public void cleanChunk(List<Chunk> ls, String[] tags, String[] tokens) {
        pos_tags = tags;
        stokens = tokens;
        dropChunks(ls);
        groupVerbPhrase(ls);
        printChunk(ls);
    }

    /**
     * TODO list:
     * 1. and/or in NP followed by VP -> split NP to get subj for VP
     * 2. VB is embbeded in NP/ ADVP -> split/or change to VP
     * 3. PP +VBing -> use [NP] in front of this VP
     * 4. Apposition vs. Coordination: NP contains and/or -> coordination
     * 5. NP,NP,VP -> apposition
     * 6. Two coordination: coord1 and coord2 -> sub-coord, then global coord
     * 7. [where/when/while] break
     * 8. [SBAR] sub-clause -> [O ,] for break
     * 9. Remove [O like: -, ', "]
     * 
     */
    /**
     * Analyzing chunks to form NP, VP...
     * @param ls : chunks from parser
     * Output: basic chunks stored in bsList
     *         verb chunks stored in verbList 
     */
    public void analyzeChunk(List<Chunk> ls, String tags[], String tokens[]) {
        Set<BasicChunk> shared = new HashSet<BasicChunk>();
        bsList.clear();
        verbList.clear();
        pos_tags = tags;
        stokens = tokens;
        debug = false;
        int i = 0;
        Chunk cur_vb, br = null, c;
        c = ls.get(ls.size() - 1);
        if (!c.txt.equals(".")) {
            Chunk o_c = new Chunk("O");
            o_c.txt = ".";
            o_c.begin = c.end + 1;
            o_c.end = c.end + 2;
            ls.add(o_c);
        }
        dropChunks(ls); // remove [O] chunk
        groupVerbPhrase(ls); // combining verb phrases and drop ADVPs neighbouring them
        dropChunks(ls); // remove [O] chunk
        List<Chunk> vbs = findVerbChunk(ls);
        List<Chunk> breaker = findBreaker(ls);
        int vbidx , br_idx = 0, sub_type ;
        BasicChunk prev_sub = null, sub , object, prev_obj = null; // prevsubject and current subject
        int start = 0, end = 0, next_start = 0, vb_type;
        boolean merged ;
        next_pos = 0;
        has_stop = false;
        shared_sub = false;
        has_breaker = false;
        /**
         * Work flow:
         * 1. get verb chunk
         * 2. check preceeded chunks: [which]; ([,])[and,but,or] -> subject
         * 3. check succeded chunks: [that, whether] ;[breaker, ;] ; [VP] ; [O.]-> object
         * 4. get verb chunk type 
         * 5. find subject ; object
         * 6. assign subject; object for verb chunk if it contains trigger
         */
        while (i < vbs.size()) {
            cur_vb = vbs.get(i);
            vbidx = ls.indexOf(cur_vb); // position of verb in the list
            merged = false ;
            vb_type = getVerbChunkType(cur_vb); // type of verb chunk: active;passive;VBing;to VB
            if (!shared_sub) {
                start = skipPhrase(ls, start, vbidx); // skip ADVP or PP that do not contain trigger and key.
                if(vb_type==2 && start == vbidx){
                    merged =true ;
                }
            }
            use_prev_obj = false;
            // check breaker in front of verb chunk
            if (br_idx < breaker.size() || br != null) { // find breaker in front and behind verb chunk
                if (br_idx < breaker.size() && br == null) { // first breaker
                    br = breaker.get(br_idx); // has breaker
                    br_idx++;
                }
                int pos = ls.indexOf(br); // breaker position
                if (((br.txt.equals("whether") || br.txt.equals("that")) && br.type.equals("PP"))
                        || br.type.equals("SBAR")) { // left_over breaker
                    if (pos > 0 && br.begin < cur_vb.begin) { // belongs to the previous VB
                        if (start < pos) {
                            BasicChunk bs = new BasicChunk();
                            for (int k = start; k < pos; k++) {
                                bs.addChunk(ls.get(k));
                            }
                            bsList.add(bs);
                            start = pos + 1;
                        }
                        br = breaker.get(br_idx);
                        br_idx++;
                        pos = ls.indexOf(br);
                    }
                }
                if (br.begin < cur_vb.begin) { // found a breaker in front of verb chunk
                    // check for which/that : which/that VBx
                    if (br.type.equals("NP") && vbidx - pos <= 2 && (br.txt.equals("which") || br.txt.equals("that"))) {
                        if (!hasNounChunk(pos + 1, ls, vbidx)) {
                            use_prev_obj = true;//use NP in front of this chunk as subj
                            has_breaker = true;
                        } else {
                            if (start < pos) {
                                BasicChunk bs = new BasicChunk();
                                for (int k = start; k < pos; k++) {
                                    bs.addChunk(ls.get(k));
                                }
                                bsList.add(bs);
                                start = pos + 1;
                            }
                        }
                    } else {
                        // Copy skip part and put in BasicChunk:
                        if (start < pos) {
                            BasicChunk bs = new BasicChunk();
                            for (int k = start; k < pos; k++) {
                                bs.addChunk(ls.get(k));
                            }
                            bsList.add(bs);
                            start = pos + 1;
                        }
                        has_breaker = true;
                    }
                    while (br_idx < breaker.size() && br.begin < cur_vb.begin) {
                        br = breaker.get(br_idx);
                        br_idx++;
                    }
                }// repeat until no more breaker in front of this verb chunk
                // check breaker behind verb chunk
                // prepare data for object and next clause
                if (reduced_clause) { // set by previous loop
                    use_prev_obj = true;
                    reduced_clause = false;
                    merged = true ;
                }
                if (i + 1 < vbs.size()) { // has next verb
                    Chunk temp = vbs.get(i + 1);
                    int next_vb_type = getVerbChunkType(temp);
                    int next_vb = ls.indexOf(temp); // position of next verb chunk
                    pos = ls.indexOf(br);// position of breaker
                    if ((next_vb > pos)
                            && (br.txt.equals("that") || br.txt.contains("whether"))
                            && (br.type.equals("PP") || br.type.equals("SBAR"))) { // belong to current verb chunk
                        next_start = pos + 1; // position of new clause
                        has_stop = true; // start new clause ; reset all prev values
                        end = pos - 1;
                        br = null;
                    } else if (br.txt.startsWith(";") && pos < next_vb && pos > vbidx) {
                        has_stop = true; // start new clause ; reset all prev values
                        next_start = pos + 1; // position of new clause
                        end = pos - 1;
                        br = null;
                    } else if (pos > vbidx && pos < next_vb) { // VB BR VB
                        if (!(br.type.equals("NP") && next_vb - pos <= 2 && (br.txt.equals("which") || br.txt.equals("that")))) {
                            has_stop = true;
                        } else {
                            next_clause = true;
                        }
                        end = pos - 1;
                        next_start = pos + 1; // position of new clause
                    } else {
                        // set end position for object
                        List<Chunk> comlist = getCommaList(vbidx + 1, ls, next_vb);
                        List<Chunk> conjlist = getConjList(vbidx + 1, ls, next_vb);
                        if (next_vb_type == 2 && next_vb_type != vb_type && comlist.isEmpty() && conjlist.isEmpty() && !has_breaker) { // reduced clause
                            reduced_clause = true;
                            next_start = next_vb;
                        } else {
                        has_stop = false;
                        }
                        end = next_vb - 1; // longest possible (this might include NP of next subject)
                    }
                } else { // last verb
                    if (br.txt.equals(".")) {
                        end = ls.size() - 1;
                        next_pos = end;
                    } else {
                        int br_pos = ls.indexOf(br);// position of breaker
                        end = br_pos - 1;
                        next_pos = br_pos + 1; // position of new clause
                    }
                    has_stop = true;
                }

            } else { // no breaker
                System.out.println("ChunkAnalyzer: analyze >>--------------NO BREAKER --------> NERVER HAPPEND----------");
                System.exit(1);
            }
            sub_type = 0;
            // finding subject
            if (use_prev_obj) { // which / that in fonr to verb chunk
                if (prev_obj == null ) { // this is the first verb, -> find subject
                    sub = findSubject(start, ls, vbidx - 1);
                } else { // second verb
                    sub = findPreviousNP(prev_obj); // find the closest NP from the preceded object
                    sub_type=1;
                }
            } else if (shared_sub) { // this flag set by previous verb ; main verb chunk of relative clause;
                if (prev_sub != null) { // no NP left to use as subject therefore use previous object
                    sub = prev_sub ;// cloneChunk(prev_sub); // use previous subject
                    sub_type=1;
                    //sub.extracted = true ; // avoid duplicating extraction -> only use pro
                } else {
                    System.out.println("Chunk Analyzer: Analyze: ----> BUG---------> shared subject --> null");
                    System.out.println(cur_vb.getText() + " ");
                    printChunk(ls);
                    sub = findSubject(start, ls, vbidx - 1); // first verb
                }
            } else { // normal case and unknown case
                if (start < vbidx) {
                    sub = findSubject(start, ls, vbidx - 1);
                } else {
                    sub = new BasicChunk();
                    debug = true ;
                    sub_count++ ;
                }
            }
            // now find object
            if (has_stop || reduced_clause) { // breaker or relative clause
                object = new BasicChunk();
                for (int k = vbidx + 1; k < end; k++) {
                    object.addChunk(ls.get(k));
                }
                if (!ls.get(end).type.equals("O") && vbidx + 1 <= end) { // last verb ; every thing belong to object
                    if (!ls.get(end).txt.equals(",")
                            && !(ls.get(end - 1).txt.equals(",") && ccMap.contains(ls.get(end).txt))
                            && !ccMap.contains(ls.get(end).txt)) {
                        object.addChunk(ls.get(end));
                    }
                }
            } else {
                object = findObject(vbidx + 1, ls, end); // must detect subj(NP) of the next verb
            }
            // set up verb chunk
            VerbChunk verb = new VerbChunk();
            verb.verb = cur_vb;
            verb.subject = sub;
            verb.object = object;
            verb.subject_type = sub_type;
            if(sub==null || sub.isEmpty()){ // 15-11-2011: Test new case
                if(vb_type==0 && !pos_tags[cur_vb.begin].equals("VBN")){
                    if(prev_obj!=null){
                        verb.subject = findPreviousNP(prev_obj);
                        verb.subject_type =1 ;
                    }
                }
            }
            verb.verb_type = vb_type;
            if(!verb.isQualify()&& (verb.subject_type==1||verb.subject.isEmpty())){ // 22-12-2011
                merged =true ;
            }
            if(!merged || verbList.isEmpty()){
                verbList.add(verb);
            }else {
                VerbChunk verb1 = verbList.get(verbList.size()-1);
                verb1.object.addChunk(cur_vb);
                verb1.object.addChunk(object);
            }
            // reset values
            if (prev_sub == null || !use_prev_obj) {
                prev_sub = sub;
            }
            prev_obj = object;
            if (has_stop) {
                start = next_start;
                has_stop = false;
                has_breaker = false;
                prev_obj = null;
                prev_sub = null;
                shared_sub = false;
            } else if (reduced_clause) {// setup values for the next loop
                start = next_start;
            } else {
                start = next_pos;
                next_start = 0;
            }
            i++;// next verb  chunk
        }// while loop
        if (next_pos < ls.size() - 1 || vbs.isEmpty()) {
            BasicChunk bsc = new BasicChunk();
            for (i = next_pos; i < ls.size(); i++) {
                bsc.addChunk(ls.get(i));
            }
            if (bsc.proCount() > 0 && bsc.trgCount() > 0) {
                bsList.add(bsc);
            }
        }
        List<VerbChunk> rlist = new ArrayList<VerbChunk>();
        for(VerbChunk vc: verbList){
            if(vc.isQualify()&& vc.verb.trigs.isEmpty()&& !vc.subject.isQualify()&& !vc.object.isQualify()&& vc.subject_type!=1){
                rlist.add(vc);
            }
        }
        if(rlist.size()>0){
            for(VerbChunk vc:rlist){
                verbList.remove(vc);
                bsList.add(vc.merge());
            }
        }
    }
    int sub_count = 0;

    private boolean hasNounChunk(int start, List<Chunk> ls, int stop) {
        Chunk c;
        for (int i = start; i <= stop; i++) {
            c = ls.get(i);
            if (c.type.equals("NP") && !(c.txt.equals("that") || c.txt.equals("which"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine verb chunk type: passive/active/gerund
     * @param vb
     * @return:0 -> active; 1 -> passive ;2 -> VBing ; 3 -> to VB
     */
    public int getVerbChunkType(Chunk vb) {
        boolean has_be = false;
        boolean VBN = false;
        if (vb.txt.toLowerCase().startsWith("to ") && vb.end > vb.begin) {
            return 4;
        }
        for (int i = vb.begin; i <= vb.end; i++) {
            if (beMap.contains(stokens[i])) {
                has_be = true;
            }
            if (pos_tags[i].equals("VBN")) {
                VBN = true;
            }
        }
        if (pos_tags[vb.end].equals("VBG") && !has_be) {
            return 3;
        }
        if ((vb.end > vb.begin && has_be) && VBN) {
            return 1;
        }
        if (VBN) {
            return 2;
        }
        return 0;
    }

    /**
     * First step: remove 
     * @param ls 
     */
    public int skipPhrase(List<Chunk> ls, int start, int end) {
        if (start == end) {
            return start;
        }
        int begin = start;
        if (ls.get(begin).txt.equals(",")) {
            begin++;
        }
        if (ccMap.contains(ls.get(begin).txt)) {
            begin++;
        }
        if (end - begin > 2 && end < ls.size() - 3 && begin >= 0) {
            if (ls.get(begin).type.equals("ADVP") && ls.get(begin + 1).txt.equals(",")) {
                begin += 2;
            }
            Chunk c = ls.get(begin);
            if (c.type.equals("PP") || (c.type.equals("VP") && c.txt.toLowerCase().startsWith("to ")) || c.type.equals("SBAR")) { // PP or VP
                List<Chunk> comma = getCommaList(start, ls, end);
                List<Chunk> conj = getConjList(start, ls, end);
                BasicChunk bs = new BasicChunk();
                if (comma.size() == 1) { // this might be the separator between object/subject
                    // use "," to separate two clauses
                    Chunk com = comma.get(0);
                    int com_pos = ls.indexOf(com);
                    if (!hasNounChunk(com_pos, ls, end)) {
                        return begin;
                    } else {
                        end = com_pos - 1;
                        // add to BasicChunk
                        for (int i = begin; i <= end; i++) {
                            bs.addChunk(ls.get(i));
                        }
                        bsList.add(bs);
                        return com_pos + 1;
                    }
                } else if (comma.isEmpty()) { // no comma, may be never happened
                    return begin;
                } else { // more than one comma, find the last comma
                    if (conj.size() > 0) {
                        // take last conj and comma
                        Chunk and = conj.get(conj.size() - 1);
                        Chunk com = comma.get(comma.size() - 1);
                        if (and.begin == com.begin + 1) { // part of co-ordination
                            int pos = ls.indexOf(and);
                            if (ls.get(pos + 1).type.equals("NP")) { //,and NP
                                // find commas that are part of this co-ordination
                                int idx = 0;
                                for (int i = pos - 1; i > begin; i--) {
                                    c = ls.get(i);
                                    if (c.txt.equals(",") || c.type.equals("NP")) {
                                        continue;
                                    } else {
                                        idx = c.begin;
                                        break;
                                    }
                                }
                                for (int i = comma.size() - 1; i >= 0; i--) {
                                    c = comma.get(i);
                                    if (c.begin < idx) {
                                        pos = ls.indexOf(c);
                                        for (int j = begin; j <= pos; j++) {
                                            bs.addChunk(ls.get(j));
                                        }
                                        bsList.add(bs);
                                        return pos + 1;
                                    }
                                }
                            }
                        }
                    }
                    // no conj or con_pos < com_pos
                    Chunk com = comma.get(0); // assume the first coma is the separator
                    int pos = ls.indexOf(com);
                    if (hasNounChunk(pos, ls, end)) {
                        end = pos - 1;
                        for (int i = begin; i <= end; i++) {
                            bs.addChunk(ls.get(i));
                        }
                        bsList.add(bs);
                        return pos + 1;
                    }

                }


            } // PP or VP
        }
        return begin;
    }

    /**
     * Drop some ADVP and [O] chunks
     * @param ls 
     */
    private void dropChunks(List<Chunk> ls) {
        List<Chunk> list = new ArrayList<Chunk>();
        Chunk c, next, prev = null;
        int begin = 0, end = ls.size() - 1;
        if (ls.get(begin).type.equals("VP")) { // drop verb chunk
            Chunk c1;
            boolean remove = true;
            c1 = ls.get(begin);
            if (c1.trigs.isEmpty()) {
                List<Chunk> comma = getCommaList(begin, ls, end);
                int pos = comma.isEmpty() ? -1 : ls.indexOf(comma.get(0));
                if (pos > 0) {
                    for (int i = begin; i < pos; i++) {
                        c1 = ls.get(i);
                        if (c1.pros.size() > 0 || c1.trigs.size() > 0) {
                            remove = false;
                            break;
                        }
                    }
                }
                if (pos > 0 && remove) {
                    while (pos >= 0) {
                        ls.remove(0);
                        pos--;
                    }
                } else {
                    c1 = ls.get(1);
                    if ((c1.type.equals("PP") || c1.type.equals("SBAR"))
                            && (c1.txt.equals("that") || c1.txt.equals("whether"))) {
                        ls.remove(0);
                        ls.remove(0);
                    }
                }
            }
        }
        for (int i = 0; i < ls.size(); i++) {
            c = ls.get(i);
            if (skipMap.contains(c.txt) && c.type.equals("O")) {
                list.add(c);
            } else if (c.type.equals("ADVP") && !allMap.contains(c.txt)) {
                if (c.pros.isEmpty() && c.trigs.isEmpty()) {
                    if (i + 1 < ls.size()) {
                        next = ls.get(i + 1);
                        if (prev != null && !(prev.txt.equals(",") && next.txt.equals(","))) {
                            list.add(c);
                        }
                    }
                }
            }
            prev = c;
        }
        for (Chunk ch : list) {
            ls.remove(ch);
        }
    }

    /**
     * For 'which' and 'that' relative clause
     * @param start
     * @param chunk
     * @param stop
     * @return 
     */
    public BasicChunk findPreviousNP(BasicChunk obj) {
        BasicChunk bs = new BasicChunk();
        if(true){
            return obj ;
        }
        int i = obj.chunkList.size()-1;
        boolean found =false ;
        while (i >0) { // skip prep and vp
            if (obj.chunkList.get(i).type.equals("PP")) {
                found =true ;
                break;
            } else {
                i--;
            }
        }
        if(found){
            i=i+1 ;
        }else{
            i = 0;
        }
        if (i < obj.chunkList.size()) {
            for (; i < obj.chunkList.size(); i++) {
                bs.addChunk(obj.chunkList.get(i));
            }
        }
        bs.extracted  = true ; //avoid duplicating extraction
        return bs;
    }

    private BasicChunk cloneChunk(BasicChunk bc) {
        BasicChunk chunk = new BasicChunk();
        for (int i = 0; i < bc.chunkList.size(); i++) {
            chunk.addChunk(bc.chunkList.get(i));
        }
        return chunk;
    }
    
    public BasicChunk findSubject(int start, List<Chunk> chunk, int stop) {
        BasicChunk sub = new BasicChunk();
        Chunk c, next;
        if (stop >= 0) {
            next = chunk.get(stop);
        } else {
            if(start<stop){
                System.out.println("ChunkAnalyzer: ---> BUG in findSubject, start< stop");
                System.exit(0);
            }
            return sub;
        }
        if (next.txt.equals("that") || next.txt.equals("which")) {
            if (stop - start >= 2) {
                c = chunk.get(stop - 1);
                if (c.txt.equals(",")) { // has comma
                    stop = stop - 2;
                } else { // no comma
                    stop = stop - 1;
                }
            }
        } else if (next.txt.equals(",")||ccMap.contains(next.txt)) { // commar before verb
            stop--;
        }
        for (int i = start; i <= stop; i++) {
            sub.addChunk(chunk.get(i));
        }
        return sub;
    }

    /**
     * print chunks contain marker word
     * @param chunk 
     */
    public void printChunk(List<Chunk> chunk) {
        for (Chunk c : chunk) {
            System.out.print("[" + c.type + " " + c.txt + "]");
        }
        System.out.println("");
    }

    private void printChunk(int start, List<Chunk> chunk, int stop) {
        for (int t = start; t <= stop; t++) {
            System.out.print("[" + chunk.get(t).type + " " + chunk.get(t).txt + "] ");
        }
        System.out.println("");
    }
    /**
     * Get list of verb
     * @param chunk
     * @return 
     */
    boolean print = false;

    private List<Chunk> getConjList(int start, List<Chunk> ls, int end) {
        List<Chunk> conj = new ArrayList<Chunk>();
        for (int i = start; i <= end; i++) {
            if (ccMap.contains(ls.get(i).txt)) {
                conj.add(ls.get(i));
            }
        }
        return conj;
    }

    private List<Chunk> findVerbChunk(List<Chunk> chunk) {
        List<Chunk> ls = new ArrayList<Chunk>();
        int i = 0;
        Chunk c, prev = null;
        while (i < chunk.size()) {
            c = chunk.get(i);
            if (c.type.equals("VP")) {
                if (getVerbChunkType(c) < 3 && !c.txt.startsWith("as ")) { // skip VB+ing and To VB
                    ls.add(c);
                }
            }
            prev = c;
            i++;
        }
        return ls;
    }
    int next_pos = 0; // set position of last used chunk
    public boolean print_chunk = false;
    public int count_chunk = 0;

    public BasicChunk findObject(int start, List<Chunk> ls, int end) {
        // end: end of sentence or next verb chunk
        // Patterns: VB NP.....,NP VB; 
        // next verb is reduced clause
        BasicChunk obj = new BasicChunk();
        List<Chunk> comma = getCommaList(start, ls, end);
        List<Chunk> conj = getConjList(start, ls, end);
        shared_sub = false;
        if (next_clause) { // which /that
            next_pos = end + 1;
            next_clause = false;
        } else if (has_breaker) {// which, that, when, while...
            if (comma.size() > 0) { // check commar in front of next verb
                Chunk com = comma.get(comma.size() - 1);
                int com_pos = ls.indexOf(com);
                if (!hasNounChunk(com_pos, ls, end)) { // no NP between comma and VB
                    shared_sub = true; // set flag to use shared subject
                } else {
                    next_pos = com_pos + 1;
                    end = com_pos - 1;
                }
            } else if (conj.isEmpty()||isShare(start,end,ls)) { // no separator ; take all chunk
                shared_sub = true;
            }else { // use all 
                shared_sub = true;
//                System.out.println("----> Find Object: unknown case-- has breaker --: start :"+start+" end: "+end );
//                printChunk(ls);
//                System.out.println("---Text: "+curr_text);
            }
        } else if(isShare(start,end,ls)){
            shared_sub = true;
            if(ls.get(end-1).txt.equals(",")){
                end =end -2 ;
            }
        } else if (comma.size() == 1) { // this might be the separator between object/subject
            if (conj.size() > 0) {
                Chunk and = conj.get(conj.size() - 1);
                int conj_pos = ls.indexOf(and);
                if (!hasNounChunk(conj_pos, ls, end)) { // share subject
                    shared_sub = true; // set flag to use shared subject
                } else { // conjunction clause
                    Chunk com = comma.get(0);
                    int com_pos = ls.indexOf(com);
                    if (com.begin < and.begin && conj_pos - com_pos == 1) { // set end point for object
                        end = com_pos - 1;
                        // skip [, and]
                        next_pos = conj_pos + 1;
                    } else {
                        if (!hasNounChunk(com_pos, ls, end)) {
                            shared_sub = true;
                        } else { //use comma as separator
                            next_pos = com_pos + 1;
                            end = com_pos - 1; // nerver happend
//                            System.out.println("---> Find Object: ---> Never happened but happened! ");
//                            System.out.println("----> Find Object: unknown case: start :"+start+" end: "+end );
//                            printChunk(ls);
//                            System.out.println("---Text: "+curr_text);
                        }
                    }
                }
            } else { // use "," to separate two clauses
                Chunk com = comma.get(0);
                int com_pos = ls.indexOf(com);
                if (!hasNounChunk(com_pos, ls, end)) { // share subject
                    shared_sub = true; // set flag to use shared subject
                } else {
                    next_pos = com_pos + 1;
                    end = com_pos - 1;
                }
            }
        } else if (comma.isEmpty()) { // find conjunction: and/but/or
            if (conj.size() > 0) {
                Chunk and = conj.get(conj.size() - 1);
                int conj_pos = ls.indexOf(and);
                if (!hasNounChunk(conj_pos, ls, end)) { // share subject
                    shared_sub = true; // set flag to use shared subject
                } else {
                    next_pos = conj_pos + 1;
                }
                end = conj_pos - 1;
            } else { // no comma ; no conj ->> take all chunk until next verb chunk ;
                //shared_sub = true; // set flag to use shared subject
                next_pos=end+1 ;
            }
        } else { // more than one comma, find the last comma
            boolean set = false;
            if (conj.size() > 0) {
                // take last conj and comma
                Chunk and = conj.get(conj.size() - 1);
                Chunk com = comma.get(comma.size() - 1);
                if (and.begin > com.begin) { // , and 
                    int pos = ls.indexOf(and);
                    if (hasNounChunk(pos, ls, end)) { // assume start of next clause
                        if (checkCoord(start, ls, pos)) {
                            next_pos = pos + 1;
                            end = pos - 1;
                        } else { // conj is a part of co-ordination
                            // don't know to to split
                            // assume shared now,
                         next_pos = end+1;  // shared_sub = true;
                        }
                    } else { // assume shared subject
                        shared_sub = true; // set flag to use shared subject
                    }
                    set = true;
                }
            }
            if (!set) { // no conj or con_pos < com_pos
                Chunk com = comma.get(comma.size() - 1);
                int pos = ls.indexOf(com);
                if (hasNounChunk(pos, ls, end)) { // assume start of next clause
                    next_pos = pos + 1;
                    end = pos - 1;
                } else { // assume shared subject
                    shared_sub = true; // set flag to use shared subject
                }
            }
        }
        if (shared_sub) {
            next_pos = end + 2;// skip verb
        }
        has_breaker = false;
        for (int i = start; i < end; i++) {
            obj.addChunk(ls.get(i));
        }
        if (end >0 && end < ls.size()&& !ls.get(end).txt.equals(",")&&
             !(ls.get(end-1).txt.equals(",") && ccMap.contains(ls.get(end).txt)) &&
               !ccMap.contains(ls.get(end).txt) ) {
            obj.addChunk(ls.get(end));
        }
        return obj;
    }

    private List<Chunk> findBreaker(List<Chunk> ls) {
        List<Chunk> list = new ArrayList<Chunk>();
        for (Chunk c : ls) {
            if (breakMap.contains(c.txt)) {
                list.add(c);
            } else if (c.txt.startsWith(";") || c.type.equals("SBAR")&& !c.txt.equals("as")) {
                list.add(c);
            }
        }
        return list;

    }

    public boolean is_a_phrase(Chunk verb) {
        if (beMap.contains(stokens[verb.end])) {
            return true;
        }
        return false;
    }

    private boolean isShare(int start, int end, List<Chunk> ls){
        if(end - start>=1){
           Chunk c1 = ls.get(end);
           if(ccMap.contains(c1.txt)){
               return true ;
           }
        }
        return false;
    }
    
    public boolean is_passive(Chunk verb) {
        boolean has_be = false;
        int count = 0;
        for (int i = verb.begin; i <= verb.end; i++) {
            if (beMap.contains(stokens[i])) {
                has_be = true;
            }
            if (pos_tags[i].equals("VBN")) {
                count++;
            }
        }
        if (verb.trigs.size() == 1) {
            Word w = verb.trigs.get(0);
            if (w.pos_tag.equals("VBN") && has_be) {
                return true;
            }
        }
//        if (has_be && count >= 1) {
//            return true;
//        }
        return false;
    }

    /**
     * Find commas in a list of chunk
     * @param start
     * @param chunk
     * @return 
     */
    private List<Chunk> getCommaList(int start, List<Chunk> chunk, int end) {
        List<Chunk> comma = new ArrayList<Chunk>();
        Chunk c = null;
        for (int i = start; i <= end; i++) {
            c = chunk.get(i);
            if (c.txt.equals(",") || c.txt.equals(":")) {
                comma.add(c);
            }
        }
        return comma;
    }

    /**
     * Fixing false positive VP due to parser errors
     * @param ls: list of chunks 
     */
    private void groupVerbPhrase(List<Chunk> ls) {
        int i = 0;
        Chunk c, prev = null, next;
        List<Chunk> remove = new ArrayList<Chunk>();
        while (i < ls.size()) {
            c = ls.get(i);
            if (c.type.equals("VP") && c.begin == c.end) {
                if (!pos_tags[c.begin].startsWith("VB")) {
                    if (prev != null && prev.type.equals("NP")) {
                        prev.merge(c);
                        remove.add(c);
                        i++;
                        if (i < ls.size()) {
                            c = ls.get(i);
                            if (c.type.equals("NP")) {
                                prev.merge(c);
                                remove.add(c);
                            }
                            i++;
                            if (i < ls.size()) {
                                c = ls.get(i);
                            }
                        }
                    } else if (i + 1 < ls.size()) {
                        next = ls.get(i + 1);
                        if (next.type.equals("NP")) {
                            c.merge(next);
                            c.type = "NP";
                            remove.add(next);
                            i++;
                            if (i < ls.size()) {
                                c = ls.get(i + 1);
                            }
                        } else if (pos_tags[c.begin].startsWith("NN")) {
                            c.type = "NP";
                        }
                    } else {
                        System.out.println("PARSER: Fixing verb phrase: ----->Unknown case: " + c.getValues());
                    }
                }
            } else if (c.type.equals("NP") && c.begin == c.end) {
                if (pos_tags[c.begin].startsWith("VB")) {
                    c.type = pos_tags[c.begin];
                }
            }
            // now remove ADVP in front of VP
            if (c.type.equals("VP")) {
                if (prev != null && prev.type.equals("ADVP") && !breakMap.contains(prev.txt)) {
                    if (prev.pros.isEmpty() && prev.trigs.isEmpty()) {
                        remove.add(prev);
                    }
                }
                if (i < ls.size() - 1) { // behind
                    next = ls.get(i + 1);
                    if (next.type.equals("ADVP") && !breakMap.contains(next.txt)) {
                        if (next.pros.isEmpty() && next.trigs.isEmpty()) {
                            remove.add(next);
                            i++;
                            if (i < ls.size() - 1) {
                                next = ls.get(i + 1);
                            }
                        }
                    } else if (next.type.equals("ADJP")) {
                        c.merge(next);
                        remove.add(next);
                        i++;
                        if (i < ls.size() - 1) {
                            next = ls.get(i + 1);
                        }
                    } else if (next.type.equals("VP") && next.txt.startsWith("to ")) {
                        c.merge(next);
                        remove.add(next);
                        i++;
                    } else if (prepMap.contains(next.txt)) {
                        if (i + 3 < ls.size()) {
                            Chunk tmp1 = ls.get(i + 2);
                            Chunk tmp2 = ls.get(i + 3);
                            if (ccMap.contains(tmp1.txt) && tmp2.type.equals("VP")) {
                                c.merge(next);
                                c.merge(tmp1);
                                c.merge(tmp2);
                                remove.add(next);
                                remove.add(tmp1);
                                remove.add(tmp2);
                                i += 3;
                            }
                        }
                    }
                }
            }
            prev = c;
            i++;
        }
        for (Chunk ch : remove) {
            ls.remove(ch);
        }

    }

    private boolean checkCoord(int start, List<Chunk> ls, int end) {
        int count = 0;
        int conj=0;
        Chunk c;
        if (end + 1 < ls.size() && ls.get(end + 1).type.equals("NP")) {
            for (int i = end - 1; i >= start; i--) {
                c = ls.get(i);
                if (c.txt.equals(",")) {
                    continue;
                } else if (c.type.equals("NP")) {
                    count++;
                } else if(ccMap.contains(c.txt)) {
                    conj++;
                }else {
                    break;
                }
            }
            if (count >= 1 && conj>=1) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Parser parser = new Parser();
        String txt = "To investigate whether PRO4 can be expressed by any T cell subset or if expression is restricted to a distinct lineage, PRO5 mRNA expression was analyzed in freshly isolated T cells such as PRO6-depleted PRO7+ cells, PRO8+ naive or PRO9+ memory T cells (Figure 1A), as well as T cells driven in vitro toward Th1, Th2, or iTreg phenotypes (Figure 1B; phenotype on Figure S1).";
        String tokens[] = parser.splitWord(txt);
        String tags[] = parser.POSTag(tokens);
        List<Chunk> ls = parser.parse(tokens, tags);
        ChunkAnalyzer analyzer = new ChunkAnalyzer();
        analyzer.printChunk(ls);
        System.out.println("");
        analyzer.analyzeChunk(ls, tags, tokens);
        for (BasicChunk bs : analyzer.bsList) {
            bs.printChunk();
            System.out.println("");
        }
        BasicChunk bc = new BasicChunk();
        for (VerbChunk vbc : analyzer.verbList) {
            //bc.findCoordinator(vbc.subject.chunkList);
            vbc.print();
            System.out.println("");
        }
    }
    boolean debug = false;

    public Set<String> prepMap = new HashSet<String>(15);
    public Set<String> appoMap = new HashSet<String>(5);
    public Set<String> breakMap = new HashSet<String>(25);
    public Set<String> skipMap = new HashSet<String>(10);
    public Set<String> is_a_Map = new HashSet<String>(10);
    public Set<String> extraMap = new HashSet<String>(10);
    public Set<String> ccMap = new HashSet<String>(10);
    public Set<String> beMap = new HashSet<String>(10);
    public Set<String> allMap = new HashSet<String>(50);
    String prepList[] = {"to", "with", "from", "of", "on", "in", "upon", "by", "for", "after", "through", "between","via"};
    String skipList[] = {"not", "neither", "-", "\"", "'", "both", "also", "nor", "(", "]", "[", ")"};
    String breakList[] = {"while", "when", "whereas", "although", "if", "because", "even though", "whether", "since", "that",
        "whenever", "whatever", "before", "how", "which", ";", "."};// start a new clause
    String ccList[] = {"and", "or", "but", "as well as", "but not"};
    // Some breakers are not used at this moment: as , after
    String appoList[] = {"like", "such as", "including", "includes","containing"};
    String to_be[] = {"be", "is", "are", "was", "were", "been"};
    String is_a[] = {"had", "have", "has"}; //+ tobe
}
