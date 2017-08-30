package dragon.matrix;

import dragon.util.*;
/**
 * <p>Super sparse matrix for double data type </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DoubleSuperSparseMatrix extends AbstractSuperSparseMatrix implements DoubleSparseMatrix{
	private static final long serialVersionUID = 1L;

	public DoubleSuperSparseMatrix(String matrixFile){
        super(null,matrixFile,DoubleCell.getCellDataLength());
    }

    public DoubleSuperSparseMatrix(String indexFile, String matrixFile){
        super(indexFile,matrixFile,DoubleCell.getCellDataLength());
    }

    public DoubleSuperSparseMatrix(String matrixFile, boolean mergeMode, boolean miniMode){
        super(null, matrixFile, DoubleCell.getCellDataLength(),mergeMode,miniMode);
    }

    public DoubleSuperSparseMatrix(String indexFile, String matrixFile, boolean mergeMode, boolean miniMode){
        super(indexFile, matrixFile, DoubleCell.getCellDataLength(),mergeMode,miniMode);
    }

    public SparseMatrix createSparseMatrix(){
        String indexFile, matrixFile;
        indexFile =FileUtil.getNewTempFilename("newmatrix","index");
        matrixFile=FileUtil.getNewTempFilename("newmatrix","matrix");
        return new DoubleSuperSparseMatrix(indexFile,matrixFile,false,false);
    }

    protected AbstractFlatSparseMatrix createFlatSparseMatrix(boolean mergeMode, boolean miniMode){
        return new DoubleFlatSparseMatrix(mergeMode,miniMode);
    }

    protected Row createRow(int row, int columns, byte[] data){
        DoubleRow cur;

        cur=new DoubleRow();
        cur.load(row,columns,data);
        return cur;
    }

    public Cell createCell(int row, int column, byte[] data){
        DoubleCell cur;

        cur=new DoubleCell(row, column);
        cur.fromByteArray(data);
        return cur;
    }

    public Cell createCell(int row, int column, String data){
        DoubleCell cur;

        cur=new DoubleCell(row, column);
        cur.fromString(data);
        return cur;
    }

    public boolean add(int row, int column, double score){
        return add(new DoubleCell(row,column,score));
    }

    public double getDouble(int row, int column){
        if(row>=rows) return 0;
        return ((DoubleRow)getRow(row)).getDouble(column);
    }

    public double getNonZeroDoubleScoreInRow(int row, int index){
        if(row>=rows) return 0;
        return ( (DoubleRow) getRow(row)).getNonZeroDoubleScore(index);
    }

    public double[] getNonZeroDoubleScoresInRow(int row) {
        double[] oldArray, newArray;

        if(row>=rows)
            return null;
        oldArray = ( (DoubleRow) getRow(row)).getNonZeroDoubleScores();
        newArray = new double[oldArray.length];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }

    public double getRowSum(int row){
        int count;
        double sum, scores[];

        sum=0;
        scores=getNonZeroDoubleScoresInRow(row);
        for(count=0;count<scores.length;count++)
        {
            sum+=scores[count];
        }
        return sum;
    }

    public double getColumnSum(int column)
    {
        return ((DoubleSuperSparseMatrix)transpose()).getRowSum(column);
    }
}