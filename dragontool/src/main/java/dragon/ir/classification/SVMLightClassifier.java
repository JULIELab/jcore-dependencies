//package dragon.ir.classification;
//
//import dragon.ir.classification.featureselection.*;
//import dragon.ir.classification.multiclass.*;
//import dragon.ir.index.*;
//import dragon.matrix.*;
//import jnisvmlight.*;
//import java.io.*;
//import java.util.*;
//
///**
// * <p>SVM light multi-class text classifier</p>
// * <p></p>
// * <p>Copyright: Copyright (c) 2005</p>
// * <p>Company: IST, Drexel University</p>
// * @author Davis Zhou
// * @version 1.0
// */
//
//public class SVMLightClassifier extends AbstractClassifier {
//    private SVMLightModel[] arrModel;
//    private LearnParam learnParam;
//    private KernelParam kernelParam;
//    private CodeMatrix codeMatrix;
//    private MultiClassDecoder classDecoder;
//    private double[] arrConfidence;
//    private boolean scale;
//
//    public SVMLightClassifier(String modelFile){
//        ObjectInputStream oin;
//        int i;
//
//        try{
//            oin = new ObjectInputStream(new FileInputStream(modelFile));
//            arrModel=new SVMLightModel[oin.readInt()];
//            for(i=0;i<arrModel.length;i++)
//                arrModel[i]=(SVMLightModel)oin.readObject();
//            codeMatrix=(CodeMatrix)oin.readObject();
//            classDecoder=(MultiClassDecoder)oin.readObject();
//            classNum=oin.readInt();
//            scale=oin.readBoolean();
//            featureSelector=(FeatureSelector)oin.readObject();
//            arrLabel=new String[classNum];
//            for(i=0;i<arrLabel.length;i++)
//                arrLabel[i]=(String)oin.readObject();
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    public SVMLightClassifier(IndexReader indexReader) {
//        super(indexReader);
//        learnParam = new LearnParam();
//        kernelParam = new KernelParam();
//        classDecoder = new LossMultiClassDecoder(new HingeLoss());
//        codeMatrix = new OVACodeMatrix(1);
//        classNum = 0;
//        scale = false;
//    }
//
//    public SVMLightClassifier(SparseMatrix doctermMatrix) {
//        super(doctermMatrix);
//        learnParam = new LearnParam();
//        kernelParam = new KernelParam();
//        classDecoder = new LossMultiClassDecoder(new HingeLoss());
//        codeMatrix = new OVACodeMatrix(1);
//        classNum = 0;
//        scale = false;
//    }
//    
//    public void setUseLinearKernel(){
//    	kernelParam.kernel_type=KernelParam.LINEAR;
//    }
//    
//    public void setUseRBFKernel(){
//    	kernelParam.kernel_type=KernelParam.RBF;
//    }
//    
//    public void setUsePolynomialKernel(){
//    	kernelParam.kernel_type=KernelParam.POLYNOMIAL;
//    }
//    
//    public void setUserSigmoidKernel(){
//    	kernelParam.kernel_type=KernelParam.SIGMOID;
//    }
//
//    /**
//     * Sets the scaling option. If it is true, the classifier will normalize all testing and training examples to euclidean length one.
//     * @param option the scaling option, true or false
//     */
//    public void setScalingOption(boolean option) {
//        this.scale = option;
//    }
//
//    /**
//         * Sets the code matrix which tells the classifier how to transform the multi-class classification problem to a set of binary classifiers
//     * @param matrix the code matrix such as one-versus-all and all pair
//     */
//    public void setCodeMatrix(CodeMatrix matrix) {
//        this.codeMatrix = matrix;
//    }
//
//    /**
//     * Sets the method for predicting the label of an example
//     * @param decoder the decoding method such as loss-based multi-class decoder
//     */
//    public void setMultiClassDecoder(MultiClassDecoder decoder) {
//        this.classDecoder = decoder;
//    }
//
//    public int[] rank(){
//    	return classDecoder.rank();
//    }
//    
//    public void train(DocClassSet trainingDocSet) {
//        SVMLightInterface svm;
//        TrainingParameters param;
//        ArrayList[] arrClass;
//        LabeledFeatureVector[] arrDoc;
//        int i, j, negNum, posNum;
//
//        if (indexReader == null && doctermMatrix == null) {
//            return;
//        }
//
//        try {
//            trainFeatureSelector(trainingDocSet);
//            arrLabel=new String[trainingDocSet.getClassNum()];
//            for (i = 0; i < trainingDocSet.getClassNum(); i++)
//                arrLabel[i] = trainingDocSet.getDocClass(i).getClassName();
//            classNum = trainingDocSet.getClassNum();
//            codeMatrix.setClassNum(classNum);
//            arrClass = new ArrayList[classNum];
//            param = new TrainingParameters(learnParam, kernelParam);
//            svm = new SVMLightInterface();
//            arrModel = new SVMLightModel[codeMatrix.getClassifierNum()];
//            for (i = 0; i < classNum; i++) {
//                arrClass[i] = loadData(trainingDocSet.getDocClass(i));
//            }
//            for (i = 0; i < codeMatrix.getClassifierNum(); i++) {
//                arrDoc = loadData(arrClass, codeMatrix, i);
//                negNum = posNum = 0;
//                for (j = 0; j < arrDoc.length; j++) {
//                    if (arrDoc[j].getLabel() > 0) {
//                        posNum++;
//                    } else {
//                        negNum++;
//                    }
//                }
//                param.getLearningParameters().svm_costratio = 1.0;
//                arrModel[i] = svm.trainModel(arrDoc, param);
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public int classify(Row doc) {
//        LabeledFeatureVector example;
//        int j;
//
//        if(arrModel==null)
//            return -1;
//        example = loadData(doc);
//        if (example == null)
//            return -1;
//        if(arrConfidence==null || arrConfidence.length!=codeMatrix.getClassifierNum())
//        	arrConfidence = new double[codeMatrix.getClassifierNum()];
//        for (j = 0; j < codeMatrix.getClassifierNum(); j++)
//            arrConfidence[j] = arrModel[j].classify(example);
//        return classDecoder.decode(codeMatrix, arrConfidence);
//    }
//    
//    public double[] getBinaryClassifierConfidence(){
//    	return arrConfidence;
//    }
//
//    public void saveModel(String modelFile){
//         ObjectOutputStream out;
//         int i;
//
//         try{
//             if(arrModel==null)
//                 return;
//             out=new ObjectOutputStream(new FileOutputStream(modelFile));
//             out.writeInt(arrModel.length);
//             for(i=0;i<arrModel.length;i++){
//            	 arrModel[i].removeTrainingData();
//                 out.writeObject(arrModel[i]);
//             }
//             out.writeObject(codeMatrix);
//             out.writeObject(classDecoder);
//             out.writeInt(classNum);
//             out.writeBoolean(scale);
//             out.writeObject(featureSelector);
//             for(i=0;i<classNum;i++)
//                 out.writeObject(getClassLabel(i));
//             out.flush();
//             out.close();
//         }
//         catch(Exception e){
//             e.printStackTrace();
//         }
//     }
//
//    private LabeledFeatureVector[] loadData(ArrayList[] arrClass, CodeMatrix matrix, int classifierIndex) {
//        ArrayList list;
//        LabeledFeatureVector curDoc, all[];
//        int i, j, label;
//
//        list = new ArrayList();
//        for (i = 0; i < classNum; i++) {
//            label = codeMatrix.getCode(i, classifierIndex);
//            if (label == 0) {
//                continue;
//            }
//            for (j = 0; j < arrClass[i].size(); j++) {
//                curDoc = (LabeledFeatureVector) arrClass[i].get(j);
//                curDoc.setLabel(label);
//                list.add(curDoc);
//            }
//        }
//
//        all = new LabeledFeatureVector[list.size()];
//        for (j = 0; j < list.size(); j++) {
//            all[j] = (LabeledFeatureVector) list.get(j);
//        }
//        list.clear();
//        return all;
//    }
//
//    private ArrayList loadData(DocClass docs) {
//        ArrayList list;
//        LabeledFeatureVector curDoc;
//        int i;
//
//        list = new ArrayList(docs.getDocNum());
//        for (i = 0; i < docs.getDocNum(); i++) {
//            curDoc = loadData(getRow(docs.getDoc(i).getIndex()));
//            if (curDoc != null) {
//                list.add(curDoc);
//            }
//        }
//        return list;
//    }
//
//    protected LabeledFeatureVector loadData(Row doc) {
//        int[] ids;
//        double[] values;
//        double sum;
//        int j, num, newIndex;
//
//        if (doc == null) {
//            return null;
//        }
//        num = 0;
//        for (j = 0; j < doc.getNonZeroNum(); j++) {
//            if (featureSelector.map(doc.getNonZeroColumn(j)) >= 0) {
//                num++;
//            }
//        }
//        if (num == 0) {
//            return null;
//        }
//
//        ids = new int[num];
//        values = new double[num];
//        num = 0;
//        for (j = 0; j < doc.getNonZeroNum(); j++) {
//            newIndex = featureSelector.map(doc.getNonZeroColumn(j));
//            if (newIndex >= 0) {
//                ids[num] = newIndex + 1; //the feature id in svm light starts from 1
//                values[num] = doc.getNonZeroDoubleScore(j);
//                num++;
//            }
//        }
//
//        if (scale) {
//            sum = 0;
//            for (j = 0; j < num; j++) {
//                sum += values[j] * values[j];
//                sum = Math.sqrt(sum);
//                for (j = 0; j < num; j++) {
//                    values[j] = values[j] / sum;
//                }
//            }
//        }
//        return new LabeledFeatureVector(1, ids, values);
//    }
//}
