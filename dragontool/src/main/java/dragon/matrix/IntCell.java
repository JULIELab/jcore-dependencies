package dragon.matrix;

import dragon.util.ByteArrayConvert;
/**
 * <p>Cell for integer data </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IntCell extends AbstractCell implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	protected int row, col;
    protected int score;

    public IntCell(int row, int column, int score) {
        this.row = row;
        this.col = column;
        this.score = score;
    }

    public IntCell(int row, int column) {
        this.row = row;
        this.col = column;
        this.score =0;
    }

    public static int getCellDataLength(){
        return 4;
    }

    public void merge(Cell cell){
        if(cell.getResetOption())
            score=cell.getIntScore();
        else
            score+=cell.getIntScore();
    }

    public byte[] toByteArray(){
        return ByteArrayConvert.toByte(score);
    }

    public void fromByteArray(byte[] data){
        score=ByteArrayConvert.toInt(data);
    }

    public String toString(){
        return String.valueOf(score);
    }

    public void fromString(String data){
        score=Integer.parseInt(data);
    }

    public Cell transpose(){
        return new IntCell(col,row,score);
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

    public void setScore(double score) {
        this.score = (int)score;
    }

    public void setDoubleScore(double score){
        this.score=(int)score;
    }

    public void setIntScore(int score){
        this.score =score;
    }

    public void setLongScore(long score){
        this.score =(int)score;
    }

    public void setByteScore(byte score){
        this.score=score;
    }
}
