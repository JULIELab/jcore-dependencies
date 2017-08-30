package dragon.ml.seqmodel.crf;

import dragon.matrix.*;
import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.feature.FeatureGenerator;
import dragon.ml.seqmodel.model.ModelGraph;
import dragon.util.MathUtil;

/**
 * <p>Viterb basic labeler</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class ViterbiBasicLabeler extends AbstractCRF implements Labeler {
    private int winningLabel[][];
    private double[] score;
    private int[] solutionOrder; //solutionOrder[0] stores the index of the best score, and so on.

    public ViterbiBasicLabeler(ModelGraph model, FeatureGenerator featureGenerator){
        super(model, featureGenerator);
        score=new double[model.getStateNum()];
    }

    public boolean label(DataSequence dataSeq){
        return label(dataSeq, lambda);
    }

    public boolean label(DataSequence dataSeq, double[] lambda) {
        DoubleDenseMatrix transMatrix, scoreMatrix;
        double[] arrPrevScore;
        double maxScore;
        int stateNum, markovOrder,yi, yp, i, prevLabel;

        stateNum=model.getStateNum();
        markovOrder=model.getMarkovOrder();
        transMatrix= new DoubleFlatDenseMatrix(stateNum, stateNum);
        scoreMatrix=new DoubleFlatDenseMatrix(stateNum, stateNum);
        arrPrevScore=new double[stateNum];
        winningLabel = new int[dataSeq.length()][stateNum];

        for (i = markovOrder-1; i < dataSeq.length(); i++) {
            computeTransMatrix(lambda, dataSeq, i,i, transMatrix, false);
            scoreMatrix.assign(0);
            for (yi = edgeGen.firstLabel(i); yi < stateNum; yi = edgeGen.nextLabel(yi, i)) {
                if (i > markovOrder-1) {
                    for (yp = edgeGen.first(yi); yp <stateNum; yp = edgeGen.next(yi, yp))
                        scoreMatrix.setDouble(yp,yi,arrPrevScore[yp]+transMatrix.getDouble(yp, yi));
                }
                else
                    arrPrevScore[yi]=transMatrix.getDouble(0, yi);
            }

            //maximize the partial path
            if(i>markovOrder-1){
                for (yi = 0; yi < stateNum; yi++) {
                    prevLabel = 0;
                    maxScore = scoreMatrix.getDouble(prevLabel, yi);
                    for (yp = 1; yp < stateNum; yp++) {
                        if (scoreMatrix.getDouble(yp, yi) > maxScore) {
                            maxScore = scoreMatrix.getDouble(yp, yi);
                            prevLabel = yp;
                        }
                    }
                    arrPrevScore[yi] = maxScore;
                    winningLabel[i][yi] = prevLabel;
                }
            }
        }
        MathUtil.copyArray(arrPrevScore,score);
        solutionOrder=MathUtil.rankElementInArray(score,true);
        getBestSolution(dataSeq,0);
        return true;
    }

    public double getBestSolution(DataSequence dataSeq, int order){
        int i, prevLabel, markovOrder;

        markovOrder=model.getMarkovOrder();
        prevLabel=solutionOrder[order];
        for(i=dataSeq.length()-1;i>=markovOrder-1;i--){
            dataSeq.setLabel(i,prevLabel);
            prevLabel=winningLabel[i][prevLabel];
        }
        model.mapStateToLabel(dataSeq);
        return score[solutionOrder[order]];
    }
};
