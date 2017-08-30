//package dragon.config;
//
//import dragon.ir.classification.*;
//import dragon.ir.index.*;
//import dragon.matrix.vector.DoubleVector;
//import dragon.nlp.SimpleElementList;
//import dragon.nlp.compare.*;
//import dragon.util.*;
//import java.io.*;
//import java.text.DecimalFormat;
//import java.util.*;
//
///**
// * <p>Text Classification Evaluation Program</p>
// * <p>The program supports three modes of evaluations.
// * (1)CrossValidation: the parameters, answerkeyfile and foldernum(number of folds) should be specified.
// * (2)Percentage: the parameters, answerkeyfile, percentage (the percentage of training examples), randomseed,
// * and runs, should be specified.
// * (3)Manual: the parameters, trainingkeyfile and testingkeyfile should be specified</p>
// * <p>Copyright: Copyright (c) 2005</p>
// * <p>Company: IST, Drexel University</p>
// * @author Davis Zhou
// * @version 1.0
// */
//
//public class ClassificationEvaAppConfig {
//    private DecimalFormat df;
//    private PrintWriter out;
//    private TreeMap map;
//    private ArrayList labelList;
//    private int maxCategory;
//
//    public ClassificationEvaAppConfig(){
//        df=FormatUtil.getNumericFormat(1,4);
//        map=new TreeMap();
//        maxCategory=0;
//        labelList=new ArrayList();
//    }
//
//    public static void main(String[] args) {
//        ClassificationEvaAppConfig classificationApp;
//        ConfigureNode root,classificationAppNode;
//        ConfigUtil util;
//
//        if(args.length!=2){
//            System.out.println("Please input two parameters: configuration xml file and clustering evaluation id");
//            return;
//        }
//
//        root=new BasicConfigureNode(args[0]);
//        util=new ConfigUtil();
//        classificationAppNode=util.getConfigureNode(root,"classificationevaapp",Integer.parseInt(args[1]));
//        if(classificationAppNode==null)
//            return;
//        classificationApp=new ClassificationEvaAppConfig();
//        classificationApp.evaluate(classificationAppNode);
//    }
//
//    public void evaluate(ConfigureNode node){
//        Classifier classifier;
//        SimpleElementList dockeyList;
//        String mode, outputFile, runName;
//        String  dockeyFile, answerKeyFile, trainingKeyFile, testingKeyFile, validatingKeyFile;
//        int classNum, foldNum, runs, randomSeed, classifierID;
//        double percentage;
//
//        dockeyFile=node.getString("dockeyfile",null);
//        if(dockeyFile==null)
//            dockeyList=null;
//        else
//            dockeyList=new SimpleElementList(dockeyFile,false);
//        classNum=node.getInt("classnum");
//        classifierID=node.getInt("classifier");
//        classifier=(new ClassifierConfig()).getClassifier(node,classifierID);
//        validatingKeyFile=node.getString("validatingkeyfile",null);
//        outputFile=node.getString("outputfile");
//        runName=node.getString("runname");
//        mode=node.getString("mode");
//        if(mode==null)
//            return;
//        if(mode.equalsIgnoreCase("CrossValidation")){
//            answerKeyFile=node.getString("answerkeyfile");
//            foldNum=node.getInt("foldnum",5);
//            evaluateCrossValidation(classifier,dockeyList, classNum,answerKeyFile,validatingKeyFile,foldNum,runName, outputFile);
//        }
//        else if(mode.equalsIgnoreCase("Percentage")){
//            randomSeed=node.getInt("randomseed",-1);
//            runs=node.getInt("runs",1);
//            answerKeyFile=node.getString("answerkeyfile");
//            percentage=node.getDouble("percentage",0.67);
//            if(percentage>1)
//                percentage=percentage/100.0;
//            evaluatePercentage(classifier,dockeyList,classNum,answerKeyFile,validatingKeyFile,percentage,runs,randomSeed,runName, outputFile);
//        }
//        else if(mode.equalsIgnoreCase("Manual")){
//            trainingKeyFile=node.getString("trainingkeyfile");
//            testingKeyFile=node.getString("testingkeyfile");
//            evaluateManual(classifier,dockeyList,classNum,trainingKeyFile,validatingKeyFile,testingKeyFile,runName,outputFile);
//        }
//        else
//            return;
//        out.close();
//    }
//
//    /**
//     * Evaluate a classifier using cross validation.
//     * @param classifier the classifier which should be able to return an index reader,
//     * otherwise switch to the overloading method with the argument of dockeyList (SimpleElementList)
//     * @param classNum the number of classes
//     * @param answerKeyFile the answer key file. The format of the file is as follows: the
//     * first line is the number of total documents. The remaining each line contains a pair
//     * of document category and document key separated by a tab key.
//     * @param validatingKeyFile list of documents for validation purpose. The format of
//     * this file has the same as the answer key file.
//     * @param foldNum the number of folds for cross validation
//     * @param runName the name of the run
//     * @param outputFile the output file
//     */
//    public void evaluateCrossValidation(Classifier classifier, int classNum,  String answerKeyFile, String validatingKeyFile,
//                                        int foldNum, String runName, String outputFile){
//       evaluateCrossValidation(classifier,null, classNum,answerKeyFile,validatingKeyFile,foldNum,runName, outputFile);
//    }
//
//    public void evaluateCrossValidation(Classifier classifier, SimpleElementList dockeyList, int classNum,  String answerKeyFile, String validatingKeyFile,
//                                        int foldNum, String runName, String outputFile){
//        IndexReader indexReader;
//        DocClassSet validatingSet, trainingSet;
//        DocClass testingDocs;
//        int[] answers;
//        ArrayList answerKeyList, folds[];
//        DoubleVector result;
//        int i, top;
//
//        map.clear();
//        maxCategory=0;
//        labelList.clear();
//        out=FileUtil.getPrintWriter(outputFile,true);
//        indexReader=classifier.getIndexReader();
//        if(validatingKeyFile!=null)
//            validatingSet=getDocClassSet(getAnswerKeyList(indexReader,dockeyList, validatingKeyFile),classNum);
//        else
//            validatingSet=null;
//        answerKeyList=getAnswerKeyList(indexReader,dockeyList, answerKeyFile);
//        Collections.shuffle(answerKeyList, new Random(100));
//        folds=split(answerKeyList,foldNum);
//
//        top=Math.min(5, classNum-1);
//        result=new DoubleVector(7+top);
//        result.assign(0);
//        write("Number of Classes: "+classNum+"\n");
//        write("Number of Documents: "+answerKeyList.size()+"\n");
//        write("Number of Folds: "+foldNum+"\n");
//        printHeader(top);
//        for(i=0;i<foldNum;i++){
//            trainingSet=getCrossTrainingSet(folds,i,classNum);
//            answers=getAnswers(folds[i]);
//            testingDocs=getDocClass(folds[i]);
//            classifier.train(trainingSet,validatingSet);
//            result.add(evaluate(classifier,classNum, testingDocs,answers, runName+"_"+(i+1)));
//        }
//        result.multiply(1.0/foldNum);
//        writeResult("Average",result);
//    }
//
//    /**
//     * Randomly spit the documents in the answerKeyFile into training set and testing set
//     * according to the parameter of percentage and then evaluate the classifier.
//     * @param classifier the classifier which should be able to return an index reader,
//     * otherwise switch to the overloading method with the argument of dockeyList (SimpleElementList)
//     * @param classNum the number of classes
//     * @param answerKeyFile the answer key file. The format of the file is as follows: the
//     * first line is the number of total documents. The remaining each line contains a pair
//     * of document category and document key separated by a tab key.
//     * @param validatingKeyFile list of documents for validation purpose. The format of
//     * this file has the same as the answer key file.
//     * @param percentage the percentage of training documents. The domain of this parameter is
//     * between 0 and 1.
//     * @param runs the number of runs each of which will do a random partition of documents.
//     * @param randomSeed the seed for the random object
//     * @param runName the name of the run
//     * @param outputFile the output file
//     */
//    public void evaluatePercentage(Classifier classifier,int classNum, String answerKeyFile, String validatingKeyFile,
//                                   double percentage, int runs, int randomSeed, String runName, String outputFile){
//        evaluatePercentage(classifier,null,classNum,answerKeyFile,validatingKeyFile,percentage,runs,randomSeed,runName, outputFile);
//    }
//
//    public void evaluatePercentage(Classifier classifier, SimpleElementList dockeyList, int classNum, String answerKeyFile, String validatingKeyFile,
//                                   double percentage, int runs, int randomSeed, String runName, String outputFile){
//        IndexReader indexReader;
//        DocClassSet validatingSet, trainingSet;
//        DocClass testingDocs;
//        ArrayList list, answerKeyList, arrList[];
//        Random random;
//        DoubleVector result;
//        int[] answers;
//        int i, top;
//
//        map.clear();
//        maxCategory=0;
//        labelList.clear();
//        out=FileUtil.getPrintWriter(outputFile,true);
//        if(randomSeed>=0)
//            random=new Random(randomSeed);
//        else
//            random=new Random();
//        indexReader=classifier.getIndexReader();
//        if(validatingKeyFile!=null)
//            validatingSet=getDocClassSet(getAnswerKeyList(indexReader,dockeyList,validatingKeyFile),classNum);
//        else
//            validatingSet=null;
//        answerKeyList=getAnswerKeyList(indexReader,dockeyList, answerKeyFile);
//
//        top=Math.min(5,classNum-1);
//        result=new DoubleVector(7+top);
//        result.assign(0);
//        write("Number of Classes: "+classNum+"\n");
//        write("Number of Documents: "+answerKeyList.size()+"\n");
//        write("Percentage of Training: "+df.format(percentage)+"\n");
//        write("Number of Runs: "+runs+"\n");
//        write("Random Seed: "+randomSeed+"\n");
//        printHeader(top);
//        for(i=0;i<runs;i++){
//            list=new ArrayList(answerKeyList);
//            Collections.shuffle(list,random);
//            arrList=split(list,classNum,percentage);
//            trainingSet=getDocClassSet(arrList[0],classNum);
//            answers=getAnswers(arrList[1]);
//            testingDocs=getDocClass(arrList[1]);
//            classifier.train(trainingSet, validatingSet);
//            result.add(evaluate(classifier, classNum,testingDocs, answers, runName+"_"+(i+1)));
//        }
//        result.multiply(1.0/runs);
//        writeResult("Average",result);
//    }
//
//    /**
//     * Manually specify the training documents and testing documents for evaluation.
//     * @param classifier the classifier which should be able to return an index reader,
//     * otherwise switch to the overloading method with the argument of dockeyList (SimpleElementList)
//     * @param classNum the number of classes
//     * @param trainingKeyFile the file containing a list of training documents. The format of the file is as follows: the
//     * first line is the number of total documents. The remaining each line contains a pair
//     * of document category and document key separated by a tab key.
//     * @param validatingKeyFile list of documents for validation purpose. The format of
//     * this file has the same as the answer key file.
//     * @param testingKeyFile list of testing documents. The format of this file has the same as the answer key file.
//     * @param runName the name of the run
//     * @param outputFile the output file
//     */
//    public void evaluateManual(Classifier classifier,int classNum, String trainingKeyFile, String validatingKeyFile,
//                                   String testingKeyFile, String runName, String outputFile){
//        evaluateManual(classifier,null,classNum,trainingKeyFile,validatingKeyFile,testingKeyFile,runName,outputFile);
//    }
//
//    public void evaluateManual(Classifier classifier, SimpleElementList dockeyList,int classNum, String trainingKeyFile, String validatingKeyFile,
//                                   String testingKeyFile, String runName, String outputFile){
//        IndexReader indexReader;
//        DocClassSet validatingSet, trainingSet;
//        DocClass testingDocs;
//        ArrayList trainingList, testingList;
//        int[] answers;
//        
//        map.clear();
//        maxCategory=0;
//        labelList.clear();
//        out=FileUtil.getPrintWriter(outputFile,true);
//        indexReader=classifier.getIndexReader();
//        if(validatingKeyFile!=null)
//            validatingSet=getDocClassSet(getAnswerKeyList(indexReader,dockeyList,validatingKeyFile),classNum);
//        else
//            validatingSet=null;
//        trainingList=getAnswerKeyList(indexReader,dockeyList,trainingKeyFile);
//        trainingSet=getDocClassSet(trainingList,classNum);
//        testingList=getAnswerKeyList(indexReader,dockeyList,testingKeyFile);
//        answers=getAnswers(testingList);
//        testingDocs=getDocClass(testingList);
//
//        write("Number of Classes: "+classNum+"\n");
//        write("Number of Training Documents: "+trainingList.size()+"\n");
//        write("Number of Testing Documents: "+testingList.size()+"\n");
//
//        classifier.train(trainingSet, validatingSet);
//        printHeader(Math.min(5, classNum-1));
//        evaluate(classifier, classNum, testingDocs, answers, runName);
//    }
//
//    private DoubleVector evaluate(Classifier classifier, int classNum, DocClass testingSet, int[] answer, String runName){
//    	ClassificationEva eva;
//        DoubleVector result;
//        int[][] arrPrediction;
//        int i, label, dim;
//        
//        arrPrediction=new int[testingSet.getDocNum()][];
//        for(i=0;i<testingSet.getDocNum();i++){
//        	label=classifier.classify(testingSet.getDoc(i));
//        	if(label>=0)
//        		arrPrediction[i]=classifier.rank();
//        }
//        dim=Math.min(5, classNum-1);
//        result=new DoubleVector(7+dim);
//        eva=new ClassificationEva();
//        eva.evaluate(classNum, answer,arrPrediction);
//        result.set(0,eva.getMicroRecall());
//        result.set(1,eva.getMicroPrecision());
//        result.set(2,eva.getMicroFScore());
//        result.set(3,eva.getMacroRecall());
//        result.set(4,eva.getMacroPrecision());
//        result.set(5,eva.getMacroFScore());
//        result.set(6, eva.getMRR());
//        for(i=0;i<dim;i++)
//        	result.set(7+i,eva.getPrecisionN(i));
//        writeResult(runName,result);
//        return result;
//    }
//    
//    private void printHeader(int top){
//    	int i;
//    	
//        write("Run\tmicro-R\tmicro-P\tmicro-F\tmacro-R\tmacro-P\tmacro-F\tMRR");
//        for(i=1;i<=top;i++)
//        	write("\tP"+i);
//        write("\n");
//    }
//
//    private void writeResult(String runName, DoubleVector result){
//        int i;
//
//        write(runName);
//        for(i=0;i<result.size();i++){
//            write("\t");
//            write(df.format(result.get(i)));
//        }
//        write("\n");
//    }
//
//    private void write(String message){
//        System.out.print(message);
//        out.write(message);
//        out.flush();
//    }
//
//    private DocClassSet getCrossTrainingSet(ArrayList[] folds, int leaveoutFold, int classNum){
//        ArrayList list;
//        int i;
//
//        list=new ArrayList();
//        for(i=0;i<folds.length;i++)
//            if(i!=leaveoutFold)
//                list.addAll(folds[i]);
//        return getDocClassSet(list,classNum);
//    }
//
//    private ArrayList[] split(ArrayList docList, int foldNum){
//        ArrayList[] folds;
//        int i;
//
//        folds=new ArrayList[foldNum];
//        for(i=0;i<foldNum;i++){
//            folds[i] = new ArrayList(docList.size() / foldNum + 1);
//        }
//        for(i=0;i<docList.size();i++){
//
//            folds[i % foldNum].add(docList.get(i));
//        }
//        return folds;
//    }
//
//    private ArrayList[] split(ArrayList docList, int classNum, double trainingPercentage){
//        ArrayList[] arrList;
//        IRDoc curDoc;
//        int i, arrStat[], arrCount[];
//
//        arrStat=new int[classNum];
//        MathUtil.initArray(arrStat,0);
//        for(i=0;i<docList.size();i++)
//            arrStat[((IRDoc)docList.get(i)).getCategory()]++;
//        for(i=0;i<classNum;i++){
//            arrStat[i] = (int) (arrStat[i] * trainingPercentage + 0.5);
//            if(arrStat[i]==0)
//                arrStat[i]=1;
//        }
//
//        arrList=new ArrayList[2];
//        arrList[0]=new ArrayList(); //training list
//        arrList[1]=new ArrayList(); //testing list
//        arrCount=new int[classNum];
//        MathUtil.initArray(arrCount,0);
//        for(i=0;i<docList.size();i++){
//            curDoc=(IRDoc)docList.get(i);
//            if(arrCount[curDoc.getCategory()]>=arrStat[curDoc.getCategory()])
//                arrList[1].add(curDoc);
//            else{
//                arrList[0].add(curDoc);
//                arrCount[curDoc.getCategory()]++;
//            }
//        }
//        return arrList;
//    }
//
//    private DocClass getDocClass(ArrayList docList){
//        DocClass set;
//        IRDoc curDoc;
//        int i;
//
//        set=new DocClass(-1);
//        for(i=0;i<docList.size();i++){
//            curDoc=(IRDoc)docList.get(i);
//            set.addDoc(curDoc.copy());
//        }
//        return set;
//    }
//
//    private DocClassSet getDocClassSet(ArrayList docList, int classNum){
//        DocClassSet set;
//        IRDoc curDoc;
//        int i;
//
//        set=new DocClassSet(classNum);
//        for(i=0;i<docList.size();i++){
//            curDoc=(IRDoc)docList.get(i);
//            set.addDoc(curDoc.getCategory(),curDoc.copy());
//        }
//        for(i=0;i<set.getClassNum();i++)
//            set.getDocClass(i).setClassName((String)labelList.get(i));
//        return set;
//    }
//    
//    private int[] getAnswers(ArrayList docList){
//    	SortedArray list;
//    	int[] answers;
//    	int i;
//    	
//    	list=new SortedArray();
//    	list.addAll(docList);
//    	list.setComparator(new IndexComparator());
//    	answers=new int[list.size()];
//    	for(i=0;i<list.size();i++)
//    		answers[i]=((IRDoc)list.get(i)).getCategory();
//    	return answers;
//    }
//
//    private ArrayList getAnswerKeyList(IndexReader indexReader, SimpleElementList dockeyList, String answerKeyFile){
//        BufferedReader br;
//        SortedArray answerKeyList;
//        IRDoc irDoc;
//        Integer curCategory;
//        String line, arrTopic[];
//
//        try{
//            br = FileUtil.getTextReader(answerKeyFile);
//            answerKeyList = new SortedArray(Integer.parseInt(br.readLine()), new IndexComparator());
//            while((line=br.readLine())!=null){
//                arrTopic = line.split("\t"); // arrTop[0]: document category, arrTopic[1]: document key
//                if(indexReader!=null){
//                    irDoc = indexReader.getDoc(arrTopic[1]);
//                    if (irDoc == null || irDoc.getTermNum() < 1)
//                        continue;
//                }
//                else{
//                    irDoc=new IRDoc(arrTopic[1]);
//                    irDoc.setIndex(dockeyList.search(arrTopic[1]));
//                }
//                curCategory=(Integer)map.get(arrTopic[0]);
//                if(curCategory==null){
//                    curCategory=new Integer(maxCategory);
//                    maxCategory++;
//                    map.put(arrTopic[0],curCategory);
//                    labelList.add(arrTopic[0]);
//                }
//                irDoc.setCategory(curCategory.intValue());
//                answerKeyList.add(irDoc);
//            }
//            br.close();
//            return answerKeyList;
//        }
//        catch(Exception ex){
//            ex.printStackTrace();
//            return null;
//        }
//    }
//}
