package dragon.ir.index;

import dragon.matrix.*;

/**
 * <p>The class is the same as BasicIndexWriter except that it does not store information to disk.</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineIndexWriter extends AbstractIndexWriter{
    public OnlineIndexWriter(boolean relationSupported) {
        super(relationSupported);
    }

    public void initialize(){
        if(initialized)
            return;
        collection=new IRCollection();
        docIndexList=new OnlineIRDocIndexList();
        termIndexList=new OnlineIRTermIndexList();
        doctermMatrix=new IntFlatSparseMatrix(false,false);

        if(relationSupported){
            relationIndexList = new OnlineIRRelationIndexList();
            docrelationMatrix=new IntFlatSparseMatrix(false,false);
        }
        initialized=true;
    }

    public void close(){
        flush();
        doctermMatrix.finalizeData();
        if(relationSupported)
            docrelationMatrix.finalizeData();
        initialized=false;
    }

    public void clean(){
        termIndexList.close();
        docIndexList.close();
        doctermMatrix.close();
        if(relationSupported){
            relationIndexList.close();
            docrelationMatrix.close();
        }
    }

    public void flush(){
        collection.setDocNum(docIndexList.size());
        collection.setTermNum(termIndexList.size());

        if(relationSupported){
            collection.setRelationNum(relationIndexList.size());
        }
    }

    public IRTermIndexList getTermIndexList(){
        return  termIndexList;
    }

    public IRRelationIndexList getRelationIndexList(){
        return relationIndexList;
    }

    public IRDocIndexList getDocIndexList(){
        return docIndexList;
    }

    public IntSparseMatrix getDocTermMatrix(){
        return doctermMatrix;
    }

    public IntSparseMatrix getDocRelationMatrix(){
        return docrelationMatrix;
    }

    public IRCollection getIRCollection(){
        return collection;
    }
}