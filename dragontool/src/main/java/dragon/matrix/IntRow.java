package dragon.matrix;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.Serializable;

/**
 * <p>Row of matrix for integer data </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IntRow extends AbstractRow implements Serializable{
	private static final long serialVersionUID = 1L;
	protected int[] columns;
    protected int[] scores;

    public IntRow() {
        length=0;
        row=-1;
        columns=null;
        scores=null;
        loadFactor=0;
    }

    public IntRow(int row, int num, int[] columns, int[] scores) {
        this.length = num;
        this.row = row;
        this.columns = columns;
        this.scores = scores;
        loadFactor = 0;
    }

    public void load(int row, int num, byte[] data){
        DataInputStream dis;
        int i;

        try{
            this.row=row;
            this.length=num;
            columns=new int[num];
            scores=new int[num];
            dis=new DataInputStream(new ByteArrayInputStream(data));
            for(i=0;i<num;i++){
                columns[i]=dis.readInt();
                scores[i]=dis.readInt();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public int[] getNonZeroColumns(){
        return columns;
    }

    public int getNonZeroColumn(int index){
        return columns[index];
    }

    public int[] getNonZeroIntScores(){
        return scores;
    }

    public int getNonZeroIntScore(int index){
        return scores[index];
    }

    public void setNonZeroIntScore(int index, int score){
        scores[index]=score;
    }

    public double getNonZeroDoubleScore(int index){
        return scores[index];
    }

    public int getInt(int column){
        int index;

        if((index=getColumnIndex(column))>=0)
            return scores[index];
        else
            return 0;
    }

    public Cell getNonZeroCell(int index){
        return new IntCell(row,columns[index],scores[index]);
    }

    public Cell getCell(int column){
        int index;

        if((index=getColumnIndex(column))>=0)
            return getNonZeroCell(index);
        else
            return null;
    }

    private int getColumnIndex(int column){
        int low, high, middle;
        int curColumn;

        low=0;
        high=length-1;
        while(low<=high)
        {
            middle=(low+high)/2;
            curColumn=columns[middle];;
            if(curColumn==column)
                return middle;
            else if(curColumn<column)
                low=middle+1;
            else
                high=middle-1;
        }
        return -1;
    }
}
