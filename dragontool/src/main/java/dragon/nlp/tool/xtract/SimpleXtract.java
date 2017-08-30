package dragon.nlp.tool.xtract;

import dragon.nlp.*;
import dragon.nlp.extract.*;
import dragon.nlp.tool.*;
import dragon.onlinedb.*;
import dragon.util.*;
import java.io.*;
import java.util.ArrayList;

/**
 * <p>Simple Xtractor for extracting phrases from text </p>
 * <p>Smadja, F. Retrieving collocations from text: Xtract. Computational Linguistics, 1993, 19(1), pp. 143--177. </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SimpleXtract {
    private int maxSpan;
    private String indexFolder;

    
    public SimpleXtract(int maxSpan, String workingDir) {
        this.maxSpan =maxSpan;
        this.indexFolder=workingDir;
    }

    public void index(CollectionReader cr, Tagger tagger, Lemmatiser lemmatiser){
        index(cr,tagger,lemmatiser);
    }

    public void index(CollectionReader cr, Tagger tagger, Lemmatiser lemmatiser, String wordDelimitor){
        CollectionReader[] arrReader;

        arrReader=new CollectionReader[1];
        arrReader[0]=cr;
        index(arrReader,tagger,lemmatiser, wordDelimitor);
    }

    public void index(CollectionReader[] cr, Tagger tagger, Lemmatiser lemmatiser){
        index(cr,tagger,lemmatiser,null);
    }

    public void index(CollectionReader[] cr, Tagger tagger, Lemmatiser lemmatiser, String wordDelimitor){
        WordPairIndexer indexer;
        int i;

        indexer=new EngWordPairIndexer(indexFolder,maxSpan, tagger,lemmatiser);
        if(wordDelimitor!=null && wordDelimitor.length()>0)
            indexer.setDocumentParser(new EngDocumentParser(wordDelimitor));
        for(i=0;i<cr.length;i++)
            indexer.index(cr[i]);
        indexer.close();
    }

    public void index(CollectionReader cr, WordPairIndexer indexer){
        CollectionReader arrReader[];

        arrReader=new CollectionReader[1];
        arrReader[0]=cr;
        index(arrReader,indexer);
    }

    public void index(CollectionReader[] cr, WordPairIndexer indexer){
        int i;

        for(i=0;i<cr.length;i++)
            indexer.index(cr[i]);
        indexer.close();
    }

    public void extract(double minStrength, double minSpread, double minZScore, double minExpandRatio, String outputFile){
        WordPairExpand expander;

        expander=new EngWordPairExpand(maxSpan,indexFolder, minExpandRatio);
        extract(expander,minStrength,minSpread,minZScore,outputFile);
    }

    public void extract(WordPairExpand expander, double minStrength, double minSpread, double minZScore, String outputFile){
        WordPairStat[] arrStat;
        SortedArray phraseList;
        PrintWriter screen;
        int i, j;

        phraseList=new SortedArray();
        screen=FileUtil.getScreen();
        arrStat=filter(minStrength,minSpread,minZScore);
        for(i=0;i<arrStat.length ;i++){
            for(j=1;j<maxSpan;j++){
                if(arrStat[i].getFrequency(j)>0)
                    addPhrase(expander.expand(arrStat[i],j),phraseList,screen);
                if(arrStat[i].getFrequency(-j)>0)
                    addPhrase(expander.expand(arrStat[i],-j),phraseList,screen);
            }
        }
        //save phrases to a textual file
        printPhrase(phraseList,outputFile);
    }

    private void addPhrase(ArrayList inputList, SortedArray phraseList, PrintWriter screen){
        Token token, old;
        int i;

        if(inputList==null)
            return;
        for(i=0;i<inputList.size();i++){
            token=(Token)inputList.get(i);
            if(!phraseList.add(token)){
                old=(Token) phraseList.get(phraseList.insertedPos());
                old.setFrequency(Math.max(old.getFrequency(),token.getFrequency()));
            }
            screen.println(token.getValue()+"  "+token.getFrequency());
        }
    }

    private WordPairStat[] filter(double minStrength, double minSpread, double minZScore){
        WordPairFilter pairFilter;
        WordPairStat arrStat[];

        pairFilter=new WordPairFilter(indexFolder,maxSpan,minStrength,minSpread,minZScore);
        arrStat=pairFilter.execute();
        return arrStat;
    }

    private void printPhrase(SortedArray list, String filename){
        BufferedWriter bw;
        Token token;
        int i;

        try{
            bw=FileUtil.getTextWriter(filename);
            bw.write(list.size()+"\n");
            for(i=0;i<list.size();i++){
                token=(Token)list.get(i);
                bw.write(token.getValue());
                bw.write('\t'+token.getFrequency());
                bw.write('\n');
                bw.flush();
            }
            bw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void printWordPair(WordPairStat[] arrStat, String filename){
        SortedArray pairList;
        SimpleElementList wordkeyList;
        String pair;
        int i;

        try{
            wordkeyList=new SimpleElementList(indexFolder+"/wordkey.list",false);
            pairList=new SortedArray();
            for(i=0;i<arrStat.length;i++){
                pair=wordkeyList.search(arrStat[i].getFirstWord())+" "+wordkeyList.search(arrStat[i].getSecondWord());
                pairList.add(pair.toLowerCase());
            }
            printPhrase(pairList,filename);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}