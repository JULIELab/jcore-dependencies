package dragon.matrix;

/**
 * <p>Abstract Sparse matrix implements the interface of sparse matrix which can be called by any data type's
 * sparse matrix such as double and integer</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSparseMatrix extends AbstractMatrix implements SparseMatrix, java.io.Serializable {
    protected boolean isFinalized;
    protected boolean mergeMode;
    protected boolean miniMode;

    public AbstractSparseMatrix(boolean mergeMode, boolean miniMode, int cellDataLength){
        this.mergeMode=mergeMode;
        this.miniMode=miniMode;
        this.cellDataLength =cellDataLength;
        transposeMatrix=null;
        isFinalized=false;
    }

    public boolean isFinalized(){
        return isFinalized;
    }

    public boolean finalizeData(){
        return finalizeData(true);
    }

    public Cell getNonZeroCellInColumn(int column, int index){
        return ((SparseMatrix)transpose()).getNonZeroCellInRow(column,index);
    }

    public int getNonZeroNumInColumn(int column){
        return ((SparseMatrix)transpose()).getNonZeroNumInRow(column);
    }

    public int getNonZeroRowInColumn(int column, int index){
        return ((SparseMatrix)transpose()).getNonZeroColumnInRow(column,index);
    }

    public int[] getNonZeroRowsInColumn(int column){
        return ((SparseMatrix)transpose()).getNonZeroColumnsInRow(column);
    }

    public double getDouble(int row, int column){
        Cell curCell;

        curCell=(Cell)getCell(row,column);
        if(curCell!=null)
            return curCell.getDoubleScore();
        else
            return 0;
    }

    public int getInt(int row, int column){
        Cell curCell;

        curCell=(Cell)getCell(row,column);
        if(curCell!=null)
            return curCell.getIntScore();
        else
            return 0;
    }

    public double getNonZeroDoubleScoreInColumn(int column, int index){
        return ((SparseMatrix)transpose()).getNonZeroDoubleScoreInRow(column,index);
    }

    public double[] getNonZeroDoubleScoresInColumn(int column){
        return ((SparseMatrix)transpose()).getNonZeroDoubleScoresInRow(column);
    }

    public int getNonZeroIntScoreInColumn(int column, int index){
        return ((SparseMatrix)transpose()).getNonZeroIntScoreInRow(column,index);
    }

    public int[] getNonZeroIntScoresInColumn(int column){
        return ((SparseMatrix)transpose()).getNonZeroIntScoresInRow(column);
    }

    public double getNonZeroDoubleScoreInRow(int row, int index){
        return getNonZeroCellInRow(row,index).getDoubleScore();
    }

    public int getNonZeroIntScoreInRow(int row, int index){
        return getNonZeroCellInRow(row,index).getIntScore();
    }

    public double[] getNonZeroDoubleScoresInRow(int row){
        int num, count;
        double[] arrScore;

        num=getNonZeroNumInRow(row);
        arrScore=new double[num];

        for(count=0;count<num;count++)
        {
            arrScore[count]=getNonZeroCellInRow(row,count).getDoubleScore();
        }
        return arrScore;
    }

    public int[] getNonZeroIntScoresInRow(int row){
        int num, count;
        int[] arrScore;

        num=getNonZeroNumInRow(row);
        arrScore=new int[num];

        for(count=0;count<num;count++)
        {
            arrScore[count]=getNonZeroCellInRow(row,count).getIntScore();
        }
        return arrScore;
    }

    public boolean genCooccurrenceMatrix(IntSparseMatrix outputCooccurMatrix){
        return genCooccurrenceMatrix(this,1, outputCooccurMatrix);
    }

    public boolean genCooccurrenceMatrix(SparseMatrix matrixY, IntSparseMatrix outputCooccurMatrix){
        return genCooccurrenceMatrix(matrixY,1, outputCooccurMatrix);
    }

    public boolean genCooccurrenceMatrix(SparseMatrix matrixY, int minOccurrence, IntSparseMatrix outputCooccurMatrix){
        SparseMatrix matrixX;
        int coOccurCount;
        int x, y, xNum, yNum;
        int i,j;
        int[] arrColumnX, arrColumnY;
        boolean equal;

        matrixX=this;
        if(matrixX.columns()!=matrixY.columns()) return false;
        equal=matrixX.equals(matrixY);

        for (i = 0; i <matrixX.rows(); i++){
            System.out.println((new java.util.Date()).toString()+" Processing Row "+i);
            arrColumnX=matrixX.getNonZeroColumnsInRow(i);
            xNum = matrixX.getNonZeroNumInRow(i);
            for (j =equal?i+1:0; j <matrixY.rows(); j++) {
                coOccurCount = 0;
                x = 0;
                y = 0;
                arrColumnY=matrixY.getNonZeroColumnsInRow(j);
                yNum = matrixY.getNonZeroNumInRow(j);

                while (x < xNum && y < yNum) {
                    if (arrColumnX[x]< arrColumnY[y])
                        x++;
                    else if (arrColumnX[x]==arrColumnY[y]) {
                        x++;
                        y++;
                        coOccurCount++;
                    }
                    else
                        y++;
                }
                if(coOccurCount>=minOccurrence){
                    outputCooccurMatrix.add(i, j, coOccurCount);
                    if(equal){
                        outputCooccurMatrix.add(i, i, xNum);
                        outputCooccurMatrix.add(j, i, coOccurCount);
                    }
                }
            }
        }
        if(equal)
            outputCooccurMatrix.finalizeData();
        else
            outputCooccurMatrix.finalizeData(false);
        return true;
    }

    public int getCooccurrenceCount(int rowA, int rowB){
        int coOccurCount;
        int x,y,xNum, yNum;
        int xCol, yCol;

        coOccurCount=0;
        x=0;
        y=0;
        xNum=getNonZeroNumInRow(rowA);
        yNum=getNonZeroNumInRow(rowB);

        while(x<xNum && y<yNum){
            xCol=getNonZeroColumnInRow(rowA,x);
            yCol=getNonZeroColumnInRow(rowB,y);
            if(xCol<yCol)
                x++;
            else if(xCol==yCol){
                x++;
                y++;
                coOccurCount++;
            }
            else
                y++;
        }
        return coOccurCount;
    }

    public double cosine(int rowA, int rowB){
        int xNum, yNum, x, y, arrXCol[], arrYCol[];
        double x2, y2, xy, arrXScore[], arrYScore[];

        x = 0;
        y = 0;
        xy=0;
        x2=0;
        y2=0;
        xNum = getNonZeroNumInRow(rowA);
        yNum = getNonZeroNumInRow(rowB);
        if(xNum==0 || yNum==0)
            return 0;
        arrXScore=getNonZeroDoubleScoresInRow(rowA);
        arrYScore=getNonZeroDoubleScoresInRow(rowB);
        arrXCol = getNonZeroColumnsInRow(rowA);
        arrYCol = getNonZeroColumnsInRow(rowB);

        while (x < xNum && y < yNum) {
            if (arrXCol[x] < arrYCol[y]) {
                x2 = x2 + arrXScore[x] * arrXScore[x];
                x++;
            } else if (arrXCol[x] == arrYCol[y]) {
                xy = xy + arrXScore[x] * arrYScore[y];
                x2 = x2 + arrXScore[x] * arrXScore[x];
                y2 = y2 + arrYScore[y] * arrYScore[y];
                x++;
                y++;

            } else {
                y2 = y2 + arrYScore[y] * arrYScore[y];
                y++;
            }
        }

        while(y<yNum)
        {
            y2=y2+arrYScore[y]*arrYScore[y];
            y++;
        }

        while(x<xNum)
        {
            x2=x2+arrXScore[x]*arrXScore[x];
            x++;
        }
        return xy/(java.lang.Math.sqrt(x2)*java.lang.Math.sqrt(y2));
    }

    public static boolean genTranslationMatrix(IntSparseMatrix inputCooccurMatrix, DoubleSparseMatrix outputTransMatrix){
        int i, j;
        double rowSum, rowLen;
        int[] arrColumn, arrScore;

        for(i=0;i<inputCooccurMatrix.rows();i++){
            arrColumn=inputCooccurMatrix.getNonZeroColumnsInRow(i);
            arrScore=inputCooccurMatrix.getNonZeroIntScoresInRow(i);
            rowLen=arrColumn.length;
            rowSum=0;
            for(j=0;j<rowLen;j++)  rowSum+=arrScore[j];
            for(j=0;j<rowLen;j++)  outputTransMatrix.add(i,arrColumn[j],arrScore[j]/rowSum);
        }
        outputTransMatrix.finalizeData(false);
        return true;
    }

    public Matrix transpose(){
        SparseMatrix curMatrix;
        int i,j,num;

        if(!isFinalized())
            return null;

        if(transposeMatrix==null)
        {
            curMatrix=createSparseMatrix();
            for(i=0;i<rows;i++)
            {
                num=getNonZeroNumInRow(i);
                for(j=0;j<num;j++)
                    curMatrix.add(getNonZeroCellInRow(i,j).transpose());
            }
            curMatrix.finalizeData();
            curMatrix.setTranspose(this);
            transposeMatrix=curMatrix;
        }
        return transposeMatrix;
    }
}