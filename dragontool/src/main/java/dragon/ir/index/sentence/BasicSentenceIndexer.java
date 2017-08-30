package dragon.ir.index.sentence;

import dragon.nlp.*;
import dragon.nlp.extract.*;
import java.util.ArrayList;

/**
 * <p>The basic indexer for sentence level indexing </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicSentenceIndexer extends AbstractSentenceIndexer{
    private BasicSentenceWriteController writer;
    private ConceptExtractor cptExtractor;
    private TripleExtractor tripleExtractor;

    public BasicSentenceIndexer(ConceptExtractor extractor, boolean useConcept, String indexFolder) {
        super(extractor.getDocumentParser());
        this.cptExtractor =extractor;
        writer=new BasicSentenceWriteController(indexFolder,false,useConcept);
    }

    public BasicSentenceIndexer(TripleExtractor extractor, boolean useConcept, String indexFolder) {
        super(extractor.getConceptExtractor().getDocumentParser());
        this.tripleExtractor =extractor;
        this.cptExtractor =extractor.getConceptExtractor();
        writer=new BasicSentenceWriteController(indexFolder,true,useConcept);
    }

    public boolean indexedSentence(String sentKey){
        return writer.indexed(sentKey);
    }

    public boolean index(Sentence sent, String sentKey){
        ArrayList cptList, tripleList;

        try{
            if (sent==null || sent.getFirstWord()==null || writer.indexed(sentKey))
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
        writer.close();
        super.close();
    }
}