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
 * <p>LBFGS basic trainer</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class LBFGSBasicTrainer extends AbstractTrainer{
    protected int mForHessian;
    protected double epsForConvergence, invSigmaSquare;

    public LBFGSBasicTrainer(ModelGraph model, FeatureGenerator featureGenerator) {
        super(model, featureGenerator);
        mForHessian=7;
        epsForConvergence=0.001;
        invSigmaSquare=0.01;
    }

    public void setGradientHistory(int history){
        mForHessian=history;
    }

    public void setAccuracy(int eps){
        this.epsForConvergence=eps;
    }

    public void setInvSigmaSquare(int invSigmaSquare){
        this.invSigmaSquare =invSigmaSquare;
    }

    public boolean train(Dataset dataset) {
        double gradLogli[], diag[], f;
        int iprint[], iflag[], icall, featureNum;

        //conver labels to states
        dataset.startScan();
        while(dataset.hasNext())
            model.mapLabelToState(dataset.next());

        //train features
        if(!featureGenerator.train(dataset))
            return false;
        featureNum=featureGenerator.getFeatureNum();
        lambda=new double[featureNum];
        gradLogli = new double[featureNum];
        diag = new double [featureNum];
        iprint= new int [2];
        iflag= new int[1];
        icall=0;
        iprint [0] =-1;
        iprint [1] =0;
        iflag[0]=0;

        for (int j = 0 ; j < lambda.length ; j ++) {
            lambda[j] =0; //initialize the parameters
        }

        do {
            f = computeFunctionGradient(dataset, lambda,gradLogli);
            System.out.println((new java.util.Date()).toString()+ " Iteration: " + icall + " log likelihood "+f + " norm(grad logli) " + norm(gradLogli) + " norm(x) "+ norm(lambda));

            // since the routine below minimizes and we want to maximize log likelihood
            f = -1*f;
            for (int j = 0 ; j < lambda.length ; j ++) {
                gradLogli[j] *= -1;
            }

            try	{
                //numF: number of features, i.e. variables
                //mForHessian: number of past gradients and updates (a number between 3 and 7 is recommended
                //lambda: variables containing initial values
                //f: value of objective function (here is the log likelihood
                //gradLogli: the gradient vector of the current iteration
                //false: the diagonal matrix Hk0 is provided by LBFGS
                //diag: Hk0. In this case, the LBFGS will handle it.
                //iprint:about output, not important
                //epsForConvergence: the accuracy with which the solution is to be found
                //xtol: machine precision
                //iflag: must be set to 0 in this case. the solution is found if the return value is 0
                LBFGS.lbfgs (featureNum, mForHessian, lambda, f, gradLogli, false, diag, iprint, epsForConvergence, xtol, iflag);
            }
            catch (LBFGS.ExceptionWithIflag e)  {
                System.err.println( "CRF: lbfgs failed.\n"+e );
                if (e.iflag == -1) {
                    System.err.println("Possible reasons could be: \n \t 1. Bug in the feature generation or data handling code\n\t 2. Not enough features to make observed feature value==expected value\n");
                }
                return false;
            }
            icall += 1;
        } while (( iflag[0] != 0) && (icall <= maxIteration));
        return true;
    }

    protected double norm(double ar[]) {
        double v = 0;
        for (int f = 0; f < ar.length; f++)
            v += ar[f] * ar[f];
        return Math.sqrt(v);
    }

    //the function below computes the gradient of the objective function (saved in grad[]) and returns the function value (saved in logli)
    protected double computeFunctionGradient(Dataset diter, double lambda[], double grad[]) {
        DataSequence dataSeq;
        DoubleDenseMatrix Mi_YY;
        Feature feature;
        double[] alpha_Y, newAlpha_Y, beta_Y[];
        double expF[], scale[];
        double val, thisSeqLogli, logli, Zx;
        int stateNum, markovOrder;
        int i, f, yp, yprev;


        logli=0;
        markovOrder=model.getMarkovOrder();
        stateNum=model.getStateNum();
        alpha_Y=new double[stateNum];
        newAlpha_Y=new double[stateNum];
        beta_Y=null;
        scale=null;
        expF = new double[featureGenerator.getFeatureNum()];
        Mi_YY=new DoubleFlatDenseMatrix(stateNum,stateNum);

        try {
            // calculate the spherical Gaussian weight prior for avoiding overfitting.
            for (f = 0; f < lambda.length; f++) {
                grad[f] = -1*lambda[f]*invSigmaSquare;
                logli -= ((lambda[f]*lambda[f])*invSigmaSquare)/2;
            }

            diter.startScan();
            while(diter.hasNext()) {
                dataSeq = (DataSequence)diter.next();
                MathUtil.initArray(alpha_Y,1); // initialize forward state-cost vector
                for (f = 0; f < lambda.length; f++)
                    expF[f] = 0; //store the expectation of F(Y, x) for current data sequence

                if ((beta_Y == null) || (beta_Y.length < dataSeq.length())) {
                    beta_Y = new double[2*dataSeq.length()][];
                    for (i = 0; i < beta_Y.length; i++)
                        beta_Y[i] = new double[stateNum];
                    scale = new double[2*dataSeq.length()];
                }

                // compute beta values in a backward scan.
                // also scale beta-values to 1 to avoid numerical problems.
                scale[dataSeq.length()-1] = (doScaling)?stateNum:1;
                MathUtil.initArray(beta_Y[dataSeq.length()-1],1.0/scale[dataSeq.length()-1]);
                for (i = dataSeq.length()-1; i >markovOrder-1; i--) {
                    // compute the Mi matrix and Beta(i-1)
                    computeTransMatrix(lambda,dataSeq,i,i,Mi_YY,true);
                    MathUtil.initArray(beta_Y[i-1],0);
                    genStateVector(Mi_YY, beta_Y[i], beta_Y[i-1],false); //beta_Y[i-1]=Mi_YY*beta_Y[i]

                    // need to scale the beta-s to avoid overflow
                    scale[i-1] = doScaling ? MathUtil.sumArray(beta_Y[i-1]):1;
                    if ((scale[i-1] < 1) && (scale[i-1] > -1))
                        scale[i-1] = 1;
                    MathUtil.multiArray(beta_Y[i-1], 1.0/scale[i-1]);
                }

                //calculate F(yk, xk) and expF(Y, xk)
                thisSeqLogli = 0;
                for (i = markovOrder-1; i < dataSeq.length(); i++) {
                    // compute the Mi matrix and new alpha (forward)
                    computeTransMatrix(lambda,dataSeq,i,i,Mi_YY,true);
                    MathUtil.initArray(newAlpha_Y,0);
                    genStateVector(Mi_YY, alpha_Y, newAlpha_Y,true); //newAlpha_Y=transpose(alpha_Y*Mi_YY)

                    featureGenerator.startScanFeaturesAt(dataSeq, i,i);
                    while (featureGenerator.hasNext()) {
                        feature = featureGenerator.next();
                        f = feature.getIndex();
                        yp = feature.getLabel();
                        yprev = feature.getPrevLabel();
                        val = feature.getValue();

                        if ((dataSeq.getLabel(i) == yp) && (((i-1 >= 0) && (yprev == dataSeq.getLabel(i-1))) || (yprev < 0))) {
                            grad[f] += val; //accumulate F(yk,xk)
                            thisSeqLogli += val*lambda[f];
                        }
                        if (yprev < 0)
                            expF[f] += val*newAlpha_Y[yp]*beta_Y[i][yp]; //state feature
                        else
                            expF[f] += val*alpha_Y[yprev]*Mi_YY.getDouble(yprev,yp)*beta_Y[i][yp]; //transition feature
                    }
                    MathUtil.copyArray(newAlpha_Y, alpha_Y);

                    // now scale the alpha-s to avoid overflow problems.
                    MathUtil.multiArray(alpha_Y, 1.0/scale[i]);
                }

                Zx = MathUtil.sumArray(alpha_Y);
                thisSeqLogli -= Math.log(Zx);

                // correct for the fact that alpha-s were scaled.
                for (i = markovOrder-1; i < dataSeq.length(); i++) {
                    thisSeqLogli -= Math.log(scale[i]);
                }
                logli += thisSeqLogli;

                // update the gradient.
                for (f = 0; f < grad.length; f++)
                    grad[f] -= expF[f]/Zx;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return logli;
    }
}
