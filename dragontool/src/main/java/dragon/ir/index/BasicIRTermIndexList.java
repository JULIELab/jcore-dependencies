package dragon.ir.index;

import dragon.util.ByteArrayConvert;
import dragon.util.FastBinaryReader;
import dragon.util.FastBinaryWriter;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
/**
 * <p>The class is used to write or load the relation indexing information for a given IR term or term set  </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicIRTermIndexList implements IRTermIndexList, IRSignatureIndexList{
    private RandomAccessFile raf;
    private ArrayList indexList;
    private int elementLength;
    private boolean writingMode;
    private String indexlistFilename;
    private int termNum;
    private byte[] buf;

    public BasicIRTermIndexList(String filename, boolean writingMode) {
        try{
            this.elementLength = 12;
            this.writingMode = writingMode;
            this.indexlistFilename = filename;
            if (writingMode) {
                raf = null;
                indexList = loadTermIndexList(filename);
            }
            else {
                raf = new RandomAccessFile(filename, "r");
                buf=new byte[elementLength];
                if(raf.length()>0)
                    termNum=raf.readInt();
                else
                    termNum=0;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public IRSignature getIRSignature(int index){
        return get(index);
    }

    public IRTerm get(int index){
        try{
            if(writingMode ||index>=termNum) return null;

            raf.seek(index*elementLength+4);
            raf.read(buf);
            return getIRTermFromByteArray(buf);
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean add(IRTerm curTerm){
        IRTerm oldTerm;

        if(!writingMode) return false;

        if(curTerm.getIndex()<indexList.size()){
            oldTerm=(IRTerm)indexList.get(curTerm.getIndex());
            oldTerm.addFrequency(curTerm.getFrequency());
            oldTerm.setDocFrequency(oldTerm.getDocFrequency()+curTerm.getDocFrequency());
        }
        else
        {
            for(int i=indexList.size();i<curTerm.getIndex();i++){
                indexList.add(new IRTerm(i,0,0));
            }
            curTerm=curTerm.copy();
            curTerm.setKey(null);
            indexList.add(curTerm);
        }
        return true;
    }

    public void close(){
        try{
            if (writingMode){
                saveTermIndexList(indexlistFilename, indexList);
                indexList.clear();
            }
            else{
                if(raf!=null)
                    raf.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public int size(){
        if(writingMode)
            return indexList.size();
        else
            return termNum;
    }

    private ArrayList loadTermIndexList(String filename){
        FastBinaryReader fbr;
        File file;
        ArrayList list;
        IRTerm cur;
        int i,total;

        try{
            file=new File(filename);
            if(!file.exists())
                return new ArrayList();

            System.out.println(new java.util.Date() + " Loading Term List...");
            fbr=new FastBinaryReader(filename);
            total=fbr.readInt();
            list=new ArrayList(total);

            for(i=0;i<total;i++){
                cur=new IRTerm(fbr.readInt(),fbr.readInt(),fbr.readInt());
                list.add(cur);
            }
            fbr.close();
            return list;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private void saveTermIndexList(String filename, ArrayList list){
        FastBinaryWriter fbw;
        IRTerm cur;
        int i;

        try {
            if(list==null) return;
            System.out.println(new java.util.Date() + " Saving Term Index List...");
            fbw= new FastBinaryWriter(filename);
            fbw.writeInt(list.size());
            for (i = 0; i < list.size(); i++) {
                cur = (IRTerm) list.get(i);
                fbw.writeInt(cur.getIndex());
                fbw.writeInt(cur.getFrequency());
                fbw.writeInt(cur.getDocFrequency());
                if(i%100000==0)
                    fbw.flush();
            }
            fbw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private IRTerm getIRTermFromByteArray(byte[] array){
        return new IRTerm(ByteArrayConvert.toInt(array,0), ByteArrayConvert.toInt(array,4), ByteArrayConvert.toInt(array,8));
    }
}