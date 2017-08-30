package dragon.ir.index;

import dragon.util.*;
import java.io.*;

/**
 * <p>IRCollection is the data structure for a document collection</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IRCollection {
    private int docNum;
    private int termNum, relationNum;
    private long termCount,relationCount;

    public IRCollection() {
        docNum=0;
        termNum=0;
        relationNum=0;
        termCount=0;
        relationCount=0;
    }

    public int getDocNum(){
        return docNum;
    }

    public void setDocNum(int docNum){
        this.docNum=docNum;
    }

    public void addDocNum(int inc){
        docNum+=inc;
    }

    public int getTermNum(){
        return termNum;
    }

    public void setTermNum(int termNum){
        this.termNum=termNum;
    }

    public long getTermCount(){
        return termCount;
    }

    public void setTermCount(long termCount){
        this.termCount=termCount;
    }

    public void addTermCount(int inc){
        termCount+=inc;
    }

    public int getRelationNum(){
        return relationNum;
    }

    public void setRelationNum(int relationNum){
        this.relationNum =relationNum;
    }

    public long getRelationCount(){
        return relationCount;
    }

    public void setRelationCount(long relationCount){
        this.relationCount =relationCount;
    }

    public void addRelationCount(int inc){
        relationCount+=inc;
    }

    public void load(String filename){
        File file;
        FastBinaryReader fbr;

        try {
            file = new File(filename);
            if (file.exists()) {
                fbr = new FastBinaryReader(file);
                setDocNum(fbr.readInt());
                setTermNum(fbr.readInt());
                setTermCount(fbr.readLong());
                setRelationNum(fbr.readInt());
                setRelationCount(fbr.readLong());
                fbr.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(String filename) {
        FastBinaryWriter fbs;

        try {
            fbs = new FastBinaryWriter(filename);
            fbs.writeInt(getDocNum());
            fbs.writeInt(getTermNum());
            fbs.writeLong(getTermCount());
            fbs.writeInt(getRelationNum());
            fbs.writeLong(getRelationCount());
            fbs.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}