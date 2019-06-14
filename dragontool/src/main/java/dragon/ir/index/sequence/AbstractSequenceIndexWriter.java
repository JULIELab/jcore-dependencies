package dragon.ir.index.sequence;

import dragon.ir.index.*;
import dragon.nlp.SimpleElementList;
import dragon.nlp.compare.IndexComparator;
import dragon.util.SortedArray;

/**
 * <p>The abstract index writer for sequencial data </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSequenceIndexWriter implements IndexWriter{
    protected static int doc_cache_size=5000;
    protected SortedArray termCache;
    protected SimpleElementList termKeyList;
    protected SimpleElementList docKeyList;
    protected IRTermIndexList termIndexList;
    protected IRDocIndexList docIndexList;
    protected SequenceWriter doctermMatrix;
    protected IRCollection collection;
    protected boolean initialized;

    public boolean indexed(String docKey){
        return docKeyList.contains(docKey);
    }

    public int size(){
        return docIndexList.size();
    }

     public void flush(){
        collection.setDocNum(docIndexList.size());
        collection.setTermNum(termIndexList.size());
    }

    public synchronized boolean write(IRDoc curDoc, IRTerm[] arrTerms, IRRelation[] arrRelations){
       return false;
    }

    public synchronized boolean write(IRDoc curDoc, IRTerm[] arrTerm){
        SortedArray termList;
        int[] arrSeq;
        int docIndex, i;

        try{
            docIndex=docKeyList.add(curDoc.getKey());
            if(docIndex!=docKeyList.size()-1){
                System.out.println("#" + curDoc.getKey() + " is already indexed");
                return false;
            }
            curDoc.setIndex(docIndex);

            processIRTerms(arrTerm);
            arrSeq=new int[arrTerm.length];
            termList=new SortedArray(new IndexComparator());
            for(i=0;i<arrTerm.length;i++){
                arrTerm[i].setDocFrequency(1);
                arrSeq[i]=arrTerm[i].getIndex();
                if(!termList.add(arrTerm[i]))
                    ((IRTerm)termList.get(termList.insertedPos())).addFrequency(1);
            }
            curDoc.setTermCount(arrTerm.length);
            curDoc.setTermNum(termList.size());

            docIndexList.add(curDoc);
            for(i=0;i<termList.size();i++){
                termIndexList.add((IRTerm)termList.get(i));
            }
            doctermMatrix.addSequence(curDoc.getIndex(),arrSeq);
            collection.addTermCount(curDoc.getTermCount());
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void processIRTerms(IRTerm[] arrTerm) {
        IRTerm cur;
        int i;

        termCache.clear();
        for(i=0;i<arrTerm.length;i++){
            cur=arrTerm[i];
            if (termCache.add(cur)) {
                cur.setIndex( termKeyList.add(cur.getKey()));
            } else {
                cur.setIndex( ( (IRTerm) termCache.get(termCache.insertedPos())).getIndex());
            }
        }
    }
}
