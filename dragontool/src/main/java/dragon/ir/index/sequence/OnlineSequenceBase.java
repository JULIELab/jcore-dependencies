package dragon.ir.index.sequence;

import java.util.ArrayList;
/**
 * <p>The online sequence base information class  </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineSequenceBase implements SequenceReader, SequenceWriter{
    private ArrayList list;
    private boolean initialized;

    public OnlineSequenceBase() {
        list=new ArrayList();
        initialized=false;
    }

    public void initialize(){
        if(initialized)
            return;
        list.clear();
    }

    public void close(){
        initialized=false;
    }

    public int[] getSequence(int index){
        return (int[])list.get(index);
    }

    public int getSequenceLength(int index){
        int[] seq;

        seq=getSequence(index);
        if(seq==null)
            return 0;
        else
            return seq.length;
    }

    public boolean addSequence(int index, int[]seq){
        if(index<list.size())
            return false;
        while(list.size()<index)
            list.add(null);
        list.add(seq);
        return true;
    }
}