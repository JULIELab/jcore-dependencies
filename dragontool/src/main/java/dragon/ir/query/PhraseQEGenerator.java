package dragon.ir.query;

import dragon.ir.kngbase.KnowledgeBase;
import dragon.matrix.DoubleSparseMatrix;
import dragon.nlp.*;
import dragon.nlp.compare.*;
import dragon.nlp.extract.*;
import dragon.onlinedb.Article;
import dragon.util.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * <p>Phrase Query generator</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class PhraseQEGenerator extends AbstractQueryGenerator{
    private DoubleSparseMatrix translationMatrix;
    private SimpleElementList phraseKeyList, tokenKeyList;
    private PhraseExtractor phraseExtractor;
    private TokenExtractor tokenExtractor;
    private DocumentParser parser;
    private double transCoefficient;
    private int expandTermNum;
    private boolean useTitle, useAbt, useBody, useMeta;

    public PhraseQEGenerator(KnowledgeBase tokenKngBase, TokenExtractor tokenExtractor, double transCoefficient, int expandTermNum) {
       this(tokenKngBase,null,tokenExtractor,transCoefficient,expandTermNum);
    }

    public PhraseQEGenerator(KnowledgeBase phraseKngBase, PhraseExtractor phraseExtractor, TokenExtractor tokenExtractor, double transCoefficient, int expandTermNum) {
        this.translationMatrix=phraseKngBase.getKnowledgeMatrix();
        this.phraseKeyList =phraseKngBase.getRowKeyList();
        this.tokenKeyList =phraseKngBase.getColumnKeyList();
        this.phraseExtractor =phraseExtractor;
        if(phraseExtractor!=null){
            phraseExtractor.setSubConceptOption(false);
            phraseExtractor.setSingleAdjectiveOption(false);
            phraseExtractor.setSingleNounOption(false);
            phraseExtractor.setSingleVerbOption(false);
            parser=phraseExtractor.getDocumentParser();
        }
        else
            parser=tokenExtractor.getDocumentParser();
        this.tokenExtractor =tokenExtractor;
        this.transCoefficient =transCoefficient;
        this.expandTermNum=expandTermNum;

        useTitle=true;
        useAbt=false;
        useBody=false;
        useMeta=false;
    }

    public void initialize(boolean useTitle, boolean useAbt, boolean useBody, boolean useMeta){
        this.useTitle =useTitle;
        this.useAbt =useAbt;
        this.useBody =useBody;
        this.useMeta =useMeta;
    }

    public IRQuery generate(Article topic){
        return new RelSimpleQuery(genQueryString(genQuery(topic)));
    }

    private String genQueryString(ArrayList queryTerms){
        StringBuffer buf;
        Token curToken;
        DecimalFormat df;
        int i;

        buf=new StringBuffer();
        df=FormatUtil.getNumericFormat(1,3);
        for(i=0;i<queryTerms.size();i++){
            curToken=(Token)queryTerms.get(i);
            buf.append("T(");
            buf.append(df.format(curToken.getWeight()));
            buf.append(",TERM=");
            buf.append(curToken.getName());
            buf.append(") ");
        }
        return buf.toString().trim();
    }

    private ArrayList genQuery(Article article){
        Document doc;
        ArrayList list;
        SortedArray tokenList, phraseList;
        Phrase curPhrase;
        Token curToken, newToken, oldToken;
        double sum;
        int i, total, termNum;

        tokenList=new SortedArray();
        phraseList=new SortedArray();
        doc=getDocument(article);

        //extract and merge tokens from the query
        list=tokenExtractor.extractFromDoc(doc);
        total=0;
        for(i=0;i<list.size();i++){
            curToken=(Token)list.get(i);
            total+=curToken.getFrequency();
            if(!tokenList.add(curToken)){
                oldToken=(Token)tokenList.get(tokenList.insertedPos());
                oldToken.addFrequency(curToken.getFrequency());
            }
        }
        for(i=0;i<tokenList.size();i++){
            curToken=(Token)tokenList.get(i);
            curToken.setWeight(curToken.getFrequency()/(double)total);
        }
        termNum=tokenList.size()+expandTermNum;

        //extract and merge phrases from the query
        if(phraseExtractor!=null){
            list = phraseExtractor.extractFromDoc(doc);
            for (i = 0; i < list.size(); i++) {
                curPhrase = (Phrase) list.get(i);
                if (curPhrase.getStartingWord().equals(curPhrase.getEndingWord()))
                    continue;
                curToken = new Token(curPhrase.getName(), -1, curPhrase.getFrequency());
                if (!phraseList.add(curToken)) {
                    oldToken = (Token) phraseList.get(phraseList.insertedPos());
                    oldToken.addFrequency(curToken.getFrequency());
                }
            }
            list = translation(phraseList);
        }
        else
            list=translation(tokenList);
        if(list!=null){
            for(i=0;i<tokenList.size();i++){
                curToken = (Token) tokenList.get(i);
                curToken.setWeight(curToken.getWeight()*(1-transCoefficient));
            }

            for(i=0;i<list.size();i++){
                newToken=(Token)list.get(i);
                newToken.setWeight(newToken.getWeight()*transCoefficient);
                if(!tokenList.add(newToken)){
                    oldToken=(Token)tokenList.get(tokenList.insertedPos());
                    oldToken.setWeight(oldToken.getWeight()+newToken.getWeight());
                }
            }

            if(tokenList.size()>termNum){
                tokenList.setComparator(new WeightComparator(true));
                while(tokenList.size()>termNum)
                    tokenList.remove(tokenList.size()-1);
                //renormalize
                sum=0;
                for(i=0;i<tokenList.size();i++){
                    curToken=(Token)tokenList.get(i);
                    sum+=curToken.getWeight();
                }
                for(i=0;i<tokenList.size();i++){
                    curToken=(Token)tokenList.get(i);
                    curToken.setWeight(curToken.getWeight()/sum);
                }
            }
        }
        return tokenList;
    }

    private Document getDocument(Article article){
        Document doc;

        doc=new Document();
        if(useTitle)
            doc.addParagraph(parser.parseParagraph(article.getTitle()));
        if(useAbt)
            doc.addParagraph(parser.parseParagraph(article.getAbstract()));
        if(useBody)
            doc.addParagraph(parser.parseParagraph(article.getBody()));
        if(useMeta)
            doc.addParagraph(parser.parseParagraph(article.getMeta()));
        return doc;
    }

    private ArrayList translation(ArrayList phraseList){
        SortedArray tokenList;
        ArrayList newList;
        Token curToken, oldToken, newToken;
        int i, j, index, total, arrIndex[];
        double arrProb[];

        total=0;
        newList=new ArrayList();
        for(i=0;i<phraseList.size();i++){
            curToken=(Token)phraseList.get(i);
            index=phraseKeyList.search(curToken.getName());
            if(index>=0 && translationMatrix.getNonZeroNumInRow(index)>0){
                curToken.setIndex(index);
                total+=curToken.getFrequency();
                newList.add(curToken);
            }
        }

        if(newList.size()==0)
            return null;

        tokenList=new SortedArray(new IndexComparator());
        for(i=0;i<newList.size();i++){
            curToken=(Token)newList.get(i);
            arrIndex=translationMatrix.getNonZeroColumnsInRow(curToken.getIndex());
            arrProb=translationMatrix.getNonZeroDoubleScoresInRow(curToken.getIndex());
            for(j=0;j<arrIndex.length;j++){
                newToken=new Token(tokenKeyList.search(arrIndex[j]),arrIndex[j],0);
                newToken.setWeight(curToken.getFrequency()/(double)total*arrProb[j]);
                if(!tokenList.add(newToken)){
                    oldToken=(Token)tokenList.get(tokenList.insertedPos());
                    oldToken.setWeight(oldToken.getWeight()+newToken.getWeight());
                }
            }
        }
        return tokenList;
    }
}