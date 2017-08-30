package dragon.ir.index;

import dragon.matrix.*;
import dragon.util.*;
import java.io.*;

/**
 * <p>The class is used to transpose a given IR indexing matrix such as termdoc to docterm </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TransposeIRMatrix {
    public TransposeIRMatrix() {
    }

    public static void main(String[] args) {
        TransposeIRMatrix transpose;
        StringBuffer directory;
        int i;

        transpose= new TransposeIRMatrix();
        directory=new StringBuffer();
        for(i=0;i<args.length;i++){
            directory.append(args[i]);
            directory.append(' ');
        }
        transpose.genTermDocMatrix(directory.toString().trim());
        //transpose.genRelationDocMatrix(directory.toString().trim());
    }

    public void genTermDocMatrix(String directory){
        FileIndex indexer;
        File oldMatrix, oldIndex, newMatrix, newIndex;
        RandomAccessFile raf;
        IntGiantSparseMatrix doctermMatrix;
        long[] arrOffset;
        int oldDocNum;

        try{
            indexer = new FileIndex(directory, false);

            oldMatrix=new File(indexer.getTermDocFilename()+".tmp");
            newMatrix=new File(indexer.getTermDocFilename());
            if(newMatrix.exists()){
                if(!newMatrix.renameTo(oldMatrix))
                    return;
            }
            oldIndex=new File(indexer.getTermDocIndexFilename()+".tmp");
            newIndex=new File(indexer.getTermDocIndexFilename());
            if(newIndex.exists()){
                if(!newIndex.renameTo(oldIndex))
                    return;
            }


            oldDocNum=0;
            arrOffset = initTermDocMatrix(indexer);
            if(oldMatrix.exists())
                oldDocNum=mergeOldMatrix(arrOffset,newMatrix,oldMatrix);
            raf = new RandomAccessFile(indexer.getTermDocFilename(), "rw");
            doctermMatrix=new IntGiantSparseMatrix(indexer.getDocTermIndexFilename(),indexer.getDocTermFilename());
            genTransposeMatrix(raf,oldDocNum,arrOffset,doctermMatrix);

            if(oldMatrix.exists())
                oldMatrix.delete();
            if(oldIndex.exists())
                oldIndex.delete();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void genRelationDocMatrix(String directory){
        FileIndex indexer;
        File oldMatrix, oldIndex, newMatrix, newIndex;
        RandomAccessFile raf;
        IntGiantSparseMatrix docrelationMatrix;
        long[] arrOffset;
        int oldDocNum;

        try{
            indexer = new FileIndex(directory, false);

            oldMatrix=new File(indexer.getRelationDocFilename()+".tmp");
            newMatrix=new File(indexer.getRelationDocFilename());
            if(newMatrix.exists()){
                if(!newMatrix.renameTo(oldMatrix))
                    return;
            }
            oldIndex=new File(indexer.getRelationDocIndexFilename()+".tmp");
            newIndex=new File(indexer.getRelationDocIndexFilename());
            if(newIndex.exists()){
                if(!newIndex.renameTo(oldIndex))
                    return;
            }

            oldDocNum=0;
            arrOffset = initRelationDocMatrix(indexer);
            if(oldMatrix.exists())
                oldDocNum=mergeOldMatrix(arrOffset,newMatrix,oldMatrix);

            raf = new RandomAccessFile(indexer.getRelationDocFilename(), "rw");
            docrelationMatrix=new IntGiantSparseMatrix(indexer.getDocRelationIndexFilename(),indexer.getDocRelationFilename());
            genTransposeMatrix(raf,oldDocNum,arrOffset,docrelationMatrix);

            if(oldMatrix.exists())
                oldMatrix.delete();
            if(oldIndex.exists())
                oldIndex.delete();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private long[] initTermDocMatrix(FileIndex indexer){
        IRCollection collection;
        IRTermIndexList indexList;
        FastBinaryWriter fbwMatrix, fbwIndex;
        long[] arrOffset;
        byte[] buf;
        int i,j,totalCell,len;

        try{
            buf=new byte[8];
            i=0;
            ByteArrayConvert.toByte(i,buf,0);
            ByteArrayConvert.toByte(i,buf,4);

            collection = new IRCollection();
            indexList = new BasicIRTermIndexList(indexer.getTermIndexListFilename(), false);
            collection.load(indexer.getCollectionFilename());
            arrOffset = new long[collection.getTermNum()];

            System.out.println( (new java.util.Date()).toString() + " initializing term doc matrix...");
            totalCell=readCellNum(indexer.getDocTermFilename());
            fbwMatrix=new FastBinaryWriter(indexer.getTermDocFilename());
            fbwIndex=new FastBinaryWriter(indexer.getTermDocIndexFilename());
            fbwMatrix.writeInt(collection.getTermNum());
            fbwMatrix.writeInt(collection.getDocNum());
            fbwMatrix.writeInt(totalCell);
            fbwIndex.writeInt(collection.getTermNum());
            fbwIndex.writeInt(collection.getDocNum());
            fbwIndex.writeInt(totalCell);
            for(i=0;i<indexList.size();i++){
                len=indexList.get(i).getDocFrequency();
                fbwIndex.writeInt(i);
                fbwIndex.writeLong(fbwMatrix.getFilePointer());
                fbwIndex.writeInt(len);
                fbwIndex.flush();
                fbwMatrix.writeInt(i);
                fbwMatrix.writeInt(len);
                arrOffset[i]=fbwMatrix.getFilePointer();
                for(j=0;j<len;j++)
                    fbwMatrix.write(buf);
            }
            fbwIndex.close();
            fbwMatrix.close();

            return arrOffset;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private long[] initRelationDocMatrix(FileIndex indexer){
        IRCollection collection;
        IRRelationIndexList indexList;
        FastBinaryWriter fbwMatrix, fbwIndex;

        long[] arrOffset;
        byte[] buf;
        int i,j,totalCell,len;

        try{
            buf=new byte[8];
            i=0;
            ByteArrayConvert.toByte(i,buf,0);
            ByteArrayConvert.toByte(i,buf,4);

            collection = new IRCollection();
            indexList = new BasicIRRelationIndexList(indexer.getRelationIndexListFilename(), false);
            collection.load(indexer.getCollectionFilename());
            arrOffset = new long[collection.getRelationNum()];

            System.out.println( (new java.util.Date()).toString() + " initializing relation doc matrix...");
            totalCell=readCellNum(indexer.getDocRelationFilename());
            fbwMatrix=new FastBinaryWriter(indexer.getRelationDocFilename());
            fbwIndex=new FastBinaryWriter(indexer.getRelationDocIndexFilename());
            fbwMatrix.writeInt(collection.getRelationNum());
            fbwMatrix.writeInt(collection.getDocNum());
            fbwMatrix.writeInt(totalCell);
            fbwIndex.writeInt(collection.getRelationNum());
            fbwIndex.writeInt(collection.getDocNum());
            fbwIndex.writeInt(totalCell);
            for(i=0;i<indexList.size();i++){
                len=indexList.get(i).getDocFrequency();
                fbwIndex.writeInt(i);
                fbwIndex.writeLong(fbwMatrix.getFilePointer());
                fbwIndex.writeInt(len);
                fbwIndex.flush();
                fbwMatrix.writeInt(i);
                fbwMatrix.writeInt(len);
                arrOffset[i]=fbwMatrix.getFilePointer();
                for(j=0;j<len;j++)
                    fbwMatrix.write(buf);
            }
            fbwIndex.close();
            fbwMatrix.close();

            return arrOffset;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void genTransposeMatrix(RandomAccessFile newMatrix, int startingRow, long[] arrOffset, IntSparseMatrix oldMatrix){
        IntFlatSparseMatrix cacheMatrix;
        int[] arrColumn, arrFreq;
        int doc_cache, i,j, count;

        try{
            doc_cache=40000;
            count=0;
            cacheMatrix=new IntFlatSparseMatrix(false,true);

            for (i = startingRow; i < oldMatrix.rows(); i++) {
                count++;
                arrColumn = oldMatrix.getNonZeroColumnsInRow(i);
                arrFreq = oldMatrix.getNonZeroIntScoresInRow(i);
                for (j = 0; j < arrColumn.length; j++) {
                    cacheMatrix.add(arrColumn[j],i,arrFreq[j]);
                }
                if(count>=doc_cache){
                    System.out.println( (new java.util.Date()).toString() + " processing row #" + i);
                    count=0;
                    cacheMatrix.finalizeData();
                    add(newMatrix,arrOffset,cacheMatrix);
                    cacheMatrix.close();
                }
            }
            if(count>0){
                System.out.println( (new java.util.Date()).toString() + " processing row #" + i);
                count=0;
                cacheMatrix.finalizeData();
                add(newMatrix,arrOffset,cacheMatrix);
                cacheMatrix.close();
            }

            newMatrix.close();
            oldMatrix.close();
            System.out.println("Done!");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void add(RandomAccessFile newMatrix, long[] arrOffset, IntFlatSparseMatrix cacheMatrix){
        int i,j, start, end, len;
        ByteArrayWriter baw;

        try{
            System.out.println( (new java.util.Date()).toString() + " dumping to disk");
            baw=new ByteArrayWriter();
            start=cacheMatrix.getBaseRow();
            end=cacheMatrix.rows();
            for(i=start;i<end;i++){
                len=cacheMatrix.getNonZeroNumInRow(i);
                if(len==0) continue;
                newMatrix.seek(arrOffset[i]);
                for(j=0;j<len;j++){
                    baw.writeInt(cacheMatrix.getNonZeroColumnInRow(i,j));
                    baw.writeInt(cacheMatrix.getNonZeroIntScoreInRow(i,j));
                }
                newMatrix.write(baw.toByteArray());
                baw.reset();
                arrOffset[i]+=8*len;
            }
            baw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private int readCellNum(String filename) {
        FastBinaryReader reader;
        int cellNum;

        try {
            reader = new FastBinaryReader(filename);
            reader.skip(8);
            cellNum=reader.readInt();
            reader.close();
            return cellNum;
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private int mergeOldMatrix(long[] arrOffset, File newMatrix, File oldMatrix){
        FastBinaryReader fbr;
        RandomAccessFile raf;
        int i, len, rows, cols;

        try{
            System.out.println((new java.util.Date()).toString() + " merging old term doc matrix...");
            fbr=new FastBinaryReader(oldMatrix);
            raf=new RandomAccessFile(newMatrix,"rw");
            rows=fbr.readInt();
            cols=fbr.readInt();
            fbr.skip(4);
            for(i=0;i<rows;i++){
                fbr.skip(4); //skip the row number
                len=fbr.readInt();
                if(len>0){
                    raf.seek(arrOffset[i]);
                    move(fbr,raf,len*8);
                    arrOffset[i]+=len*8;
                }
            }
            fbr.close();
            raf.close();
            return cols;
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private void move(FastBinaryReader src, RandomAccessFile dest, long length) {
        int count;
        byte[] buf;

        try {
            buf=new byte[(int)(10240<length?10240:length)];
            while (length > 0) {
                count = (int) (length > buf.length ? buf.length : length);
                count = src.read(buf, 0, count);
                if (count > 0) {
                    dest.write(buf, 0, count);
                    length = length - count;
                }
                else {
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}