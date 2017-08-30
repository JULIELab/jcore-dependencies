package dragon.ir.index.sequence;

import dragon.ir.index.*;
import dragon.nlp.*;
import dragon.onlinedb.*;

/**
 * <p>The abstract index reader for sequencial data </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSequenceIndexReader implements SequenceIndexReader{
    protected CollectionReader collectionReader;
    protected SimpleElementList termKeyList, docKeyList;
    protected IRTermIndexList termIndexList;
    protected IRDocIndexList docIndexList;
    protected SequenceReader doctermSeq;
    protected IRCollection collection;
    protected boolean initialized;

    public void close(){
        termIndexList.close();
        docIndexList.close();
        doctermSeq.close();
        initialized=false;
    }

    public boolean isRelationSupported(){
        return false;
    }

    public IRCollection getCollection(){
        return collection;
    }

    public IRDoc getDoc(int index) {
        return ((IRDoc) docIndexList.get(index)).copy();
    }

    public String getDocKey(int index){
        return docKeyList.search(index);
    }

    public IRDoc getDoc(String key){
        IRDoc cur;
        int index;

        index=docKeyList.search(key);
        if(index>=0){
            cur=getDoc(index);
            cur.setKey(key);
            return cur;
        }
        else
            return null;
    }

    public Article getOriginalDoc(String key){
        if (collectionReader == null) {
            System.out.println("Collection Reader is not set yet!");
            return null;
        }
        else
            return collectionReader.getArticleByKey(key);
    }

    public Article getOriginalDoc(int index) {
        return getOriginalDoc(getDocKey(index));
    }

    public IRTerm[] getTermList(int docIndex){
        IRTerm[] arrTerm;
        int i, len, arrIndex[];

        len=doctermSeq.getSequenceLength(docIndex);
        if(len==0)
            return null;
        else{
            arrIndex=getTermIndexList(docIndex);
            arrTerm=new IRTerm[len];
            for(i=0;i<len;i++){
                arrTerm[i] =new IRTerm(arrIndex[i],1);
            }
            return arrTerm;
        }
    }

    public int[] getTermIndexList(int docIndex){
        return doctermSeq.getSequence(docIndex);
    }

    public int[] getTermFrequencyList(int docIndex){
        int i, len, arrFreq[];

        len=doctermSeq.getSequenceLength(docIndex);
        if(len==0)
            return null;
        else{
            arrFreq=new int[len];
            for(i=0;i<len;i++)
                arrFreq[i]=1;
            return arrFreq;
        }
    }

    public IRTerm getIRTerm(int index) {
        return ((IRTerm) termIndexList.get(index));
    }

    public String getTermKey(int index){
        return termKeyList.search(index);
    }

    public IRTerm getIRTerm(String key) {
        IRTerm cur;
        int pos;

        pos = termKeyList.search(key);
        if (pos >= 0) {
            cur=((IRTerm) termIndexList.get(pos));
            cur.setKey(key);
            return cur;
        }
        else {
            return null;
        }
    }

    public IRTerm getIRTerm(int termIndex, int docIndex){
        return null;
    }

    public IRDoc[] getTermDocList(int termIndex){
        return null;
    }

    public int[] getTermDocFrequencyList(int termIndex){
        return null;
    }

    public int[] getTermDocIndexList(int termIndex){
        return null;
    }

    public IRRelation[] getRelationList(int docIndex){
        return null;
    }

    public int[] getRelationFrequencyList(int docIndex){
        return null;
    }

    public int[] getRelationIndexList(int docIndex){
        return null;
    }

    public IRRelation getIRRelation(int index){
        return null;
    }

    public IRRelation getIRRelation(int relationIndex, int docIndex){
        return null;
    }

    public IRDoc[] getRelationDocList(int relationIndex){
        return null;
    }

    public int[] getRelationDocFrequencyList(int relationIndex){
        return null;
    }

    public int[] getRelationDocIndexList(int relationIndex){
        return null;
    }
}