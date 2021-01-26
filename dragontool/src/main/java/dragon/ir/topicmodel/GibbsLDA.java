package dragon.ir.topicmodel;

import dragon.ir.index.IndexReader;
import dragon.ir.index.sequence.SequenceIndexReader;

import java.util.Random;

/**
 * <p>Latent dirichlet allocation (LDA) with Gibbs sampling </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class GibbsLDA extends AbstractTopicModel {
    private int indexType; //0: basic 1: seq;
    private int tokenNum;
    private double alpha, beta, wBeta, tAlpha;

    public GibbsLDA(SequenceIndexReader indexReader, double alpha, double beta) {
        this(indexReader,alpha,beta,1);
    }

    public GibbsLDA(IndexReader indexReader, double alpha, double beta){
        this(indexReader,alpha,beta,0);
    }

    private GibbsLDA(IndexReader indexReader, double alpha, double beta, int indexType) {
        super(indexReader);

        this.alpha =alpha;
        this.beta=beta;
        this.iterations=500;
        termNum = indexReader.getCollection().getTermNum();
        tokenNum = (int) indexReader.getCollection().getTermCount();
        docNum = indexReader.getCollection().getDocNum();
        wBeta = termNum*beta;
        this.indexType=indexType;
    }

    public boolean estimateModel(int topicNum) {
        int[][] arrTTCount, arrDTCount;
        int[] arrTerm, arrDoc, arrTermLabel;
        int i, j;
        double topicSum, docSum;

        this.themeNum =topicNum;
        tAlpha=themeNum*alpha;
        arrTTCount = new int[termNum][themeNum];
        arrDTCount = new int[docNum][themeNum];
        arrTerm = new int[tokenNum];
        arrDoc = new int[tokenNum];
        arrTermLabel=new int[tokenNum];

        //read the sequence of terms from the corpus
        printStatus(new java.util.Date().toString()+" Reading sequence...");
        if(indexType==0)
            readSequence(indexReader, arrTerm,arrDoc);
        else
            readSequence((SequenceIndexReader)indexReader, arrTerm,arrDoc);

        //run one makov-chain
        run(seed,arrTerm,arrDoc,arrTermLabel,arrTTCount, arrDTCount);

        //estimate topic distribution
        arrThemeTerm=new double[themeNum][termNum];
        for(i=0;i<themeNum;i++){
            topicSum=wBeta;
            for(j=0;j<termNum;j++)
                topicSum+=arrTTCount[j][i];
            for(j=0;j<termNum;j++)
                arrThemeTerm[i][j]=(arrTTCount[j][i]+beta)/topicSum;
        }

        //estimate document distribution
        arrDocTheme=new double[docNum][themeNum];
        for(i=0;i<docNum;i++){
            docSum=tAlpha;
            for(j=0;j<themeNum;j++)
                docSum+=arrDTCount[i][j];
            for(j=0;j<themeNum;j++)
                arrDocTheme[i][j]=(arrDTCount[i][j]+alpha)/docSum;
        }
        return true;
    }

    private void run(int seed, int[] arrTerm, int[] arrDoc, int[] arrTermLabel, int[][] arrTTCount, int[][] arrDTCount){
        Random random;
        int[] arrTopicCount, arrOrder;
        int termIndex, docIndex, topic;
        int i, j, k,iter;

        //initiliazation
        arrTopicCount=new int[themeNum];
        arrOrder=new int[tokenNum];
        random=new Random();
        if(seed>=0)
            random.setSeed(seed);

        printStatus(new java.util.Date().toString()+" Starting random initialization...");
        for(i=0;i<tokenNum;i++){
            topic=random.nextInt(themeNum);
            arrTermLabel[i]=topic;
            termIndex=arrTerm[i];
            docIndex=arrDoc[i];
            arrTTCount[termIndex][topic]++;
            arrDTCount[docIndex][topic]++;
            arrTopicCount[topic]++;
        }

        //Determine random update sequence
        printStatus(new java.util.Date().toString()+" Determining random update sequence...");
        for (i=0; i<tokenNum; i++) arrOrder[i]=i;
        for (i = 0; i < (tokenNum - 1); i++) {
            // pick a random integer between i and n
            k = i +random.nextInt(tokenNum-i);
            // switch contents on position i and position k
            j = arrOrder[k];
            arrOrder[k] = arrOrder[i];
            arrOrder[i] = j;
        }

        for (iter = 0; iter <iterations; iter++) {
            printStatus(new java.util.Date().toString()+" Iteration #"+(iter+1));
            for (k = 0; k <tokenNum; k++) {
                i = arrOrder[k]; // current word token to assess
                termIndex = arrTerm[i];
                docIndex =arrDoc[i];
                topic = arrTermLabel[i];

                // substract the current instance from counts
                arrTopicCount[topic]--;
                arrTTCount[termIndex][topic]--;
                arrDTCount[docIndex][topic]--;

                // sample a topic from the distribution for the current instance
                topic=sampleTopic(random,arrTTCount[termIndex],arrDTCount[docIndex],arrTopicCount);

                //update counts
                arrTermLabel[i] = topic;
                arrTTCount[termIndex][topic]++;
                arrDTCount[docIndex][topic]++;
                arrTopicCount[topic]++;
            }
        }
    }

    private int sampleTopic(Random random, int[] arrTTCount, int[] arrDTCount, int[] arrTopicCount){
        double[] arrProb;
        double totalProb, r, max;
        int j,topic;

        totalProb = 0;
        arrProb=new double[themeNum];
        for (j = 0; j < themeNum; j++) {
            arrProb[j] = (arrTTCount[j] + beta)/(arrTopicCount[j] + wBeta) *(arrDTCount[j]+ alpha);
            totalProb += arrProb[j];
        }
        r = totalProb * random.nextDouble();
        max = arrProb[0];
        topic = 0;
        while (r > max) {
            topic++;
            max += arrProb[topic];
        }
        return topic;
    }

    private void readSequence(SequenceIndexReader indexReader, int[] arrTerm, int[] arrDoc){
        int[] arrSeq;
        int docNum, i, j, count;

        docNum=indexReader.getCollection().getDocNum();
        count=0;
        for(i=0;i<docNum;i++){
            arrSeq=indexReader.getTermIndexList(i);
            if(arrSeq==null || arrSeq.length==0) continue;
            for(j=0;j<arrSeq.length;j++){
                arrTerm[count+j]=arrSeq[j];
                arrDoc[count+j]=i;
            }
            count+=arrSeq.length;
        }
    }

    private void readSequence(IndexReader indexReader, int[] arrTerm, int[] arrDoc){
        int[] arrIndex, arrFreq;
        int docNum, i, j, k, count;

        docNum=indexReader.getCollection().getDocNum();
        count=0;
        for(i=0;i<docNum;i++){
            arrIndex=indexReader.getTermIndexList(i);
            arrFreq=indexReader.getTermFrequencyList(i);
            if(arrIndex==null || arrIndex.length==0) continue;
            for(j=0;j<arrIndex.length;j++){
                for(k=0;k<arrFreq[j];k++){
                    arrTerm[count + k] = arrIndex[j];
                    arrDoc[count + k] = i;
                }
                count+=arrFreq[j];
            }
        }
    }
}