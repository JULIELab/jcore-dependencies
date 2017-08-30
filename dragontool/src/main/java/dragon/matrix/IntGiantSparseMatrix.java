package dragon.matrix;

import dragon.util.FileUtil;
/**
 * <p>Giant sparse matrix for extreme large integer matrix</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IntGiantSparseMatrix extends AbstractGiantSparseMatrix implements IntSparseMatrix {
	private static final long serialVersionUID = 1L;

	public IntGiantSparseMatrix(String indexFile, String matrixFile){
        super(indexFile, matrixFile, IntCell.getCellDataLength());
    }

    public IntGiantSparseMatrix(String indexFile, String matrixFile, boolean mergeMode, boolean miniMode){
        super(indexFile, matrixFile, IntCell.getCellDataLength(), mergeMode, miniMode);
    }

    public SparseMatrix createSparseMatrix(){
        String indexFile, matrixFile;
        indexFile =FileUtil.getNewTempFilename("newmatrix","index");
        matrixFile=FileUtil.getNewTempFilename("newmatrix","matrix");
        return new IntGiantSparseMatrix(indexFile,matrixFile,false,false);
    }

    protected AbstractFlatSparseMatrix createFlatSparseMatrix(boolean mergeMode, boolean miniMode){
        return new IntFlatSparseMatrix(mergeMode,miniMode);
    }

    protected Row createRow(int row, int columns, byte[] data){
        IntRow cur;

        cur=new IntRow();
        cur.load(row,columns,data);
        return cur;
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

    public int getInt(int row, int column){
        if(row>=rows) return 0;
        return ((IntRow)getRow(row)).getInt(column);
    }

    public int getNonZeroIntScoreInRow(int row, int index){
        if(row>=rows) return 0;
        return ( (IntRow) getRow(row)).getNonZeroIntScore(index);
    }

    public int[] getNonZeroIntScoresInRow(int row) {
        int[] oldArray, newArray;

        if(row>=rows)
            return null;
        oldArray = ( (IntRow) getRow(row)).getNonZeroIntScores();
        newArray = new int[oldArray.length];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }

    public double[] getNonZeroDoubleScoresInRow(int row) {
        int[] oldArray;
        double[] newArray;
        int i;

        if(row>=rows)
            return null;
        oldArray = ( (IntRow) getRow(row)).getNonZeroIntScores();
        if(oldArray==null)
            return null;

        newArray = new double[oldArray.length];
        for(i=0;i<oldArray.length;i++)
            newArray[i]=oldArray[i];
        return newArray;
    }


    public long getRowSum(int row){
        int count,  scores[];
        long sum;

        sum=0;
        scores=getNonZeroIntScoresInRow(row);
        for(count=0;count<scores.length;count++)
        {
            sum+=scores[count];
        }
        return sum;
    }

    public long getColumnSum(int column)
    {
        return ((IntSuperSparseMatrix)transpose()).getRowSum(column);
    }
}
