package dragon.matrix;

import dragon.util.ByteArrayConvert;
/**
 * <p>The cell object for handling double data</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DoubleCell extends AbstractCell implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	protected int row, col;
    protected double score;

    public DoubleCell(int row, int column) {
        this.row = row;
        this.col = column;
        this.score = 0;
    }

    public DoubleCell(int row, int column, double score) {
        this.row = row;
        this.col = column;
        this.score = score;
    }

    public static int getCellDataLength(){
        return 8;
    }

    public void merge(Cell cell){
        if(cell.getResetOption())
            score=cell.getDoubleScore();
        else
            score+=cell.getDoubleScore();
    }

    public byte[] toByteArray(){
        return ByteArrayConvert.toByte(score);
    }

    public void fromByteArray(byte[] data){
        score=ByteArrayConvert.toDouble(data);
    }

    public String toString(){
        return String.valueOf(score);
    }

    public void fromString(String data){
        score=Double.parseDouble(data);
    }

    public Cell transpose(){
        return new DoubleCell(col,row,score);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    public double getDoubleScore(){
        return score;
    }

    public int getIntScore(){
        return (int)score;
    }

    public long getLongScore(){
        return (long)score;
    }

    public byte getByteScore(){
        return (byte)score;
    }

    public void setDoubleScore(double score){
        this.score=score;
    }

    public void setIntScore(int score){
        this.score =score;
    }

    public void setLongScore(long score){
        this.score =score;
    }

    public void setByteScore(byte score){
        this.score=score;
    }
}
