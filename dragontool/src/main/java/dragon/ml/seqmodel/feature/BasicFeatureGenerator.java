package dragon.ml.seqmodel.feature;

import dragon.ml.seqmodel.data.*;
import dragon.ml.seqmodel.model.ModelGraph;
import java.util.*;
import java.io.*;

/**
 * The FeatureGenerator is an aggregator over all these different
 * feature types. You can inherit from the FeatureGenImpl class and
 * after calling one of the constructors that does not make a call to
 * (addFeatures()) you can then implement your own addFeatures
 * class. There you will typically add the EdgeFeatures feature first
 * and then the rest.  So, for example if you wanted to add some
 * parameter for each label (like a prior), you can create a new
 * FeatureTypes class that will create as many featureids as the
 * number of labels.
 * This class  is responsible for converting the
 * string-ids that the FeatureTypes assign to their features into
 * distinct numbers. It has a inner class called FeatureMap that will
 * make one pass over the training data and create the map of
 * featurenames->integer id and as a side effect count the number of
 * features.
 *
 * @author Sunita Sarawagi modified by davis
 * */

public class BasicFeatureGenerator implements FeatureGenerator{
    protected ModelGraph model;
    protected Vector featureVector;
    protected Iterator featureIter;
    protected FeatureType currentFeatureType;
    protected FeatureMap featureMap;
    protected Feature featureToReturn;
    protected DataSequence curSeq;
    protected int curStartPos, curEndPos;
    protected int totalFeatures;
    protected boolean featureCollectingMode;
    protected boolean supportSegment;

    public BasicFeatureGenerator(ModelGraph model){
        this(model,false);
    }

    public BasicFeatureGenerator(ModelGraph model, boolean supportSegment) {
        this.model =model;
        totalFeatures = 0;
        this.supportSegment =supportSegment;
        featureVector = new Vector();
        featureToReturn = null;
        featureMap = new FeatureMap();
        featureCollectingMode=false;
    }

    public boolean supportSegment(){
        return supportSegment;
    }

    public boolean addFeatureType(FeatureType fType) {
        if(featureMap.isFrozen())
            return false;
        if(supportSegment() && !fType.supportSegment())
            return false;

        fType.setTypeID(featureVector.size());
        featureVector.add(fType);
        return true;
    }

    public int getFeatureTypeNum(){
        return featureVector.size();
    }

    public FeatureType getFeatureTYpe(int index){
        return (FeatureType)featureVector.get(index);
    }

    protected FeatureType getFeatureType(int i) {
        return (FeatureType)featureVector.elementAt(i);
    }

    //the method below will be called only in training mode
    public boolean train(Dataset trainData){
        FeatureType cur;
        int i;

        for(i=0;i<featureVector.size();i++){
            cur=getFeatureType(i);
            if(cur.needTraining()){
                if(!cur.train(trainData))
                    return false;
                cur.saveTrainingResult();
            }
        }
        collectFeatureIdentifiers(trainData);
        totalFeatures=featureMap.getFeatureNum();
        return true;
    };

    //the method below will be called only in testing mode
    public boolean loadFeatureData(){
        FeatureType cur;
        int i;

        for(i=0;i<featureVector.size();i++){
            cur=getFeatureType(i);
            if(cur.needTraining())
                cur.readTrainingResult();
        }
        return true;
    }

    protected void collectFeatureIdentifiers(Dataset trainData){
        DataSequence seq;
        int segStart, segEnd;

        featureCollectingMode = true;
        for (trainData.startScan(); trainData.hasNext(); ) {
            seq = trainData.next();
            segStart=0;
            while(segStart<seq.length()){
                if(supportSegment)
                    segEnd=seq.getSegmentEnd(segStart);
                else
                    segEnd=segStart;
                for (startScanFeaturesAt(seq, segStart, segEnd); hasNext(); ) {
                    next();
                }
                segStart=segEnd+1;
            }
        }
        featureCollectingMode = false;
        featureMap.freezeFeatures();
    }

    protected void advance() {
        FeatureIdentifier id;
        int featureIndex;

        while (true) {
            for (;((currentFeatureType == null) || !currentFeatureType.hasNext()) && featureIter.hasNext();) {
                currentFeatureType = (FeatureType)featureIter.next();
            }
            if (!currentFeatureType.hasNext())
                break;
            while (currentFeatureType.hasNext()) {
                featureToReturn=currentFeatureType.next();
                id=featureToReturn.getID();
                //gurantee feature id is unique as long as the id within the feature type is unique.
                id.setId(id.getId()*getFeatureTypeNum()+currentFeatureType.getTypeID());
                featureIndex=featureMap.getId(id);
                if(featureIndex<0 && featureCollectingMode){
                    if(retainFeature(curSeq,featureToReturn))
                        featureIndex=featureMap.add(id);
                }

                if (featureIndex < 0){
                    featureToReturn=null;
                    continue;
                }
                featureToReturn.setIndex(featureIndex);

                if(!isValidFeature(curSeq,curStartPos,curEndPos,featureToReturn)){
                    featureToReturn = null;
                    continue;
                }
                return;
            }
        }
        featureToReturn=null;
    }

    protected boolean isValidFeature(DataSequence data, int curStartPos, int curEndPos, Feature featureToReturn) {
        if ( (curStartPos > 0) && (curEndPos < data.length() - 1))
            return true;
        if ( (curStartPos == 0) && (model.isStartState(featureToReturn.getLabel()))
            && ( (data.length() > 1) || (model.isEndState(featureToReturn.getLabel()))))
            return true;
        if ( (curEndPos == data.length() - 1) && (model.isEndState(featureToReturn.getLabel())))
            return true;
        return false;
    }

    protected boolean retainFeature(DataSequence seq, Feature f) {
        return (seq.getLabel(curEndPos) == f.getLabel()) &&
            ( (curStartPos == 0) || (f.getPrevLabel() < 0) || (seq.getLabel(curStartPos - 1) == f.getPrevLabel()));
    }

    protected void initScanFeaturesAt(DataSequence d) {
        curSeq = d;
        currentFeatureType = null;
        featureIter = featureVector.iterator();
        advance();
    }

    public void startScanFeaturesAt(DataSequence d, int startPos, int endPos) {
        this.curStartPos = startPos;
        this.curEndPos=endPos;
        for (int i = 0; i < featureVector.size(); i++) {
            getFeatureType(i).startScanFeaturesAt(d,startPos,endPos);
        }
        initScanFeaturesAt(d);
    }

    public boolean hasNext() {
        return (featureToReturn!=null);
    }

    public Feature next() {
        Feature cur;

        cur=featureToReturn.copy();
        advance();
        return cur;
    }

    public int getFeatureNum() {
        return totalFeatures;
    }

    public String getFeatureName(int featureIndex) {
        return featureMap.getName(featureIndex);
    }

    //The method below will be called only in testing mode
    public boolean readFeatures(String fileName) {
        BufferedReader in;

        try{
            in = new BufferedReader(new FileReader(fileName));
            totalFeatures = featureMap.read(in);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //The method below will be called only in training mode
    public boolean saveFeatures(String fileName){
        PrintWriter out;

        try{
            out = new PrintWriter(new FileOutputStream(fileName));
            featureMap.write(out);
            out.close();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
};
