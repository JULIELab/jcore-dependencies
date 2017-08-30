package dragon.config;

import dragon.ir.index.*;
import dragon.ir.topicmodel.*;
import dragon.matrix.vector.DoubleVector;

/**
 * <p>Topic model configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TopicModelConfig extends ConfigUtil{
    public TopicModelConfig() {
       super();
    }

    public TopicModelConfig(ConfigureNode root){
       super(root);
    }

    public TopicModelConfig(String configFile){
        super(configFile);
    }

    public TopicModel getTopicModel(int modelID){
        return getTopicModel(root,modelID);
    }

    public TopicModel getTopicModel(ConfigureNode node, int modelID){
        return loadTopicModel(node,modelID);
    }

    private TopicModel loadTopicModel(ConfigureNode node, int modelID){
        ConfigureNode modelNode;
        String modelName;

        modelNode=getConfigureNode(node,"topicmodel",modelID);
        if(modelNode==null)
            return null;
        modelName=modelNode.getNodeName();
        return loadTopicModel(modelName,modelNode);
    }

    protected TopicModel loadTopicModel(String modelName,ConfigureNode modelNode){
        if(modelName.equalsIgnoreCase("GibbsLDA"))
            return loadGibbsLDA(modelNode);
        else if(modelName.equalsIgnoreCase("AspectModel"))
            return loadAspectModel(modelNode);
        else if(modelName.equalsIgnoreCase("SimpleMixtureModel"))
            return loadSimpleMixtureModel(modelNode);
        else
            return (TopicModel)loadResource(modelNode);
    }

    private TopicModel loadGibbsLDA(ConfigureNode node){
        GibbsLDA model;
        IndexReader indexReader;
        int indexReaderID;
        double alpha, beta;

        alpha=node.getDouble("alpha");
        beta=node.getDouble("beta");
        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        model= new GibbsLDA(indexReader,alpha, beta);
        model.setIterationNum(node.getInt("iterations",100));
        model.setRandomSeed(node.getInt("randomseed",-1));
        return model;
    }

    private TopicModel loadAspectModel(ConfigureNode node){
        AspectModel model;
        IndexReader indexReader;
        int indexReaderID;

        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        model= new AspectModel(indexReader);
        model.setIterationNum(node.getInt("iterations",100));
        model.setRandomSeed(node.getInt("randomseed",-1));
        return model;
    }

    private TopicModel loadSimpleMixtureModel(ConfigureNode node){
        SimpleMixtureModel model;
        IndexReader indexReader, bkgIndexReader;
        DoubleVector bkgModel;
        int indexReaderID, bkgIndexReaderID;
        double bkgCoefficient;

        bkgCoefficient=node.getDouble("bkgCoefficient");
        indexReaderID=node.getInt("indexreader");
        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
        bkgIndexReaderID=node.getInt("bkgindexreader",indexReaderID);
        if(bkgIndexReaderID==indexReaderID)
            model= new SimpleMixtureModel(indexReader,bkgCoefficient);
        else{
            bkgIndexReader=(new IndexReaderConfig()).getIndexReader(node,bkgIndexReaderID);
            bkgModel=getBackgroundModel(indexReader,bkgIndexReader);
            bkgIndexReader.close();
            model=new SimpleMixtureModel(indexReader,bkgModel, bkgCoefficient);
        }
        model.setIterationNum(node.getInt("iterations",100));
        model.setRandomSeed(node.getInt("randomseed",-1));
        return model;
    }

    private DoubleVector getBackgroundModel(IndexReader indexReader, IndexReader bkgIndexReader){
        IRTerm curTerm;
        DoubleVector bkgModel;
        double sum;
        boolean needSmooth;
        int i, freq;

        bkgModel=new DoubleVector(indexReader.getCollection().getTermNum());
        needSmooth=false;

        sum=0;
        for(i=0;i<bkgModel.size();i++){
            curTerm=bkgIndexReader.getIRTerm(indexReader.getTermKey(i));
            if(curTerm!=null){
                freq = curTerm.getFrequency();
                if (freq == 0)
                    needSmooth = true;
            }
            else{
                freq=0;
                needSmooth=true;
            }
            sum+=freq;
            bkgModel.set(i,freq);
        }

        if(needSmooth){
            for(i=0;i<bkgModel.size();i++)
                bkgModel.add(i,1);
            sum+=bkgModel.size();
        }
        bkgModel.multiply(1.0/sum);
        return bkgModel;
    }
}
