package dragon.matrix;

import dragon.util.FastBinaryReader;
import dragon.util.FastBinaryWriter;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * <p>Sparse matrix factory is for storing, reading and expanding matrix operations </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SparseMatrixFactory {
    private String matrixFilename;
    private int rows, columns, cells;
    private int cellDataLength;

    public SparseMatrixFactory(String matrixFilename, int cellDataLength) {
        this.matrixFilename = matrixFilename;
        this.cellDataLength=cellDataLength;
        readStatInfo(matrixFilename);
    }

    public String getMatrixFilename(){
        return matrixFilename;
    }

    public int rows(){
        return rows;
    }

    public int columns(){
        return columns;
    }

    public int getCellDataLength(){
        return cellDataLength;
    }

    public int getNonZeroNum(){
        return cells;
    }

    public boolean add(SparseMatrix newMatrix) {
        RandomAccessFile rafMatrixOld;
        FastBinaryReader fbMatrixOld;
        FastBinaryWriter fbMatrixNew;
        File matrixf, matrixTemp;
        ArrayList mergedCellList;
        Cell oldCell, newCell;
        String tempMatrixFilename;
        int i, j, num, oldPos, newPos, oldNum, newNum;
        int minRows, mergedCellNum, column, lastColumnInRow;
        long priorBaseRowData;
        boolean rowAppendMode;
        byte[] cellData;

        try {
            if(newMatrix==null || newMatrix.getNonZeroNum()==0)
                return false;

            matrixf = new File(matrixFilename);
            if (!matrixf.exists()) {
                return saveSparseMatrix(newMatrix);
            }

            if(newMatrix.getBaseRow()>=rows){
                return append(newMatrix);
            }

            System.out.println(new java.util.Date() +  " Adding to old matrix...");
            mergedCellList=new ArrayList();
            cellData=new byte[cellDataLength];
            mergedCellNum=0;
            tempMatrixFilename=matrixFilename+".tmp";
            fbMatrixOld = new FastBinaryReader(matrixf);
            fbMatrixNew = new FastBinaryWriter(tempMatrixFilename);

            //write the head into new matrix file
            fbMatrixOld.skip(12); //skip the head (rows, columns, cells) of the old matrix file
            fbMatrixNew.writeInt(rows>newMatrix.rows()?rows:newMatrix.rows());
            fbMatrixNew.writeInt(columns>newMatrix.columns()?columns:newMatrix.columns());
            fbMatrixNew.writeInt(cells + newMatrix.getNonZeroNum());

            //copy the old data before the base row in the old matrix to the new matrix
            rafMatrixOld=new RandomAccessFile(matrixf,"r");
            priorBaseRowData=getSuperMatrixRowStart(rafMatrixOld,newMatrix.getBaseRow())-12;
            if(priorBaseRowData>0)
                fbMatrixNew.write(fbMatrixOld,priorBaseRowData);
            rafMatrixOld.seek(priorBaseRowData+12+8); //point to the firt cell of the base row.

            //merge the new data with old data
            minRows=newMatrix.rows()<rows?newMatrix.rows():rows;
            for (i = newMatrix.getBaseRow(); i < minRows; i++) {
                fbMatrixOld.readInt();
                oldNum=fbMatrixOld.readInt();
                newNum=newMatrix.getNonZeroNumInRow(i);

                //determine the writing mode: appending mode or merging mode
                if(newNum==0){
                    rowAppendMode=true;
                    rafMatrixOld.skipBytes(oldNum*(cellDataLength+4)+8);
                }
                else if(oldNum==0){
                    rowAppendMode=true;
                    rafMatrixOld.skipBytes(8);
                }
                else{
                    rafMatrixOld.skipBytes((oldNum-1)*(cellDataLength+4));
                    lastColumnInRow=rafMatrixOld.readInt();
                    rafMatrixOld.skipBytes(cellDataLength+8); //point to the first cell of next row;
                    if(lastColumnInRow<newMatrix.getNonZeroColumnInRow(i,0))
                        rowAppendMode=true;
                    else
                        rowAppendMode=false;
                }

                if(rowAppendMode){
                    //writing mode: appending mode
                    fbMatrixNew.writeInt(i);
                    fbMatrixNew.writeInt(newNum + oldNum);
                    fbMatrixNew.write(fbMatrixOld, oldNum * (newMatrix.getCellDataLength() + 4));

                    //append new data to the end
                    for (j = 0; j < newNum; j++) {
                        fbMatrixNew.writeInt(newMatrix.getNonZeroColumnInRow(i, j));
                        fbMatrixNew.write(newMatrix.getNonZeroCellInRow(i, j).toByteArray());
                    }
                    fbMatrixNew.flush();
                }
                else{
                    //writing mode: merging mode
                    mergedCellList.clear();
                    oldPos=0;
                    newPos=0;
                    oldCell=null;
                    newCell=null;
                    while(oldPos<oldNum && newPos<newNum){
                        if(oldCell==null){
                            column=fbMatrixOld.readInt();
                            fbMatrixOld.read(cellData);
                            oldCell=newMatrix.createCell(i, column, cellData);
                        }
                        if(newCell==null)
                            newCell=newMatrix.getNonZeroCellInRow(i,newPos);

                        if(oldCell.getColumn()<newCell.getColumn()){
                            mergedCellList.add(oldCell);
                            oldPos++;
                            oldCell=null;
                        }
                        else if(oldCell.getColumn()>newCell.getColumn()){
                            mergedCellList.add(newCell);
                            newPos++;
                            newCell=null;
                        }
                        else{
                            oldCell.merge(newCell);
                            mergedCellList.add(oldCell);
                            oldPos++;
                            newPos++;
                            oldCell=null;
                            newCell=null;
                            mergedCellNum++;
                        }
                    }

                    if(oldCell!=null)
                    {
                        mergedCellList.add(oldCell);
                        oldPos++;
                    }

                    while(oldPos<oldNum){
                        column = fbMatrixOld.readInt();
                        fbMatrixOld.read(cellData);
                        oldCell = newMatrix.createCell(i, column, cellData);
                        mergedCellList.add(oldCell);
                        oldPos++;
                    }

                    while(newPos<newNum){
                        mergedCellList.add(newMatrix.getNonZeroCellInRow(i,newPos));
                        newPos++;
                    }

                    //write into new matrix file
                    num=mergedCellList.size();
                    fbMatrixNew.writeInt(i);
                    fbMatrixNew.writeInt(num);

                    for (j = 0; j < num; j++) {
                        newCell=(Cell)mergedCellList.get(j);
                        fbMatrixNew.writeInt(newCell.getColumn());
                        fbMatrixNew.write(newCell.toByteArray());
                    }
                    fbMatrixNew.flush();
                }
            }
            mergedCellList.clear();
            rafMatrixOld.close();
            if(minRows<rows)
                fbMatrixNew.write(fbMatrixOld,fbMatrixOld.remaining());
            fbMatrixOld.close();

            //add new rows to the new matrix file
            for(i=minRows;i<newMatrix.rows();i++){
                num = newMatrix.getNonZeroNumInRow(i);
                fbMatrixNew.writeInt(i);
                fbMatrixNew.writeInt(num);
                for (j = 0; j < num; j++) {
                    fbMatrixNew.writeInt(newMatrix.getNonZeroColumnInRow(i, j));
                    fbMatrixNew.write(newMatrix.getNonZeroCellInRow(i,j).toByteArray() );
                }
                fbMatrixNew.flush();
            }
            fbMatrixNew.close();

            rows=rows>newMatrix.rows()?rows:newMatrix.rows();
            columns=columns>newMatrix.columns()?columns:newMatrix.columns();
            cells=cells+ newMatrix.getNonZeroNum()-mergedCellNum;

            if(!matrixf.delete()){
                matrixFilename=tempMatrixFilename;
            }
            else{
                matrixTemp = new File(tempMatrixFilename);
                matrixTemp.renameTo(matrixf);
            }

            //modify the head of the new matrix file
            if(mergedCellNum>0){
                rafMatrixOld=new RandomAccessFile(matrixFilename,"rw");
                rafMatrixOld.skipBytes(8);
                rafMatrixOld.writeInt(cells);
                rafMatrixOld.close();
            }

            System.out.println(new java.util.Date() + " Finish adding");
            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean append(SparseMatrix newMatrix) {
        RandomAccessFile rafMatrix;
        FastBinaryWriter fbMatrix;
        File matrixf;
        int i, j, num;
        try {
            if(newMatrix==null || newMatrix.getNonZeroNum()==0) return false;

            matrixf = new File(matrixFilename);
            if (!matrixf.exists()) {
                return saveSparseMatrix(newMatrix);
            }
            System.out.println(new java.util.Date() +  " Appending to old matrix...");

            rafMatrix=new RandomAccessFile(matrixf,"rw");
            rafMatrix.writeInt(newMatrix.rows());
            rafMatrix.writeInt(newMatrix.columns());
            rafMatrix.writeInt(cells + newMatrix.getNonZeroNum());
            rafMatrix.close();

            fbMatrix= new FastBinaryWriter(matrixFilename,true);
            //add new rows to the new matrix file
            for(i=rows;i<newMatrix.rows();i++){
                num = newMatrix.getNonZeroNumInRow(i);
                fbMatrix.writeInt(i);
                fbMatrix.writeInt(num);
                for (j = 0; j < num; j++) {
                    fbMatrix.writeInt(newMatrix.getNonZeroColumnInRow(i, j));
                    fbMatrix.write(newMatrix.getNonZeroCellInRow(i,j).toByteArray() );
                }
                fbMatrix.flush();
            }

            fbMatrix.close();
            rows=newMatrix.rows();
            columns=newMatrix.columns();
            cells=cells+ newMatrix.getNonZeroNum();

            System.out.println(new java.util.Date() + " Finish appending");
            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void genIndexFile(String indexFilename){
        FastBinaryWriter fwIndex;
        RandomAccessFile rafMatrix;
        File file;
        int i, rowLen, row;
        long rowOffset;

        try{
            file=new File(matrixFilename);
            if(!file.exists())
                return;
            rafMatrix=new RandomAccessFile(file,"r");
            (new File(indexFilename)).delete();
            fwIndex = new FastBinaryWriter(indexFilename);

            rafMatrix.skipBytes(12);  //skip the head (rows, columns, cells)
            fwIndex.writeInt(rows);
            fwIndex.writeInt(columns);
            fwIndex.writeInt(cells);
            for (i = 0; i <rows; i++) {
                rowOffset=rafMatrix.getFilePointer();
                row=rafMatrix.readInt();
                if(row!=i){
                    System.out.println("error");
                }
                rowLen=rafMatrix.readInt(); //the number of elements in the row
                fwIndex.writeInt(row); //row index
                fwIndex.writeLong(rowOffset);
                fwIndex.writeInt(rowLen);
                rafMatrix.skipBytes(rowLen*(cellDataLength+4));
                fwIndex.flush();
            }
            rafMatrix.close();
            fwIndex.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean saveSparseMatrix(SparseMatrix matrix) {
        FastBinaryWriter rafMatrix;
        int num, i, j;

        System.out.println(new java.util.Date() + " Saving Matrix...");
        try {
            rafMatrix = new FastBinaryWriter(matrixFilename);
            rows=matrix.rows();
            columns=matrix.columns();
            cells=matrix.getNonZeroNum();
            rafMatrix.writeInt(rows);
            rafMatrix.writeInt(columns);
            rafMatrix.writeInt(cells);

            for (i = 0; i <rows; i++) {
                num = matrix.getNonZeroNumInRow(i);
                rafMatrix.writeInt(i);
                rafMatrix.writeInt(num);
                for (j = 0; j < num; j++) {
                    rafMatrix.writeInt(matrix.getNonZeroColumnInRow(i, j));
                    rafMatrix.write(matrix.getNonZeroCellInRow(i,j).toByteArray());
                }
                rafMatrix.flush();
            }
            rafMatrix.close();
            System.out.println(new java.util.Date() + " Finish saving");
            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void readStatInfo(String matrixFile){
        File matrixf;
        FastBinaryReader rafMatrix;

        matrixf = new File(matrixFilename);
        if (!matrixf.exists())
        {
            rows=0;
            columns=0;
            cells=0;
            return;
        }

        try{
            rafMatrix = new FastBinaryReader(matrixFile);
            rows=rafMatrix.readInt();
            columns=rafMatrix.readInt();
            cells=rafMatrix.readInt();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private long getSuperMatrixRowStart(RandomAccessFile raf, int row){
        int len;

        try{
            raf.seek(12);
            while(raf.readInt()<row){
                len = raf.readInt();
                raf.skipBytes(len * (cellDataLength + 4));
            }
            return raf.getFilePointer()-4;
        }
        catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }
}