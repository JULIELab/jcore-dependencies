package dragon.matrix;

import dragon.util.ByteArrayConvert;
import dragon.util.FileUtil;

import java.io.File;
import java.io.RandomAccessFile;
/**
 * <p>The abstract sparse matrix for handling extreme large sparse matrirwhich will write matrix data to disk whenever
 * it's over fulsh interval by default 1000,000 and superior to AbstractSuperSparseMatrix, however it's lack of some
 * basic matrix operation functions such as getNonZeroColumnInRow privided by AbstractSuperSparseMatrix  because it focuses
 * on storing and loading matrix to disk efficiently   </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractGiantSparseMatrix extends AbstractSparseMatrix{
    private static int DEFAULT_FLUSHINTERVAL=1000000;
    protected String indexFilename, matrixFilename;

    //for reading mode only
    protected int totalCell;
    protected RandomAccessFile rafIndex, rafMatrix;
    private int lastAccessIndex, lastAccessRowLen;
    private long lastAccessRowStart;
    private Row lastAccessRow;
    private byte[] buf;

    //for writing mode only
    protected AbstractFlatSparseMatrix cacheMatrix;
    protected int flushInterval;
    protected SparseMatrixFactory matrixFactory;

    abstract protected Row createRow(int row, int columns, byte[] data);
    abstract protected AbstractFlatSparseMatrix createFlatSparseMatrix(boolean mergeMode, boolean miniMode);

    public AbstractGiantSparseMatrix(String indexFilename, String matrixFilename, int cellDataLength) {
        super(false,false,cellDataLength);
        this.indexFilename =indexFilename;
        this.matrixFilename =matrixFilename;
        matrixFactory=null;
        flushInterval=0;
        isFinalized=true;
        cacheMatrix=null;
        initData();
    }

    public AbstractGiantSparseMatrix(String indexFilename, String matrixFilename, int cellDataLength, boolean mergeMode, boolean miniMode ){
        super(mergeMode,miniMode, cellDataLength);
        this.indexFilename =indexFilename;
        this.matrixFilename=matrixFilename;
        matrixFactory=new SparseMatrixFactory(matrixFilename,cellDataLength);
        flushInterval=DEFAULT_FLUSHINTERVAL;
        isFinalized=false;
        cacheMatrix=createFlatSparseMatrix(mergeMode, miniMode);
    }

    private void initData(){
        try{
            buf=new byte[12];
            if(FileUtil.exist(indexFilename) && FileUtil.exist(matrixFilename)){
                rafIndex = new RandomAccessFile(indexFilename, "r");
                rafMatrix = new RandomAccessFile(matrixFilename, "r");
                lastAccessIndex = -1;
                rows = rafIndex.readInt();
                columns = rafIndex.readInt();
                totalCell = rafIndex.readInt();
            }
            else{
                rafIndex=null;
                rafMatrix=null;
                lastAccessIndex=-1;
                rows=0;
                columns=0;
                totalCell=0;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setFlushInterval(int interval){
        this.flushInterval =interval;
    }

    public void close(){
        try{
            if (isFinalized) {
                if(rafIndex!=null)
                    rafIndex.close();
                if(rafMatrix!=null)
                    rafMatrix.close();
                lastAccessIndex=-1;
            }
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
        if(isFinalized) return;

        cacheMatrix.finalizeData(true);
        matrixFactory.add(cacheMatrix);
        cacheMatrix.close();
    }

    public boolean finalizeData(boolean sorting){
        File file;

        if(isFinalized) return false;
        flush();
        this.rows=matrixFactory.rows();
        this.columns=matrixFactory.columns();
        totalCell=matrixFactory.getNonZeroNum();
        isFinalized=true;
        if(indexFilename!=null) matrixFactory.genIndexFile(indexFilename);
        if(!matrixFactory.getMatrixFilename().equalsIgnoreCase(matrixFilename)){
            file=new File(matrixFilename);
            file.delete();
            (new File(matrixFactory.getMatrixFilename())).renameTo(file);
        }
        initData();
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
        if(row>=rows)
            return 0;
        else
            return getRowLen(row);
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

    private int getRowLen(int index){
        try{
            if(lastAccessIndex==index)
                return lastAccessRowLen;
            else{
                rafIndex.seek(index*16+12+4);
                rafIndex.read(buf);
                lastAccessRowStart=ByteArrayConvert.toLong(buf,0);
                lastAccessRowLen=ByteArrayConvert.toInt(buf,8);
                lastAccessRow=null;
                lastAccessIndex = index;
                return lastAccessRowLen;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    /*If the row wanted is cached, simply get it from the cache. Otherwise, load the row from the file.*/
    protected Row getRow(int index) {
        if (lastAccessIndex==index) {
            if(lastAccessRow!=null)
                return lastAccessRow;
            else
                return loadRow(index,lastAccessRowLen);
        }
        else {
            return loadRow(index,-1);
        }
    }

    /* load a row from the file. */
    private Row loadRow(int index, int rowLen) {
        Row curRow;
        byte[] data;

        try {
            if(rowLen<0) rowLen=getRowLen(index);
            if(rowLen==0)
                data=new byte[0];
            else{
                rafMatrix.seek(lastAccessRowStart + 8);
                data = new byte[rowLen * (getCellDataLength() + 4)];
                rafMatrix.read(data);
            }
            curRow = createRow(index, rowLen, data);
            lastAccessRow=curRow;
            return curRow;
        }
        catch (Exception e) {
            return null;
        }
    }
}