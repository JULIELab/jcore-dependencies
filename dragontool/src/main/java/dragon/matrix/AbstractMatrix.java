package dragon.matrix;

/**
 * <p>Abstract Matrix implements basic functions such as getting base row, getting base column and so on. </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractMatrix implements Matrix,java.io.Serializable {
    protected Matrix transposeMatrix;
    protected int rows, columns;
    protected int rowBase,columnBase;
    protected int cellDataLength;

    public int rows(){
        return rows;
    }

    public int columns(){
        return columns;
    }

    public void setTranspose(Matrix matrix){
        transposeMatrix=matrix;
    }

    public Matrix getTranspose(){
        return transposeMatrix;
    }

    public Matrix transpose(){
        return transposeMatrix;
    }

    public int getCellDataLength(){
        return cellDataLength;
    }

    public int getBaseRow(){
        return rowBase;
    }

    public int getBaseColumn(){
        return columnBase;
    }
}