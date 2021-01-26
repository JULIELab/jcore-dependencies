package dragon.ir.topicmodel;

import dragon.ir.index.IndexReader;

import java.util.Random;

/**
 * <p>Aspect topic model </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class AspectModel extends AbstractTopicModel{

    public AspectModel(IndexReader indexReader) {
        super(indexReader);
    }

    public boolean estimateModel(int topicNum){
        return estimateModel(null,topicNum);
    }

    public boolean estimateModel(int[] arrDoc, int topicNum){
        double[][] arrTempProb;
        double[] arrDocWeightSum;
        double themeProb, themeProbSum, docWeightSum, termProbSum, termProb;
        int[] arrIndex, arrFreq;
        int termIndex;
        int i,j,k,m;

        //initialization
        this.themeNum =topicNum;
        termNum=indexReader.getCollection().getTermNum();
        if(arrDoc==null)
            docNum=indexReader.getCollection().getDocNum();
        else
            docNum=arrDoc.length;

        arrThemeTerm=new double[themeNum][termNum];
        arrTempProb=new double[themeNum][termNum];
        arrDocTheme=new double[docNum][themeNum];
        arrDocWeightSum=new double[themeNum];

        //initialize parameters
        initialize(termNum, themeNum, docNum, arrThemeTerm, arrDocTheme);

        //compute coefficients for mixture components
        printStatus("Estimating the coefficients of simple mixture model...");
        for(k=0;k<iterations;k++){
            printStatus((new java.util.Date()).toString()+" Iteration #" + (k + 1));
            for(i=0;i<themeNum;i++)
                for (j = 0; j < termNum; j++)
                    arrTempProb[i][j] = 0;

            for (i = 0; i < docNum; i++) {
                if(arrDoc==null){
                    arrIndex = indexReader.getTermIndexList(i);
                    arrFreq = indexReader.getTermFrequencyList(i);
                }
                else{
                    arrIndex = indexReader.getTermIndexList(arrDoc[i]);
                    arrFreq = indexReader.getTermFrequencyList(arrDoc[i]);
                }
                for(m=0;m<themeNum;m++) arrDocWeightSum[m] = 0;

                for (j = 0; j < arrIndex.length; j++) {
                    termIndex=arrIndex[j];
                    themeProbSum=0;
                    for(m=0;m<themeNum;m++){
                        themeProbSum+=arrThemeTerm[m][termIndex]*arrDocTheme[i][m];
                    }

                    for (m = 0; m <themeNum; m++) {
                        if(themeProbSum!=0)
                           // themeProb is Zd,w=j
                           themeProb=arrThemeTerm[m][termIndex]*arrDocTheme[i][m]/themeProbSum;
                        else
                            themeProb=0;
                        termProb =arrFreq[j]*themeProb;
                        arrDocWeightSum[m]+=termProb;
                        arrTempProb[m][termIndex]+=termProb;
                    }
                }

                //update the doc-specific coefficient for each theme
                docWeightSum=0;
                for (m = 0; m < themeNum; m++)
                    docWeightSum+=arrDocWeightSum[m];
                if(docWeightSum>0){
                    for (m = 0; m < themeNum; m++)
                        arrDocTheme[i][m] = arrDocWeightSum[m] / docWeightSum;
                }
                else{
                    for (m = 0; m < themeNum; m++)
                        arrDocTheme[i][m] = 0;
                }
            }

            //update the generative model for each theme
            for(i=0;i<themeNum;i++){
                termProbSum=0;
                for(j=0;j<termNum;j++)
                    termProbSum+=arrTempProb[i][j];
                for(j=0;j<termNum;j++)
                    arrThemeTerm[i][j]=arrTempProb[i][j]/termProbSum;
            }
        }
        printStatus("");
        return true;
    }

    protected void initialize(int termNum, int themeNum, int docNum, double[][] arrModel, double[][] arrDocMembership){
        Random random;
        double termProb, docProb;
        int i, j;

        termProb=1.0/termNum;
        for(i=0;i<themeNum;i++)
            for(j=0;j<termNum;j++)
                arrModel[i][j]=termProb;

        random=new Random(seed);
        for(i=0;i<docNum;i++){
            docProb=0;
            for(j=0;j<themeNum;j++){
                arrDocMembership[i][j] = random.nextDouble();
                docProb+=arrDocMembership[i][j];
            }
            for(j=0;j<themeNum;j++)
                arrDocMembership[i][j]=arrDocMembership[i][j]/docProb;
        }
    }
}
