package dragon.matrix;

/**
 * <p>Implements basic functions of interface--Cell such as resetting and comparing cell objects </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractCell implements Cell, java.io.Serializable {
    private boolean resetOption;

    public boolean getResetOption(){
        return resetOption;
    }

    public void setResetOption(boolean option){
        this.resetOption =option;
    }

    public int compareTo(Object obj){
        Cell cellObj;

        cellObj = (Cell)obj;
        if (getRow()>cellObj.getRow())
            return 1;
        else if (getRow()==cellObj.getRow()) {
            if (getColumn()>cellObj.getColumn())
                return 1;
            else if (getColumn()==cellObj.getColumn())
                return 0;
            else
                return -1;
        }
        else
            return -1;
    }
}