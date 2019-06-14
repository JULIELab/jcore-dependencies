package dragon.ir.index.sequence;

import dragon.ir.index.*;
import dragon.nlp.SimpleElementList;
import dragon.util.SortedArray;

/**
 * <p>The online index writer for sequencial data </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineSequenceIndexWriter extends AbstractSequenceIndexWriter{
    public OnlineSequenceIndexWriter() {
    }

    public void initialize(){
        if(initialized)
            return;
        collection=new IRCollection();
        termCache=new SortedArray(500);
        docKeyList=new SimpleElementList();
        termKeyList=new SimpleElementList();
        docIndexList=new OnlineIRDocIndexList();
        termIndexList=new OnlineIRTermIndexList();
        doctermMatrix=new OnlineSequenceBase();
        initialized=true;
    }

    public void close(){
        flush();
        initialized=false;
    }

    public void clean(){
        termIndexList.close();
        docIndexList.close();
        doctermMatrix.close();
        docKeyList.close();
        termKeyList.close();
    }

    public IRTermIndexList getTermIndexList(){
        return termIndexList;
    }

    public IRDocIndexList getDocIndexList(){
        return docIndexList;
    }

    public SimpleElementList getDocKeyList(){
        return docKeyList;
    }

    public SimpleElementList getTermKeyList(){
        return termKeyList;
    }

    public IRCollection getIRCollection(){
        return collection;
    }

    public SequenceReader getSequenceReader(){
        return (SequenceReader)doctermMatrix;
    }
}