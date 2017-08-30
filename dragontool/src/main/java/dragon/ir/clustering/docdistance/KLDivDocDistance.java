package dragon.ir.clustering.docdistance;

import dragon.ir.index.*;
import dragon.matrix.*;

/**
 * <p>Document distance measure of using KL divergence</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class KLDivDocDistance extends AbstractDocDistance{
    private double bkgCoefficient, normThreshold;
    private double[] arrBkgModel;
    private boolean norm;

    public KLDivDocDistance(DoubleSparseMatrix docModelMatrix){
        this(docModelMatrix,0);
    }

    public KLDivDocDistance(DoubleSparseMatrix doctermMatrix, double normThreshold){
        super(doctermMatrix);

        int i, termNum;
        this.bkgCoefficient =0.1;
        termNum=matrix.columns();
        arrBkgModel=new double[termNum];
        for(i=0;i<termNum;i++)
            arrBkgModel[i]=bkgCoefficient/termNum;
        if(normThreshold>0){
            norm = true;
            this.normThreshold =normThreshold;
        }
        else{
            norm=false;
            this.normThreshold =0;
        }
    }

    public KLDivDocDistance(IndexReader indexReader, DoubleSparseMatrix docModelMatrix){
        this(indexReader,docModelMatrix,0);
    }

    public KLDivDocDistance(IndexReader indexReader, DoubleSparseMatrix doctermMatrix, double normThreshold) {

        super(doctermMatrix);

        double totalTermCount;
        int i, termNum;

        this.bkgCoefficient =0.1;
        termNum=indexReader.getCollection().getTermNum();
        arrBkgModel=new double[termNum];
        totalTermCount=indexReader.getCollection().getTermCount();
        for(i=0;i<termNum;i++)
            arrBkgModel[i]=indexReader.getIRTerm(i).getFrequency()/totalTermCount*bkgCoefficient;
        if(normThreshold>0){
            norm = true;
            this.normThreshold =normThreshold;
        }
        else{
            norm=false;
            this.normThreshold =0;
        }
    }

    public double getDistance(IRDoc first, IRDoc second){
        double[] arrFirstScore, arrSecondScore;
        double distance, firstProb, secondProb;
        int[] arrFirstIndex,arrSecondIndex;
        int i, j, firstCount, secondCount;

        arrFirstIndex=matrix.getNonZeroColumnsInRow(first.getIndex());
        arrFirstScore=matrix.getNonZeroDoubleScoresInRow(first.getIndex());
        firstCount=matrix.getNonZeroNumInRow(first.getIndex());
        arrSecondIndex=matrix.getNonZeroColumnsInRow(second.getIndex());
        arrSecondScore=matrix.getNonZeroDoubleScoresInRow(second.getIndex());
        secondCount=matrix.getNonZeroNumInRow(second.getIndex());
        distance=0;
        i=0;
        j=0;

        while(i<firstCount){
            while(j<secondCount && arrSecondIndex[j]<arrFirstIndex[i]) j++;
            if(j>=secondCount || arrSecondIndex[j]!=arrFirstIndex[i])
                secondProb=arrBkgModel[arrFirstIndex[i]];
            else
                secondProb=(1-bkgCoefficient)*arrSecondScore[j]+arrBkgModel[arrFirstIndex[i]];
            firstProb=(1-bkgCoefficient)*arrFirstScore[i]+arrBkgModel[arrFirstIndex[i]];
            distance += firstProb *Math.log(firstProb/secondProb);
            i++;
        }

        if(norm){
            if(distance>=normThreshold)
                return 1;
            else
                return distance/normThreshold;
        }
        else
            return distance;
    }
}