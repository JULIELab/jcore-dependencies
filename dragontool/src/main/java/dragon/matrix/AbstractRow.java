package dragon.matrix;

/**
 * <p>Abstract row class implementing functions of as row related operations </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractRow implements Row, java.io.Serializable{
    protected int row, length;
    protected float loadFactor;

    public int compareTo(Object obj){
        double objLoadFactor;

        objLoadFactor=((Row)obj).getLoadFactor();
        if(loadFactor>objLoadFactor)
            return -1;
        else if(loadFactor<objLoadFactor)
            return 1;
        else
            return 0;
    }

    public int getRowIndex(){
        return row;
    }

    public int getNonZeroNum(){
        return length;
    }

    public void setLoadFactor(float factor){
        loadFactor = factor;
    }

    public float getLoadFactor() {
        return loadFactor;
    }
}