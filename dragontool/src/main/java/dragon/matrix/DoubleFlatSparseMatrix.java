package dragon.matrix;

/**
 * <p>Double flat sparse matrix handles data smaller than super sparse matrix, however it provides options
 * of storing data to disk either in binary or text format </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DoubleFlatSparseMatrix extends AbstractFlatSparseMatrix implements DoubleSparseMatrix, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public DoubleFlatSparseMatrix(){
        super(false,false, DoubleCell.getCellDataLength());
    }

    public DoubleFlatSparseMatrix(boolean mergeMode, boolean miniMode){
        super(mergeMode,miniMode, DoubleCell.getCellDataLength());
    }

    public DoubleFlatSparseMatrix(String filename){
        super(false,false, DoubleCell.getCellDataLength());
        readTextMatrixFile(filename);
    }

    public DoubleFlatSparseMatrix(String filename, boolean binaryFile){
        super(false,false, DoubleCell.getCellDataLength());
        if(binaryFile)
            readBinaryMatrixFile(filename);
        else
            readTextMatrixFile(filename);
    }

    public SparseMatrix createSparseMatrix(){
        return new DoubleFlatSparseMatrix();
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

    public boolean add(int row, int column, double value){
        return add(new DoubleCell(row,column,value));
    }

    public double get(int row, int column){
        return getDouble(row, column);
    }

    public void set(int row, int column, double score){
        setDouble(row, column, score);
    }

    public double getQuick(int row, int column){
        return getDouble(row, column);
    }

    public void setQuick(int row, int column, double score) {
        setDouble(row, column, score);
    }

    public double getRowSum(int row){
        int num, count;
        double sum;

        sum=0;
        num=getNonZeroNumInRow(row);
        for(count=0;count<num;count++)
        {
            sum+=getNonZeroDoubleScoreInRow(row,count);
        }
        return sum;
    }

    public double getColumnSum(int column)
    {
        return ((DoubleFlatSparseMatrix)transpose()).getRowSum(column);
    }

    public void normalizeColumns(){
        DoubleFlatSparseMatrix matrix;
        double[] arrNorm;
        double score;
        int num, i, j;
        DoubleCell cur;

        matrix=(DoubleFlatSparseMatrix)transpose();
        arrNorm=new double[columns];
        for(i=0;i<matrix.rows();i++)
        {
            arrNorm[i]=0;
            num=matrix.getNonZeroNumInRow(i);
            for(j=0;j<num;j++)
            {
                score=matrix.getNonZeroDoubleScoreInRow(i,j);
                arrNorm[i]+=score*score;
            }
            arrNorm[i]=Math.sqrt(arrNorm[i]);
            //change the row value in transpose matrix
            for(j=0;j<num;j++)
            {
                score=matrix.getNonZeroDoubleScoreInRow(i,j);
                matrix.setNonZeroDoubleScoreInRow(i,j,score);
            }
        }

        //change the column value of the current matrix
        for(i=0;i<list.size();i++)
        {
            cur=(DoubleCell)getNonZeroCell(i);
            cur.setDoubleScore(cur.getDoubleScore()/arrNorm[cur.getColumn()]);
        }
    }

    public void normalizeRows(){
        ((DoubleFlatSparseMatrix)transpose()).normalizeColumns();
    }

    public DoubleDenseMatrix product(DoubleFlatSparseMatrix matrixY){
        DoubleFlatSparseMatrix matrixX;
        DoubleDenseMatrix output;
        double score;
        int x, y, xNum, yNum;
        int i,j, xCol, yCol;

        matrixX=this;
        if(matrixX.columns()!=matrixY.rows()) return null;

        output=new DoubleFlatDenseMatrix(rows,matrixY.columns());
        for (i = 0; i < matrixX.rows(); i++){
            for (j = 0; j < matrixY.columns(); j++) {
                score = 0;
                x = 0;
                y = 0;
                xNum = matrixX.getNonZeroNumInRow(i);
                yNum = matrixY.getNonZeroNumInColumn(j);

                while (x < xNum && y < yNum) {
                    xCol = matrixX.getNonZeroColumnInRow(i, x);
                    yCol = matrixY.getNonZeroRowInColumn(j, y);
                    if (xCol < yCol)
                        x++;
                    else if (xCol == yCol) {
                        x++;
                        y++;
                        score+=matrixX.getNonZeroDoubleScoreInRow(i,x)*matrixY.getNonZeroDoubleScoreInColumn(j,y);
                    }
                    else
                        y++;
                }
                output.setDouble(i , j, score);
            }
        }
        return output;
    }

    public DoubleDenseMatrix product(DoubleDenseMatrix b) {
        int row, col, i;
        int[] arrColumn;
        double[] arrScore;
        double score;
        DoubleDenseMatrix output;

        output = new DoubleFlatDenseMatrix(rows, b.columns());
        for (row = 0; row < rows(); row++) {
            arrColumn = getNonZeroColumnsInRow(row);
            arrScore = getNonZeroDoubleScoresInRow(row);
            for (col = 0; col < b.columns(); col++) {
                score = 0;
                for (i = 0; i < arrColumn.length; i++)
                    score += arrScore[i] * b.getDouble(arrColumn[i], col);
                output.setDouble(row, col, score);
            }
        }
        return output;
    }
}