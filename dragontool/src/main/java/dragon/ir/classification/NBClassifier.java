package dragon.ir.classification;

import dragon.ir.classification.featureselection.*;
import dragon.ir.index.*;
import dragon.matrix.*;
import dragon.matrix.vector.DoubleVector;
import java.io.*;

/**
 * <p>Naive Bayesian classifier which uses Laplacian smoothing</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class NBClassifier extends AbstractClassifier{
    protected DoubleFlatDenseMatrix model;
    protected DoubleVector classPrior, lastClassProb;
    private int[] rank;

    public NBClassifier(String modelFile){
        ObjectInputStream oin;
        int i;

        try{
            oin = new ObjectInputStream(new FileInputStream(modelFile));
            model=(DoubleFlatDenseMatrix)oin.readObject();
            classPrior=(DoubleVector)oin.readObject();
            classNum=classPrior.size();
            featureSelector=(FeatureSelector)oin.readObject();
            arrLabel=new String[model.rows()];
            for(i=0;i<arrLabel.length;i++)
                arrLabel[i]=(String)oin.readObject();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public NBClassifier(IndexReader indexReader) {
        super(indexReader);
    }

    public NBClassifier(SparseMatrix doctermMatrix) {
       super(doctermMatrix);
    }

    public void train(DocClassSet trainingDocSet){
        DocClass cur;
        IRDoc curDoc;
        Row row;
        int i, j, k, classSum, newTermIndex;
        double rate;

        if(indexReader==null && doctermMatrix==null)
        	return;

        classNum=trainingDocSet.getClassNum();
        classPrior=getClassPrior(trainingDocSet);
        trainFeatureSelector(trainingDocSet);
        arrLabel=new String[classNum];
        for(i=0;i<classNum;i++)
            arrLabel[i]=trainingDocSet.getDocClass(i).getClassName();
        model=new DoubleFlatDenseMatrix(classNum,featureSelector.getSelectedFeatureNum());
        model.assign(1);
        for(i=0;i<classNum;i++){
            classSum=featureSelector.getSelectedFeatureNum();
            cur=trainingDocSet.getDocClass(i);
            for(j=0;j<cur.getDocNum();j++){
                curDoc=cur.getDoc(j);
                row=getRow(curDoc.getIndex());
                for(k=0;k<row.getNonZeroNum();k++){
                    newTermIndex=featureSelector.map(row.getNonZeroColumn(k));
                    if(newTermIndex>=0){
                        classSum+=row.getNonZeroDoubleScore(k);
                        model.add(i,newTermIndex,row.getNonZeroDoubleScore(k));
                    }
                }
            }

            rate=1.0/classSum;
            for(k=0;k<model.columns();k++)
                model.setDouble(i,k,Math.log(model.getDouble(i,k)*rate)); // attention: log is used
        }
    }

    protected DoubleVector getClassPrior(DocClassSet docSet){
        DoubleVector vector;
        int i, sum;

        sum=docSet.getClassNum();
        vector=new DoubleVector(docSet.getClassNum());
        vector.assign(1);
        for(i=0;i<docSet.getClassNum();i++){
            vector.set(i,docSet.getDocClass(i).getDocNum());
            sum+=docSet.getDocClass(i).getDocNum();
        }
        for(i=0;i<docSet.getClassNum();i++)
            vector.set(i, Math.log(vector.get(i)/sum)); //attention: log is used
        return vector;
    }

    public int classify(IRDoc doc){
    	int label;
    	
    	label=classify(getRow(doc.getIndex()));
    	doc.setWeight(lastClassProb.get(label));
    	return label;
    }

    public int classify(Row doc){
        
        int newTermIndex, classNum, k, j;

        lastClassProb=classPrior.copy();
        classNum=model.rows();
        for(k=0;k<doc.getNonZeroNum();k++){
            newTermIndex=featureSelector.map(doc.getNonZeroColumn(k));
            if(newTermIndex>=0){
                for(j=0;j<classNum;j++)
                    lastClassProb.add(j, doc.getNonZeroDoubleScore(k)*model.getDouble(j,newTermIndex));
            }
        }
        rank=lastClassProb.rank(true);
        return rank[0];
    }
    
    public int[] rank(){
    	return rank;
    }

    public void saveModel(String modelFile){
        ObjectOutputStream out;
        int i;

        try{
            out=new ObjectOutputStream(new FileOutputStream(modelFile));
            out.writeObject(model);
            out.writeObject(classPrior);
            out.writeObject(featureSelector);
            for(i=0;i<model.rows();i++)
                out.writeObject(getClassLabel(i));
            out.flush();
            out.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}