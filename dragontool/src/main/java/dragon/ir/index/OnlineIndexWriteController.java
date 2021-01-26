package dragon.ir.index;

import dragon.matrix.IntSparseMatrix;
import dragon.nlp.SimpleElementList;
import dragon.nlp.SimplePairList;

import java.util.ArrayList;

/**
 * <p>The class is the same as BasicIndexWriteController except that it does not store information to disk</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineIndexWriteController extends AbstractIndexWriteController{
    private OnlineIndexWriter indexWriter;

    public OnlineIndexWriteController(boolean relationSupported, boolean indexConceptEntry) {
        super(relationSupported,indexConceptEntry);
        curDocIndex=-1;
        curDocKey=null;
    }

    public void initialize(){
        if(initialized)
            return;
        docKeyList=new SimpleElementList();
        termKeyList = new SimpleElementList();
        if (relationSupported) {
            relationKeyList = new SimplePairList();
        }
        indexWriter = new OnlineIndexWriter(relationSupported);
        indexWriter.initialize();
        initialized=true;
    }

    public void close(){
        indexWriter.close();
        initialized=false;
    }

    public boolean write(ArrayList conceptList){
        IRTerm[] arrTerms;
        IRDoc curDoc;
        try{
            if (curDocKey ==null)
                return false;

            curDoc = new IRDoc(new String(curDocKey));
            curDoc.setIndex(curDocIndex);
            arrTerms = getIRTermArray(generateIRTermList(conceptList), curDoc);
            return indexWriter.write(curDoc, arrTerms);
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean write(ArrayList conceptList, ArrayList tripleList){
        IRTerm[] arrTerms;
        IRRelation[] arrRelations;
        IRDoc curDoc;
        try{
            if (curDocKey==null)
                return false;

            curDoc = new IRDoc(new String(curDocKey));
            curDoc.setIndex(curDocIndex);
            arrTerms = getIRTermArray(generateIRTermList(conceptList), curDoc);
            arrRelations = getIRRelationArray(generateIRRelationList(tripleList), curDoc);
            return indexWriter.write(curDoc, arrTerms, arrRelations);
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public SimpleElementList getDocKeyList(){
        return docKeyList;
    }

    public SimpleElementList getTermKeyList(){
        return termKeyList;
    }

    public SimplePairList getRelationKeyList(){
        return relationKeyList;
    }

    public IRTermIndexList getTermIndexList(){
        return  indexWriter.getTermIndexList();
    }

    public IRRelationIndexList getRelationIndexList(){
        return indexWriter.getRelationIndexList();
    }

    public IRDocIndexList getDocIndexList(){
        return indexWriter.getDocIndexList();
    }

    public IntSparseMatrix getDocTermMatrix(){
        return indexWriter.getDocTermMatrix();
    }

    public IntSparseMatrix getDocRelationMatrix(){
        return indexWriter.getDocRelationMatrix();
    }

    public IRCollection getIRCollection(){
        return indexWriter.getIRCollection();
    }
}