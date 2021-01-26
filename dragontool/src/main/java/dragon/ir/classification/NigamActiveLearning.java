package dragon.ir.classification;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IRTerm;
import dragon.ir.index.IndexReader;
import dragon.matrix.DoubleFlatDenseMatrix;
import dragon.matrix.IntRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * <p>Nigam Active Learning which can utilize unlabeled documents during text classification</p>
 * <p>Please refer to the following paper for details:<br>
 * Nigam, K., McCallum, A., Thrun, S., Mitchell, T. <em>Text Classification from Labeled and Unlabeled Documents using EM</em>,
 * Machine Learning, Volume 39, Issue 2-3 (May-June 2000), pp103-134</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class NigamActiveLearning extends NBClassifier{
    private IntRow[] externalUnlabeled;
    private DocClass unlabeledSet, unlabeledSetBackup;
    private int externalDocOffset;
    private double convergeThreshold;
    private double unlabeledRate;
    private int runNum;

    public NigamActiveLearning(String modelFile){
    	super(modelFile);
    }

    public NigamActiveLearning(IndexReader indexReader, double unlabeledRate) {
        super(indexReader);
        this.externalDocOffset =indexReader.getCollection().getDocNum();
        this.runNum =15;
        this.convergeThreshold =0.0001;
        this.unlabeledRate =unlabeledRate;
    }

    public void setUnlabeledData(IndexReader newIndexReader, DocClass docSet){
        IRDoc curDoc;
        int[] termMap, arrIndex, arrFreq, arrNewIndex, arrNewFreq;
        int i, j, termNum, docNum, newIndex;


        //build the map between two indices
        termMap=getTermMap(newIndexReader,indexReader);
        externalUnlabeled=new IntRow[docSet.getDocNum()];
        unlabeledSet=new DocClass(0);

        docNum=0;
        for(i=0;i<externalUnlabeled.length;i++){
            curDoc=docSet.getDoc(i);
            arrIndex=newIndexReader.getTermIndexList(curDoc.getIndex());
            arrFreq=newIndexReader.getTermFrequencyList(curDoc.getIndex());
            if(arrIndex==null)
                continue;
            termNum=0;
            for(j=0;j<arrIndex.length;j++)
                if(termMap[arrIndex[j]]>=0)
                    termNum++;
            if(termNum==0)
                continue;

            arrNewIndex=new int[termNum];
            arrNewFreq=new int[termNum];
            termNum=0;
            for(j=0;j<arrIndex.length;j++){
                newIndex=termMap[arrIndex[j]];
                if(newIndex>=0){
                    arrNewIndex[termNum]=newIndex;
                    arrNewFreq[termNum]=arrFreq[j];
                    termNum++;
                }
            }
            externalUnlabeled[docNum]=new IntRow(docNum,termNum,arrNewIndex,arrNewFreq);
            curDoc.setIndex(externalDocOffset+docNum);
            curDoc.setKey("external_unlabeled"+curDoc.getKey());
            unlabeledSet.addDoc(curDoc);
            docNum++;
        }
    }

    public void setUnlabeledData(DocClass docSet){
        this.unlabeledSet =docSet;
        this.externalUnlabeled =null;
    }

    public DocClassSet classify(DocClassSet trainingDocSet, DocClass testingDocs){
        ArrayList list;
        int i, num;

        if(indexReader==null && doctermMatrix==null)
        	return null;

        if(unlabeledRate>0){
            //prepare unlabeled document set
            unlabeledSetBackup=unlabeledSet;
            unlabeledSet=new DocClass(0);
            if(unlabeledSetBackup!=null){
                for(i=0;i<unlabeledSetBackup.getDocNum();i++)
                    unlabeledSet.addDoc(unlabeledSetBackup.getDoc(i));
            }

            list=new ArrayList(testingDocs.getDocNum());
            for(i=0;i<testingDocs.getDocNum();i++){
                list.add(testingDocs.getDoc(i));
            }
            Collections.shuffle(list, new Random(10));
            num=(int)(unlabeledRate*list.size());
            for(i=0;i<num;i++)
                unlabeledSet.addDoc((IRDoc)list.get(i));

            train(trainingDocSet);

            unlabeledSet.removeAll();
            unlabeledSet=unlabeledSetBackup;
        }
        else
            train(trainingDocSet);
        return classify(testingDocs);
    }

    public void train(DocClassSet trainingDocSet){
        DocClassSet classifiedUnlabeledSet, newTrainingSet;
        DocClass cur;
        IRDoc curDoc;
        int[] arrIndex, arrFreq;
        int i, j, k, newTermIndex, curRun;
        double prevProb, prob, docProb;

        if(indexReader==null && doctermMatrix==null)
        	return;

        classNum=trainingDocSet.getClassNum();
        arrLabel=new String[classNum];
        for(i=0;i<classNum;i++)
            arrLabel[i]=trainingDocSet.getDocClass(i).getClassName();

        //initialize the classifier
        eStep(trainingDocSet);
        prevProb=0;
        curRun=0;
        prob=-Double.MAX_VALUE;

        while(Math.abs(prob-prevProb)>convergeThreshold && curRun<runNum){
            prevProb=prob;
            prob = 0;

            //classify unlabeled documents
            classifiedUnlabeledSet = classify(unlabeledSet);

            //compute the probability of the unlabeled document set
            for (i = 0; i < trainingDocSet.getClassNum(); i++) {
                cur = trainingDocSet.getDocClass(i);
                for (j = 0; j < cur.getDocNum(); j++) {
                    curDoc = cur.getDoc(j);
                    prob+=curDoc.getWeight();
                }
            }

            //compute the probability of the training document set
            for (i = 0; i < trainingDocSet.getClassNum(); i++) {
                cur = trainingDocSet.getDocClass(i);
                for (j = 0; j < cur.getDocNum(); j++) {
                    curDoc = cur.getDoc(j);
                    docProb=classPrior.get(i);
                    arrIndex= indexReader.getTermIndexList(curDoc.getIndex());
                    arrFreq = indexReader.getTermFrequencyList(curDoc.getIndex());
                    for(k=0;k<arrIndex.length;k++){
                        newTermIndex = featureSelector.map(arrIndex[k]);
                        if (newTermIndex >= 0)
                            docProb+=arrFreq[k] * model.getDouble(i, newTermIndex);
                    }
                    prob+=docProb;
                }
            }

            //prepare the training document set
            newTrainingSet=new DocClassSet(trainingDocSet.getClassNum());
            for (i = 0; i < trainingDocSet.getClassNum(); i++) {
                cur = trainingDocSet.getDocClass(i);
                for (j = 0; j < cur.getDocNum(); j++)
                    newTrainingSet.addDoc(i,cur.getDoc(j));
            }
            for (i = 0; i < classifiedUnlabeledSet.getClassNum(); i++) {
                cur = classifiedUnlabeledSet.getDocClass(i);
                for (j = 0; j < cur.getDocNum(); j++)
                    newTrainingSet.addDoc(i,cur.getDoc(j));
            }
            //re-estimate the classifier
            eStep(newTrainingSet);

            curRun++;
        }
    }

    public int classify(IRDoc curDoc){
    	IntRow row;
    	int[] arrIndex, arrFreq;
    	int label;
    	
    	if(curDoc.getKey().startsWith("external_unlabeled") ){
            //this document is from other index
            arrIndex =externalUnlabeled[curDoc.getIndex()-this.externalDocOffset].getNonZeroColumns();
            arrFreq = externalUnlabeled[curDoc.getIndex()-this.externalDocOffset].getNonZeroIntScores();
        }
        else{
            arrIndex = indexReader.getTermIndexList(curDoc.getIndex());
            arrFreq = indexReader.getTermFrequencyList(curDoc.getIndex());
        }
    	row=new IntRow(0,arrIndex.length,arrIndex,arrFreq);
    	label=classify(row);
    	curDoc.setWeight(lastClassProb.get(label));
    	return label;
    }

    /**
     * re-estimate the classifier model
     * @param trainingDocSet the training document set. It include the original training document set and classified
     * unlabeled document set. The key of external document is set to "external_unlabeled. The index of first external
     * document is the index of last internal document plus 1 and so on.
     */
    private void eStep(DocClassSet trainingDocSet){
        DocClass cur;
        IRDoc curDoc;
        int[] arrIndex, arrFreq;
        int i, j, k, classSum, newTermIndex;
        double rate;

        classPrior=getClassPrior(trainingDocSet);
        featureSelector.train(indexReader,trainingDocSet);
        model=new DoubleFlatDenseMatrix(trainingDocSet.getClassNum(),featureSelector.getSelectedFeatureNum());
        model.assign(1);
        for(i=0;i<trainingDocSet.getClassNum();i++){
            classSum=featureSelector.getSelectedFeatureNum();
            cur=trainingDocSet.getDocClass(i);
            for(j=0;j<cur.getDocNum();j++){
                curDoc=cur.getDoc(j);
                if(curDoc.getKey().startsWith("external_unlabeled") ){
                    //this document is from other index
                    arrIndex =externalUnlabeled[curDoc.getIndex()-this.externalDocOffset].getNonZeroColumns();
                    arrFreq = externalUnlabeled[curDoc.getIndex()-this.externalDocOffset].getNonZeroIntScores();
                }
                else{
                    arrIndex = indexReader.getTermIndexList(curDoc.getIndex());
                    arrFreq = indexReader.getTermFrequencyList(curDoc.getIndex());
                }
                for(k=0;k<arrIndex.length;k++){
                    newTermIndex=featureSelector.map(arrIndex[k]);
                    if(newTermIndex>=0){
                        classSum+=arrFreq[k];
                        model.add(i,newTermIndex,arrFreq[k]);
                    }
                }
            }

            rate=1.0/classSum;
            for(k=0;k<model.columns();k++)
                model.setDouble(i,k,Math.log(model.getDouble(i,k)*rate)); // attention: log is used
        }
    }

    private int[] getTermMap(IndexReader src,IndexReader dest){
        IRTerm irTerm;
        int[] termMap;
        int i;

        termMap=new int[src.getCollection().getTermNum()];
        for(i=0;i<termMap.length;i++){
            irTerm=dest.getIRTerm(src.getTermKey(i));
            if(irTerm!=null)
                termMap[i]=irTerm.getIndex();
            else
                termMap[i]=-1;
        }
        return termMap;
    }
}