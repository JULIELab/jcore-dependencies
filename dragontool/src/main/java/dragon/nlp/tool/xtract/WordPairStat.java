package dragon.nlp.tool.xtract;

import dragon.nlp.compare.IndexSortable;

/**
 * <p>Couting word pair statistics </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class WordPairStat implements IndexSortable, Comparable{
    private int maxSpan, index;
    private int first, second;
    private int[] arrCount;
    private int totalCount;

    public WordPairStat(int first, int second, int maxSpan) {
        this.index =-1;
        this.maxSpan=maxSpan;
        this.first=first;
        this.second=second;
        arrCount=new int[2*maxSpan];
        for(int i=0;i<2*maxSpan;i++) arrCount[i]=0;
        totalCount=0;
    }

    public WordPairStat(int index, int first, int second, int maxSpan) {
        this.index =index;
        this.first=first;
        this.second=second;
        this.maxSpan =maxSpan;
        arrCount=new int[2*maxSpan];
        for(int i=0;i<2*maxSpan;i++) arrCount[i]=0;
        totalCount=0;
    }

    public WordPairStat copy(){
        WordPairStat stat;
        int i;

        stat=new WordPairStat(index,first, second, maxSpan);
        for(i=0;i<maxSpan;i++) stat.addFrequency(i-maxSpan,arrCount[i]);
        for(i=1;i<=maxSpan;i++) stat.addFrequency(i,arrCount[i+maxSpan-1]);
        return stat;
    }

    public void addFrequency(int span, int inc){
        if(span<0)
            arrCount[span+maxSpan]+=inc;
        else
            arrCount[span+maxSpan-1]+=inc;
        totalCount+=inc;
    }

    public int getFrequency(int span){
        if(span>0)
            return arrCount[span+maxSpan-1];
        else
            return arrCount[span+maxSpan];
    }

    public int getTotalFrequency(){
        return totalCount;
    }

    public void setIndex(int index){
        this.index=index;
    }

    public int getIndex(){
        return index;
    }

    public int getFirstWord(){
        return first;
    }

    public void setFirstWord(int word){
        this.first =word;
    }

    public int getSecondWord(){
        return second;
    }

    public void setSecondWord(int word){
        this.second=word;
    }

    public int compareTo(Object obj){
        int indexObj;

        indexObj=((WordPairStat)obj).getFirstWord();
        if(first==indexObj){
            indexObj=((WordPairStat)obj).getSecondWord();
            if(second==indexObj)
                return 0;
            else if(second>indexObj)
                return 1;
            else
                return -1;
        }
        else if(first>indexObj)
            return 1;
        else
            return -1;
    }
}