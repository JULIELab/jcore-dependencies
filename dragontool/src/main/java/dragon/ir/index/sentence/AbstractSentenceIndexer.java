package dragon.ir.index.sentence;

import dragon.ir.index.Indexer;
import dragon.nlp.*;
import dragon.onlinedb.Article;
import dragon.util.*;
import java.io.PrintWriter;

/**
 * <p>The abstract indexder for sentence level indexing</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSentenceIndexer implements Indexer{
    protected boolean initialized;
    protected DocumentParser parser;
    private PrintWriter log;
    private SortedArray docs;
    private int minSentenceLength;
    private boolean useTitle, useAbstract, useBody;


    public AbstractSentenceIndexer(DocumentParser parser) {
        this.parser =parser;
        docs=new SortedArray();
        initialized=false;
        useTitle=true;
        useAbstract=true;
        useBody=true;
        initialized=false;
        minSentenceLength=3;
    }

    public abstract boolean index(Sentence sent, String sentKey);

    public void setMinSentenceLength(int minLength){
        if(minLength>=1)
            minSentenceLength=minLength;
    }

    public int getMinSentenceLength(){
        return minSentenceLength;
    }

    public boolean screenArticleContent(boolean useTitle, boolean useAbstract, boolean useBody){
        if(initialized)
            return false;
        this.useTitle =useTitle;
        this.useAbstract =useAbstract;
        this.useBody =useBody;
        return true;
    }

    public void setLog(String logFile) {
        log = FileUtil.getPrintWriter(logFile);
    }

    public boolean indexed(String docKey){
        return docs.contains(docKey);
    }

    public synchronized boolean index(Article article){
        Document doc;
        Paragraph para;
        Sentence sent;
        String sentKey;
        int index;

        try{
            if(!initialized){
                System.out.println("Please initialize the indexer before indexing!");
                return false;
            }
            if(article.getKey()==null || docs.contains(article.getKey()))
                return false;
            doc=getDocument(article);
            if(doc==null)
                return false;

            index=-1;
            docs.add(article.getKey());
            para=doc.getFirstParagraph();
            while(para!=null){
                sent=para.getFirstSentence();
                while(sent!=null){
                    //make sure the sentence is non-empty
                    if(sent.getWordNum()>=minSentenceLength ){
                        index=index+1;
                        sentKey=getSentenceKey(article.getKey(),index);
                        if(index(sent,sentKey))
                           writeLog((new java.util.Date()).toString()+" "+sentKey+" successful");
                        else
                           writeLog((new java.util.Date()).toString()+" "+sentKey+" failed");
                    }
                    sent=sent.next;
                }
                para=para.next;
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    protected Document getDocument(Article article){
        Document doc;

        if(article.getKey()==null || article.getKey().trim().length()==0)
            return null;

        doc=new Document();
        if(useTitle)
            doc.addParagraph(parser.parseParagraph(article.getTitle()));
        if(useAbstract)
            doc.addParagraph(parser.parseParagraph(article.getAbstract()));
        if(useBody && article.getBody()!=null)
            doc.addParagraph(parser.parseParagraph(article.getBody()));
        if(doc.getFirstParagraph()!=null)
            return doc;
        else
            return null;
    }

    protected String getSentenceKey(String docKey, int sentIndex){
        return docKey+"_"+sentIndex;
    }

    protected void writeLog(String content) {
        if (log != null) {
            log.write(content);
            log.flush();
        }
    }

    public void close(){
        if(log!=null)
            log.close();
        initialized=false;
    }
}