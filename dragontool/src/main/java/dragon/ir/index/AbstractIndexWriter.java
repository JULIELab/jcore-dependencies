package dragon.ir.index;

import dragon.matrix.*;

/**
 * <p>The class implements two methods writing termdoc and docterm matrix to disk with options of term based, relation based or both </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractIndexWriter implements IndexWriter{
    protected static int doc_cache_size=5000;
    protected IRTermIndexList termIndexList;
    protected IRRelationIndexList relationIndexList;
    protected IRDocIndexList docIndexList;
    protected IntSparseMatrix doctermMatrix;
    protected IntSparseMatrix docrelationMatrix;
    protected IRCollection collection;
    protected int doc_in_cache;
    protected boolean relationSupported, initialized;

    public AbstractIndexWriter(boolean relationSupported) {
        this.relationSupported=relationSupported;
        doc_in_cache=0;
    }

    public int size(){
        return docIndexList.size();
    }

    public synchronized boolean write(IRDoc curDoc, IRTerm[] arrTerms, IRRelation[] arrRelations){
        int i;

        try{
            if(!relationSupported) return false;

            if (!docIndexList.add(curDoc)) {
                System.out.println("#" + curDoc.getKey() + " is alrady indexed");
                return false;
            }

            if(doc_in_cache>doc_cache_size){
                flush();
            }
            doc_in_cache++;

            for(i=0;i<arrTerms.length;i++){
                arrTerms[i].setDocFrequency(1);
                doctermMatrix.add(curDoc.getIndex(), arrTerms[i].getIndex(), arrTerms[i].getFrequency());
                termIndexList.add(arrTerms[i]);
            }
            collection.addTermCount(curDoc.getTermCount());

            for(i=0;i<arrRelations.length;i++){
                arrRelations[i].setDocFrequency(1);
                docrelationMatrix.add(curDoc.getIndex(), arrRelations[i].getIndex(), arrRelations[i].getFrequency());
                relationIndexList.add(arrRelations[i]);
            }
            collection.addRelationCount(curDoc.getRelationCount());

            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean write(IRDoc curDoc, IRTerm[] arrTerms){
        int i;

        try{
            if (!docIndexList.add(curDoc)) {
                System.out.println("#" + curDoc.getKey() + " is already indexed");
                return false;
            }

            if(doc_in_cache>doc_cache_size){
                flush();
            }
            doc_in_cache++;

            for(i=0;i<arrTerms.length;i++){
                arrTerms[i].setDocFrequency(1);
                doctermMatrix.add(curDoc.getIndex(), arrTerms[i].getIndex(), arrTerms[i].getFrequency());
                termIndexList.add(arrTerms[i]);
            }
            collection.addTermCount(curDoc.getTermCount());
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
