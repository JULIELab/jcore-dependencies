//package dragon.ir.classification;
//
//import dragon.ir.classification.featureselection.*;
//import dragon.ir.classification.multiclass.*;
//import dragon.ir.index.*;
//import dragon.matrix.*;
//import dragon.util.MathUtil;
//import libsvm.*;
//import java.io.*;
//import java.util.Vector;
//
///**
// * <p>libsvm multi-class text classifier</p>
// * <p></p>
// * <p>Copyright: Copyright (c) 2005</p>
// * <p>Company: IST, Drexel University</p>
// * @author Davis Zhou
// * @version 1.0
// */
//
//public class LibSVMClassifier extends AbstractClassifier{
//    private svm_parameter param;
//    private svm_model model;
//    private CodeMatrix codeMatrix;
//    private MultiClassDecoder classDecoder;
//    private double[] arrProb, arrConfidence;
//    private boolean scale;
//    private int[] rank;
//    
//    public LibSVMClassifier(String modelFile){
//        ObjectInputStream oin;
//        int i;
//
//        try{
//            oin = new ObjectInputStream(new FileInputStream(modelFile));
//            model=(svm_model)oin.readObject();
//            param=(svm_parameter)oin.readObject();
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
//    public LibSVMClassifier(IndexReader indexReader) {
//        super(indexReader);
//        param=getDefaultParameter();
//        codeMatrix=new AllPairCodeMatrix(1);
//        classDecoder=null;
//        svm.showErrorMessage=true;
//        svm.showMessage =false;
//        model=null;
//        scale=true;
//    }
//
//    public LibSVMClassifier(SparseMatrix doctermMatrix) {
//        super(doctermMatrix);
//        param=getDefaultParameter();
//        codeMatrix=new AllPairCodeMatrix(1);
//        classDecoder=null;
//        svm.showErrorMessage=true;
//        svm.showMessage =false;
//        model=null;
//        scale=true;
//    }
//    /**
//     * Sets the method for predicting the label of an example
//     * @param decoder the decoding method such as loss-based multi-class decoder
//     */
//    public void setMultiClassDecoder(MultiClassDecoder decoder){
//        this.classDecoder =decoder;
//    }
//
//    public void setUseProbEstimate(boolean option){
//        param.probability =option? 1:0;
//    }
//
//    /**
//     * Sets the scaling option. If it is true, the classifier will normalize all testing and training examples to euclidean length one.
//     * @param option the scaling option, true or false
//     */
//    public void setScalingOption(boolean option){
//        this.scale = option;
//    }
//
//    public void train(DocClassSet trainingDocSet){
//        svm_problem prob;
//        int i;
//
//        if(indexReader==null && doctermMatrix==null)
//        	return;
//
//        trainFeatureSelector(trainingDocSet);
//        arrLabel=new String[trainingDocSet.getClassNum()];
//        for(i=0;i<trainingDocSet.getClassNum();i++)
//            arrLabel[i]=trainingDocSet.getDocClass(i).getClassName();
//        classNum=trainingDocSet.getClassNum();
//        codeMatrix.setClassNum(classNum);
//        prob=getTrainingProblem(trainingDocSet);
//        model=svm.svm_train(prob,param);
//    }
//
//    public int classify(Row doc){
//        svm_node[] curDoc;
//        int label;
//
//        curDoc=readDoc(doc);
//        if(curDoc==null)
//            return -1;
//        if(classDecoder==null){
//            //use the class decoder provided by libsvm
//        	
//            if (param.probability == 1){
//            	if(arrProb==null || arrProb.length!=classNum)
//            		arrProb=new double[classNum];
//                label = (int) (svm.svm_predict_probability(model, curDoc, arrProb));
//                rank=MathUtil.rankElementInArray(arrProb,true);
//            }
//            else{
//                label = (int) (svm.svm_predict(model,curDoc));
//            }
//        }
//        else{
//        	if(arrConfidence==null || arrConfidence.length!=codeMatrix.getClassifierNum())
//        		arrConfidence=new double[codeMatrix.getClassifierNum()];
//            svm.svm_predict_values(model, curDoc, arrConfidence);
//            label=classDecoder.decode(codeMatrix,arrConfidence);
//        }
//        return label;
//    }
//    
//    public int[] rank(){
//    	if(classDecoder==null)
//    		return rank;
//    	else
//    		return classDecoder.rank();
//    }
//
//    public void saveModel(String modelFile){
//         ObjectOutputStream out;
//         int i;
//
//         try{
//             if(model==null)
//                 return;
//             out=new ObjectOutputStream(new FileOutputStream(modelFile));
//             out.writeObject(model);
//             out.writeObject(param);
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
//    private svm_parameter getDefaultParameter(){
//        svm_parameter param;
//
//        param = new svm_parameter();
//        param.svm_type = svm_parameter.C_SVC;
//        param.kernel_type = svm_parameter.LINEAR;
//        param.degree = 3;
//        param.gamma = 0;
//        param.coef0 = 0;
//        param.nu = 0.5;
//        param.cache_size = 100;
//        param.C =1;
//        param.eps = 1e-3;
//        param.p = 0.1;
//        param.shrinking = 1;
//        param.probability = 1;
//        param.nr_weight = 0;
//        param.weight_label = new int[0];
//        param.weight = new double[0];
//        return param;
//    }
//
//    private svm_problem getTrainingProblem(DocClassSet trainingDocSet){
//        svm_problem prob;
//        DocClass curClass;
//        svm_node[] curDoc;
//        Vector vx, vy;
//        int i, j, maxIndex;
//
//        vx=new Vector();
//        vy=new Vector();
//        maxIndex=0;
//        for(i=0;i<trainingDocSet.getClassNum();i++){
//            curClass=trainingDocSet.getDocClass(i);
//            for(j=0;j<curClass.getDocNum();j++){
//                curDoc=readDoc(getRow(curClass.getDoc(j).getIndex()));
//                if(curDoc!=null){
//                    vx.addElement(curDoc);
//                    vy.addElement(new Integer(curClass.getClassID()));
//                    maxIndex=Math.max(maxIndex,curDoc[curDoc.length-1].index);
//                }
//            }
//        }
//
//        prob = new svm_problem();
//        prob.l = vy.size();
//        prob.x = new svm_node[prob.l][];
//        for(i=0;i<prob.l;i++)
//            prob.x[i] = (svm_node[])vx.elementAt(i);
//        prob.y = new double[prob.l];
//        for(i=0;i<prob.l;i++)
//            prob.y[i] =((Integer)vy.elementAt(i)).intValue();
//
//        if(param.gamma == 0)
//            param.gamma = 1.0/maxIndex;
//
//        return prob;
//    }
//
//
//    protected svm_node[] readDoc(Row curDoc){
//        svm_node[] arrNode;
//        double sum;
//        int j, num, newIndex;
//
//        if(curDoc==null)
//            return null;
//        num=0;
//        for(j=0;j<curDoc.getNonZeroNum();j++)
//            if(featureSelector.map(curDoc.getNonZeroColumn(j))>=0)
//                num++;
//        if(num==0)
//            return null;
//        arrNode=new svm_node[num];
//        num=0;
//        for(j=0;j<curDoc.getNonZeroNum();j++){
//            newIndex=featureSelector.map(curDoc.getNonZeroColumn(j));
//            if(newIndex>=0){
//                arrNode[num]=new svm_node();
//                arrNode[num].index =newIndex;
//                arrNode[num].value=curDoc.getNonZeroDoubleScore(j);
//                num++;
//            }
//        }
//
//        if(scale){
//            sum=0;
//            for(j=0;j<num;j++){
//                sum += arrNode[j].value * arrNode[j].value;
//                sum = Math.sqrt(sum);
//                for (j = 0; j < num; j++)
//                    arrNode[j].value = arrNode[j].value/ sum;
//            }
//        }
//        return arrNode;
//    }
//}