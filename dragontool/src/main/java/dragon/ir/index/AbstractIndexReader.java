package dragon.ir.index;

import dragon.matrix.IntCell;
import dragon.matrix.IntSparseMatrix;
import dragon.nlp.SimpleElementList;
import dragon.onlinedb.Article;
import dragon.onlinedb.CollectionReader;

/**
 * <p>AbstractIndexReader implements functions defined in interface IndexReader such as getting
 * termdoc and docterm matrix, getting termkey and dockey, getting termindex and docindex,
 * getting all the indexed terms for a given document, getting all the indexed documents for a given term
 * ...  </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractIndexReader  implements IndexReader {
    protected CollectionReader collectionReader;
    protected SimpleElementList termKeyList;
    protected IRTermIndexList termIndexList;
    protected IRRelationIndexList relationIndexList;
    protected SimpleElementList docKeyList;
    protected IRDocIndexList docIndexList;
    protected IntSparseMatrix termdocMatrix, doctermMatrix;
    protected IntSparseMatrix relationdocMatrix, docrelationMatrix;
    protected boolean relationSupported;
    protected boolean initialized;
    protected IRCollection collection;

    public AbstractIndexReader(boolean relationSupported) {
        this(relationSupported,null);
    }

    public AbstractIndexReader(boolean relationSupported, CollectionReader collectionReader) {
        this.relationSupported = relationSupported;
        this.collectionReader =collectionReader;
    }

    public IntSparseMatrix getDocTermMatrix(){
        return doctermMatrix;
    }

    public IntSparseMatrix getTermDocMatrix(){
        return termdocMatrix;
    }

    public IntSparseMatrix getDocRelationMatrix(){
        return docrelationMatrix;
    }

    public IntSparseMatrix getRelaitonDocMatrix(){
        return relationdocMatrix;
    }

    public void close(){
        termIndexList.close();
        docIndexList.close();
        termdocMatrix.close();
        doctermMatrix.close();
        if(relationSupported){
            relationdocMatrix.close();
            docrelationMatrix.close();
            relationIndexList.close();
        }
        initialized=false;
    }

    public boolean isRelationSupported(){
        return relationSupported;
    }

    public void setIRDocKeyList(SimpleElementList keyList){
        this.docKeyList =keyList;
    }

    public void setIRTermKeyList(SimpleElementList keyList){
        this.termKeyList=keyList;
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
        IntCell cell;

        cell=(IntCell)termdocMatrix.getCell(termIndex,docIndex);
        if(cell==null)
            return null;
        else
            return new IRTerm(termIndex,cell.getIntScore());
    }

    public IRTerm[] getTermList(int docIndex) {
        IRTerm[] arrTerm;
        IntCell curCell;
        int i, len;

        len = doctermMatrix.getNonZeroNumInRow(docIndex);
        arrTerm = new IRTerm[len];
        for (i = 0; i < len; i++) {
            curCell=(IntCell) doctermMatrix.getNonZeroCellInRow(docIndex, i);
            arrTerm[i] = new IRTerm(curCell.getColumn(),curCell.getIntScore());
        }
        return arrTerm;
    }

    public int[] getTermFrequencyList(int docIndex) {
        return doctermMatrix.getNonZeroIntScoresInRow(docIndex);
    }

    public int[] getTermIndexList(int docIndex) {
        return doctermMatrix.getNonZeroColumnsInRow(docIndex);
    }

    public IRRelation getIRRelation(int index) {
        return ( (IRRelation) relationIndexList.get(index));
    }

    public IRRelation getIRRelation(int relationIndex, int docIndex){
        IntCell cell;

        cell=(IntCell)docrelationMatrix.getCell(docIndex, relationIndex);
        return new IRRelation(relationIndex,-1,-1,cell.getIntScore(),0);
   }

    public int[] getRelationIndexList(int docIndex) {
        return docrelationMatrix.getNonZeroColumnsInRow(docIndex);
    }

    public int[] getRelationFrequencyList(int docIndex) {
        return docrelationMatrix.getNonZeroIntScoresInRow(docIndex);
    }

    public IRRelation[] getRelationList(int docIndex) {
        IRRelation[] arrRelation;
        IntCell cell;
        int i, len;

        len = docrelationMatrix.getNonZeroNumInRow(docIndex);
        arrRelation = new IRRelation[len];
        for (i = 0; i < len; i++) {
            cell=(IntCell)docrelationMatrix.getNonZeroCellInRow(docIndex, i);
            arrRelation[i] = new IRRelation(cell.getColumn(),-1,-1,cell.getIntScore(),0);
        }
        return arrRelation;
    }

    public IRCollection getCollection() {
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
        if(collectionReader==null){
            System.out.println("Collection Reader is not set yet!");
            return null;
        }
        else{
            return collectionReader.getArticleByKey(key);
        }
    }

    public Article getOriginalDoc(int index){
        return getOriginalDoc(getDocKey(index));
    }

    public int[] getTermDocIndexList(int termIndex) {
        return termdocMatrix.getNonZeroColumnsInRow(termIndex);
    }

    public IRDoc[] getTermDocList(int termIndex) {
        int[] arrDocIndex;
        IRDoc[] arrDoc;
        int i;

        arrDocIndex = getTermDocIndexList(termIndex);
        arrDoc = new IRDoc[arrDocIndex.length];
        for (i = 0; i < arrDocIndex.length; i++) {
            arrDoc[i] = getDoc(arrDocIndex[i]);
        }
        return arrDoc;
    }

    public int[] getTermDocFrequencyList(int termIndex) {
        return termdocMatrix.getNonZeroIntScoresInRow(termIndex);
    }

    public int[] getRelationDocFrequencyList(int relationIndex){
        return relationdocMatrix.getNonZeroIntScoresInRow(relationIndex);
    }

    public IRDoc[] getRelationDocList(int relationIndex){
        int[] arrDocIndex;
        IRDoc[] arrDoc;
        int i;

        arrDocIndex = getRelationDocIndexList(relationIndex);
        arrDoc = new IRDoc[arrDocIndex.length];
        for (i = 0; i < arrDocIndex.length; i++) {
            arrDoc[i] = getDoc(arrDocIndex[i]);
        }
        return arrDoc;

    }

    public int[] getRelationDocIndexList(int relationIndex){
        return relationdocMatrix.getNonZeroColumnsInRow(relationIndex);
    }
}
