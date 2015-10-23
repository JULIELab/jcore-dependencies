
package relations;

/**
 *
 * @author Chinh
 * @Date: Jun 18, 2011
 */
public class VerbChunk {
    BasicChunk subject=null ;
    BasicChunk object=null ;
    Chunk verb = null ;
    int verb_type =0; // to , passtive/ active / VBing / reduced clause
    int subject_type =0; // how to form subject: shared/normal/relative clause/ reduction clause
    public void addVerb(Chunk v){
        verb =v ;
    }
    public boolean isQualify(){
        int trgCount = subject.trgCount()+ object.trgCount()+verb.trigs.size();
        int prCount  = subject.proCount()+ object.proCount()+ verb.pros.size();
        if(trgCount>0 && prCount>0){
            return true ;
        }else {
            return false ;
        }
    }
    
    /**
     * Merge this verb chunk to create BasicChunk
     * @return : BasicChunk
     */
    public BasicChunk merge(){
        BasicChunk bsc = new BasicChunk();
        bsc.addChunk(subject);
        bsc.addChunk(verb);
        bsc.addChunk(object);
        bsc.is_merged = true ;
        return bsc ;
    }
    
    /**
     * For simple events/ or binding without theme2
     * @param trg
     * @param pro
     * @return true if trg and pro belong to this verb chunk
     */
    public boolean belongTO(Word trg, Word pro){
        if(verb.contains(trg)&& (subject.containsKey(pro)||object.containsKey(pro))){
            return true ; // verb is a trg
        }else if(subject.belongTO(trg, pro)|| object.belongTO(trg, pro)){
            return true ; // in basic chunk
        }else if((subject.containsKey(pro)&& object.containsKey(trg)) 
                || (subject.containsKey(trg)&& object.containsKey(pro))){
            return true ; // span over clause
        }
        return false ;
    }
    
    public boolean belongTO(Word trg, Word pro, Word pro2){
        if(subject.belongTO(trg, pro, pro2)){
            return true ;
        }else if(object.belongTO(trg, pro, pro2)){
            return true ;
        }else if(pro2==null){
            return belongTO(trg,pro);
        }else { // has pro2
            if(verb.contains(trg)){
                if((subject.containsKey(pro)&& object.containsKey(pro2))||
                    (subject.containsKey(pro2)&& object.containsKey(pro))){
                    return true ;
                }else if((subject.containsKey(pro) && subject.containsKey(pro2))
                        ||(object.containsKey(pro) && object.containsKey(pro2))){
                    return true ;
                }
            }else { // verb has no trig
                if((subject.belongTO(trg, pro)&& object.containsKey(pro2))||
                   (subject.belongTO(trg, pro2)&& object.containsKey(pro))||
                   (object.belongTO(trg, pro)&& subject.containsKey(pro2))|| 
                   (object.belongTO(trg, pro2)&& subject.containsKey(pro)) ){
                    return true ;
                }
            }
        }
        return false ;
    }
    
    
    public void print(){
        System.out.print("-> Sub: ");
        subject.printChunk();
        if(verb!=null){
            System.out.print(" -> Verb: "+verb.getText());
        }
        System.out.print("-> Object: ");
        object.printChunk();
    }
}
