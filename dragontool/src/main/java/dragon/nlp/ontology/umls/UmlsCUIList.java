package dragon.nlp.ontology.umls;

import dragon.nlp.compare.IndexComparator;
import dragon.util.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>UMLS concept ID lists</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsCUIList extends SortedArray {
	private static final long serialVersionUID = 1L;

	public UmlsCUIList(){
    }

    public UmlsCUIList(String cuiFile) {
        loadCUIList(cuiFile,false);
    }

    public UmlsCUIList(String cuiFile, boolean loadConceptName) {
        loadCUIList(cuiFile,loadConceptName);
    }
    
    public UmlsCUIList(String cuiFile, boolean binary, boolean loadConceptName){
    	loadCUIList(cuiFile,binary,loadConceptName);
    }

    public UmlsCUI cuiAt(int index){
        return (UmlsCUI)get(index);
    }

    public ArrayList getListSortedByIndex(){
        ArrayList list;

        list=new ArrayList(size());
        list.addAll(this);
        Collections.sort(list, new IndexComparator());
        return list;
    }

    public boolean add(String cui, String[] stys){
        UmlsCUI cur;

        cur=new UmlsCUI(size(),cui,stys);
        return this.add(cur);
    }

    public UmlsCUI lookup(String cui){
        int pos;

        pos=binarySearch(new UmlsCUI(0,cui,null));
        if(pos<0)
            return null;
        else
            return (UmlsCUI)get(pos);
    }

    public UmlsCUI lookup(UmlsCUI cui){
        int pos;

        pos=binarySearch(cui);
        if(pos<0)
            return null;
        else
            return (UmlsCUI)get(pos);
    }

    public void saveTo(String filename) {
        PrintWriter bw;
        UmlsCUI cur;
        int i,j;

        try {
            System.out.println(new java.util.Date() + " Saving CUI List...");
            bw = FileUtil.getPrintWriter(filename);
            bw.write(size() + "\n");
            for (i = 0; i <size(); i++) {
                cur = (UmlsCUI) get(i);
                bw.write(cur.getIndex() + "\t" + cur.toString() + "\t" + cur.getSTY(0));
                for(j=1;j<cur.getSTYNum();j++)
                    bw.write("_"+cur.getSTY(j));
                if(cur.getName()!=null && cur.getName().length()>0)
                    bw.write("\t"+cur.getName());
                bw.write('\n');
                bw.flush();
            }
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void saveTo(String filename, boolean binary){
    	FastBinaryWriter fbw;
    	UmlsCUI cui;
    	int i,j;
    	
    	if(!binary){
    		saveTo(filename);
    		return;
    	}
    	
    	try{
	    	fbw=new FastBinaryWriter(filename);
	    	fbw.writeInt(size());
	    	for(i=0;i<size();i++){
	    		cui=cuiAt(i);
	    		fbw.writeInt(cui.getIndex());
	    		fbw.write(cui.getCUI().getBytes());
	    		fbw.writeShort(cui.getSTYNum());
	    		for(j=0;j<cui.getSTYNum();j++)
	    			fbw.writeShort(Integer.parseInt(cui.getSTY(j).substring(1)));
	    	}
	    	fbw.close();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }

    private boolean loadCUIList(String filename, boolean loadConceptName){
        BufferedReader br;
        String prevSTY, line;
        String[] arrField;
        String[] arrSTY, prevArrSTY;
        int i, total;
        ArrayList list;
        UmlsCUI cur;

        try{
            System.out.println(new java.util.Date() + " Loading CUI List...");
            br=FileUtil.getTextReader(filename);
            line=br.readLine();
            total=Integer.parseInt(line);
            list=new ArrayList(total);
            prevSTY="";
            prevArrSTY=null;

            for(i=0;i<total;i++){
                line=br.readLine();
                arrField=line.split("\t");
                if(prevSTY.equals(arrField[2]))
                    arrSTY=prevArrSTY;
                else
                {
                    arrSTY = arrField[2].split("_");
                    prevSTY = arrField[2];
                    prevArrSTY=arrSTY;
                }
                cur=new UmlsCUI(Integer.parseInt(arrField[0]),arrField[1],arrSTY);
                if(arrField.length>=4 && loadConceptName)
                    cur.setName(arrField[3]);
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
    
    private boolean loadCUIList(String filename, boolean binary, boolean loadConceptName){
        FastBinaryReader fbr;
        DecimalFormat df;
        String[] stys, arrSTY;
        byte[] buf;
        int index,total,i,j;
        ArrayList list;
        UmlsCUI cur;

        if(!binary)
        	return loadCUIList(filename,loadConceptName);
        
        try{
            System.out.println(new java.util.Date() + " Loading CUI List...");
            fbr=new FastBinaryReader(filename);
            total=fbr.readInt();
            
            list=new ArrayList(total);
            buf=new byte[8];
            stys=new String[512];
            df=FormatUtil.getNumericFormat(3, 0);
            for(i=0;i<stys.length;i++)
            	stys[i]="T"+df.format(i);

            for(i=0;i<total;i++){
                index=fbr.readInt();
                fbr.read(buf);
                arrSTY=new String[fbr.readShort()];
                for(j=0;j<arrSTY.length;j++)
                	arrSTY[j]=stys[fbr.readShort()];
                cur=new UmlsCUI(index,new String(buf),arrSTY);
                list.add(cur);
            }
            fbr.close();
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