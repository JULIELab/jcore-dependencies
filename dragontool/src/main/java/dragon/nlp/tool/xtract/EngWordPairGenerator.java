package dragon.nlp.tool.xtract;

import dragon.nlp.*;
import dragon.nlp.tool.*;
import dragon.util.SortedArray;

/**
 * <p>Generating word pairs (for noun phrases) from a given sentence according to max span range</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class EngWordPairGenerator implements WordPairGenerator{
    protected int maxSpan;
    protected SortedArray list;

    public EngWordPairGenerator(int maxSpan) {
        this.maxSpan =maxSpan;
        list=new SortedArray();
    }

    public void setMaxSpan(int maxSpan){
        this.maxSpan =maxSpan;
    }

    public int generate(Sentence sent){
        Word start, end;
        WordPairStat curPair;
        int span, pos;

        list.clear();
        start=sent.getFirstWord();
        while(start!=null){
            if(start.getPOSIndex()!=Tagger.POS_NOUN && start.getPOSIndex()!=Tagger.POS_ADJECTIVE){
                start=start.next;
                continue;
            }

            end=start.next;
            span=1;
            while(end!=null && span<=maxSpan){
                pos=end.getPOSIndex();
                if(pos==Tagger.POS_NOUN){
                    if(start.getIndex()<=end.getIndex()){
                        curPair = new WordPairStat(start.getIndex(), end.getIndex(),maxSpan);
                        curPair.addFrequency(span, 1);
                        if (!list.add(curPair)) {
                            curPair = (WordPairStat) list.get(list.insertedPos());
                            curPair.addFrequency(span, 1);
                        }
                    }
                    else{
                        curPair = new WordPairStat(end.getIndex(), start.getIndex(), maxSpan);
                        curPair.addFrequency(0-span, 1);
                        if (!list.add(curPair)) {
                            curPair = (WordPairStat) list.get(list.insertedPos());
                            curPair.addFrequency(0-span, 1);
                        }
                    }
                }
                else{
                    if(pos!=Tagger.POS_ADJECTIVE) break;
                }

                span++;
                end=end.next;
            }
            start=start.next;
        }
        return list.size();
    }

    public WordPairStat getWordPairs(int index){
        return (WordPairStat)list.get(index);
    }
}