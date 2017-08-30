package dragon.ir.summarize;

import dragon.nlp.compare.*;
import dragon.util.SortedArray;

/**
 * <p>Data structure for topic summary</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TopicSummary {
    private SortedArray textList;
    private int textGranularity;

    public TopicSummary(int textGranularity) {
        textList=new SortedArray();
        this.textGranularity =textGranularity;
    }

    public int getTextGranularity(){
        return textGranularity;
    }

    public boolean addText(TextUnit text){
        return textList.add(text);
    }

    public boolean contains(TextUnit text){
        return textList.contains(text);
    }

    public int size(){
        return textList.size();
    }

    public TextUnit getTextUnit(int index){
        return (TextUnit)textList.get(index);
    }

    public void sortByWegiht(){
        textList.setComparator(new WeightComparator(true));
    }

    public void sortByIndex(){
        textList.setComparator(new IndexComparator());
    }

    public void sortByText(){
        textList.setComparator(null);
    }
}