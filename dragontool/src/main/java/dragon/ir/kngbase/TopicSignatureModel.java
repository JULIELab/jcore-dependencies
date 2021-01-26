package dragon.ir.kngbase;

import dragon.ir.index.IRSignatureIndexList;
import dragon.matrix.DoubleSuperSparseMatrix;
import dragon.matrix.IntSparseMatrix;
import dragon.nlp.Counter;
import dragon.nlp.Token;
import dragon.util.MathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * <p>A program for topic signature model estimation</p>
 * <p>This program finds a set of weighted terms to represent the semantics of a topic signature. A topic signature
 * can be anything here including multiword phrases, concept pairs, and individual terms. There are two approaches
 * to thee model estimation. One is the maximum likelihood estimator. The other uses EM algoritm if one can provide
 * the disbribution of individual terms on a corpus. See more details about the EM algorithm in our previous work.<br><br>
 * Zhou, X., Zhang, X., and Hu, X., <em>Semantic Smoothing of Document Models for Agglomerative Clustering</em>,
 * In the Twentieth International Joint Conference on Artificial Intelligence(IJCAI 07), Hyderabad, India, Jan 6-12,
 * 2007, pp. 2928-2933<br><br>One should provide indices for topic signatures and individual terms, respectively, or
 * give the occurrence matrix of topic signatures and individual terms.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TopicSignatureModel{
    private IRSignatureIndexList srcIndexList;
    private IRSignatureIndexList destIndexList;
    private IntSparseMatrix srcSignatureDocMatrix;
    private IntSparseMatrix destDocSignatureMatrix;
    private IntSparseMatrix cooccurMatrix;
    private boolean useDocFrequency;
    private boolean useMeanTrim;
    private boolean useEM;
    private double probThreshold;
    private double bkgCoeffi;
    private int[] buf;
    private int iterationNum;
    private int totalDestSignatureNum;
    private int DOC_THRESH;

    /**
     * The constructor for the mode of the maximum likelihood estimator
     * @param srcIndexList the statisitcs of topic signatures in the collection.
     * @param srcSignatureDocMatrix the doc-term matrix for topic signatures.
     * @param destDocSignatureMatrix the doc-term matrix for individual terms topic signatures will translate to.
     */
    public TopicSignatureModel(IRSignatureIndexList srcIndexList, IntSparseMatrix srcSignatureDocMatrix, IntSparseMatrix destDocSignatureMatrix) {
        this.srcIndexList =srcIndexList;
        this.srcSignatureDocMatrix=srcSignatureDocMatrix;
        this.destDocSignatureMatrix=destDocSignatureMatrix;
        useDocFrequency=true;
        useMeanTrim=true;
        probThreshold=0.001;
        useEM=false;
        iterationNum =15;
        bkgCoeffi =0.5;
        totalDestSignatureNum=destDocSignatureMatrix.columns();
    }

    /**
     * The constructor for the mode of the maximum likelihood estimator
     * @param srcIndexList the statisitcs of topic signatures in the collection.
     * @param cooccurMatrix the cooccurence matrix of topic signatures and individual terms.
     */
    public TopicSignatureModel(IRSignatureIndexList srcIndexList, IntSparseMatrix cooccurMatrix) {
        this.srcIndexList =srcIndexList;
        this.cooccurMatrix =cooccurMatrix;
        useMeanTrim=true;
        probThreshold=0.001;
        useEM=false;
        iterationNum =15;
        bkgCoeffi =0.5;
        totalDestSignatureNum=cooccurMatrix.columns();
    }

    /**
     * The constructor for the mode of EM algorithm
     * @param srcIndexList the statisitcs of topic signatures in the collection.
     * @param destIndexList the statisitcs of individual terms in the collection.
     * @param cooccurMatrix the cooccurence matrix of topic signatures and individual terms.
     */
    public TopicSignatureModel(IRSignatureIndexList srcIndexList, IRSignatureIndexList destIndexList, IntSparseMatrix cooccurMatrix) {
        this.srcIndexList =srcIndexList;
        this.destIndexList =destIndexList;
        this.cooccurMatrix =cooccurMatrix;
        useMeanTrim=true;
        probThreshold=0.001;
        useEM=true;
        iterationNum =15;
        bkgCoeffi =0.5;
        totalDestSignatureNum=cooccurMatrix.columns();
    }

    /**
     * The constructor for the mode of EM algorithm
     * @param srcIndexList the statisitcs of topic signatures in the collection.
     * @param srcSignatureDocMatrix srcSignatureDocMatrix the doc-term matrix for topic signatures.
     * @param destIndexList the statisitcs of individual terms in the collection.
     * @param destDocSignatureMatrix destDocSignatureMatrix the doc-term matrix for individual terms topic signatures will translate to.
     */
    public TopicSignatureModel(IRSignatureIndexList srcIndexList, IntSparseMatrix srcSignatureDocMatrix, IRSignatureIndexList destIndexList, IntSparseMatrix destDocSignatureMatrix) {
        this.srcIndexList =srcIndexList;
        this.srcSignatureDocMatrix=srcSignatureDocMatrix;
        this.destIndexList =destIndexList;
        this.destDocSignatureMatrix=destDocSignatureMatrix;
        useDocFrequency=true;
        useMeanTrim=true;
        probThreshold=0.001;
        useEM=true;
        iterationNum =15;
        bkgCoeffi =0.5;
        totalDestSignatureNum=destDocSignatureMatrix.columns();
    }

    public void setUseEM(boolean option){
        this.useEM=option;
    }

    public boolean getUseEM(){
        return useEM;
    }

    public void setEMBackgroundCoefficient(double coeffi){
        this.bkgCoeffi =coeffi;
    }

    public double getEMBackgroundCoefficient(){
        return this.bkgCoeffi;
    }

    public void setEMIterationNum(int iterationNum){
        this.iterationNum =iterationNum;
    }

    public int getEMIterationNum(){
        return this.iterationNum;
    }

    public void setUseDocFrequency(boolean option){
        this.useDocFrequency=option;
    }

    public boolean getUseDocFrequency(){
        return useDocFrequency;
    }

    public void setUseMeanTrim(boolean option){
        this.useMeanTrim =option;
    }

    public boolean getUseMeanTrim(){
        return useMeanTrim;
    }

    public void setProbThreshold(double threshold){
        this.probThreshold =threshold;
    }

    public double getProbThreshold(){
        return probThreshold;
    }

    public boolean genTransMatrix(int minDocFrequency,String matrixPath, String matrixKey){
        ArrayList tokenList;
        DoubleSuperSparseMatrix outputTransMatrix, outputTransTMatrix;
        File file;
        Token curToken;
        String transIndexFile, transMatrixFile;
        String transTIndexFile, transTMatrixFile;
        int cellNum,rowNum;
        int i, j;

        transIndexFile=matrixPath+"/"+matrixKey+".index";
        transMatrixFile=matrixPath+"/"+matrixKey+".matrix";
        transTIndexFile=matrixPath+"/"+matrixKey+"t.index";
        transTMatrixFile=matrixPath+"/"+matrixKey+"t.matrix";
        file=new File(transMatrixFile);
        if(file.exists()) file.delete();
        file=new File(transIndexFile);
        if(file.exists()) file.delete();
        file=new File(transTMatrixFile);
        if(file.exists()) file.delete();
        file=new File(transTIndexFile);
        if(file.exists()) file.delete();

        outputTransMatrix=new DoubleSuperSparseMatrix(transIndexFile, transMatrixFile,false,false);
        outputTransMatrix.setFlushInterval(Integer.MAX_VALUE);
        outputTransTMatrix=new DoubleSuperSparseMatrix(transTIndexFile, transTMatrixFile,false,false);
        outputTransTMatrix.setFlushInterval(Integer.MAX_VALUE);
        cellNum=0;
        rowNum=srcIndexList.size();
        buf=new int[totalDestSignatureNum];
        if(destDocSignatureMatrix!=null)
            this.DOC_THRESH=computeDocThreshold(destDocSignatureMatrix);

        for(i=0;i<rowNum;i++){
            if(i%1000==0) System.out.println((new java.util.Date()).toString()+" Processing Row#"+i);

            if (srcIndexList.getIRSignature(i).getDocFrequency() < minDocFrequency) continue;
            if (cooccurMatrix!=null && cooccurMatrix.getNonZeroNumInRow(i)<5) continue;

            tokenList=genSignatureTranslation(i);
            for (j = 0; j <tokenList.size(); j++) {
                curToken=(Token)tokenList.get(j);
                outputTransMatrix.add(i,curToken.getIndex(),curToken.getWeight());
                outputTransTMatrix.add(curToken.getIndex(), i, curToken.getWeight());
            }
            cellNum+=tokenList.size();
            tokenList.clear();
            if(cellNum>=5000000){
                outputTransTMatrix.flush();
                outputTransMatrix.flush();
                cellNum=0;
            }
        }
        outputTransTMatrix.finalizeData();
        outputTransTMatrix.close();
        outputTransMatrix.finalizeData();
        outputTransMatrix.close();
        return true;
    }

    public ArrayList genSignatureTranslation(int srcSignatureIndex){
        ArrayList tokenList;
        int[] arrDoc;

        if(srcSignatureDocMatrix!=null){
            arrDoc = srcSignatureDocMatrix.getNonZeroColumnsInRow(srcSignatureIndex);
            if (arrDoc.length > DOC_THRESH)
                tokenList = computeDistributionByArray(arrDoc);
            else
                tokenList = computeDistributionByHash(arrDoc);
        }
        else
            tokenList=computeDistributionByCooccurMatrix(srcSignatureIndex);

        if(useEM)
            tokenList=emTopicSignatureModel(tokenList);
        return tokenList;
    }

    private int computeDocThreshold(IntSparseMatrix doctermMatrix){
        return (int)(doctermMatrix.columns()/computeAvgTermNum(doctermMatrix)/8.0);
    }

    private double computeAvgTermNum(IntSparseMatrix doctermMatrix){
        Random random;
        int i, num, index;
        double sum;

        random=new Random();
        num=Math.min(50,doctermMatrix.rows());
        sum=0;
        for(i=0;i<num;i++){
            index=random.nextInt(doctermMatrix.rows());
            sum+=doctermMatrix.getNonZeroNumInRow(index);
        }
        return sum/num;
    }

    private ArrayList computeDistributionByCooccurMatrix(int signatureIndex){
        ArrayList list;
        Token curToken;
        int[] arrIndex, arrFreq;
        int i;
        double rowTotal, mean;

        rowTotal=0;
        arrIndex=cooccurMatrix.getNonZeroColumnsInRow(signatureIndex);
        arrFreq=cooccurMatrix.getNonZeroIntScoresInRow(signatureIndex);
        for(i=0;i<arrFreq.length;i++)
            rowTotal+=arrFreq[i];
        if(useMeanTrim)
            mean=rowTotal/arrFreq.length;
        else
            mean=0.5;
        if(mean<rowTotal*getMinInitProb())
            mean=rowTotal*getMinInitProb();

        rowTotal=0;
        list=new ArrayList();
        for(i=0;i<arrFreq.length;i++){
            if(arrFreq[i]>=mean){
                list.add(new Token(arrIndex[i],arrFreq[i]));
                rowTotal+=arrFreq[i];
            }
        }
        for(i=0;i<list.size();i++){
            curToken=(Token)list.get(i);
            curToken.setWeight(curToken.getFrequency()/rowTotal);
        }
        return list;
    }

    private ArrayList computeDistributionByArray(int[] arrDoc){
        ArrayList list;
        Token curToken;
        int[] arrIndex, arrFreq;
        int i, j, k, nonZeroNum;
        double rowTotal, mean;

        rowTotal=0;
        if(buf==null)
            buf=new int[totalDestSignatureNum];
        MathUtil.initArray(buf,0);
        for(j=0;j<arrDoc.length;j++){
            arrIndex = destDocSignatureMatrix.getNonZeroColumnsInRow(arrDoc[j]);
            if(useDocFrequency)
                arrFreq=null;
            else
                arrFreq=destDocSignatureMatrix.getNonZeroIntScoresInRow(arrDoc[j]);
            for (k = 0; k <arrIndex.length; k++) {
                if(useDocFrequency)
                    buf[arrIndex[k]]+=1;
                else
                    buf[arrIndex[k]]+=arrFreq[k];
            }
        }

        nonZeroNum=0;
        for(i=0;i<buf.length;i++){
            if (buf[i] > 0) {
                nonZeroNum++;
                rowTotal += buf[i];
            }
        }
        if(useMeanTrim)
            mean=rowTotal/nonZeroNum;
        else
            mean=0.5;
        if(mean<rowTotal*getMinInitProb())
            mean=rowTotal*getMinInitProb();

        rowTotal=0;
        list=new ArrayList();
        for(i=0;i<buf.length;i++){
            if(buf[i]>=mean){
                list.add(new Token(i,buf[i]));
                rowTotal+=buf[i];
            }
        }
        for(i=0;i<list.size();i++){
            curToken=(Token)list.get(i);
            curToken.setWeight(curToken.getFrequency()/rowTotal);
        }
        return list;
    }

    private ArrayList computeDistributionByHash(int[] arrDoc){
        ArrayList list, tokenList;
        Token curToken;
        int i;
        double rowTotal, mean;

        tokenList=countTokensByHashMap(arrDoc);
        rowTotal=0;
        for(i=0;i<tokenList.size();i++)
            rowTotal+=((Token)tokenList.get(i)).getFrequency();

        if(useMeanTrim || rowTotal*getMinInitProb()>1){
            if(useMeanTrim)
                mean=rowTotal/tokenList.size();
            else
                mean=0.5;
            if(mean<rowTotal*getMinInitProb())
                mean=rowTotal*getMinInitProb();
            list=new ArrayList();
            rowTotal=0;
            for(i=0;i<tokenList.size();i++){
                curToken=(Token)tokenList.get(i);
                if(curToken.getFrequency()>=mean){
                    list.add(curToken);
                    rowTotal+=curToken.getFrequency();
                }
            }
            tokenList.clear();
        }
        else
            list=tokenList;

        for(i=0;i<list.size();i++){
            curToken=(Token)list.get(i);
            curToken.setWeight(curToken.getFrequency()/rowTotal);
        }
        return list;
    }

    private ArrayList countTokensByHashMap(int[] arrDoc){
        HashMap hash;
        ArrayList list;
        Token curToken;
        Counter counter;
        Iterator iterator;
        int[] arrTerm, arrFreq;
        int i,j, termNum;

        hash=new HashMap();
        for(j=0;j<arrDoc.length;j++){
            termNum = destDocSignatureMatrix.getNonZeroNumInRow(arrDoc[j]);
            if(termNum==0) continue;

            arrTerm = destDocSignatureMatrix.getNonZeroColumnsInRow(arrDoc[j]);
            if(useDocFrequency)
                arrFreq=null;
            else
                arrFreq=destDocSignatureMatrix.getNonZeroIntScoresInRow(arrDoc[j]);
            for(i=0;i<termNum;i++){
                if (useDocFrequency)
                    curToken = new Token(arrTerm[i], 1);
                else
                    curToken = new Token(arrTerm[i], arrFreq[i]);
                counter=(Counter)hash.get(curToken);
                if(counter==null){
                    counter=new Counter(curToken.getFrequency());
                    hash.put(curToken,counter);
                }
                else
                    counter.addCount(curToken.getFrequency());
            }
        }

        list=new ArrayList(hash.size());
        iterator=hash.keySet().iterator();
        while(iterator.hasNext()){
            curToken=(Token)iterator.next();
            counter=(Counter)hash.get(curToken);
            curToken.setFrequency(counter.getCount());
            list.add(curToken);
        }
        hash.clear();
        return list;
    }

    private double getMinInitProb(){
        /*
        if(useEM)
            return Math.min(0.0001,probThreshold);
        else
            return probThreshold;*/
        return probThreshold;
    }

    private ArrayList emTopicSignatureModel(ArrayList list){
        Token curToken;
        double[]  arrCollectionProb, arrProb;
        double weightSum;
        int termNum;
        int i, j;

        termNum =list.size();
        arrProb = new double[termNum];

        //initialize the background model;
        arrCollectionProb=new double[termNum];
        weightSum=0;
        for(i=0;i<termNum;i++){
            curToken=(Token)list.get(i);
            if(useDocFrequency)
                arrCollectionProb[i]=destIndexList.getIRSignature(curToken.getIndex()).getDocFrequency();
            else
                arrCollectionProb[i]=destIndexList.getIRSignature(curToken.getIndex()).getFrequency();
            weightSum+=arrCollectionProb[i];
        }
        for(i=0;i<termNum;i++)
            arrCollectionProb[i]=arrCollectionProb[i]/weightSum;

        //start EM
        for (i = 0; i < iterationNum; i++) {
            weightSum = 0;
            for (j = 0; j < termNum; j++) {
                curToken=(Token)list.get(j);
                arrProb[j] = (1 - bkgCoeffi) * curToken.getWeight() /
                    ( (1 - bkgCoeffi) * curToken.getWeight() + bkgCoeffi * arrCollectionProb[j]) * curToken.getFrequency();
                weightSum += arrProb[j];
            }
            for (j = 0; j < termNum; j++){
                curToken=(Token)list.get(j);
                curToken.setWeight(arrProb[j]/ weightSum);
            }
        }

        /*newList=new ArrayList(list.size());
        for (j = 0; j < termNum; j++){
            curToken=(Token)list.get(j);
            if(curToken.getWeight()>=probThreshold)
                newList.add(curToken);
        }
        return newList;*/
        return list;
    }
}
