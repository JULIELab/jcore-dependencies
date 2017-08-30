package dragon.matrix;

import dragon.util.*;
import java.io.*;
import java.util.*;

/**
 * <p>Abstract flat sparse matrix handles sparse matrix smaller than super sparse matrix while adding options for read and store
 * matrix data to disk either in text or binary format</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractFlatSparseMatrix extends AbstractSparseMatrix implements Serializable{
    protected ArrayList list;
    private int[] arrRowStartPos; //used for non-mini mode
    private int lastAccessedRow, lastRowStart, lastRowEnd; //used for mini mode
    private CoordinateComparator coordinateComparator;

    public AbstractFlatSparseMatrix(boolean mergeMode, boolean miniMode,int cellDataLength){
        super(mergeMode,miniMode,cellDataLength);
        coordinateComparator=new CoordinateComparator();
        list = new ArrayList();
    }

    protected int getRowStart(int row){
        if(row<rowBase) return 0;

        if(!miniMode){
            return arrRowStartPos[row - rowBase];
        }
        else{
            if(row!=lastAccessedRow)
                setRowRange(row);
            return lastRowStart;
        }
    }

    protected int getRowEnd(int row){
        if(row<rowBase) return 0;

        if(!miniMode){
            return arrRowStartPos[row+1- rowBase];
        }
        else{
            if(row!=lastAccessedRow)
                setRowRange(row);
            return lastRowEnd;
        }
    }

    public void close(){
        isFinalized=false;
        if(list!=null)
            list.clear();
        transposeMatrix=null;
        arrRowStartPos=null;
        rows=0;
        rowBase=0;
        columns=0;
    }

    public int getNonZeroNum(){
        return list.size();
    }

    public Cell getNonZeroCell(int index){
        return (Cell)list.get(index);
    }

    public Cell getCell(int row,int column){
        int start, end, pos;
        Cell curCell;

        if(row<rowBase || row>=rows) return null;

        start=getRowStart(row);
        end=getRowEnd(row)-1;
        curCell=new IntCell(row,column,0);
        pos=SortedArray.binarySearch(list,curCell,start,end,coordinateComparator);
        if(pos<0)
            return null;
        else
            return (Cell)list.get(pos);
    }

    public Cell getNonZeroCellInRow(int row, int index){
        return (Cell)list.get(getRowStart(row)+index);
    }

    public int getNonZeroNumInRow(int row){
        if(row>=rows) return 0;
        return (getRowEnd(row)-getRowStart(row));
    }

    public boolean add(Cell cell){
        if(isFinalized) return false;

        if(mergeMode){
            int pos=SortedArray.binarySearch(list,cell,0,list.size()-1,coordinateComparator);
            if(pos>=0)
                ((Cell)list.get(pos)).merge(cell);
            else
                list.add((-1)*pos-1,cell);
        }
        else{
            list.add(cell);
        }
        if(cell.getColumn()>=columns)
            columns=cell.getColumn()+1;
        return true;
    }

    public int getNonZeroColumnInRow(int row, int index){
        return getNonZeroCellInRow(row,index).getColumn();
    }

    public int[] getNonZeroColumnsInRow(int row){
        int num, count;
        int[] arrColumn;

        num=getNonZeroNumInRow(row);
        arrColumn=new int[num];

        for(count=0;count<num;count++)
        {
            arrColumn[count]=getNonZeroCellInRow(row,count).getColumn();
        }
        return arrColumn;
    }

    public void setNonZeroDoubleScoreInRow(int row, int index, double score){
        getNonZeroCellInRow(row,index).setDoubleScore(score);
    }

    public void setNonZeroIntScoreInRow(int row, int index, int score){
        getNonZeroCellInRow(row,index).setIntScore(score);
    }

    public void setDouble(int row, int column, double score){
        Cell curCell;

        curCell=getCell(row,column);
        if(curCell!=null)
            curCell.setDoubleScore(score);
    }

    public void setInt(int row, int column, int score){
        Cell curCell;

        curCell=getCell(row,column);
        if(curCell!=null)
            curCell.setDoubleScore(score);
    }

    public void flush(){
    }

    public boolean finalizeData(boolean sorting){
        if(isFinalized)
            return false;

        if(mergeMode)
            sorting=false;
        if(list.size()>0){
            if (sorting) Collections.sort(list, new CoordinateComparator());
            this.rowBase = ( (Cell) list.get(0)).getRow();
            this.rows = ( (Cell) list.get(list.size() - 1)).getRow() + 1;
        }
        else{
            this.rowBase =0;
            this.rows =0;
        }
        isFinalized=true;
        if(!miniMode)
            setRowStart();
        else
            setRowRange(0);
        return true;
    }

    private void setRowStart(){
        int high, i, total, pos;
        Cell curCell;

        total=rows-rowBase;
        arrRowStartPos = new int[total+1];
        high=list.size()-1;
        arrRowStartPos[0]=0;
        arrRowStartPos[total]=list.size();

        for(i=1;i<total;i++)
        {
            curCell=new IntCell(i+rowBase,0);
            pos=SortedArray.binarySearch(list,curCell,arrRowStartPos[i-1],high);
            if(pos<0) pos=(-1)*pos-1;
            arrRowStartPos[i]=pos;
        }
    }

    public void saveTo(String filename, boolean binary){
        SparseMatrixFactory factory;

        if(!binary)
            saveTo(filename);
        else
        {
            factory=new SparseMatrixFactory(filename,getCellDataLength());
            factory.add(this);
        }
    }

    public void saveTo(String filename)
    {
        print(FileUtil.getPrintWriter(filename));
    }

    public void print(PrintWriter out){
        int i,j, num;
        Cell cur;

        try{
            //output rows, columns and edges.
            out.write(String.valueOf(rows)+","+String.valueOf(columns)+","+String.valueOf(list.size())+"\n");
            //output all rows
            for(i=0;i<rows;i++)
            {
                num=getNonZeroNumInRow(i);
                for(j=0;j<num;j++)
                {
                    cur=getNonZeroCellInRow(i,j);
                    out.write("("+i+","+cur.getColumn()+","+cur.toString()+")\t");
                }
                out.write("\n");
            }
            out.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    protected boolean readBinaryMatrixFile(String filename){
        FastBinaryReader rafMatrix;
        int rows, cells;
        int i,j,num, row, column;
        byte[] data;

        isFinalized = false;
        data=new byte[getCellDataLength()];

        //read number of dimensions
        try{
            rafMatrix=new FastBinaryReader(filename);
            rows=rafMatrix.readInt();
            columns=rafMatrix.readInt();
            cells=rafMatrix.readInt();
            list = new ArrayList(cells);

            //read matrix
            for(i=0;i<rows;i++)
            {
                row=i;
                rafMatrix.readInt();
                num=rafMatrix.readInt();
                for(j=0;j<num;j++)
                {
                    column=rafMatrix.readInt();
                    rafMatrix.read(data);
                    add(createCell(row,column,data));
                }
            }
            rafMatrix.close();
            finalizeData(false);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    protected boolean readTextMatrixFile(String filename){
        BufferedReader br;
        String line,fragment;
        int i,j,lineNo;
        int cells;
        isFinalized = false;

        //read number of dimensions
        try{
            br=FileUtil.getTextReader(filename);
            fragment=br.readLine();
            i = 0;
            j = fragment.indexOf(',', i);
            rows = Integer.parseInt(fragment.substring(i, j));
            i = j + 1;
            j = fragment.indexOf(',', i);
            columns= Integer.parseInt(fragment.substring(i, j));
            cells = Integer.parseInt(fragment.substring(j + 1));
            list = new ArrayList(cells);

            //read feature occurrence information
            lineNo = -1;
            line=br.readLine();
            while (line!=null) {
                lineNo++;
                processLine(lineNo, line);
                line=br.readLine();
            }
            br.close();
            finalizeData(false);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void processLine(int lineNo, String line){
        Cell cur;
        int i,j,startPos, endPos,  row, col;
        String fragment;

        startPos=0;
        endPos =line.indexOf('\t', startPos);
        while (endPos > 0) {
            fragment = line.substring(startPos, endPos);
            startPos = endPos + 1;
            endPos = line.indexOf('\t', startPos);

            i = fragment.indexOf('(') + 1;
            j = fragment.indexOf(',', i);
            row = Integer.parseInt(fragment.substring(i, j));
            i = j + 1;
            j = fragment.indexOf(',', i);
            col = Integer.parseInt(fragment.substring(i, j));
            i = j + 1;
            j = fragment.indexOf(')', i);
            cur=createCell(row,col,fragment.substring(i, j));
            add(cur);
        }
    }

    private void setRowRange(int row){
        lastAccessedRow=row;
        lastRowStart=SortedArray.binarySearch(list,new IntCell(row,0),0,list.size()-1,coordinateComparator);
        if(lastRowStart<0) lastRowStart = lastRowStart * -1 - 1;
        lastRowEnd = SortedArray.binarySearch(list,new IntCell(row+1,0),lastRowStart, list.size()-1, coordinateComparator);
        if (lastRowEnd < 0) lastRowEnd = lastRowEnd * -1 - 1;
    }
}
