package dragon.config;

import dragon.ir.summarize.*;
import dragon.matrix.vector.DoubleVector;
import dragon.onlinedb.*;
import dragon.util.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * <p>Summarization evaluation configuration </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class SummarizationEvaAppConfig {
    PrintWriter out;

    public static void main(String[] args) {
        SummarizationEvaAppConfig sumApp;
        ConfigureNode root,sumAppNode;
        ConfigUtil util;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and retrieval evaluation id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
        sumAppNode=util.getConfigureNode(root,"summarizationevaapp",Integer.parseInt(args[1]));
        if(sumAppNode==null)
            return;
        sumApp=new SummarizationEvaAppConfig();
        sumApp.evaluate(sumAppNode);
    }

    public void evaluate(ConfigureNode node){
        GenericMultiDocSummarizer summarizer;
        ArticleParser parser;
        String testDataFolder, modelSummaryFolder, outputFolder;
        int maxLength, summarizerID;

        summarizerID=node.getInt("summarizer");
        summarizer=(new SummarizerConfig()).getGenericMultiDocSummarizer(node,summarizerID);
        parser=getArticleParser(node.getString("articleparser"));
        testDataFolder=node.getString("testdatafolder");
        modelSummaryFolder=node.getString("modelsummaryfolder");
        outputFolder=node.getString("outputfolder");
        maxLength=node.getInt("maxlength");
        evaluate(summarizer,parser,testDataFolder, modelSummaryFolder, outputFolder,maxLength);
    }

    public void evaluate(GenericMultiDocSummarizer summarizer, ArticleParser parser, String testDataFolder, String modelSummaryFolder,
                         String outputFolder, int maxLength){
        SimpleCollectionReader reader;
        ROUGE rouge;
        String[] refSummaries, clusterNames;
        String summary;
        DoubleVector result;
        DecimalFormat df;
        int i;

        rouge=new ROUGE();
        rouge.setStopwordOption(false);
        rouge.setLemmatiserOption(false);
        rouge.setCaseOption(false);
        df=FormatUtil.getNumericFormat(1,4);
        result=new DoubleVector(6);
        result.assign(0);
        clusterNames=(new File(testDataFolder)).list();
        (new File(outputFolder)).mkdirs();
        out=FileUtil.getPrintWriter(outputFolder+"/eva.txt");
        printHeader();
        for(i=0;i<clusterNames.length;i++){
            reader=new SimpleCollectionReader(testDataFolder+"/"+clusterNames[i], parser);
            refSummaries = getModelSummaries(modelSummaryFolder, clusterNames[i]);
            summary=summarizer.summarize(reader,maxLength);
            reader.close();
            FileUtil.saveTextFile(outputFolder+"/"+clusterNames[i]+"_sum.txt",summary);
            result.add(evaluate(rouge, clusterNames[i], summary,refSummaries));
        }
        result.multiply(1.0/clusterNames.length);
        write("Average\t"+df.format(result.get(0))+"\t"+df.format(result.get(1))+"\t"+df.format(result.get(2))+"\t"+
            df.format(result.get(3))+"\t"+df.format(result.get(4))+"\t"+df.format(result.get(5)));
        out.close();
    }

    private void printHeader(){
        write("Run\tR-1.Min\tR-1.Max\tR-1.Avg\tR-2.Min\tR-2.Max\tR-2.Avg");
    }

    private DoubleVector evaluate(ROUGE rouge, String clusterName, String autoSummary, String[] refSummaries ){
        DoubleVector result;
        DecimalFormat df;

        df=FormatUtil.getNumericFormat(1,4);
        result=new DoubleVector(6);
        rouge.useRougeN(1);
        rouge.evaluate(autoSummary, refSummaries);
        rouge.setMultipleReferenceMode(ROUGE.MULTIPLE_MIN);
        result.set(0,rouge.getRecall());
        rouge.setMultipleReferenceMode(ROUGE.MULTIPLE_MAX);
        result.set(1,rouge.getRecall());
        rouge.setMultipleReferenceMode(ROUGE.MULTIPLE_AVG);
        result.set(2,rouge.getRecall());
        rouge.useRougeN(2);
        rouge.evaluate(autoSummary, refSummaries);
        rouge.setMultipleReferenceMode(ROUGE.MULTIPLE_MIN);
        result.set(3,rouge.getRecall());
        rouge.setMultipleReferenceMode(ROUGE.MULTIPLE_MAX);
        result.set(4,rouge.getRecall());
        rouge.setMultipleReferenceMode(ROUGE.MULTIPLE_AVG);
        result.set(5,rouge.getRecall());
        write(clusterName+"\t"+df.format(result.get(0))+"\t"+df.format(result.get(1))+"\t"+df.format(result.get(2))+"\t"+
            df.format(result.get(3))+"\t"+df.format(result.get(4))+"\t"+df.format(result.get(5)));
        return result;
    }

    private String[] getModelSummaries(String modelFolder, String clusterName){
        File modelDir, arrSum[];
        ArrayList list;
        String[] summaries;
        int i;

        modelDir=new File(modelFolder);
        arrSum=modelDir.listFiles(new WildCardFilter(clusterName+"*"));
        if(arrSum==null || arrSum.length==0){
            clusterName=clusterName.substring(0,clusterName.length()-1);
            arrSum = modelDir.listFiles(new WildCardFilter(clusterName + "*"));
        }
        list=new ArrayList(arrSum.length);
        summaries=new String[arrSum.length];
        for(i=0;i<arrSum.length;i++)
            if(arrSum[i].isFile())
                list.add(FileUtil.readTextFile(arrSum[i]));
        summaries=new String[list.size()];
        for(i=0;i<summaries.length;i++)
            summaries[i]=(String)list.get(i);
        return summaries;
    }

    private void write(String message){
        System.out.println(message);
        if(out!=null){
            out.println(message);
            out.flush();
        }
    }

    private ArticleParser getArticleParser(String className){
     Class myClass;

     try{
         myClass = Class.forName(className);
         return (ArticleParser) myClass.newInstance();
     }
     catch(Exception e){
         e.printStackTrace();
         return null;
     }
}

}
