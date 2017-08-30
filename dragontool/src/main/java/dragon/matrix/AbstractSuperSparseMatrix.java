package dragon.matrix;

import dragon.util.*;
import java.io.*;
import java.util.*;

/**
 * <p>Abstract super sparse matrix is designed for large sparse matrix which first caches data and then processes data
 * and write data to disk when it's over flush interval </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSuperSparseMatrix extends AbstractSparseMatrix {
    private static int DEFAULT_CACHESIZE=10000;
    private static int DEFAULT_FLUSHINTERVAL=1000000;
    protected String matrixFilename, indexFilename;

    //for reading mode only
    protected int totalCell;
    protected RandomAccessFile matrix;
    protected long[] arrRowPosInFile; //record the starting point of each row in the raw matrix file
    protected int cacheSize;
    protected Row[] arrCachedRow;
    protected float[] arrRowLoadFactor;
    protected int[] arrRowPosInCache;
    protected int[] arrRowStart;
    protected int firstEmpty;

    //for writing mode only
    protected AbstractFlatSparseMatrix cacheMatrix;
    protected int flushInterval;
    protected SparseMatrixFactory matrixFactory;

    abstract protected Row createRow(int row, int columns, byte[] data);
    abstract protected AbstractFlatSparseMatrix createFlatSparseMatrix(boolean mergeMode, boolean miniMode);

    //the constructor below is for writing mode only
    public AbstractSuperSparseMatrix(String indexFilename, String matrixFilename, int cellDataLength, boolean mergeMode, boolean miniMode ){
        super(mergeMode,miniMode, cellDataLength);
        this.indexFilename =indexFilename;
        this.matrixFilename=matrixFilename;
        matrixFactory=new SparseMatrixFactory(matrixFilename,cellDataLength);
        flushInterval=DEFAULT_FLUSHINTERVAL;
        isFinalized=false;
        cacheMatrix=createFlatSparseMatrix(mergeMode, miniMode);

        matrix=null;
        arrRowPosInFile=null;
        arrCachedRow=null;
        arrRowLoadFactor=null;
        arrRowPosInCache=null;
        arrRowStart=null;
        firstEmpty=0;
        cacheSize=0;
    }

    //the constructor below is for reading mode only
    public AbstractSuperSparseMatrix(String indexFilename, String matrixFilename, int cellDataLength){
        super(false,false,cellDataLength);
        this.indexFilename =indexFilename;
        this.matrixFilename=matrixFilename;
        matrixFactory=null;
        flushInterval=0;
        isFinalized=true;
        cacheMatrix=null;

        matrix=null;
        arrRowPosInFile=null;
        arrCachedRow=null;
        arrRowLoadFactor=null;
        arrRowPosInCache=null;
        arrRowStart=null;
        firstEmpty=0;
        cacheSize=0;

        initData(DEFAULT_CACHESIZE);
    }

    private void initData(int cacheSize){
        if(indexFilename!=null && FileUtil.exist(indexFilename))
            readIndexFile(indexFilename);
        else if(FileUtil.exist(matrixFilename))
            readIndexFromMatrix(matrixFilename);
        try{
            if(FileUtil.exist(matrixFilename)){
                matrix = new RandomAccessFile(matrixFilename, "r");
                setCache(cacheSize);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setCache(int cacheSize){
        if(cacheSize<=0)
            cacheSize=DEFAULT_CACHESIZE;
        if(arrCachedRow==null){
            this.cacheSize = cacheSize;
            arrCachedRow = new Row[cacheSize];
            firstEmpty = 0;
        }
    }

    public void setFlushInterval(int interval){
        this.flushInterval =interval;
    }

    public void close(){
        try{
            totalCell = 0;
            if (matrix != null)
                matrix.close();
            arrRowPosInFile = null;
            arrRowPosInFile = null;
            cacheSize = 0;
            arrCachedRow = null;
            arrRowLoadFactor = null;
            arrRowStart = null;
            firstEmpty = 0;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean add(Cell cell){
        if(isFinalized)
            return false;
        else{
            cacheMatrix.add(cell);
            if(cacheMatrix.getNonZeroNum()>=flushInterval){
                flush();
            }
            return true;
        }
    }

    public void flush(){
        cacheMatrix.finalizeData(true);
        matrixFactory.add(cacheMatrix);
        cacheMatrix.close();
    }

    public boolean finalizeData(boolean sorting){
        File file;

        if(isFinalized) return false;
        flush();
        this.columns =matrixFactory.columns();
        this.rows=matrixFactory.rows();
        totalCell=matrixFactory.getNonZeroNum();
        isFinalized=true;
        if(indexFilename!=null) matrixFactory.genIndexFile(indexFilename);
        if(!matrixFactory.getMatrixFilename().equalsIgnoreCase(matrixFilename)){
            file=new File(matrixFilename);
            file.delete();
            (new File(matrixFactory.getMatrixFilename())).renameTo(file);
        }
        return true;
    }

    public String getMatrixFilename(){
        return matrixFilename;
    }

    public String getIndexFilename(){
        return indexFilename;
    }

    public int getNonZeroNum() {
        return totalCell;
    }

    public int getNonZeroNumInRow(int row) {
        if(row>=rows) return 0;

		if(arrRowStart==null){
            initData(cacheSize);
        }
        return arrRowStart[row + 1] - arrRowStart[row];
    }

    public int getNonZeroColumnInRow(int row, int index) {
        return getRow(row).getNonZeroColumn(index);
    }

    public int[] getNonZeroColumnsInRow(int row) {
        int[] oldArray, newArray;

        if(row>=rows) return null;

        oldArray = getRow(row).getNonZeroColumns();
        newArray = new int[oldArray.length];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }

    public Cell getCell(int row, int col) {
        if(row>=rows) return null;

        return getRow(row).getCell(col);
    }

    public Cell getNonZeroCellInRow(int row, int index) {
        return getRow(row).getNonZeroCell(index);
    }

     /*If the row wanted is cached, simply get it from the cache. Otherwise, load the row from the file. If the cache is
     full, we spare a position for the wanted row first. The filtering algorithm is based on the load factor. */
    protected Row getRow(int index) {
        if(arrRowStart==null){
            initData(cacheSize);
        }

        if (arrRowPosInCache[index] >= 0) {
            return arrCachedRow[arrRowPosInCache[index]];
        }
        else {
            return loadRow(index, getRoomInCache());
        }
    }

    /* load a row from the file and put it in the specified position in the cache)*/
    private Row loadRow(int index, int posInCache) {
        Row curRow;
        int len;
        byte[] data;

        try {
            arrRowLoadFactor[index] += 0.1 * Math.log(arrRowStart[index + 1] - arrRowStart[index]);
            curRow = arrCachedRow[posInCache];
            if (curRow != null) {
                arrRowPosInCache[curRow.getRowIndex()] = -1;
            }

            matrix.seek(arrRowPosInFile[index]);
            matrix.readInt(); //read row #
            len = matrix.readInt();
            data = new byte[len * (getCellDataLength() + 4)];
            matrix.read(data);

            if (curRow != null) {
                curRow.load(index, len, data);
                curRow.setLoadFactor(arrRowLoadFactor[index]);
            }
            else {
                curRow = createRow(index, len, data);
                curRow.setLoadFactor(arrRowLoadFactor[index]);
                arrCachedRow[posInCache] = curRow;
            }
            arrRowPosInCache[index] = posInCache;
            return curRow;
        }
        catch (Exception e) {
            return null;
        }
    }

    private int getRoomInCache() {
        int i, pos, breakpoint;
        ArrayList list;
        Row row;

        if (firstEmpty >= 0) {
            pos = firstEmpty;
            firstEmpty++;
            if (firstEmpty >= cacheSize) {
                firstEmpty = -1;
            }
            return pos;
        }
        else {
            list = new ArrayList(cacheSize);
            for (i = 0; i < cacheSize; i++) {
                list.add(arrCachedRow[i]);
            }
            Collections.sort(list); //sorting in reversed load factor order and remove 10% rows in the bottom
            breakpoint = (int) (cacheSize * 0.9);
            for (i = 0; i < breakpoint; i++) {
                row = (Row) list.get(i);
                arrRowPosInCache[row.getRowIndex()] = i;
                arrCachedRow[i] = row;
            }
            for (i = breakpoint; i < cacheSize; i++) {
                row = (Row) list.get(i);
                arrRowPosInCache[row.getRowIndex()] = -1;
                arrCachedRow[i] = null;
            }
            pos = breakpoint;
            firstEmpty = pos + 1;
            return pos;
        }
    }

    /*read indexing information include the offset of reach row in the matrix file from the index file.*/
    private void readIndexFile(String filename) {
        FastBinaryReader reader;
        int i, freq, row;
        long offset;
        try {
            reader = new FastBinaryReader(filename);
            rows = reader.readInt();
            columns = reader.readInt();
            totalCell = reader.readInt();

            arrRowLoadFactor = new float[rows];
            arrRowPosInCache = new int[rows];
            arrRowPosInFile = new long[rows + 1];
            arrRowStart = new int[rows + 1];
            arrRowStart[0] = 0;
            for (i = 0; i < rows; i++) {
                //row must be equal to i
                row = reader.readInt();
                offset = reader.readLong();
                freq = reader.readInt();
                arrRowLoadFactor[row] = (float) Math.log(freq);
                arrRowPosInCache[row] = -1;
                arrRowPosInFile[row] = offset;
                arrRowStart[row + 1] = arrRowStart[row] + freq;
            }
            arrRowPosInFile[rows] = arrRowPosInFile[rows - 1] + (arrRowStart[rows]-arrRowStart[rows-1])*(cellDataLength+4)+8;
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readIndexFromMatrix(String filename) {
        RandomAccessFile raf;
        int i, freq, row;
        long offset;
        try {
            raf = new RandomAccessFile(filename,"r");
            rows = raf.readInt();
            columns = raf.readInt();
            totalCell = raf.readInt();

            arrRowLoadFactor = new float[rows];
            arrRowPosInCache = new int[rows];
            arrRowPosInFile = new long[rows + 1];
            arrRowStart = new int[rows + 1];
            arrRowStart[0] = 0;
            for (i = 0; i < rows; i++) {
                //row must be equal to i
                offset=raf.getFilePointer();
                row =raf.readInt();
                freq=raf.readInt();
                arrRowLoadFactor[row] = (float) Math.log(freq);
                arrRowPosInCache[row] = -1;
                arrRowPosInFile[row] = offset;
                arrRowStart[row + 1] = arrRowStart[row] + freq;
                raf.skipBytes(freq*(cellDataLength+4));
            }
            arrRowPosInFile[rows] = raf.getFilePointer();
            raf.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}