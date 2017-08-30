package dragon.ir.summarize;

import dragon.nlp.*;
import dragon.nlp.compare.*;
import dragon.nlp.extract.*;
import dragon.nlp.tool.*;
import dragon.util.*;
import java.util.*;
import java.util.ArrayList;

/**
 * <p>A Program for Summarizaiton Evaluation</p>
 * <p>We only implemented and tested the ROUGE-N metric so far</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang, Davis Zhou
 * @version 1.0
 */

public class ROUGE {
    public static final int ROUGE_N=1;
    public static final int ROUGE_L=2;
    public static final int ROUGE_W=3;
    public static final int ROUGE_S=4;
    public static final int ROUGE_SU=5;
    public static final int MULTIPLE_MAX=1;
    public static final int MULTIPLE_MIN=2;
    public static final int MULTIPLE_AVG=3;
    private static final String stopwordFile="nlpdata/rouge/rouge.stopword";
    //private static final String wordDelimitor=" \r\n\t_-;,?/\"'`:(){}!+[]><=%$#*@&^~|\\";

    private TokenExtractor tokenExtractor;
    private double beta; //control the f-score
    private double[][] evaStat; //store evaluation result
    private int metric; //ROUGE metrics
    private int multipleMode;
    private int gram; //used for ROUGE-N metric
    private int maxSkip; //used for ROUGE-S metric
    private boolean caseSensitive;

    public ROUGE() {
        caseSensitive=false;
        tokenExtractor=new BasicTokenExtractor(null);
        beta=1.0;
        metric=ROUGE_N;
        gram=2;
    }

    public void setBeta(double beta){
        if(beta>0)
            this.beta =beta;
    }

    public double getBeta(){
        return beta;
    }

    public void setLemmatiser(Lemmatiser lemmatiser){
        tokenExtractor.setLemmatiser(lemmatiser);
    }

    public Lemmatiser getLemmatiser(){
        return tokenExtractor.getLemmatiser();
    }

    public void setLemmatiserOption(boolean option){
        if(option)
            tokenExtractor.setLemmatiser(new PorterStemmer());
        else
            tokenExtractor.setLemmatiser(null);
    }

    public boolean getLemmatiserOption(){
        return tokenExtractor.getLemmatiser()!=null;
    }

    public void setMultipleReferenceMode(int mode){
        this.multipleMode =mode;
    }

    public void setStopwordOption(boolean option){
        if (option)
            tokenExtractor.setConceptFilter(new BasicConceptFilter(EnvVariable.getDragonHome()+"/"+stopwordFile));
        else
            tokenExtractor.setFilteringOption(false);
    }

    public boolean getStopwordOption(){
        return tokenExtractor.getFilteringOption();
    }

    public void setStopwordFile(String stopwordFile){
        tokenExtractor.setConceptFilter(new BasicConceptFilter(stopwordFile));
    }

    public void setCaseOption(boolean sensitive){
        caseSensitive=sensitive;
    }

    public boolean getCaseOption(){
        return caseSensitive;
    }

    public void useRougeN(int gram){
        this.gram=gram;
        metric=ROUGE_N;
    }

    public int getGram(){
        return gram;
    }

    public void useRougeS(){
        this.maxSkip =Integer.MAX_VALUE;
        metric=ROUGE_S;
    }

    public void useRougeS(int maxSkip){
        this.maxSkip =maxSkip;
        metric=ROUGE_S;
    }

    public double getPrecision(){
        return getEvaResult(1);
    }

    public double getRecall(){
        return getEvaResult(0);
    }

    public double getFScore(){
        return getEvaResult(2);
    }

    private double getEvaResult(int dimension){
        double[] results;
        int i;

        results=new double[evaStat.length];
        for(i=0;i<results.length;i++)
            results[i]=evaStat[i][dimension];
        if(multipleMode==MULTIPLE_MAX)
            return MathUtil.max(results);
        else if(multipleMode==MULTIPLE_AVG)
            return MathUtil.average(results);
        else if(multipleMode==MULTIPLE_MIN)
            return MathUtil.min(results);
        else
            return -1;
    }

    public synchronized boolean evaluate(String testSummary, String[] refSummaries){
        boolean ret;

        ret=true;
        if(metric==ROUGE_N)
            computeRougeN(testSummary,refSummaries);
        else if(metric==ROUGE_S)
            computeRougeS(testSummary,refSummaries);
        else if(metric==ROUGE_L)
            computeRougeL(testSummary,refSummaries);
        else if(metric==ROUGE_SU)
            computeRougeSU(testSummary,refSummaries);
        else
            ret=false;
        return ret;
    }

    public void printResult() {
        int j, k;
        for (k = 0; k < 50; k++) {
            System.out.print("-");
        }
        System.out.println();
        for (j = 0; j < evaStat.length; j++) {
            System.out.println("ReferenceModel: " + (j + 1));
            System.out.println("Average_R: " + evaStat[j][0]);
            System.out.println("Average_P: " + evaStat[j][1]);
            System.out.println("Average_F: " + evaStat[j][2]);
            System.out.println();
        }
        for (k = 0; k < 50; k++) {
            System.out.print("-");
        }
        System.out.println();
    }

    private void computeRougeN(String testSummary, String[] refSummaries) {
        HashMap testHash, refHash;
        ArrayList testList, referenceList;
        int match, reference, test, j;

        testList=tokenize(testSummary);
        evaStat = new double[refSummaries.length][3];
        testHash=computeNgrams(testList,gram);
        test=testList.size()-gram+1;

        for (j = 0; j < refSummaries.length; j++) {
            referenceList=tokenize(refSummaries[j]);
            refHash = computeNgrams(referenceList, gram);
            match = matchNgrams(testHash,refHash);
            reference=referenceList.size()-gram+1;

            if (reference<=0)
                evaStat[j][0] = 0;
            else
                evaStat[j][0] = (double) match / reference;

            if(test<=0)
                evaStat[j][1] =0;
            else
                evaStat[j][1] =match/(double)test;

            evaStat[j][2] =computeFScore(evaStat[j][1],evaStat[j][0]);
        }
    }

    private void computeRougeS(String testSummary, String[] refSummaries) {
        HashSet hashGrams;
        SimpleElementList keyList;
        ArrayList testList, referenceList;
        int match, reference, test, j;

        keyList=new SimpleElementList();
        testList=index(tokenize(testSummary),keyList);
        test=countSkipBigram(testList.size(),maxSkip);
        evaStat = new double[refSummaries.length][3];

        for (j = 0; j < refSummaries.length; j++) {
            referenceList=index(tokenize(refSummaries[j]),keyList);
            hashGrams = computeSkipBigram(referenceList, maxSkip);
            match = matchSkipBigram(testList,maxSkip, hashGrams);
            reference=countSkipBigram(testList.size(),maxSkip);

            if (reference<=0)
                evaStat[j][0] = 0;
            else
                evaStat[j][0] = (double) match / reference;

            if(test<=0)
                evaStat[j][1] =0;
            else
                evaStat[j][1] =match/(double)test;

            evaStat[j][2] =computeFScore(evaStat[j][1],evaStat[j][0]);
        }
    }

    private void computeRougeSU(String testSummary, String[] refSummaries) {
        HashSet hashGrams;
        SimpleElementList keyList;
        ArrayList testList, referenceList;
        int match, reference, test, j;

        keyList=new SimpleElementList();
        testList=index(tokenize(testSummary),keyList);
        test=countSkipBigram(testList.size(),maxSkip)+testList.size();
        evaStat = new double[refSummaries.length][3];

        for (j = 0; j < refSummaries.length; j++) {
            referenceList=index(tokenize(refSummaries[j]),keyList);
            hashGrams = computeSkipBigram(referenceList, maxSkip);
            match = matchSkipBigram(testList,maxSkip, hashGrams);
            reference=countSkipBigram(testList.size(),maxSkip)+referenceList.size();

            if (reference<=0)
                evaStat[j][0] = 0;
            else
                evaStat[j][0] = (double) match / reference;

            if(test<=0)
                evaStat[j][1] =0;
            else
                evaStat[j][1] =match/(double)test;

            evaStat[j][2] =computeFScore(evaStat[j][1],evaStat[j][0]);
        }
    }

    private void computeRougeL(String testSummary, String[] refSummaries) {
        Document testDoc, refDoc;
        Sentence curSent;
        Paragraph curPara;
        DocumentParser parser;
        int match, reference, test, j;

        parser=tokenExtractor.getDocumentParser();
        testDoc=parser.parse(testSummary);
        test=tokenize(testDoc).size();
        evaStat = new double[refSummaries.length][3];

        for (j = 0; j < refSummaries.length; j++) {
            match=0;
            refDoc=parser.parse(refSummaries[j]);
            curPara=refDoc.getFirstParagraph();
            while (curPara != null) {
                curSent = curPara.getFirstSentence();
                while (curSent != null) {
                    match+=matchLCS(curSent,testDoc);
                    curSent = curSent.next;
                }
                curPara = curPara.next;
            }
            reference=tokenize(refDoc).size();

            if (reference<=0)
                evaStat[j][0] = 0;
            else
                evaStat[j][0] = (double) match / reference;

            if(test<=0)
                evaStat[j][1] =0;
            else
                evaStat[j][1] =match/(double)test;

            evaStat[j][2] =computeFScore(evaStat[j][1],evaStat[j][0]);
        }
    }

    private HashMap computeNgrams(ArrayList wordList, int nGram) {
        HashMap hashGrams;
        Counter counter;
        String gramStr;
        int start, end;

        start = 0;
        end = nGram;
        hashGrams=new HashMap();
        while (end <= wordList.size()) {
            gramStr=getNgram(wordList,start,end);
            counter=(Counter)hashGrams.get(gramStr);
            if(counter!=null)
                counter.addCount(1);
            else
                hashGrams.put(gramStr,new Counter(1));
            start = start + 1;
            end = end + 1;
        }
        return hashGrams;
    }

    private int matchNgrams(HashMap testHash, HashMap refMap) {
        Iterator iterator;
        String gramStr;
        Counter testCounter, refCounter;
        int count;

        count=0;
        iterator=testHash.keySet().iterator();
        while(iterator.hasNext()){
            gramStr=(String)iterator.next();
            testCounter=(Counter)testHash.get(gramStr);
            refCounter=(Counter)refMap.get(gramStr);
            if(refCounter!=null)
                count+=Math.min(testCounter.getCount(),refCounter.getCount());
        }
        return count;
    }

    private String getNgram(ArrayList wordList, int start, int end){
        String gramStr;
        int i;

        gramStr =null;
        for (i = start; i < end; i++) {
            if (i == 0)
                gramStr =( (Token) wordList.get(i)).getName();
            else
                gramStr = gramStr + "\t"+( (Token) wordList.get(i)).getName();
        }
        return gramStr;
    }

    private HashSet computeSkipBigram(ArrayList list, int maxSkip){
        HashSet hash;
        int i, start, end, first, second;

        hash=new HashSet();
        start=0;
        end=Math.min(start+maxSkip+1,list.size()-1);
        while(start<end){
            first=((Token)list.get(start)).getIndex();
            for(i=start+1;i<=end;i++){
                second=((Token)list.get(i)).getIndex();
                hash.add(new SimplePair(hash.size(),first,second));
            }
            start=start+1;
            end=Math.min(start+maxSkip+1,list.size()-1);
        }
        return hash;
    }

    private int matchSkipBigram(ArrayList list, int maxSkip, HashSet reference){
        int i, start, end, first, second, count;

        start=0;
        count=0;
        end=Math.min(start+maxSkip+1,list.size()-1);
        while(start<end){
            first=((Token)list.get(start)).getIndex();
            for(i=start+1;i<=end;i++){
                second=((Token)list.get(i)).getIndex();
                if(reference.contains(new SimplePair(-1,first,second)))
                    count++;
            }
            start=start+1;
            end=Math.min(start+maxSkip+1,list.size()-1);
        }
        return count;
    }

    private int countSkipBigram(int textLength, int maxSkip){
        int start, end, count;

        start=0;
        count=0;
        end=Math.min(start+maxSkip+1,textLength-1);
        while(start<end){
            count+=end-start;
            start=start+1;
            end=Math.min(start+maxSkip+1,textLength-1);
        }
        return count;
    }

    private int matchLCS(Sentence refSent, Document testDoc){
        SortedArray list;
        ArrayList refList, testList, lcsList;
        SimpleElementList keyList;
        Sentence curSent;
        Paragraph curPara;
        int i;

        keyList=new SimpleElementList();
        list=new SortedArray(new IndexComparator());
        refList=index(tokenize(refSent),keyList);
        curPara=testDoc.getFirstParagraph();
        while(curPara!=null){
            curSent=curPara.getFirstSentence();
            while(curSent!=null){
                testList=index(tokenize(curSent),keyList);
                lcsList=computeLCS(refList,testList);
                for(i=0;i<lcsList.size();i++)
                    list.add(lcsList.get(i));
                curSent=curSent.next;
            }
            curPara=curPara.next;
        }
        return list.size();
    }
    private ArrayList computeLCS(ArrayList first, ArrayList second){
        return null;
    }

    private ArrayList index(ArrayList list,SimpleElementList keyList){
        Token curToken;
        int i;

        for(i=0;i<list.size();i++){
            curToken=(Token)list.get(i);
            curToken.setIndex(keyList.add(curToken.getValue()));
        }
        return list;
    }

    private ArrayList tokenize(String doc){
        ArrayList list;
        Token curToken;
        int i;

        list=tokenExtractor.extractFromDoc(doc);
        if(!caseSensitive){
            //convert all words to lower case
            for(i=0;i<list.size();i++){
                curToken=(Token)list.get(i);
                curToken.setValue(curToken.getValue().toLowerCase());
            }
        }
        return list;
    }

    private ArrayList tokenize(Document doc){
        ArrayList list;
        Token curToken;
        int i;

        list=tokenExtractor.extractFromDoc(doc);
        if(!caseSensitive){
            //convert all words to lower case
            for(i=0;i<list.size();i++){
                curToken=(Token)list.get(i);
                curToken.setValue(curToken.getValue().toLowerCase());
            }
        }
        return list;
    }

    private ArrayList tokenize(Sentence sent){
        ArrayList list;
        Token curToken;
        int i;

        list=tokenExtractor.extractFromSentence(sent);
        if(!caseSensitive){
            //convert all words to lower case
            for(i=0;i<list.size();i++){
                curToken=(Token)list.get(i);
                curToken.setValue(curToken.getValue().toLowerCase());
            }
        }
        return list;
    }

    private double computeFScore(double precision, double recall){
        if(precision==0 || recall==0)
            return 0;
        else if(beta==Double.MAX_VALUE)
            return recall;
        else
            return (1+beta*beta)*precision*recall/(recall+beta*beta*precision);
    }
}