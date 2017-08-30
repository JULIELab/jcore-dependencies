package dragon.ir.index.sentence;

import dragon.ir.index.*;
import dragon.matrix.IntSparseMatrix;
import dragon.nlp.*;
import dragon.nlp.extract.*;
import java.util.ArrayList;

/**
 * <p>Indexer for indexing sentence without storing to disk </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class OnlineSentenceIndexer extends AbstractSentenceIndexer{
    private OnlineSentenceWriteController writer;
    private ConceptExtractor cptExtractor;
    private TripleExtractor tripleExtractor;

    public OnlineSentenceIndexer(ConceptExtractor extractor, boolean useConcept) {
        super(extractor.getDocumentParser());
        this.cptExtractor =extractor;
        writer=new OnlineSentenceWriteController(false,useConcept);
    }

    public OnlineSentenceIndexer(TripleExtractor extractor, boolean useConcept) {
        super(extractor.getConceptExtractor().getDocumentParser());
        this.tripleExtractor =extractor;
        this.cptExtractor =extractor.getConceptExtractor();
        writer=new OnlineSentenceWriteController(true,useConcept);
    }

    public boolean indexedSentence(String sentKey){
        return writer.indexed(sentKey);
    }

    public boolean isRelationSupported(){
        return writer.isRelationSupported();
    }

    public boolean index(Sentence sent, String sentKey){
        ArrayList cptList, tripleList;

        try{
            if (sent==null || sent.getFirstWord()==null || !writer.setDoc(sentKey))
                return false;

            writer.addRawSentence(sent);
            cptList=cptExtractor.extractFromSentence(sent);
            if (writer.isRelationSupported()) {
                tripleList=tripleExtractor.extractFromSentence(sent);
                writer.write(cptList, tripleList);
            }
            else
                writer.write(cptList);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void initialize(){
        if(initialized)
            return;
        writer.initialize();
        initialized=true;
    }

    public void close(){
        initialized=false;
        writer.close();
    }

    public OnlineSentenceBase getSentenceBase(){
        return writer.getSentenceBase();
    }

    public IRTermIndexList getTermIndexList(){
        return writer.getTermIndexList();
    }

    public IRRelationIndexList getRelationIndexList(){
        return writer.getRelationIndexList();
    }

    public IRDocIndexList getDocIndexList(){
        return writer.getDocIndexList();
    }

    public SimpleElementList getDocKeyList(){
        return writer.getDocKeyList();
    }

    public SimpleElementList getTermKeyList(){
        return writer.getTermKeyList();
    }

    public SimplePairList getRelationKeyList(){
        return writer.getRelationKeyList();
    }

    public IntSparseMatrix getDocTermMatrix(){
        return writer.getDocTermMatrix();
    }

    public IntSparseMatrix getDocRelationMatrix(){
        return writer.getDocRelationMatrix();
    }

    public IRCollection getIRCollection(){
        return writer.getIRCollection();
    }
}