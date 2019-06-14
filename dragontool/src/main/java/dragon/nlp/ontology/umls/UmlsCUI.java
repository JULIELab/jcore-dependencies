package dragon.nlp.ontology.umls;

import dragon.nlp.compare.IndexSortable;
/**
 * <p>Data structure for UMLS concept ID(CUI) </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsCUI implements IndexSortable, Comparable{
    private String[] arrSTY;
    private int index;
    private String cui;
    private String name;

    public UmlsCUI(int index, String cui, String[] stys) {
        this.cui=cui;
        this.index=index;
        this.arrSTY=stys;
        this.name=null;
    }

    public UmlsCUI(int index, String cui, String[] stys, String name) {
        this.cui=cui;
        this.index=index;
        this.arrSTY=stys;
        this.name =name;
    }

    public int compareTo(Object obj) {
        String objValue;

        objValue = ( (UmlsCUI) obj).toString();
        return toString().compareTo(objValue);
    }

    public String getCUI(){
        return cui;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index=index;
    }

    public int getSTYNum(){
        if(arrSTY==null)
            return 0;
        else
            return arrSTY.length;
    }

    public String getSTY(int index){
        return arrSTY[index];
    }

    public String toString(){
        return cui;
    }

    public String[] getAllSTY(){
        return arrSTY;
    }

    public boolean addSTY(String sty){
        if(arrSTY==null)
        {
            arrSTY=new String[1];
            arrSTY[0]=sty;
            return true;
        }
        else if(sty.compareTo(arrSTY[arrSTY.length-1])>0){
            String[] arrTemp=new String[arrSTY.length+1];
            System.arraycopy(arrSTY,0,arrTemp,0,arrSTY.length);
            arrTemp[arrSTY.length]=sty;
            arrSTY=arrTemp;
            return true;
        }
        else
            return false;
    }
}