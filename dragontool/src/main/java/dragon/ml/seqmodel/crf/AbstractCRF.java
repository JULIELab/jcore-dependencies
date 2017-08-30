package dragon.ml.seqmodel.crf;

import dragon.matrix.DoubleDenseMatrix;
import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.feature.Feature;
import dragon.ml.seqmodel.feature.FeatureGenerator;
import dragon.ml.seqmodel.model.ModelGraph;
import java.io.*;

/**
 * <p>Abstract class for conditional random field </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractCRF {
    protected EdgeGenerator edgeGen;
    protected FeatureGenerator featureGenerator;
    protected ModelGraph model;
    protected double lambda[];

    public AbstractCRF(ModelGraph model, FeatureGenerator featureGen) {
        this.model =model;
        edgeGen=new EdgeGenerator(model.getMarkovOrder(),model.getOriginalLabelNum());
        this.featureGenerator =featureGen;
    }

    public FeatureGenerator getFeatureGenerator(){
        return featureGenerator;
    }

    public ModelGraph getModelGraph(){
        return model;
    }

    public double[] getModelParameter(){
        return lambda;
    }

    public boolean saveModelParameter(String filename){
        PrintWriter out;

        try{
            out = new PrintWriter(new FileOutputStream(filename));
            out.println(lambda.length);
            for (int i = 0; i < lambda.length; i++)
                out.println(lambda[i]);
            out.close();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean readModelParameter(String filename){
        BufferedReader in;
        String line;
        int pos, featureNum;

        try{
            in=new BufferedReader(new FileReader(filename));
            featureNum = Integer.parseInt(in.readLine());
            lambda = new double[featureNum];
            pos=0;
            while ( (line = in.readLine()) != null) {
                lambda[pos++] = Double.parseDouble(line);
            }
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    protected void computeTransMatrix(double lambda[],DataSequence data, int startPos, int endPos, DoubleDenseMatrix transMatrix, boolean takeExp){
        featureGenerator.startScanFeaturesAt(data,startPos, endPos);
        computeTransMatrix(lambda,transMatrix,takeExp);
    }

    protected void computeTransMatrix(double lambda[],DoubleDenseMatrix transMatrix, boolean takeExp) {
        Feature feature;
        double stateFeatureCost[];
        int label, index;
        int i, j, stateNum;

        stateNum=transMatrix.rows();
        stateFeatureCost=new double[stateNum];
        transMatrix.assign(0);
        while (featureGenerator.hasNext()) {
            feature = featureGenerator.next();
            label= feature.getLabel();
            index=feature.getIndex();

            if (feature.getPrevLabel()< 0)
                // this is a single state feature.
                stateFeatureCost[label]+=lambda[index]*feature.getValue();
            else
                // this is a edge feature
                transMatrix.add(feature.getPrevLabel(),label,lambda[index]*feature.getValue());
        }

        for(i=0;i<stateNum;i++){
            for(j=0;j<stateNum;j++)
                transMatrix.setDouble(j,i,transMatrix.getDouble(j,i)+stateFeatureCost[i]);
        }

        if (takeExp) {
            for (i = 0; i < stateNum; i++)
                for (j = 0; j < stateNum; j++)
                    transMatrix.setDouble(i, j, Math.exp(transMatrix.getDouble(i,j)));
        }
    }
}