package dragon.nlp.tool.xtract;

import dragon.matrix.IntSuperSparseMatrix;
import dragon.nlp.*;
import dragon.nlp.extract.EngDocumentParser;
import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.Tagger;
import dragon.onlinedb.Article;
import dragon.onlinedb.CollectionReader;

import java.io.File;

/**
 * <p>Indexer for word pair</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class EngWordPairIndexer implements WordPairIndexer{
    protected int maxSpan;
    protected Tagger tagger;
    protected Lemmatiser lemmatiser;
    protected WordPairGenerator pairGenerator;
    protected SentenceBase sentenceBase;
    protected IntSuperSparseMatrix[] arrPairSentLeftMatrix, arrPairSentRightMatrix;
    protected SimpleElementList docKeyList, wordKeyList;
    protected SimplePairList pairKeyList;
    protected WordPairStatList wordpairStatList;
    protected DocumentParser parser;
    protected int flushInterval, indexedNum;

    public EngWordPairIndexer(String folder, int maxSpan, Tagger tagger, Lemmatiser lemmatiser){
    	this(folder,maxSpan,tagger,lemmatiser,new EngWordPairGenerator(maxSpan));
    }
    
    public EngWordPairIndexer(String folder, int maxSpan, Tagger tagger, Lemmatiser lemmatiser, WordPairGenerator pairGenerator) {
        this.maxSpan =maxSpan;
        this.tagger =tagger;
        this.lemmatiser =lemmatiser;
        this.pairGenerator =pairGenerator;
        this.flushInterval=10000;

        (new File(folder)).mkdirs();
        parser=new EngDocumentParser();
        sentenceBase=new SentenceBase(folder+"/sentencebase.index",folder+"/sentencebase.matrix");
        docKeyList=new SimpleElementList(folder+"/dockey.list",true);
        wordKeyList=new SimpleElementList(folder+"/wordkey.list",true);
        pairKeyList=new SimplePairList(folder+"/pairkey.list",true);
        wordpairStatList=new WordPairStatList(folder+"/pairstat.list",maxSpan,true);
        arrPairSentRightMatrix=new IntSuperSparseMatrix[maxSpan];
        for(int i=1;i<=maxSpan;i++){
            arrPairSentRightMatrix[i-1] = new IntSuperSparseMatrix(folder + "/pairsentr" + i + ".index",
                                              folder + "/pairsentr" + i + ".matrix", false, false);
            arrPairSentRightMatrix[i-1].setFlushInterval(Integer.MAX_VALUE);
        }
        arrPairSentLeftMatrix=new IntSuperSparseMatrix[maxSpan];
        for(int i=1;i<=maxSpan;i++){
            arrPairSentLeftMatrix[i-1] = new IntSuperSparseMatrix(folder + "/pairsentl" + i + ".index",
                                              folder + "/pairsentl" + i + ".matrix", false, false);
            arrPairSentLeftMatrix[i-1].setFlushInterval(Integer.MAX_VALUE);
        }
    }

    public DocumentParser getDocumentParser(){
        return parser;
    }

    public void setDocumentParser(DocumentParser parser){
        this.parser = parser;
    }

    public void close(){
        int i;

        sentenceBase.close();
        docKeyList.close();
        wordKeyList.close();
        wordpairStatList.close();
        pairKeyList.close();
        for(i=0;i<maxSpan;i++){
            arrPairSentRightMatrix[i].finalizeData();
            arrPairSentRightMatrix[i].close();
        }
        for(i=0;i<maxSpan;i++){
            arrPairSentLeftMatrix[i].finalizeData();
            arrPairSentLeftMatrix[i].close();
        }
    }

    public void flush(){
        int i;

        for(i=0;i<maxSpan;i++)
            arrPairSentRightMatrix[i].flush();
        for(i=0;i<maxSpan;i++)
            arrPairSentLeftMatrix[i].flush();
    }

    public void index(CollectionReader collectionReader){
        Article curArticle;

        try{
            indexedNum=0;
            curArticle = collectionReader.getNextArticle();
            while (curArticle != null) {
                if(indexedNum>0 && indexedNum%flushInterval==0)
                    flush();
                indexArticle(curArticle);
                indexedNum++;
                curArticle = collectionReader.getNextArticle();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean indexArticle(Article curArticle){
        Document curDoc;
        Paragraph curParagraph;
        Sentence curSent;

        try{
            if(docKeyList.contains(curArticle.getKey()))
               return true;

            System.out.println(new java.util.Date().toString()+" "+curArticle.getKey());

            docKeyList.add(curArticle.getKey());
            curDoc=new Document();
            curDoc.addParagraph(parser.parseParagraph(curArticle.getTitle()));
            curDoc.addParagraph(parser.parseParagraph(curArticle.getAbstract()));
            curDoc.addParagraph(parser.parseParagraph(curArticle.getBody()));
            curParagraph=curDoc.getFirstParagraph();
            while(curParagraph!=null){
                curSent=curParagraph.getFirstSentence();
                while(curSent!=null){
                    indexSentence(curSent);
                    curSent=curSent.next;
                }
                curParagraph=curParagraph.next;
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean indexSentence(Sentence sent){
        WordPairStat curPair;
        int i,j, num,sentIndex;

        try{
            if(sent.getWordNum()<2)
                return true;

            //preprocess a sentence including tagging, lemmatising and indexing
            preprocessSentence(sent);


            //generate and save word pairs
            num=pairGenerator.generate(sent);
            //output the sentence
            if(num>0)
                 sentIndex=sentenceBase.addSentence(sent);
             else
                 return true;
            for(i=0;i<num;i++){
                curPair=pairGenerator.getWordPairs(i);
                curPair.setIndex(pairKeyList.add(curPair.getFirstWord(),curPair.getSecondWord()));
                wordpairStatList.add(curPair);
                for(j=1;j<=maxSpan;j++)
                {
                    if(curPair.getFrequency(j)>0)
                        arrPairSentRightMatrix[j-1].add(curPair.getIndex(),sentIndex, curPair.getFrequency(j));
                }
                for(j=1;j<=maxSpan;j++)
                {
                    if(curPair.getFrequency(-j)>0)
                        arrPairSentLeftMatrix[j-1].add(curPair.getIndex(),sentIndex, curPair.getFrequency(-j));
                }
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Preprocess a sentence including tagging, lemmatising and indexing. This method can be overrided.
     * @param sent the sentence for preprocessing
     */
    protected void preprocessSentence(Sentence sent){
        Word cur;

        //part of speech tagging
        if(tagger!=null) tagger.tag(sent);

        //lemmatising and indexing words
        cur=sent.getFirstWord();
        while(cur!=null){
            if(cur.getPOSIndex()==Tagger.POS_NOUN)
                if(lemmatiser!=null)
                    cur.setLemma(lemmatiser.lemmatize(cur.getContent(),Tagger.POS_NOUN));
                else
                    cur.setLemma(cur.getContent().toLowerCase());
            else
                cur.setLemma(cur.getContent().toLowerCase());
            cur.setIndex(wordKeyList.add(cur.getLemma()));
            cur=cur.next;
        }
    }
}