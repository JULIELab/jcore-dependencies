package dragon.matrix;

/**
 * <p>Flat sparse matrix for integer data</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IntFlatSparseMatrix extends AbstractFlatSparseMatrix implements IntSparseMatrix, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public IntFlatSparseMatrix(){
        super(false,false, IntCell.getCellDataLength());
    }

    public IntFlatSparseMatrix(boolean mergeMode, boolean miniMode){
        super(mergeMode,miniMode, IntCell.getCellDataLength());
    }

    public IntFlatSparseMatrix(String filename){
        super(false,false, IntCell.getCellDataLength());
        readTextMatrixFile(filename);
    }

    public IntFlatSparseMatrix(String filename, boolean binaryFile){
        super(false,false, IntCell.getCellDataLength());
        if(binaryFile)
            readBinaryMatrixFile(filename);
        else
            readTextMatrixFile(filename);
    }

    public SparseMatrix createSparseMatrix(){
        return new IntFlatSparseMatrix();
    }

    public Cell createCell(int row, int column, byte[] data){
        IntCell cur;

        cur=new IntCell(row, column);
        cur.fromByteArray(data);
        return cur;
    }

    public Cell createCell(int row, int column, String data){
        IntCell cur;

        cur=new IntCell(row, column);
        cur.fromString(data);
        return cur;
    }

    public boolean add(int row, int column, int score){
        return add(new IntCell(row,column,score));
    }

    public long getRowSum(int row){
        int num, count;
        long sum;

        sum=0;
        num=getNonZeroNumInRow(row);
        for(count=0;count<num;count++)
        {
            sum+=getNonZeroIntScoreInRow(row,count);
        }
        return sum;
    }

    public long getColumnSum(int column)
    {
        return ((IntFlatSparseMatrix)transpose()).getRowSum(column);
    }
}