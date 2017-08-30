package dragon.ir.topicmodel;

import dragon.matrix.*;
import java.util.*;

/**
 * <p>Cross mixture topic model </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CrossMixtureModel extends AbstractModel {
    protected IntSparseMatrix[] arrTopicReader;
    protected double[] bkgModel;
    protected double bkgCoefficient, comCoefficient;
    protected int themeNum, collectionNum, maxTermNum, maxDocNum;
    private double[][][] arrDocWeight, arrProb;
    private double[][] arrCommonProb;

    public CrossMixtureModel(IntSparseMatrix[] arrTopicMatrix, int themeNum, double[] bkgModel, double bkgCoefficient, double comCoefficient) {
        int i;

        this.arrTopicReader =arrTopicMatrix;
        this.themeNum =themeNum;
        this.collectionNum =arrTopicReader.length;
        this.bkgModel =new double[bkgModel.length];
        this.comCoefficient =comCoefficient;
        for(i=0;i<bkgModel.length;i++)
            this.bkgModel[i]=bkgModel[i]*bkgCoefficient;
        this.bkgCoefficient=bkgCoefficient;
        maxTermNum=arrTopicReader[0].columns();
        maxDocNum=arrTopicReader[0].rows();
        for(i=1;i<arrTopicReader.length;i++){
            if(arrTopicReader[i].columns()>maxTermNum)
                maxTermNum=arrTopicReader[i].columns();
            if(arrTopicReader[i].rows()>maxDocNum)
                maxDocNum=arrTopicReader[i].rows();
        }
    }

    public double[][][] getModels(){
        return arrProb;
    }

    public double[][] getCommonModels(){
        return arrCommonProb;
    }

    public double[][][] getDocMemberships(){
        return arrDocWeight;
    }

    public boolean estimateModel(){
        double[][][] arrTempProb;
        double[][] arrTempCommonProb;
        double[] arrDocWeightSum;
        double bkgProb, comThemeProb, themeProb, themeProbSum, docWeightSum, termProbSum, termProb;
        int[] arrIndex, arrFreq;
        int docNum;
        int termIndex;
        int i,j,k,n,m;

        //initialization
        arrProb=new double[collectionNum][themeNum][maxTermNum];
        arrCommonProb=new double[themeNum][maxTermNum];
        arrDocWeight=new double[collectionNum][themeNum][maxDocNum];
        arrTempProb=new double[collectionNum][themeNum][maxTermNum];
        arrTempCommonProb=new double[themeNum][maxTermNum];
        arrDocWeightSum=new double[themeNum];

        //initialize parameters
        initialize(maxTermNum, collectionNum, themeNum, maxDocNum, arrCommonProb, arrProb, arrDocWeight);

        //compute coefficients for mixture components
        printStatus("Estimating the coefficients of simple mixture model...");
        for(k=0;k<iterations;k++){
            printStatus("Iteration #" + (k + 1));
            for (i = 0; i < themeNum; i++)
                for (j = 0; j < maxTermNum; j++)
                    arrTempCommonProb[i][j] = 0;
            for(n=0;n<collectionNum;n++)
                for (i = 0; i < themeNum; i++)
                    for (j = 0; j < maxTermNum; j++)
                        arrTempProb[n][i][j] = 0;

            for(n=0;n<collectionNum;n++){
                docNum=arrTopicReader[n].rows();

                for (i = 0; i < docNum; i++) {
                    arrIndex = arrTopicReader[n].getNonZeroColumnsInRow(i);
                    arrFreq = arrTopicReader[n].getNonZeroIntScoresInRow(i);
                    for (m = 0; m < themeNum; m++)
                        arrDocWeightSum[m] = 0;

                    for (j = 0; j < arrIndex.length; j++) {
                        termIndex=arrIndex[j];
                        themeProbSum = 0;
                        for (m = 0; m < themeNum; m++) {
                            themeProbSum += (comCoefficient*arrCommonProb[m][j]+(1-comCoefficient)*arrProb[n][m][j]) * arrDocWeight[n][m][i];
                        }
                        bkgProb = bkgModel[termIndex] / (themeProbSum * (1 - bkgCoefficient) + bkgModel[termIndex]);

                        for (m = 0; m < themeNum; m++) {
                            if(themeProbSum==0)
                                themeProb=0;
                            else
                                themeProb =(comCoefficient*arrCommonProb[m][termIndex]+(1-comCoefficient)*arrProb[n][m][termIndex]) * arrDocWeight[n][m][i]/themeProbSum;
                            comThemeProb=(comCoefficient*arrCommonProb[m][termIndex]+(1-comCoefficient)*arrProb[n][m][termIndex]);
                            if(comThemeProb>0)
                                comThemeProb=comCoefficient*arrCommonProb[m][termIndex]/comThemeProb;
                            else
                                comThemeProb=0;
                            termProb = arrFreq[j] * themeProb;
                            arrDocWeightSum[m] += termProb;
                            termProb=termProb* (1 - bkgProb);
                            arrTempProb[n][m][termIndex] += termProb *(1-comThemeProb);
                            arrTempCommonProb[m][termIndex]+=termProb*comThemeProb;
                        }
                    }

                    //update the doc-specific coefficient for each theme
                    docWeightSum = 0;
                    for (m = 0; m < themeNum; m++) {
                        docWeightSum += arrDocWeightSum[m];
                    }
                    if(docWeightSum>0){
                        for (m = 0; m < themeNum; m++)
                            arrDocWeight[n][m][i] = arrDocWeightSum[m] / docWeightSum;
                    }
                    else{
                        for (m = 0; m < themeNum; m++)
                            arrDocWeight[n][m][i] =0;
                    }
                }
            }

            //update the generative model for each theme
            for (i = 0; i < themeNum; i++) {
                //update common model
                termProbSum = 0;
                for (j = 0; j < maxTermNum; j++)
                    termProbSum += arrTempCommonProb[i][j];
                for (j = 0; j < maxTermNum; j++)
                    arrCommonProb[i][j] = arrTempCommonProb[i][j] / termProbSum;

                //update collection-specific model
                for(n=0;n<collectionNum;n++){
                    termProbSum = 0;
                    for (j = 0; j < maxTermNum; j++)
                        termProbSum += arrTempProb[n][i][j];
                    for (j = 0; j < maxTermNum; j++)
                        arrProb[n][i][j] = arrTempProb[n][i][j] / termProbSum;
                }
            }
        }
        printStatus("");
        return true;
    }

    protected void initialize(int maxTermNum, int collectionNum, int themeNum, int maxDocNum,
                              double[][] arrCommonModel, double[][][] arrModel, double[][][] arrDocMembership){
        Random random;
        double termProb, docProb;
        int n, i, j;

        termProb=1.0/maxTermNum;
        for(i=0;i<themeNum;i++)
            for(j=0;j<maxTermNum;j++)
                arrCommonModel[i][j]=termProb;

        for(n=0;n<collectionNum;n++)
            for(i=0;i<themeNum;i++)
                for(j=0;j<maxTermNum;j++)
                    arrModel[n][i][j]=termProb;

        if(seed>=0)
            random=new Random(seed);
        else
            random=new Random();
        for(n=0;n<collectionNum;n++){
             for (j = 0; j < maxDocNum; j++){
                 docProb=0;
                 for (i = 0; i < themeNum; i++){
                     arrDocMembership[n][i][j] = random.nextDouble();
                     docProb+=arrDocMembership[n][i][j];
                 }
                 for(i=0;i<themeNum;i++)
                     arrDocMembership[n][i][j]=arrDocMembership[n][i][j]/docProb;
             }
        }
    }
}
