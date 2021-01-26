//package dragon.config;
//
//import dragon.ir.index.*;
//import dragon.ir.classification.*;
//import dragon.ir.classification.featureselection.*;
//import dragon.ir.kngbase.KnowledgeBase;
//import dragon.matrix.*;
//import java.util.*;
//
///**
// * <p>Classifier configuration </p>
// * <p></p>
// * <p>Copyright: Copyright (c) 2005</p>
// * <p>Company: IST, Drexel University</p>
// * @author Davis Zhou
// * @version 1.0
// */
//
//public class ClassifierConfig extends ConfigUtil{
//    public ClassifierConfig() {
//       super();
//    }
//
//    public ClassifierConfig(ConfigureNode root){
//       super(root);
//    }
//
//    public ClassifierConfig(String configFile){
//        super(configFile);
//    }
//
//    public Classifier getClassifier(int classifierID){
//        return getClassifier(root,classifierID);
//    }
//
//    public Classifier getClassifier(ConfigureNode node, int classifierID){
//        return loadClassifier(node,classifierID);
//    }
//
//    private Classifier loadClassifier(ConfigureNode node, int classifierID){
//        ConfigureNode classifierNode;
//        String classifierName;
//
//        classifierNode=getConfigureNode(node,"classifier",classifierID);
//        if(classifierNode==null)
//            return null;
//        classifierName=classifierNode.getNodeName();
//        return loadClassifier(classifierName,classifierNode);
//    }
//
//    protected Classifier loadClassifier(String classifierName,ConfigureNode classifierNode){
//        if(classifierName.equalsIgnoreCase("NBClassifier"))
//            return loadNBClassifier(classifierNode);
//        else if(classifierName.equalsIgnoreCase("SVMLightClassifier"))
//            return loadSVMLightClassifier(classifierNode);
//        else if(classifierName.equalsIgnoreCase("LibSVMClassifier"))
//            return loadLibSVMClassifier(classifierNode);
//        else if(classifierName.equalsIgnoreCase("NigamActiveLearning"))
//            return loadNigamActiveLearning(classifierNode);
//        else if(classifierName.equalsIgnoreCase("SemanticNBClassifier"))
//            return loadSemanticNBClassifier(classifierNode);
//        else
//            return (Classifier)loadResource(classifierNode);
//    }
//
//    private Classifier loadNBClassifier(ConfigureNode node){
//        NBClassifier classifier;
//        FeatureSelector featureSelector;
//
//        int selectorID, indexReaderID, matrixID ;
//
//        indexReaderID=node.getInt("indexreader");
//        if(indexReaderID>0)
//            classifier=new NBClassifier((new IndexReaderConfig()).getIndexReader(node,indexReaderID));
//        else{
//            matrixID=node.getInt("doctermmatrix");
//            classifier=new NBClassifier((new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID));
//        }
//
//        selectorID=node.getInt("featureselector");
//        featureSelector=(new FeatureSelectorConfig()).getFeatureSelector(node, selectorID);
//        if(featureSelector!=null)
//            classifier.setFeatureSelector(featureSelector);
//        return classifier;
//    }
//
//    private Classifier loadSVMLightClassifier(ConfigureNode node){
//        SVMLightClassifier classifier;
//        FeatureSelector featureSelector;
//
//        int selectorID, indexReaderID, matrixID, decoderID;
//
//        indexReaderID=node.getInt("indexreader");
//        if(indexReaderID>0)
//            classifier=new SVMLightClassifier((new IndexReaderConfig()).getIndexReader(node,indexReaderID));
//        else{
//            matrixID=node.getInt("doctermmatrix");
//            classifier=new SVMLightClassifier((new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID));
//        }
//
//        selectorID=node.getInt("featureselector");
//        featureSelector=(new FeatureSelectorConfig()).getFeatureSelector(node, selectorID);
//        if(featureSelector!=null)
//            classifier.setFeatureSelector(featureSelector);
//        matrixID=node.getInt("codematrix");
//        if(matrixID>0)
//            classifier.setCodeMatrix((new CodeMatrixConfig()).getCodeMatrix(node,matrixID));
//        decoderID=node.getInt("multiclassdecoder");
//        if(decoderID>0)
//            classifier.setMultiClassDecoder((new MultiClassDecoderConfig()).getMultiClassDecoder(node,decoderID));
//        classifier.setScalingOption(node.getBoolean("scaling",false));
//        return classifier;
//    }
//
//    private Classifier loadLibSVMClassifier(ConfigureNode node){
//        LibSVMClassifier classifier;
//        FeatureSelector featureSelector;
//
//        int selectorID, indexReaderID, decoderID, matrixID;
//
//        indexReaderID=node.getInt("indexreader");
//        if(indexReaderID>0)
//            classifier=new LibSVMClassifier((new IndexReaderConfig()).getIndexReader(node,indexReaderID));
//        else{
//            matrixID=node.getInt("doctermmatrix");
//            classifier=new LibSVMClassifier((new SparseMatrixConfig()).getDoubleSparseMatrix(node,matrixID));
//        }
//
//        selectorID=node.getInt("featureselector");
//        featureSelector=(new FeatureSelectorConfig()).getFeatureSelector(node, selectorID);
//        if(featureSelector!=null)
//            classifier.setFeatureSelector(featureSelector);
//        decoderID=node.getInt("multiclassdecoder");
//        if (decoderID > 0)
//            classifier.setMultiClassDecoder( (new MultiClassDecoderConfig()).getMultiClassDecoder(node, decoderID));
//        classifier.setScalingOption(node.getBoolean("scaling", false));
//        classifier.setScalingOption(node.getBoolean("propestimate", true));
//        return classifier;
//    }
//
//    private Classifier loadNigamActiveLearning(ConfigureNode node){
//        NigamActiveLearning classifier;
//        FeatureSelector featureSelector;
//        IndexReader indexReader;
//        int selectorID, indexReaderID, unlabeledDocNum;
//        double unlabeledRate;
//
//        selectorID=node.getInt("featureselector");
//        featureSelector=(new FeatureSelectorConfig()).getFeatureSelector(node, selectorID);
//        indexReaderID=node.getInt("indexreader");
//        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
//        unlabeledRate=node.getDouble("unlabeledrate",0);
//        classifier=new NigamActiveLearning(indexReader,unlabeledRate);
//        if(featureSelector!=null)
//            classifier.setFeatureSelector(featureSelector);
//
//        indexReaderID=node.getInt("unlabeledindexreader");
//        unlabeledDocNum=node.getInt("unlabeleddocnum");
//        if(indexReaderID>0 && unlabeledDocNum>0){
//            indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
//            classifier.setUnlabeledData(indexReader,prepareUnlabeledDocSet(indexReader,10,unlabeledDocNum));
//        }
//        return classifier;
//    }
//
//    private DocClass prepareUnlabeledDocSet(IndexReader reader, int randomSeed, int num){
//        ArrayList list;
//        DocClass docSet;
//        int i, docNum;
//
//        if(reader==null)
//            return null;
//
//        docNum=reader.getCollection().getDocNum();
//        list=new ArrayList(docNum);
//        for(i=0;i<docNum;i++)
//            list.add(new Integer(i));
//        Collections.shuffle(list, new Random(randomSeed));
//        docSet=new DocClass(0);
//        for(i=0;i<num;i++)
//            docSet.addDoc(reader.getDoc(((Integer)list.get(i)).intValue()));
//        return docSet;
//    }
//
//    private Classifier loadSemanticNBClassifier(ConfigureNode node){
//        SemanticNBClassifier classifier;
//        IndexReader indexReader, topicIndexReader;
//        KnowledgeBase kngBase;
//        DoubleSparseMatrix transMatrix;
//        double transCoefficient, bkgCoefficient;
//        int matrixID, kngID, indexReaderID, topicIndexReaderID, selectorID;
//
//        bkgCoefficient=node.getDouble("bkgcoefficient");
//        indexReaderID=node.getInt("indexreader");
//        indexReader=(new IndexReaderConfig()).getIndexReader(node,indexReaderID);
//
//        matrixID=node.getInt("transmatrix");
//        kngID=node.getInt("knowledgebase");
//        if(matrixID>0){
//            transCoefficient=node.getDouble("transcoefficient");
//            transMatrix = (new SparseMatrixConfig()).getDoubleSparseMatrix(node, matrixID);
//            topicIndexReaderID = node.getInt("topicindexreader", indexReaderID);
//            if(topicIndexReaderID==indexReaderID)
//                topicIndexReader=indexReader;
//            else
//                topicIndexReader=(new IndexReaderConfig()).getIndexReader(node,topicIndexReaderID);
//            classifier = new SemanticNBClassifier(indexReader, topicIndexReader, transMatrix, transCoefficient, bkgCoefficient);
//        }
//        else if(kngID>0){
//            transCoefficient=node.getDouble("transcoefficient");
//            kngBase= (new KnowledgeBaseConfig()).getKnowledgeBase(node,kngID);
//            topicIndexReaderID = node.getInt("topicindexreader", indexReaderID);
//            if(topicIndexReaderID==indexReaderID)
//                topicIndexReader=indexReader;
//            else
//                topicIndexReader=(new IndexReaderConfig()).getIndexReader(node,topicIndexReaderID);
//            classifier = new SemanticNBClassifier(indexReader, topicIndexReader, kngBase, transCoefficient, bkgCoefficient);
//        }
//        else
//            classifier=new SemanticNBClassifier(indexReader,bkgCoefficient);
//        selectorID=node.getInt("featureselector");
//        if(selectorID>0)
//            classifier.setFeatureSelector((new FeatureSelectorConfig()).getFeatureSelector(node,selectorID));
//        return classifier;
//    }
//}
