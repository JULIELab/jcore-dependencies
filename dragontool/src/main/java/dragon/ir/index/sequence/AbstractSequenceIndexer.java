package dragon.ir.index.sequence;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IRTerm;
import dragon.ir.index.Indexer;
import dragon.nlp.Token;
import dragon.nlp.extract.ConceptExtractor;
import dragon.onlinedb.Article;
import dragon.util.FileUtil;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * <p>The abstract indexer for indexing sequencial data </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSequenceIndexer implements Indexer{
    private ConceptExtractor ce;
    private PrintWriter log;
    protected AbstractSequenceIndexWriter writer;
    protected boolean initialized;
    private boolean useTitle, useAbstract, useBody, useMeta;

    public AbstractSequenceIndexer(ConceptExtractor ce) {
        this.ce=ce;
        useTitle=true;
        useAbstract=true;
        useMeta=false;
        useBody=true;
    }

    public void setSectionIndexOption(boolean title, boolean abt, boolean body, boolean meta) {
        useMeta=meta;
        useTitle=title;
        useAbstract=abt;
        useBody=body;
    }

    public void initialize(){
        if (initialized)
            return;
        writer.initialize();
        initialized = true;
    }


    public void close(){
        if(writer!=null)
            writer.close();
        if(log!=null)
            log.close();
        initialized=false;
    }

    public boolean index(Article article){
        StringBuffer sb;
        ArrayList list;
        IRDoc curDoc;
        IRTerm[] arrTerm;
        boolean result;
        int i;

        try{
            sb = new StringBuffer();
            if (useTitle && article.getTitle() != null) {
                if (sb.length() > 0)
                    sb.append("\n\n");
                sb.append(article.getTitle());
            }
            if (useAbstract && article.getAbstract() != null) {
                if (sb.length() > 0)
                    sb.append("\n\n");
                sb.append(article.getAbstract());
            }
            if (useBody && article.getBody() != null) {
                if (sb.length() > 0)
                    sb.append("\n\n");
                sb.append(article.getBody());
            }
            if (useMeta && article.getMeta() != null) {
                if (sb.length() > 0)
                    sb.append("\n\n");
                sb.append(article.getMeta());
            }
            if (sb.length() <= 0){
                writeLog(new java.util.Date().toString()+" Indexing article #" + article.getKey()+": no content\n");
                return false;
            }

            curDoc = new IRDoc(article.getKey());
            list = ce.extractFromDoc(sb.toString());
            arrTerm = new IRTerm[list.size()];
            for (i = 0; i < list.size(); i++) {
                arrTerm[i] = new IRTerm( ( (Token) list.get(i)).getName(), -1, 1);
            }
            result=writer.write(curDoc, arrTerm);
            if(result)
                writeLog(new java.util.Date().toString()+" Indexing article #" + article.getKey()+": successful\n");
            else
                writeLog(new java.util.Date().toString()+" Indexing article #" + article.getKey()+": failed\n");
            return result;
        }
        catch(Exception e){
            e.printStackTrace();
            writeLog(new java.util.Date().toString()+" Indexing article #" + article.getKey()+": failed\n");
            return false;
        }
    }

    public void setLog(String logFile) {
        log = FileUtil.getPrintWriter(logFile);
    }

    public boolean indexed(String docKey) {
        return writer.indexed(docKey);
    }

    protected void writeLog(String content) {
        if (log != null) {
            log.write(content);
            log.flush();
        }
    }
}
