package dragon.ir.summarize;

import dragon.nlp.compare.*;
/**
 * <p>Data structure for text unit</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TextUnit implements WeightSortable, IndexSortable, Comparable{
    public static final int UNIT_TERM=1;
    public static final int UNIT_RELATION=2;
    public static final int UNIT_SENTENCE=3;
    public static final int UNIT_PARAGRAPH=4;
    public static final int UNIT_ARTICLE=5;

    private String text;
    private double weight;
    private int index;

    public TextUnit(String text){
        this(text,-1,0);
    }

    public TextUnit(String text, double weight){
        this(text,-1,weight);
    }

    public TextUnit(String text, int index, double weight) {
        this.text =text;
        this.index =index;
        this.weight =weight;
    }

    public double getWeight(){
        return weight;
    }

    public void setWeight(double weight){
        this.weight =weight;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index =index;
    }

    public String getText(){
        return text;
    }

    public void setText(String text){
        this.text =text;
    }

    public int compareTo(Object obj){
        return text.compareToIgnoreCase(((TextUnit)obj).getText());
    }
}