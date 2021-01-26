package dragon.nlp;

import dragon.nlp.compare.IndexComparator;
import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>The list data structure for simple element data </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimpleElementList {
    private SortedArray keyList, indexList;
    private boolean writingMode;
    private boolean continuousIndex;
    private String keylistFilename;

    public SimpleElementList(){
        keylistFilename=null;
        keyList=new SortedArray();
        writingMode=true;
    }

    public SimpleElementList(String filename, boolean writingMode) {
        this.writingMode =writingMode;
        this.keylistFilename=filename;
        this.keyList=loadKeyList(filename);
        if(!writingMode){
            indexList = keyList.copy(new IndexComparator());
            if(indexList.size()==0 || indexList.size()==((SimpleElement)indexList.get(indexList.size()-1)).getIndex()+1)
                continuousIndex=true;
            else
                continuousIndex=false;
        }
    }

    public boolean isWritingMode(){
        return writingMode;
    }

    public int add(String key){
        SimpleElement term;

        if(!writingMode)
            return -1;
        term=new SimpleElement(new String(key), keyList.size());
        if(!keyList.add(term))
            term=(SimpleElement)keyList.get(keyList.insertedPos());
        return term.getIndex();
    }

    public boolean add(SimpleElement element){
        if(!writingMode)
            return false;
        return keyList.add(element);
    }

    public int search(String key){
        int pos;

        pos=keyList.binarySearch(new SimpleElement(key,-1));
        if(pos>=0)
            return ((SimpleElement)keyList.get(pos)).getIndex();
        else
            return -1;
    }

    public String search(int index){
        if(indexList==null){
            //change to reading mode
            if(keylistFilename==null){
                writingMode = false;
                indexList = keyList.copy(new IndexComparator());
                if(indexList.size()==((SimpleElement)indexList.get(indexList.size()-1)).getIndex()+1)
                    continuousIndex = true;
                else
                    continuousIndex = false;
            }
            else
                return null;
        }

        if(continuousIndex)
            return ( (SimpleElement) indexList.get(index)).getKey();
        else{
            int pos=indexList.binarySearch(new SimpleElement(null,index));
            if(pos>=0)
                return ( (SimpleElement) indexList.get(pos)).getKey();
            else
                return null;
        }
    }

    public boolean contains(String key){
        return keyList.contains(new SimpleElement(key,-1));
    }

    public int size(){
        return keyList.size();
    }

    public void close(){
        if(writingMode && keylistFilename!=null)
            saveKeyList(keylistFilename,keyList);
        if(keyList!=null) keyList.clear();
        if(indexList!=null) indexList.clear();
    }

    private void saveKeyList(String filename, ArrayList list){
        BufferedWriter bw;
        SimpleElement cur;
        int i;

        try {
            if(list==null) return;

            System.out.println(new java.util.Date() + " Saving Element List...");
          	bw = FileUtil.getTextWriter(filename);
            bw.write(list.size() + "");
            bw.write("\n");
            for (i = 0; i < list.size(); i++) {
                cur = (SimpleElement) list.get(i);
                bw.write(cur.getKey() + "\t" + cur.getIndex()+"\n");
                bw.flush();
            }
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private SortedArray loadKeyList(String filename){
        BufferedReader br;
        File file;
        SortedArray termList;
        ArrayList list;
        String line;
        String[] arrField;
        int i,total;

        try{
            file=new File(filename);
            if(!file.exists())
                return new SortedArray();

            System.out.println(new java.util.Date() + " Loading Element List...");
            br=FileUtil.getTextReader(filename);
            line=br.readLine();
            arrField=line.split("\t");
            total=Integer.parseInt(arrField[0]);
            list=new ArrayList(total);

            for(i=0;i<total;i++){
                line=br.readLine();
                arrField=line.split("\t");
                list.add(new SimpleElement(arrField[0], Integer.parseInt(arrField[arrField.length-1])));
            }
            br.close();
            Collections.sort(list);
            termList=new SortedArray();
            termList.addAll(list);
            return termList;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}