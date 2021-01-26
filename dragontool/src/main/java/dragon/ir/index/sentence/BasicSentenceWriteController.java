package dragon.ir.index.sentence;

import dragon.ir.index.*;
import dragon.nlp.Sentence;
import dragon.nlp.SimpleElementList;
import dragon.nlp.SimplePairList;
import dragon.onlinedb.BasicArticle;
import dragon.onlinedb.BasicCollectionWriter;

import java.util.ArrayList;

/**
 * <p>The basic controller for writting sentence indexing information to disk </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicSentenceWriteController extends AbstractIndexWriteController{
    private IndexWriter indexWriter;
    private BasicCollectionWriter rawBW;
    private FileIndex fileIndex;

    public BasicSentenceWriteController(String directory, boolean relationSupported, boolean indexConceptEntry) {
        super(relationSupported, indexConceptEntry);
        fileIndex=new FileIndex(directory,relationSupported);
    }

    public void initialize(){
        if(initialized)
            return;

        docKeyList=new SimpleElementList(fileIndex.getDocKeyListFilename(),true);
        termKeyList = new SimpleElementList(fileIndex.getTermKeyListFilename(), true);
        if (relationSupported)
            relationKeyList = new SimplePairList(fileIndex.getRelationKeyListFilename(), true);
        indexWriter = new BasicIndexWriter(fileIndex.getDirectory(), relationSupported);
        indexWriter.initialize();
        initialized=true;
    }

    public void flush(){
         indexWriter.flush();
    }

    public void close(){
        try{
            docKeyList.close();
            termKeyList.close();
            if (relationSupported)
                relationKeyList.close();
            indexWriter.close();
            initialized=false;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean addRawSentence(Sentence sent){
        BasicArticle article;

        try{
            article=new BasicArticle();
            article.setKey(curDocKey);
            article.setTitle(sent.toString());
            rawBW.add(article);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean write(ArrayList conceptList){
        IRTerm[] arrTerms;
        IRDoc curDoc;
        try{
            if (curDocKey ==null)
                return false;

            curDoc = new IRDoc(curDocKey);
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

            curDoc = new IRDoc(curDocKey);
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
}
