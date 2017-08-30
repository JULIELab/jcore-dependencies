package dragon.ir.index;

import dragon.nlp.extract.*;
import dragon.onlinedb.*;
import dragon.util.*;
import java.io.*;

/**
 * <p>The class handles two dimensional document language model indexing </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DualIndexer  implements Indexer {
    private PrintWriter log;
    protected BasicIndexWriteController firstWriter, secondWriter;
    protected boolean relationSupported;
    protected boolean initialized;
    protected DualConceptExtractor extractor;
    protected boolean firstUseConcept,secondUseConcept;

    public DualIndexer(DualConceptExtractor extractor, String firstIndexFolder, String secondIndexFolder){
       this(extractor,false,firstIndexFolder,false,secondIndexFolder);
   }

   public DualIndexer(DualConceptExtractor extractor, boolean useConcept, String firstIndexFolder, String secondIndexFolder){
       this(extractor,useConcept,firstIndexFolder,useConcept,secondIndexFolder);
   }

   public DualIndexer(DualConceptExtractor extractor, boolean firstUseConcept, String firstIndexFolder,
                              boolean secondUseConcept, String secondIndexFolder) {
       this.extractor = extractor;
       this.firstUseConcept = firstUseConcept;
       this.relationSupported =false;
       firstWriter = new BasicIndexWriteController(firstIndexFolder,relationSupported,firstUseConcept);
       secondWriter = new BasicIndexWriteController(secondIndexFolder,relationSupported,secondUseConcept);
       log=null;
       initialized=false;
   }

   public void setLog(String logFile) {
       log = FileUtil.getPrintWriter(logFile);
   }

    public void initialize() {
        if(initialized) return;

        firstWriter.addSection(new IRSection(IRSection.SEC_ALL));
        secondWriter.addSection(new IRSection(IRSection.SEC_ALL));
        initialized=true;
    }

    public void close() {
        initialized=false;
        firstWriter.close();
        secondWriter.close();
    }

    public synchronized boolean index(Article article) {
        boolean ret;

        try {
            if(!initialized) return false;

            writeLog(new java.util.Date().toString());
            writeLog("Indexing article #" + article.getKey()+": ");

            extractor.initDocExtraction();
            ret = firstWriter.indexed(article.getKey()) || secondWriter.indexed(article.getKey());
            if(ret){
                writeLog("indexed\n");
                return false;
            }

            ret =extractor.extractFromDoc(article);
            if(ret){
                firstWriter.setDoc(article.getKey());
                firstWriter.write(IRSection.SEC_ALL, extractor.getFirstConceptList());
                secondWriter.setDoc(article.getKey());
                secondWriter.write(IRSection.SEC_ALL, extractor.getSecondConceptList());
                writeLog("succeeded\r\n");
                return true;
            }
            else{
                writeLog("failed\r\n");
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            writeLog("failed\r\n");
            return false;
        }
    }

    public boolean indexed(String docKey) {
        return firstWriter.indexed(docKey);
    }

    protected void writeLog(String content) {
        if (log != null) {
            log.write(content);
            log.flush();
        }
    }
}
