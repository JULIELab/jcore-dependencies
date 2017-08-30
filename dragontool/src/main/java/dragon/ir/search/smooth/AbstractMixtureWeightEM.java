package dragon.ir.search.smooth;

import dragon.ir.index.*;
import dragon.ir.query.*;
import java.io.*;
import java.util.ArrayList;

/**
 * <p>Abstract EM Algorithm for Mixture Weights Estimation</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractMixtureWeightEM {
    protected IndexReader indexReader;
    protected int iterationNum;
    protected int componentNum;
    private PrintWriter statusOut;
    private boolean docFirst;

    public AbstractMixtureWeightEM(IndexReader indexReader, int componentNum, int iterationNum, boolean docFirst) {
        this.indexReader =indexReader;
        this.iterationNum=iterationNum;
        this.componentNum =componentNum;
        this.docFirst =docFirst;
    }

    abstract protected void setInitialParameters(double[] arrCoefficient, IRDoc[] arrDoc);
    abstract protected void init(RelSimpleQuery query);
    abstract protected void setDoc(IRDoc doc);
    abstract protected void setQueryTerm(SimpleTermPredicate queryTerm);
    abstract protected void getComponentValue(SimpleTermPredicate queryTerm, int freq, double[] componentProbs);
    abstract protected void getComponentValue(IRDoc curDoc, int freq, double[] componentProbs);

    public void setStatusOut(PrintWriter out){
        this.statusOut =out;
    }

    public double[] estimateModelCoefficient(RelSimpleQuery query){
        if(docFirst)
            return breadthFirstEstimate(query);
        else
            return depthFirstEstimate(query);
    }

    private double[] breadthFirstEstimate(RelSimpleQuery query){
        SimpleTermPredicate[] arrPredicate;
        IRTerm docTerm;
        IRDoc[] arrDoc;
        double[] arrPreParam, arrParam, arrParamDocSum;
        double[] arrDocWeight, arrComp;
        double allDocSum, docSum, termProb;
        int docNum, termNum;
        int i,j,k,m;

        //initialization
        arrPredicate=checkSimpleTermQuery(query);
        init(query);

        arrPreParam=new double[componentNum];
        arrParam=new double[componentNum];
        arrParamDocSum=new double[componentNum];
        arrComp=new double[componentNum];

        termNum=arrPredicate.length;
        docNum=getDocNum();
        arrDocWeight=new double[docNum];
        arrDoc=new IRDoc[docNum];

        setInitialParameters(arrPreParam,arrDoc);

        //compute coefficients for mixed components
        printStatus("Estimating the coefficients of the mixed model...");
        for(k=0;k<iterationNum;k++){
            printStatus("Iteration #"+(k+1));
            allDocSum=0;
            for(m=0;m<componentNum;m++) arrParam[m]=0;

            for (i = 0; i < docNum; i++) {
                docSum=arrDoc[i].getWeight();
                for(m=0;m<componentNum;m++) arrParamDocSum[m]=0;
                setDoc(arrDoc[i]);

                for(j=0;j<termNum;j++){
                    docTerm=indexReader.getIRTerm(arrPredicate[j].getIndex(),i);
                    getComponentValue(arrPredicate[j],docTerm.getFrequency(),arrComp);
                    termProb=0;
                    for(m=0;m<componentNum;m++){
                        arrComp[m] = arrPreParam[m]*arrComp[m];
                        termProb+=arrComp[m];
                    }
                    docSum=docSum*termProb;
                    for(m=0;m<componentNum;m++) arrParamDocSum[m]+=arrComp[m]/termProb;
                }
                for(m=0;m<componentNum;m++) arrParam[m]+=arrDoc[i].getWeight()*arrParamDocSum[m];
                arrDocWeight[i]=docSum;
                allDocSum+=arrDocWeight[i];
            }
            for(m=0;m<componentNum;m++)
            {
                arrPreParam[m] = arrParam[m] / termNum;
                printStatus("Coefficient #"+(m+1)+" "+arrPreParam[m]);
            }
            for(m=0;m<docNum;m++)
                arrDoc[m].setWeight(arrDocWeight[m]/allDocSum);
        }
        printStatus("");
        return arrPreParam;
    }

    private double[] depthFirstEstimate(RelSimpleQuery query){
        SimpleTermPredicate[] arrPredicate;
        IRDoc arrDoc[];
        double[] arrDocWeight, arrComp;
        double[] arrPreParam, arrParam;
        double termProb, allSum;
        int[] arrFreq, arrIndex;
        int termNum, docNum, count,i,j,k, m;

        //initialization
        arrPredicate=checkSimpleTermQuery(query);
        init(query);

        arrPreParam=new double[componentNum];
        arrParam=new double[componentNum];
        arrComp=new double[componentNum];

        termNum=arrPredicate.length;
        docNum=getDocNum();
        arrDocWeight=new double[docNum];
        arrDoc=new IRDoc[docNum];

        setInitialParameters(arrPreParam,arrDoc);

        printStatus("Estimating the coefficients of the mixed model...");
        for(count=0;count<iterationNum;count++){
            printStatus("Iteration #"+(count+1));
            for (i = 0; i < docNum; i++) arrDocWeight[i] = arrDoc[i].getWeight();
            for(m=0;m<componentNum;m++) arrParam[m]=0;

            for (i = 0; i < arrPredicate.length; i++) {
                setQueryTerm(arrPredicate[i]);
                arrIndex = indexReader.getTermDocIndexList(arrPredicate[i].getIndex());
                arrFreq = indexReader.getTermDocFrequencyList(arrPredicate[i].getIndex());
                k = 0;
                for (j = 0; j < arrIndex.length; j++) {
                    while (k < arrIndex[j]) {
                        getComponentValue(arrDoc[k],0,arrComp);
                        termProb=0;
                        for (m = 0; m < componentNum; m++) {
                            arrComp[m] = arrPreParam[m] * arrComp[m];
                            termProb += arrComp[m];
                        }
                        arrDocWeight[k] = arrDocWeight[k] * termProb;
                        for(m=0;m<componentNum;m++) arrParam[m]+=arrDoc[k].getWeight()*arrComp[m]/termProb;
                        k++;
                    }

                    getComponentValue(arrDoc[k],arrFreq[j],arrComp);
                    termProb=0;
                    for (m = 0; m < componentNum; m++) {
                        arrComp[m] = arrPreParam[m] * arrComp[m];
                        termProb += arrComp[m];
                    }
                    arrDocWeight[k] = arrDocWeight[k] * termProb;
                    for(m=0;m<componentNum;m++) arrParam[m]+=arrDoc[k].getWeight()*arrComp[m]/termProb;
                    k++;
                }
                while(k<docNum){
                    getComponentValue(arrDoc[k],0,arrComp);
                        termProb=0;
                        for (m = 0; m < componentNum; m++) {
                            arrComp[m] = arrPreParam[m] * arrComp[m];
                            termProb += arrComp[m];
                        }
                        arrDocWeight[k] = arrDocWeight[k] * termProb;
                        for(m=0;m<componentNum;m++) arrParam[m]+=arrDoc[k].getWeight()*arrComp[m]/termProb;
                        k++;
                }
            }

            //update the parameter estimation
            for(m=0;m<componentNum;m++)
            {
                arrPreParam[m] = arrParam[m] / termNum;
                printStatus("Coefficient #"+(m+1)+" "+arrPreParam[m]);
            }
            allSum=0;
            for (i = 0; i < docNum; i++) allSum+=arrDocWeight[i];
            for (i = 0; i < docNum; i++) arrDoc[i].setWeight(arrDocWeight[i]/allSum);
        }

        printStatus("");
        return arrPreParam;
    }


    protected int getDocNum(){
        return indexReader.getCollection().getDocNum();
    }

    protected IRDoc getDoc(int seq){
        return indexReader.getDoc(seq);
    }

    private void printStatus(String line){
        try{
            System.out.println(line);
            if(statusOut!=null){
                statusOut.write(line + "\n");
                statusOut.flush();
            }
        }
        catch(Exception e){
            e.printStackTrace() ;
        }
    }

    private SimpleTermPredicate[] checkSimpleTermQuery(RelSimpleQuery query){
        SimpleTermPredicate predicate, arrPredicate[];
        IRTerm curIRTerm;
        ArrayList list;
        int i;

        list=new ArrayList();
        for(i=0;i<query.getChildNum();i++){
            if(((Predicate)query.getChild(i)).isTermPredicate()){
                predicate = (SimpleTermPredicate) query.getChild(i);
                if (predicate.getDocFrequency()==0) {
                    curIRTerm=indexReader.getIRTerm(predicate.getKey());
                    if(curIRTerm!=null){
                        predicate.setDocFrequency(curIRTerm.getDocFrequency());
                        predicate.setFrequency(curIRTerm.getFrequency());
                        predicate.setIndex(curIRTerm.getIndex());
                    }
                }
                if(predicate.getDocFrequency()>0){
                    list.add(predicate);
                }
            }
        }
        arrPredicate=new SimpleTermPredicate[list.size()];
        for(i=0;i<list.size();i++){
            arrPredicate[i] = ( (SimpleTermPredicate) list.get(i)).copy();
        }
        return arrPredicate;
    }
}