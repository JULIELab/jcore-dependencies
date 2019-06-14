package dragon.ir.index.sequence;

import dragon.util.FastBinaryReader;

import java.io.RandomAccessFile;

/**
 * <p>The file reader for reading sequencial data from file</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SequenceFileReader implements SequenceReader {
    private RandomAccessFile rafMatrix;
    private long[] arrRowStart;
    private int rowNum;

    public SequenceFileReader(String seqIndexFile,String seqMatrixFile) {
        try{
            loadIndexFile(seqIndexFile);
            rafMatrix = new RandomAccessFile(seqMatrixFile, "r");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void initialize(){

    }

    public int getSequenceLength(int seqIndex){
        if(seqIndex<0 || seqIndex>=rowNum)
            return 0;
        else
            return (int)((arrRowStart[seqIndex+1]-arrRowStart[seqIndex]-8)/4);
    }

    public int[] getSequence(int seqIndex){
        FastBinaryReader fbr;
        int i, len, arrSeq[];

        try{
            len = getSequenceLength(seqIndex);
            if (len <= 0)
                return null;
            rafMatrix.seek(arrRowStart[seqIndex] + 8);
            fbr = new FastBinaryReader(rafMatrix, len * 4);
            arrSeq=new int[len];
            for(i=0;i<len;i++)
                arrSeq[i]=fbr.readInt();
            return arrSeq;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void close(){
        try{
            if (rafMatrix != null)
                rafMatrix.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadIndexFile(String indexFile){
        FastBinaryReader fbr;
        int i, len;

        try{
            len=0;
            fbr = new FastBinaryReader(indexFile);
            rowNum = fbr.readInt();
            fbr.readInt();
            fbr.readInt();
            arrRowStart=new long[rowNum+1];
            for(i=0;i<rowNum;i++){
                fbr.readInt(); //read the row number;
                arrRowStart[i]=fbr.readLong();
                len=fbr.readInt(); //read the length of the sequency
            }
            arrRowStart[rowNum]=arrRowStart[rowNum-1]+(len+2)*4;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}