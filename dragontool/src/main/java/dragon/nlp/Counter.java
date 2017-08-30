package dragon.nlp;

/**
 * <p>A light integer counter </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang
 * @version 1.0
 */

public class Counter {
    private int count;

    public Counter (){
        count=0;
    }
    public Counter(int k) {
        count=k;
    }

    public void addCount(int k){
        count=count+k;
    }

    public void setCount(int k){
        count=k;
    }

    public int getCount(){
        return count;
    }
}
