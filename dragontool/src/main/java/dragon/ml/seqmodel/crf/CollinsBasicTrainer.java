package dragon.ml.seqmodel.crf;

import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.data.Dataset;
import dragon.ml.seqmodel.feature.Feature;
import dragon.ml.seqmodel.feature.FeatureGenerator;
import dragon.ml.seqmodel.model.ModelGraph;
import dragon.util.MathUtil;

/**
 * <p>Collins training conditional random field</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CollinsBasicTrainer extends AbstractTrainer {
    protected int topSolutions;
    protected double beta;
    protected boolean useUpdated;

    public CollinsBasicTrainer(ModelGraph model, FeatureGenerator featureGenerator) {
        super(model,featureGenerator);
        topSolutions=Math.min(3,model.getStateNum());
        beta=0.05;
        useUpdated=false;
    }

    public boolean train(Dataset dataset) {
        Labeler labeler;
        DataSequence manualSeq, autoSeq, solutions[];
        int trainingCount, featureNum, numErrs, solutionNum;
        int t,k,s, startPos, endPos, autoStartPos[], autoEndPos;
        double curScore, correctScore, lambdaSum[], lambdaAvg[];
        boolean different;

        //convert labels to states
        dataset.startScan();
        while(dataset.hasNext())
            model.mapLabelToState(dataset.next());

        //train features
        if(!featureGenerator.train(dataset))
            return false;
        featureNum=featureGenerator.getFeatureNum();
        lambda=new double[featureNum];
        lambdaAvg=new double[featureNum];
        lambdaSum=new double[featureNum];
        MathUtil.initArray(lambda,0);
        MathUtil.initArray(lambdaAvg,0);
        MathUtil.initArray(lambda,0);
        labeler=getLabeler();
        solutions= new DataSequence[topSolutions];
        autoStartPos=new int[topSolutions];
        trainingCount=0;

        for (t = 0; t < maxIteration; t++) {
            numErrs=0;
            dataset.startScan();
            while(dataset.hasNext()) {
                if(trainingCount>0){
                    MathUtil.copyArray(lambdaSum, lambdaAvg);
                    MathUtil.multiArray(lambdaAvg,1.0/trainingCount);
                }
                MathUtil.initArray(autoStartPos,0);
                manualSeq = dataset.next();
                autoSeq=manualSeq.copy();
                labeler.label(autoSeq, (useUpdated) ? lambdaAvg:lambda);
                correctScore= getSequenceScore(manualSeq, (useUpdated) ? lambdaAvg:lambda);
                solutionNum=0;
                for (k = 0; k <topSolutions; k++) {
                    autoSeq=manualSeq.copy();
                    curScore= labeler.getBestSolution(autoSeq,k);
                    if (curScore < correctScore * (1 - beta)) {
                        break;
                    }
                    model.mapLabelToState(autoSeq);
                    if (!isCorrect(manualSeq, autoSeq)) {
                        solutions[solutionNum]=autoSeq;
                        solutionNum++;
                    }
                }

                if (solutionNum > 0) {
                    startPos=model.getMarkovOrder()-1;
                    while(startPos<manualSeq.length()){
                        endPos=getSegmentEnd(manualSeq,startPos);
                        different=false;
                        for (s = 0; s <solutionNum; s++) {
                             if (autoStartPos[s]!=startPos || getSegmentEnd(solutions[s],autoStartPos[s])!=endPos
                                  || manualSeq.getLabel(endPos)!=solutions[s].getLabel(endPos)) {
                                different = true;
                                break;
                            }
                        }
                        if (different) {
                            numErrs++;
                            updateWeights(manualSeq, startPos, endPos, 1.0, lambda);
                            for (s = 0; s < solutionNum; s++) {
                                // if within current frontier, i.e. starting point overlaps with current segment
                                while(autoStartPos[s]<=endPos){
                                    autoEndPos=getSegmentEnd(solutions[s],autoStartPos[s]);
                                    updateWeights(solutions[s],autoStartPos[s],autoEndPos, -1.0 /solutionNum, lambda);
                                    autoStartPos[s]=autoEndPos+1;
                                }
                            }
                        }
                        {
                            //advance all solutions
                            for (s = 0; s < solutionNum; s++) {
                                // if within current frontier, i.e. starting point overlaps with current segment
                                while (autoStartPos[s] <= endPos) {
                                    autoEndPos = getSegmentEnd(solutions[s], autoStartPos[s]);
                                    autoStartPos[s] = autoEndPos + 1;
                                }
                            }
                        }
                        startPos=endPos+1;
                    }
                }

                // voted perceptron, so add.
                MathUtil.sumArray(lambdaSum, lambda);
                trainingCount++;
            }

            System.out.println("Iteration " + t + " numErrs " + numErrs);
            if (numErrs == 0) {
                break;
            }
        }

        MathUtil.multiArray(lambdaSum,1.0/trainingCount);
        MathUtil.copyArray(lambdaSum,lambda);
        return true;
    }

    protected boolean isCorrect(DataSequence manual, DataSequence auto) {
        int i;
        for(i=0; i<manual.length(); i++){
            if(manual.getLabel(i)!=auto.getLabel(i))
                return false;
        }
        return true;
    }

    protected void updateWeights(DataSequence dataSeq, int startPos, int endPos, double wt, double grad[]) {
        Feature feature;
        int f, yp, yprev;

        featureGenerator.startScanFeaturesAt(dataSeq, startPos, endPos);
        while (featureGenerator.hasNext()) {
            feature = featureGenerator.next();
            f = feature.getIndex();
            yp = feature.getLabel();
            yprev = feature.getPrevLabel();

            if ((dataSeq.getLabel(endPos) == yp) && ((yprev < 0) || (yprev ==dataSeq.getLabel(startPos-1)))) {
                grad[f]+=wt*feature.getValue();
            }
        }
    }

    protected double getSequenceScore(DataSequence dataSeq, double[] grad) {
        Feature feature;
        double score;
        int f, yp, yprev;
        int startPos, endPos;

        startPos=model.getMarkovOrder()-1;
        score=0;
        while(startPos<dataSeq.length()){
            endPos=getSegmentEnd(dataSeq,startPos);
            featureGenerator.startScanFeaturesAt(dataSeq,startPos, endPos);
            while (featureGenerator.hasNext()) {
                feature = featureGenerator.next();
                f = feature.getIndex();
                yp = feature.getLabel();
                yprev = feature.getPrevLabel();
                if ((dataSeq.getLabel(endPos) == yp) && ((yprev < 0) || (yprev ==dataSeq.getLabel(startPos-1)))) {
                    score += grad[f] * feature.getValue();
                }
            }
            startPos=endPos+1;
        }
        return score;
    }

    protected Labeler getLabeler(){
        return new ViterbiBasicLabeler(model,featureGenerator);
    }

    protected int getSegmentEnd(DataSequence dataSeq, int start){
        return start;
    }
};
