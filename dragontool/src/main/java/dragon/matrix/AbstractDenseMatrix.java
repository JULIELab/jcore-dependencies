package dragon.matrix;

/**
 * <p>Implements basic functions of interface--DenseMatrix and extends class AbstractMatrix </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractDenseMatrix extends AbstractMatrix implements DenseMatrix, java.io.Serializable {
    public AbstractDenseMatrix(int row, int column, int cellDataLength) {
        this.rows=row;
        this.columns=column;
        this.cellDataLength =cellDataLength;
    }

    public double cosine(int rowA, int rowB){
        int i,productSum, vLenA, vLenB;

        productSum=0;
        vLenA=0;
        vLenB=0;
        for(i=0;i<columns;i++){
            if(cellDataLength==8){
                productSum += getDouble(rowA, i) * getDouble(rowB, i);
                vLenA += getDouble(rowA, i) * getDouble(rowA, i);
                vLenB += getDouble(rowB, i) * getDouble(rowB, i);
            }
            else{
                productSum += getInt(rowA, i) * getInt(rowB, i);
                vLenA += getInt(rowA, i) * getInt(rowA, i);
                vLenB += getInt(rowB, i) * getInt(rowB, i);
            }
        }

        return productSum/Math.sqrt(vLenA)*Math.sqrt(vLenB);
    }

    public int getCooccurrenceCount(int rowA, int rowB){
        int coOccurCount,i;
        for(i=0,coOccurCount=0;i<columns;i++){
            if(cellDataLength==8)
                if (getDouble(rowA, i) != 0 && getDouble(rowB, i) != 0)
                    coOccurCount++;
                else
                if (getInt(rowA, i) != 0 && getInt(rowB, i) != 0)
                    coOccurCount++;
        }
        return coOccurCount;
    }
}