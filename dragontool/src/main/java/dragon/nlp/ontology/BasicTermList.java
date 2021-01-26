package dragon.nlp.ontology;

import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>List of basic terms in ontology</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicTermList extends SortedArray {
	private static final long serialVersionUID = 1L;

	public BasicTermList(String cuiFile) {
        loadTermList(cuiFile);
    }

    public BasicTerm termAt(int index){
        return (BasicTerm)get(index);
    }

    public BasicTerm lookup(String term){
        int pos;

        pos=binarySearch(new BasicTerm(0,term,null));
        if(pos<0)
            return null;
        else
            return (BasicTerm)get(pos);
    }

    public BasicTerm lookup(BasicTerm term){
        int pos;

        pos=binarySearch(term);
        if(pos<0)
            return null;
        else
            return (BasicTerm)get(pos);
    }

    private boolean loadTermList(String filename){
        BufferedReader br;
        String line;
        String[] arrField;
        int i,total;
        ArrayList list;
        BasicTerm cur;

        try{
            if(filename==null)
                return false;
            System.out.println(new java.util.Date() + " Loading Term List...");
            br=FileUtil.getTextReader(filename);
            line=br.readLine();
            total=Integer.parseInt(line);
            list=new ArrayList(total);

            for(i=0;i<total;i++){
                line=br.readLine();
                arrField=line.split("\t");
                cur=new BasicTerm(i,arrField[1],arrField[2].split("_"));
                list.add(cur);
            }
            br.close();
            Collections.sort(list);
            this.addAll(list);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}