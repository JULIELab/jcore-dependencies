package dragon.ir.index.sequence;

import dragon.util.*;
import java.io.*;

/**
 * <p>The file writer for writing sequencial data to file </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SequenceFileWriter implements SequenceWriter{
    private static final int BUF_THRESHOLD=512000;
    private String seqIndexFile, seqMatrixFile;
    private FastBinaryWriter matrixFile, indexFile;
    private int maxRowIndex, maxTermIndex, termCount;

    public SequenceFileWriter(String seqIndexFile, String seqMatrixFile) {
        this.seqIndexFile =seqIndexFile;
        this.seqMatrixFile =seqMatrixFile;
        init();
    }

    public void initialize(){
    }

    public boolean addSequence(int rowIndex, int[] seq){
        int i;

        try{
            if (rowIndex <= maxRowIndex)
                return false;

            for (i = maxRowIndex + 1; i < rowIndex; i++) {
                indexFile.writeInt(i);
                indexFile.writeLong(matrixFile.getFilePointer());
                indexFile.writeInt(0);
                matrixFile.writeInt(i);
                matrixFile.writeInt(0);
            }

            indexFile.writeInt(rowIndex);
            indexFile.writeLong(matrixFile.getFilePointer());
            indexFile.writeInt(seq.length);
            matrixFile.writeInt(rowIndex);
            matrixFile.writeInt(seq.length);
            for(i=0;i<seq.length;i++){
                if(seq[i]>maxTermIndex)
                    maxTermIndex=seq[i];
                matrixFile.writeInt(seq[i]);
            }
            termCount+=seq.length;
            if(indexFile.bytesInBuffer()>BUF_THRESHOLD)
                indexFile.flush();
            if(matrixFile.bytesInBuffer()>BUF_THRESHOLD)
                matrixFile.flush();
            maxRowIndex=rowIndex;
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void close(){
        if(matrixFile!=null){
            matrixFile.close();
            editHeader(seqMatrixFile);
        }
        if(indexFile!=null){
            indexFile.close();
            editHeader(seqIndexFile);
        }
    }

    private void init(){
        File file;
        FastBinaryReader fbr;

        try{
            file=new File(seqMatrixFile);
            if(!file.exists() || file.length()==0){
                maxRowIndex=-1;
                maxTermIndex=-1;
                termCount=0;
            }
            else{
                fbr=new FastBinaryReader(file);
                maxRowIndex=fbr.readInt()-1;
                maxTermIndex=fbr.readInt()-1;
                termCount=fbr.readInt();
                fbr.close();
            }

            matrixFile=new FastBinaryWriter(seqMatrixFile,true);
            if(matrixFile.getFilePointer()==0){
                matrixFile.writeInt(0);
                matrixFile.writeInt(0);
                matrixFile.writeInt(0);
            }

            indexFile=new FastBinaryWriter(seqIndexFile,true);
            if(indexFile.getFilePointer()==0){
                indexFile.writeInt(0);
                indexFile.writeInt(0);
                indexFile.writeInt(0);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void editHeader(String file){
        RandomAccessFile raf;

        try{
            raf=new RandomAccessFile(file, "rw");
            raf.writeInt(maxRowIndex+1);
            raf.writeInt(maxTermIndex+1);
            raf.writeInt(termCount);
            raf.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}