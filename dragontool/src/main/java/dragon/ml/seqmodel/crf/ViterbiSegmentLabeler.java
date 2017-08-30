package dragon.ml.seqmodel.crf;

import dragon.matrix.*;
import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.feature.FeatureGenerator;
import dragon.ml.seqmodel.model.ModelGraph;
import dragon.util.MathUtil;

/**
 * <p>Viterb segment labeler</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class ViterbiSegmentLabeler extends AbstractCRF implements Labeler{
    private int maxLen;
    private int[][] winningLabel, winningPos;
    private double[] score;
    private int[] solutionOrder; //solutionOrder[0] stores the index of the best score, and so on.

    //The Nested Model is not allowed
    public ViterbiSegmentLabeler(ModelGraph model, FeatureGenerator featureGenerator, int maxSegmentLength) {
        super(model, featureGenerator);
        this.maxLen =maxSegmentLength;
        score=new double[model.getStateNum()];
    }

    public boolean label(DataSequence dataSeq){
        return label(dataSeq,lambda);
    }

    public boolean label(DataSequence dataSeq, double[] lambda) {
        DoubleDenseMatrix transMatrix, scoreMatrix[];
        double[][] partialScore;
        double maxScore;
        int stateNum,yi, yp, i, ell, startPos, prevLabel;

        stateNum=model.getStateNum();
        transMatrix= new DoubleFlatDenseMatrix(stateNum, stateNum);
        scoreMatrix=new DoubleFlatDenseMatrix[maxLen];
        for(i=0;i<maxLen;i++)
            scoreMatrix[i]=new DoubleFlatDenseMatrix(stateNum,stateNum);
        partialScore=new double[dataSeq.length()][stateNum];
        winningLabel= new int[dataSeq.length()][stateNum];
        winningPos= new int[dataSeq.length()][stateNum];

        for (i = 0; i < dataSeq.length(); i++) {
            if(i>0){
                for (ell = 0; (ell < maxLen) && (i - ell > 0); ell++) {
                    computeTransMatrix(lambda, dataSeq, i - ell, i, transMatrix, false);
                    scoreMatrix[ell].assign(0);
                    for (yi = 0; yi < stateNum; yi++) {
                        for (yp = 0; yp < stateNum; yp++)
                            scoreMatrix[ell].setDouble(yp, yi,partialScore[i - ell - 1][yp] + transMatrix.getDouble(yp, yi));
                    }
                }
            }
            else{
                for (yi = 0; yi < stateNum; yi++) {
                    partialScore[i][yi] = transMatrix.getDouble(0, yi);
                    winningPos[i][yi] = 0;
                }
            }


            //maximize the partial path
            if(i>0){
                for (yi = 0; yi < stateNum; yi++) {
                    prevLabel =-1;
                    startPos=-1;
                    maxScore =-1;
                    for (ell = 0; (ell <maxLen) && (i-ell >=0); ell++){
                        for (yp = 0; yp < stateNum; yp++) {
                            if (scoreMatrix[ell].getDouble(yp, yi) > maxScore) {
                                maxScore = scoreMatrix[ell].getDouble(yp, yi);
                                prevLabel = yp;
                                startPos=i-ell;
                            }
                        }
                    }
                    partialScore[i][yi] = maxScore;
                    winningLabel[i][yi] = prevLabel;
                    winningPos[i][yi] = startPos;
                }
            }
        }
        MathUtil.copyArray(partialScore[dataSeq.length()-1],score);
        solutionOrder=MathUtil.rankElementInArray(score,true);
        getBestSolution(dataSeq,0);
        return true;
    }

    public double getBestSolution(DataSequence dataSeq, int order){
        int startPos, endPos;
        int prevLabel;

        endPos=dataSeq.length()-1;
        prevLabel=solutionOrder[order];
        startPos=winningPos[endPos][prevLabel];
        while(startPos>=0){
            dataSeq.setSegment(startPos,endPos,prevLabel);
            prevLabel=winningLabel[endPos][prevLabel];
            endPos=startPos-1;
            if(endPos>=0)
                startPos=winningPos[endPos][prevLabel];
            else
                break;
        }
        model.mapStateToLabel(dataSeq);
        return score[solutionOrder[order]];
    }
};
