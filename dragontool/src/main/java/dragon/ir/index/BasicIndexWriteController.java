package dragon.ir.index;

import dragon.nlp.*;
import java.util.ArrayList;

/**
 * <p>This class implements function of writing section index.</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicIndexWriteController extends AbstractIndexWriteController{
    private static final int MAX_SEC=10;
    private FileIndex fileIndex;
    private IRSection[] arrSections;

    public BasicIndexWriteController(String directory, boolean relationSupported, boolean indexConceptEntry) {
        super(relationSupported, indexConceptEntry);
        fileIndex=new FileIndex(directory,relationSupported);
        arrSections=new IRSection[MAX_SEC];
    }

    public void initialize(){
        if(initialized)
            return;
        processedDoc=0;
        docKeyList=new SimpleElementList(fileIndex.getDocKeyListFilename(),true);
        termKeyList=new SimpleElementList(fileIndex.getTermKeyListFilename(),true);
        if(relationSupported){
            relationKeyList = new SimplePairList(fileIndex.getRelationKeyListFilename(), true);
        }
        for(int i=0;i<arrSections.length;i++)
            if(arrSections[i]!=null && arrSections[i].getIndexWriter()!=null)
                arrSections[i].getIndexWriter().initialize();
        initialized=true;
    }

    public boolean addSection(IRSection section){
        IndexWriter cur;

        if(section.getSectionID()>MAX_SEC)  return false;

        cur=new BasicIndexWriter(fileIndex.getDirectory()+"/"+section.getSectionName(),relationSupported);
        cur.initialize();
        section.setIndexWriter(cur);
        arrSections[section.getSectionID()]=section;
        return true;
    }

    public void flush(){
        int i;

        for(i=0;i<MAX_SEC;i++){
            if (arrSections[i] != null)
                arrSections[i].getIndexWriter().flush();
        }
    }

    public void close(){
        int i;

        docKeyList.close();
        termKeyList.close();
        if(relationSupported)
            relationKeyList.close();

        for(i=0;i<MAX_SEC;i++){
            if (arrSections[i] != null)
                arrSections[i].getIndexWriter().close();
        }
    }

    public boolean write(int section, ArrayList conceptList){
       IRTerm[] arrTerms;
       IRDoc curDoc;
       try{
           if (arrSections[section] == null || curDocKey ==null)
               return false;

           curDoc = new IRDoc(curDocKey);
           curDoc.setIndex(curDocIndex);
           arrTerms = getIRTermArray(generateIRTermList(conceptList), curDoc);
           return arrSections[section].getIndexWriter().write(curDoc, arrTerms);
       }
       catch(Exception e){
           e.printStackTrace();
           return false;
       }
   }

   public boolean write(int section, ArrayList conceptList, ArrayList tripleList){
       IRTerm[] arrTerms;
       IRRelation[] arrRelations;
       IRDoc curDoc;
       try{
           if (arrSections[section] == null || curDocKey==null)
               return false;

           curDoc = new IRDoc(curDocKey);
           curDoc.setIndex(curDocIndex);
           arrTerms = getIRTermArray(generateIRTermList(conceptList), curDoc);
           arrRelations = getIRRelationArray(generateIRRelationList(tripleList), curDoc);
           return arrSections[section].getIndexWriter().write(curDoc, arrTerms, arrRelations);
       }
       catch(Exception e){
           e.printStackTrace();
           return false;
       }
   }
}