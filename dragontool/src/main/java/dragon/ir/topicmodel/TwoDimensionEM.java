package dragon.ir.topicmodel;

import dragon.ir.index.IndexReader;
import dragon.matrix.vector.DoubleVector;

import java.util.Random;

/**
 * <p>The EM based two dimesional topical model </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TwoDimensionEM extends AbstractTwoDimensionModel{
    protected DoubleVector viewBkgModel, themeBkgModel;
    protected double viewBkgCoeffi, themeBkgCoeffi;
    protected double comThemeCoeffi;

    public TwoDimensionEM(IndexReader viewIndexReader, IndexReader topicIndexReader,
                          double viewBkgCoeffi, double themeBkgCoeffi, double comThemeCoeffi){
        this(viewIndexReader,null,viewBkgCoeffi,topicIndexReader,null,themeBkgCoeffi,comThemeCoeffi);
    }

    public TwoDimensionEM(IndexReader viewIndexReader, DoubleVector viewBkgModel, double viewBkgCoeffi,
                          IndexReader topicIndexReader, DoubleVector themeBkgModel, double themeBkgCoeffi, double comThemeCoeffi) {
        super(viewIndexReader,topicIndexReader);
        if(viewBkgModel==null)
            viewBkgModel=getBkgModel(viewIndexReader);
        else
            viewBkgModel=viewBkgModel.copy();
        this.viewBkgCoeffi=viewBkgCoeffi;
        this.viewBkgModel.multiply(viewBkgCoeffi);

        if(themeBkgModel==null)
            themeBkgModel=getBkgModel(topicIndexReader);
        else
            themeBkgModel=themeBkgModel.copy();
        this.themeBkgCoeffi=themeBkgCoeffi;
        this.themeBkgModel.multiply(themeBkgCoeffi);

        this.comThemeCoeffi =comThemeCoeffi;
    }

    public boolean estimateModel(int viewNum, int topicNum){
        double[][][] arrTempThemeProb;
        double[][] arrTempViewProb, arrTempThemeCommonProb;
        double[] arrDocViewSum;
        double[][] arrDocThemeSum;
        double[] arrViewProbSum;
        double viewBkgProb, themeBkgProb, themeProb, commonThemeProb, themeProbSum, docWeightSum, termProbSum, termProb;
        int[] arrIndex, arrFreq;
        int termIndex;
        int i,j,k,l,m;

        //initialization
        this.viewNum=viewNum;
        this.themeNum =topicNum;

        arrViewProb=new double[viewNum][viewTermNum];
        arrTempViewProb=new double[viewNum][viewTermNum];
        arrDocView=new double[docNum][viewNum];
        arrDocViewSum=new double[viewNum];
        arrViewProbSum=new double[viewNum];

        arrThemeProb=new double[viewNum][themeNum][themeTermNum];
        arrTempThemeProb=new double[viewNum][themeNum][themeTermNum];
        arrCommonThemeProb=new double[themeNum][themeTermNum];
        arrTempThemeCommonProb=new double[themeNum][themeTermNum];
        arrDocTheme=new double[docNum][viewNum][themeNum];
        arrDocThemeSum=new double[viewNum][themeNum];


        //initialize parameters
        initialize(docNum, viewTermNum, viewNum, arrViewProb, arrDocView,
                   themeTermNum, themeNum, arrCommonThemeProb, arrThemeProb,arrDocTheme);

        //compute coefficients for mixture components
        printStatus("Estimating the coefficients of two-dimensional mixture model...");
        for(k=0;k<iterations;k++){
            printStatus("Iteration #" + (k + 1));
            for(m=0;m<viewNum;m++)
                for (i = 0; i < viewTermNum; i++)
                    arrTempViewProb[m][i] = 0;
            for(l=0;l<themeNum;l++)
               for (i = 0; i <themeTermNum; i++)
                   arrTempThemeCommonProb[l][i] = 0;
            for(m=0;m<viewNum;m++)
                for(l=0;l<themeNum;l++)
                    for (i = 0; i <themeTermNum; i++)
                        arrTempThemeProb[m][l][i] = 0;

            for (i = 0; i < docNum; i++) {
                for(m=0;m<viewNum;m++) arrDocViewSum[m] = 0;
                for(m=0;m<viewNum;m++)
                    for(l=0;l<themeNum;l++)
                        arrDocThemeSum[m][l] = 0;

                //process the second dimension
                arrIndex = topicIndexReader.getTermIndexList(i);
                arrFreq = topicIndexReader.getTermFrequencyList(i);

                for (j = 0; j < arrIndex.length; j++) {
                    termIndex=arrIndex[j];
                    themeProbSum=0;
                    for(m=0;m<viewNum;m++){
                        arrViewProbSum[m]=0;
                        for (l = 0; l < themeNum; l++)
                            arrViewProbSum[m] += ((1-comThemeCoeffi)*arrThemeProb[m][l][termIndex]+comThemeCoeffi*arrCommonThemeProb[l][termIndex]) * arrDocTheme[i][m][l];
                        arrViewProbSum[m]=arrViewProbSum[m]*arrDocView[i][m];
                        themeProbSum+=arrViewProbSum[m];
                    }

                    if(themeProbSum!=0)
                        themeBkgProb=themeBkgModel.get(termIndex)/(themeProbSum*(1-themeBkgCoeffi)+themeBkgModel.get(termIndex));
                    else
                        themeBkgProb=0;
                    for (m = 0; m <viewNum; m++){
                        //add the contribute from the terms in the second dimension
                        if(themeProbSum!=0)
                           arrDocViewSum[m]+=arrFreq[j]*arrViewProbSum[m]/themeProbSum;

                        for (l = 0; l < themeNum; l++) {
                            if (themeProbSum != 0)
                                themeProb = ( (1 - comThemeCoeffi) * arrThemeProb[m][l][termIndex] +
                                             comThemeCoeffi * arrCommonThemeProb[l][termIndex]) *
                                    arrDocView[i][m]* arrDocTheme[i][m][l]/ themeProbSum;
                            else
                                themeProb = 0;
                            commonThemeProb = (1 - comThemeCoeffi) * arrThemeProb[m][l][termIndex] +
                                comThemeCoeffi * arrCommonThemeProb[l][termIndex];
                            if (commonThemeProb > 0)
                                commonThemeProb = comThemeCoeffi * arrCommonThemeProb[l][termIndex] / commonThemeProb;
                            else
                                commonThemeProb = 0;
                            termProb = arrFreq[j] * themeProb;
                            arrDocThemeSum[m][l] += termProb;
                            termProb = termProb * (1 - themeBkgProb);
                            arrTempThemeProb[m][l][termIndex] += termProb * (1 - commonThemeProb);
                            arrTempThemeCommonProb[l][termIndex] += termProb * commonThemeProb;
                        }
                    }
                }
                //update the doc-specific coefficient for each theme
                for (m = 0; m < viewNum; m++) {
                    docWeightSum = 0;
                    for (l = 0; l < themeNum; l++)
                        docWeightSum += arrDocThemeSum[m][l];
                    if(docWeightSum>0){
                        for (l = 0; l < themeNum; l++)
                            arrDocTheme[i][m][l] = arrDocThemeSum[m][l] / docWeightSum;
                    }
                    else{
                        for (l = 0; l < themeNum; l++)
                            arrDocTheme[i][m][l] = 0;
                    }
                }

                //process the first dimension
                arrIndex = viewIndexReader.getTermIndexList(i);
                arrFreq = viewIndexReader.getTermFrequencyList(i);

                for (j = 0; j < arrIndex.length; j++) {
                    termIndex=arrIndex[j];
                    themeProbSum=0;
                    for(m=0;m<viewNum;m++){
                        themeProbSum+=arrViewProb[m][termIndex]*arrDocView[m][i];
                    }
                    viewBkgProb=viewBkgModel.get(termIndex)/(themeProbSum*(1-viewBkgCoeffi)+viewBkgModel.get(termIndex));

                    for (m = 0; m <viewNum; m++) {
                        if(themeProbSum!=0){
                            themeProb = arrViewProb[m][termIndex] * arrDocView[i][m]/ themeProbSum;
                        }
                        else
                            themeProb=0;
                        termProb =arrFreq[j]*themeProb;
                        arrDocViewSum[m]+=termProb;
                        termProb=termProb*(1-viewBkgProb);
                        arrTempViewProb[m][termIndex]+=termProb;
                    }
                }

                //update the doc-specific coefficient for each theme
                docWeightSum=0;
                for (m = 0; m < viewNum; m++)
                    docWeightSum+=arrDocViewSum[m];
                if(docWeightSum>0){
                    for (m = 0; m < viewNum; m++)
                        arrDocView[i][m] = arrDocViewSum[m] / docWeightSum;
                }
                else{
                    for (m = 0; m < viewNum; m++)
                        arrDocView[i][m]= 0;
                }
            }

            //update the generative model for each theme
            //first dimension
            for(m=0;m<viewNum;m++){
                termProbSum=0;
                for(i=0;i<viewTermNum;i++)
                    termProbSum+=arrTempViewProb[m][i];
                for(i=0;i<viewTermNum;i++)
                    if(termProbSum!=0)
                        arrViewProb[m][i]=arrTempViewProb[m][i]/termProbSum;
                    else
                        arrViewProb[m][i]=0;
            }
            //second dimension
            //common model
            for(l=0;l<themeNum;l++){
                termProbSum = 0;
                for (i = 0; i < themeTermNum; i++){
                    termProbSum += arrTempThemeCommonProb[l][i];
                }
                for (i = 0; i < themeTermNum; i++)
                    if(termProbSum!=0)
                        arrCommonThemeProb[l][i] = arrTempThemeCommonProb[l][i]/termProbSum;
                    else
                        arrCommonThemeProb[1][i]=0;
            }

            //specific model
            for(m=0;m<viewNum;m++){
                for(l=0;l<themeNum;l++){
                    termProbSum = 0;
                    for (i = 0; i < themeTermNum; i++){
                        termProbSum += arrTempThemeProb[m][l][i];
                    }
                    for (i = 0; i < themeTermNum; i++)
                        if(termProbSum!=0)
                            arrThemeProb[m][l][i] = arrTempThemeProb[m][l][i]/termProbSum;
                        else
                            arrThemeProb[m][1][i]=0;
                }
            }
        }
        printStatus("");
        return true;
    }

    protected void initialize(int docNum, int viewTermNum, int viewNum, double[][] arrViewModel, double[][] arrDocView,
                              int themeTermNum, int themeNum, double[][] arrThemeCommonModel, double[][][] arrThemeModel, double[][][] arrDocTheme){
        Random random;
        double termProb, docProb;
        int i, j, k;

        if(seed>=0)
            random=new Random(seed);
        else
            random=new Random();
        termProb=1.0/viewTermNum;
        for(i=0;i<viewNum;i++)
            for(j=0;j<viewTermNum;j++)
                arrViewModel[i][j]=termProb;
        for (i = 0; i < docNum; i++) {
            docProb = 0;
            for (j = 0; j < viewNum; j++) {
                arrDocView[i][j] = random.nextDouble();
                docProb += arrDocView[i][j];
            }
            for (j = 0; j < viewNum; j++)
                arrDocView[i][j] = arrDocView[i][j] / docProb;
        }

        termProb=1.0/themeTermNum;
        for(j=0;j<themeNum;j++)
            for(k=0;k<themeTermNum;k++)
                arrThemeCommonModel[j][k]=termProb;
        for(i=0;i<viewNum;i++)
            for(j=0;j<themeNum;j++)
                for(k=0;k<themeTermNum;k++)
                    arrThemeModel[i][j][k]=termProb;
        for(i=0;i<viewNum;i++){
            for (k = 0; k < docNum; k++){
                docProb=0;
                for (j = 0; j < themeNum; j++){
                    arrDocTheme[k][i][j] =random.nextDouble();
                    docProb+=arrDocTheme[k][i][j];
                }
                for (j = 0; j < themeNum; j++)
                    if(docProb!=0)
                        arrDocTheme[k][i][j]=arrDocTheme[k][i][j]/docProb;
                    else
                        arrDocTheme[k][i][j]=0;
            }
        }
    }
}
