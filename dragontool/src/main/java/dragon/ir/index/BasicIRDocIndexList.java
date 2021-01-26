package dragon.ir.index;

import dragon.util.FastBinaryReader;
import dragon.util.FastBinaryWriter;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * <p>The class is used to write or load the indexing information for a given document or document set </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicIRDocIndexList implements IRDocIndexList {
    private static int doc_cache_size=5000;
    private FastBinaryWriter fbw;
    private ArrayList indexList;
    private boolean writingMode;
    private String indexlistFilename;
    private int docNum;
    private int lastDocIndex;
    private int doc_in_cache;

    public BasicIRDocIndexList(String filename, boolean writingMode) {
        try{
            this.writingMode = writingMode;
            this.indexlistFilename =filename;
            if(writingMode){
                doc_in_cache=0;
                fbw=new FastBinaryWriter(filename,true);
                if(fbw.getFilePointer()<4)
                    fbw.writeInt(0);
                lastDocIndex=((int)fbw.getFilePointer()-4)/20-1;
            }
            else{
                indexList=loadDocIndexList(filename);
                docNum=indexList.size();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public IRDoc get(int index){
        return index<docNum?(IRDoc)indexList.get(index):null;
    }

    public boolean add(IRDoc curDoc){
        int i;

        try{
            if(!writingMode || curDoc.getIndex()<=lastDocIndex)
                return false;
            for(i=lastDocIndex+1;i<curDoc.getIndex();i++){
                doc_in_cache++;
                fbw.writeInt(i);
                fbw.writeInt(0);
                fbw.writeInt(0);
                fbw.writeInt(0);
                fbw.writeInt(0);
            }

            fbw.writeInt(curDoc.getIndex());
            fbw.writeInt(curDoc.getTermNum());
            fbw.writeInt(curDoc.getTermCount());
            fbw.writeInt(curDoc.getRelationNum());
            fbw.writeInt(curDoc.getRelationCount());
            doc_in_cache++;
            if(doc_in_cache>=doc_cache_size)
                flush();
            lastDocIndex=curDoc.getIndex();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void close(){
        RandomAccessFile raf;

        try{
            if (writingMode){
                fbw.close();
                raf=new RandomAccessFile(indexlistFilename,"rw");
                raf.writeInt(lastDocIndex+1);
                raf.close();
            }
            else{
                indexList.clear();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public int size(){
        if(writingMode)
            return lastDocIndex+1;
        else
            return docNum;
    }

    private void flush(){
        if (writingMode && fbw != null) {
            fbw.flush();
            doc_in_cache = 0;
        }
    }

    private ArrayList loadDocIndexList(String filename){
        FastBinaryReader br;
        File file;
        ArrayList list;
        IRDoc cur;
        int i,total;

        try{
            file=new File(filename);
            if(!file.exists())
                return new ArrayList();

            System.out.println(new java.util.Date() + " Loading Document List...");
            br=new FastBinaryReader(filename);
            total=br.readInt();
            list=new ArrayList(total);

            for(i=0;i<total;i++){
                cur=new IRDoc(br.readInt());
                cur.setTermNum(br.readInt());
                cur.setTermCount(br.readInt());
                cur.setRelationNum(br.readInt());
                cur.setRelationCount(br.readInt());
                list.add(cur);
            }
            br.close();
            br.close();
            return list;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}