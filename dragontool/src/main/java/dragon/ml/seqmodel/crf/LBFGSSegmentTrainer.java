package dragon.ml.seqmodel.crf;

import dragon.matrix.DoubleDenseMatrix;
import dragon.matrix.DoubleFlatDenseMatrix;
import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.data.Dataset;
import dragon.ml.seqmodel.feature.Feature;
import dragon.ml.seqmodel.feature.FeatureGenerator;
import dragon.ml.seqmodel.model.ModelGraph;
import dragon.util.MathUtil;

/**
 * <p>LBFGS segment trainer</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class LBFGSSegmentTrainer extends LBFGSBasicTrainer {
    private int maxSegmentLength;

    public LBFGSSegmentTrainer(ModelGraph model, FeatureGenerator featureGenerator, int maxSegmentLength) {
        super(model, featureGenerator);
        this.maxSegmentLength=maxSegmentLength;
    }

    protected double computeFunctionGradient(Dataset diter, double lambda[], double grad[]) {
        DataSequence dataSeq;
        Feature feature;
        DoubleDenseMatrix Mi_YY;
        double[] alpha_Y[], beta_Y[], expF;
        double val, thisSeqLogli, logli, Zx;
        int stateNum, i, f, yp, yprev, base,ell, segmentStart, segmentEnd;
        boolean invalid, isSegment;

        try {
            if(doScaling)
                return computeFunctionGradientLL(diter,lambda,grad);

            stateNum=model.getStateNum();
            logli=0;
            alpha_Y=null;
            beta_Y = null;
            expF = new double[featureGenerator.getFeatureNum()];
            Mi_YY = new DoubleFlatDenseMatrix(stateNum, stateNum);

            for (f = 0; f < lambda.length; f++) {
                grad[f] = -1 * lambda[f] *invSigmaSquare;
                logli -= ( (lambda[f] * lambda[f])*invSigmaSquare) / 2;
            }
            diter.startScan();
            while(diter.hasNext()) {
                dataSeq = diter.next();
                for (f = 0; f < lambda.length; f++)
                    expF[f] = 0;

                base = -1;
                if ( (alpha_Y== null) || (alpha_Y.length < dataSeq.length() - base)) {
                    alpha_Y = new double[2 * dataSeq.length()][];
                    for (i = 0; i < alpha_Y.length; i++)
                        alpha_Y[i] = new double[stateNum];
                }
                if ( (beta_Y == null) || (beta_Y.length < dataSeq.length())) {
                    beta_Y = new double[2 * dataSeq.length()][];
                    for (i = 0; i < beta_Y.length; i++)
                        beta_Y[i] = new double[stateNum];
                }

                // compute beta values in a backward scan.
                MathUtil.initArray(beta_Y[dataSeq.length() - 1],1.0);
                for (i = dataSeq.length() - 2; i >= 0; i--) {
                    MathUtil.initArray(beta_Y[i],0);
                    for (ell = 1; (ell <= maxSegmentLength) && (i + ell < dataSeq.length()); ell++) {
                        // compute the Mi matrix
                        computeTransMatrix(lambda, dataSeq, i+1, i+ell, Mi_YY, true);
                        genStateVector(Mi_YY, beta_Y[i + ell], beta_Y[i], false); //beta_Y[i]=Mi_YY*beta_Y[i+ell]+beta_Y[i]
                    }
                }

                thisSeqLogli = 0;
                MathUtil.initArray(alpha_Y[0],1); //alpha_Y[0] initialization value, not the value for the first position in the sequence
                segmentStart = 0;
                segmentEnd = -1;
                invalid = false;
                for (i = 0; i < dataSeq.length(); i++) {
                    if (segmentEnd < i) {
                        segmentStart = i;
                        segmentEnd = dataSeq.getSegmentEnd(i);
                    }
                    if (segmentEnd - segmentStart + 1 >maxSegmentLength) {
                        invalid = true;
                        break;
                    }

                    MathUtil.initArray(alpha_Y[i - base],0);
                    for (ell = 1; (ell <= maxSegmentLength) && (i - ell >= base); ell++) {
                        // compute the Mi matrix
                        computeTransMatrix(lambda,dataSeq, i-ell+1, i, Mi_YY,  true);

                        // find features that fire at this position..
                        featureGenerator.startScanFeaturesAt(dataSeq, i - ell+1, i);
                        isSegment = ( (i - ell + 1 == segmentStart) && (i == segmentEnd));
                        while (featureGenerator.hasNext()) {
                            feature = featureGenerator.next();
                            f = feature.getIndex();
                            yp = feature.getLabel();
                            yprev = feature.getPrevLabel();
                            val = feature.getValue();

                            if (isSegment && (dataSeq.getLabel(i) == yp) && ( ( (i - ell >= 0) && (yprev == dataSeq.getLabel(i - ell))) || (yprev < 0))) {
                                grad[f] += val;//accumulate F(yk,xk)
                                thisSeqLogli += val * lambda[f];
                            }

                            if (yprev < 0) //state feature
                            {
                                for (yprev = 0; yprev < Mi_YY.rows(); yprev++)
                                    expF[f] +=val*alpha_Y[i - ell -base][yprev] * Mi_YY.getDouble(yprev, yp)*beta_Y[i][yp];
                            }
                            else //edge feature
                                expF[f] +=val*alpha_Y[i - ell -base][yprev] * Mi_YY.getDouble(yprev, yp)*beta_Y[i][yp];
                        }

                        //alpha_Y[i-base]=alpha_Y[i-base]+alpha_Y[i-ell-base]*Mi_YY
                        genStateVector(Mi_YY, alpha_Y[i - ell - base], alpha_Y[i - base], true);
                    }
                }

                if (invalid) continue;
                Zx = MathUtil.sumArray(alpha_Y[dataSeq.length() - 1 - base]);
                thisSeqLogli -= Math.log(Zx);
                logli += thisSeqLogli;

                // update the gradident.
                for (f = 0; f < grad.length; f++)
                    grad[f] -= expF[f]/Zx;
            }
            return logli;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return 0;
        }
    }

    protected double computeFunctionGradientLL(Dataset diter, double lambda[], double grad[]) {
        DataSequence dataSeq;
        DoubleDenseMatrix Mi_YY;
        Feature feature;
        double[] alpha_Y[], beta_Y[];
        double expF[];
        double val, thisSeqLogli, logli, lZx;
        int stateNum, i, f, yp, yprev, base,ell, segmentStart, segmentEnd;
        boolean invalid, isSegment;

        try {
            logli=0;
            stateNum=model.getStateNum();
            alpha_Y=null;
            beta_Y = null;
            expF = new double[featureGenerator.getFeatureNum()];
            Mi_YY = new DoubleFlatDenseMatrix(stateNum, stateNum);

            for (f = 0; f < lambda.length; f++) {
                grad[f] = -1 * lambda[f] * invSigmaSquare;
                logli -= ( (lambda[f] * lambda[f]) * invSigmaSquare) / 2;
            }

            diter.startScan();
            while(diter.hasNext()) {
                dataSeq = diter.next();
                for (f = 0; f < lambda.length; f++) {
                    expF[f] = MathUtil.LOG0;

                }
                base = -1;
                if ( (alpha_Y== null) || (alpha_Y.length < dataSeq.length() - base)) {
                    alpha_Y = new double[2 * dataSeq.length()][];
                    for (i = 0; i < alpha_Y.length; i++)
                        alpha_Y[i] = new double[stateNum];
                }
                if ( (beta_Y == null) || (beta_Y.length < dataSeq.length())) {
                    beta_Y = new double[2 * dataSeq.length()][];
                    for (i = 0; i < beta_Y.length; i++)
                        beta_Y[i] = new double[stateNum];
                }

                // compute beta values in a backward scan.
                MathUtil.initArray(beta_Y[dataSeq.length() - 1],0);
                for (i = dataSeq.length() - 2; i >= 0; i--) {
                    MathUtil.initArray(beta_Y[i],MathUtil.LOG0);
                    for (ell = 1; ell <=maxSegmentLength && (i + ell < dataSeq.length()); ell++) {
                        // compute the Mi matrix
                        computeTransMatrix(lambda, dataSeq, i+1, i+ell, Mi_YY, false);
                        genStateVectorLog(Mi_YY, beta_Y[i + ell], beta_Y[i], false);
                    }
                }

                thisSeqLogli = 0;
                MathUtil.initArray(alpha_Y[0],0);
                segmentStart = 0;
                segmentEnd = -1;
                invalid = false;

                for (i = 0; i < dataSeq.length(); i++) {
                    if (segmentEnd < i) {
                        segmentStart = i;
                        segmentEnd = dataSeq.getSegmentEnd(i);
                    }
                    if (segmentEnd - segmentStart + 1 >maxSegmentLength) {
                        invalid = true;
                        break;
                    }
                    MathUtil.initArray(alpha_Y[i - base], MathUtil.LOG0);
                    for (ell = 1; (ell <= maxSegmentLength) && (i - ell >= base); ell++) {
                        // compute the Mi matrix
                        computeTransMatrix(lambda, dataSeq, i-ell+1, i, Mi_YY,false);

                        // find features that fire at this position..
                        featureGenerator.startScanFeaturesAt(dataSeq, i - ell, i);
                        isSegment = ( (i - ell + 1 == segmentStart) && (i == segmentEnd));
                        while (featureGenerator.hasNext()) {
                            feature = featureGenerator.next();
                            f = feature.getIndex();
                            yp = feature.getLabel();
                            yprev = feature.getPrevLabel();
                            val = feature.getValue();

                            if (isSegment && (dataSeq.getLabel(i) == yp) && ( ( (i - ell >= 0) && (yprev == dataSeq.getLabel(i - ell))) || (yprev < 0))) {
                                grad[f] += val;
                                thisSeqLogli += val * lambda[f];
                            }

                            if(yprev < 0) {
                                for (yprev = 0; yprev < Mi_YY.rows(); yprev++) {
                                    expF[f] = MathUtil.logSumExp(expF[f],
                                        (alpha_Y[i - ell - base][yprev] + Mi_YY.getDouble(yprev, yp) +MathUtil.log(val) + beta_Y[i][yp]));
                                }
                            }
                            else
                                expF[f] = MathUtil.logSumExp(expF[f],alpha_Y[i - ell - base][yprev] + Mi_YY.getDouble(yprev, yp) +MathUtil.log(val) + beta_Y[i][yp]);
                        }

                        //alpha_Y[i-base]=alpha_Y[i-base]+alpha_Y[i-ell-base]*Mi_YY
                        genStateVectorLog(Mi_YY, alpha_Y[i - ell - base], alpha_Y[i - base], true);
                    }
                }

                if (invalid) continue;
                lZx =MathUtil.logSumExp(alpha_Y[dataSeq.length() - 1 - base]);
                thisSeqLogli -= lZx;
                logli += thisSeqLogli;

                // update gradient
                for (f = 0; f < grad.length; f++) {
                    grad[f] -= MathUtil.exp(expF[f] - lZx);
                }
            }
            return logli;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return 0;
        }
    }
};
