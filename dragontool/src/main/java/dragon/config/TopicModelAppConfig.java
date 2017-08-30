//package dragon.config;
//
//import dragon.ir.topicmodel.*;
//import dragon.nlp.SimpleElementList;
//
///**
// * <p>Topic model application configuration </p>
// * <p> </p>
// * <p>Copyright: Copyright (c) 2005</p>
// * <p>Company: IST, Drexel University</p>
// * @author Davis Zhou
// * @version 1.0
// */
//
//public class TopicModelAppConfig {
//    public TopicModelAppConfig() {
//    }
//
//    public static void main(String[] args) {
//        TopicModelAppConfig topicApp;
//        ConfigureNode root,topicNode;
//        ConfigUtil util;
//
//        if(args.length!=2){
//            System.out.println("Please input two parameters: configuration xml file and indexing applicaiton id");
//            return;
//        }
//
//        root=new BasicConfigureNode(args[0]);
//        util=new ConfigUtil();
//        topicNode=util.getConfigureNode(root,"topicmodelapp",Integer.parseInt(args[1]));
//        if(topicNode==null)
//            return;
//        topicApp=new TopicModelAppConfig();
//        topicApp.runTopicModel(topicNode);
//    }
//
//    public void runTopicModel(ConfigureNode node){
//        TopicModel topicModel;
//        ModelExcelWriter writer;
//        String outputFile, termKeyFile, termKeyList[];
//        int modelID, topicNum, top;
//
//        topicNum=node.getInt("topicnum");
//        top=node.getInt("top", 20);
//        modelID=node.getInt("topicmodel");
//        topicModel=(new TopicModelConfig()).getTopicModel(node,modelID);
//        outputFile=node.getString("outputfile");
//        if(!outputFile.endsWith(".xls"))
//            outputFile=outputFile+".xls";
//        termKeyFile=node.getString("termkeyfile",null);
//        if(termKeyFile==null)
//            termKeyList=null;
//        else
//            termKeyList=getTermKeyList(termKeyFile);
//        if(!topicModel.estimateModel(topicNum))
//            return;
//        writer=new ModelExcelWriter();
//        writer.write(topicModel,termKeyList,top,outputFile);
//    }
//
//    private String[] getTermKeyList(String termKeyFile){
//        SimpleElementList list;
//        String[] termKeyList;
//        int i;
//
//        list=new SimpleElementList(termKeyFile,false);
//        termKeyList=new String[list.size()];
//        for(i=0;i<termKeyList.length;i++)
//            termKeyList[i]=list.search(i);
//        return termKeyList;
//    }
//}
