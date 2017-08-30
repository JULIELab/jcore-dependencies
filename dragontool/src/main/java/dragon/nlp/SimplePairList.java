package dragon.nlp;

import dragon.nlp.compare.IndexComparator;
import dragon.util.*;
import java.io.*;
import java.util.*;

/**
 * <p>Simple pair list is the list for pair data elements </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimplePairList {
    private SortedArray keyList, indexList;
    private boolean writingMode;
    private String keylistFilename;

    public SimplePairList(){
        keyList=new SortedArray();
        keylistFilename=null;
        writingMode=true;
    }

    public SimplePairList(String filename, boolean writingMode) {
        this.writingMode =writingMode;
        this.keylistFilename=filename;
        this.keyList=loadRelationKeyList(filename);
        if(!writingMode)
            indexList=keyList.copy(new IndexComparator());
    }

    public int add(int firstElement, int secondElement){
        SimplePair pair;

        if(!writingMode)  return -1;
        pair=new SimplePair(keyList.size(),firstElement,secondElement);
        if(!keyList.add(pair))
            pair=(SimplePair)keyList.get(keyList.insertedPos());
        return pair.getIndex();
    }

    public int search(int firstElement, int secondElement){
        int pos;

        pos=keyList.binarySearch(new SimplePair(0,firstElement,secondElement));
        if(pos>=0)
            return ((SimplePair)keyList.get(pos)).getIndex();
        else
            return -1;
    }

    public SimplePair get(int index){
        if(indexList==null){
            //change to reading mode
            if(keylistFilename==null){
                writingMode = false;
                indexList = keyList.copy(new IndexComparator());
            }
            else
                return null;
        }
        return (SimplePair)indexList.get(index);
    }

    public int size(){
        return keyList.size();
    }

    public void close(){
        if(writingMode)
            saveRelationKeyList(keylistFilename,keyList);
        keyList.clear();
    }

    private SortedArray loadRelationKeyList(String filename){
        FastBinaryReader br;
        File file;
        SortedArray relationList;
        ArrayList list;
        SimplePair cur;
        int i, total;

        try{
            file=new File(filename);
            if(!file.exists())
                return new SortedArray();

            System.out.println(new java.util.Date() + " Loading Pair List...");
            br=new FastBinaryReader(filename);
            total=br.readInt();
            list=new ArrayList(total);
            for(i=0;i<total;i++){
                cur=new SimplePair(br.readInt(),br.readInt(),br.readInt());
                list.add(cur);
            }
            br.close();

            relationList=new SortedArray();
            relationList.addAll(list);
            return relationList;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private void saveRelationKeyList(String filename, ArrayList list){
        FastBinaryWriter fbw;
        SimplePair cur;
        int i;

        try {
            if(list==null) return;

            System.out.println(new java.util.Date() + " Saving Pair List...");
            fbw= new FastBinaryWriter(filename);
            fbw.writeInt(list.size());
            for (i = 0; i < list.size(); i++) {
                cur = (SimplePair) list.get(i);
                fbw.writeInt(cur.getIndex());
                fbw.writeInt(cur.getFirstElement());
                fbw.writeInt(cur.getSecondElement());
                if(i%100000==0)
                    fbw.flush();
            }
            fbw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}