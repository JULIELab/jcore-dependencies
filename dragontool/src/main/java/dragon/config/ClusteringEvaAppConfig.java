package dragon.config;

import dragon.ir.clustering.Clustering;
import dragon.ir.clustering.ClusteringEva;
import dragon.ir.clustering.DocClusterSet;
import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.nlp.SimpleElementList;
import dragon.nlp.compare.IndexComparator;
import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * <p>Clustering evaluation configuration </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ClusteringEvaAppConfig {
    private TreeMap map;
    private ArrayList labelList;
    private int maxCategory;

    public ClusteringEvaAppConfig() {
        map=new TreeMap();
        maxCategory=0;
        labelList=new ArrayList();
    }

    public static void main(String[] args) {
        ClusteringEvaAppConfig clusteringApp;
        ConfigureNode root,clusteringAppNode;
        ConfigUtil util;
        String appName;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and clustering evaluation id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
        clusteringAppNode=util.getConfigureNode(root,"clusteringevaapp",Integer.parseInt(args[1]));
        if(clusteringAppNode==null)
            return;
        clusteringApp=new ClusteringEvaAppConfig();
        appName=clusteringAppNode.getNodeName();
        if(appName.equalsIgnoreCase("AgglomerativeEvaApp"))
            clusteringApp.evaAgglomerativeClustering(clusteringAppNode);
        else if(appName.equalsIgnoreCase("PartitionEvaApp"))
            clusteringApp.evaPartitionClustering(clusteringAppNode);
        else
            return;
    }

    public void evaAgglomerativeClustering(ConfigureNode node){
        Clustering clusteringMethod;
        SimpleElementList docKeyList;
        String  docKeyFile, answerKey, outputFile, runName;
        int clusteringID;

        docKeyFile=node.getString("dockeyfile");
        if(docKeyFile==null)
        	docKeyList=null;
        else
        	docKeyList=new SimpleElementList(docKeyFile,false);
        clusteringID=node.getInt("clustering");
        clusteringMethod=(new ClusteringConfig()).getClustering(node,clusteringID);
        answerKey=node.getString("answerkey");
        outputFile=node.getString("outputfile");
        runName=node.getString("runname");
        evaAgglomerativeClustering(clusteringMethod,docKeyList, answerKey,runName,outputFile);
    }
    
    public void evaAgglomerativeClustering(Clustering clusterMethod,String answerKeyFile,String runName, String outFile){
    	evaAgglomerativeClustering(clusterMethod,null,answerKeyFile, runName,outFile);
    }
    
    public void evaAgglomerativeClustering(Clustering clusterMethod,SimpleElementList docKeyList, String answerKeyFile,String runName, String outFile){
        DocClusterSet human, machine;
        IRDoc[] arrDoc;
        int i;

        map.clear();
        maxCategory=0;
        labelList.clear();
        arrDoc=getValidDocs(clusterMethod.getIndexReader(),docKeyList, answerKeyFile);
        human=readHumanClusterSet(arrDoc,maxCategory);
        clusterMethod.cluster(arrDoc);
        machine=clusterMethod.getClusterSet();
        for(i=0;i<machine.getClusterNum();i++){
            System.out.println(machine.getDocCluster(i).getDocNum());
        }
        printHeader(outFile);
        evaluate(machine, human, runName, outFile);
    }

    public void evaPartitionClustering(ConfigureNode node){
        Clustering clusteringMethod;
        SimpleElementList docKeyList;
        String  docKeyFile, answerKey, outputFile, runName;
        int clusteringID, run;

        docKeyFile=node.getString("dockeyfile");
        if(docKeyFile==null)
        	docKeyList=null;
        else
        	docKeyList=new SimpleElementList(docKeyFile,false);
        clusteringID=node.getInt("clustering");
        clusteringMethod=(new ClusteringConfig()).getClustering(node,clusteringID);
        answerKey=node.getString("answerkey");
        outputFile=node.getString("outputfile");
        runName=node.getString("runname");
        run=node.getInt("run",1);
        evaPartitionClustering(clusteringMethod,docKeyList, answerKey,run, runName,outputFile);
    }
    
    public void evaPartitionClustering(Clustering clusterMethod, String answerKeyFile, int run, String runName, String outFile){
    	evaPartitionClustering(clusterMethod,null,answerKeyFile,run,runName,outFile);
    }
    
    public void evaPartitionClustering(Clustering clusterMethod, SimpleElementList docKeyList, String answerKeyFile, int run, String runName, String outFile){
        DocClusterSet human, machine;
        IRDoc[] arrDoc;
        String curRunName;
        long randomSeed;
        int  i,j;

        map.clear();
        maxCategory=0;
        labelList.clear();
        randomSeed=clusterMethod.getRandomSeed();
        if(randomSeed<0 && run>1)
            randomSeed=0;
        arrDoc=getValidDocs(clusterMethod.getIndexReader(),docKeyList, answerKeyFile);
        human=readHumanClusterSet(arrDoc,maxCategory);
        printHeader(outFile);
        for(i=0;i<run;i++){
            clusterMethod.setRandomSeed(randomSeed);
            clusterMethod.cluster(arrDoc);
            machine = clusterMethod.getClusterSet();
            for (j = 0; j < machine.getClusterNum(); j++) {
                System.out.println(machine.getDocCluster(j).getDocNum());
            }
            if(runName.length()==0)
                curRunName = String.valueOf(randomSeed);
            else
                curRunName = runName + " " + String.valueOf(randomSeed);
            evaluate(machine, human, curRunName, outFile);
            randomSeed+=100;
        }
    }

    private void evaluate(DocClusterSet machine, DocClusterSet human, String runName, String outFile){
        ClusteringEva eva;

        eva=new ClusteringEva();
        eva.evaluate(machine,human);
        System.out.println("Number of Clusters: "+human.getClusterNum());
        System.out.println("Entropy: "+eva.getEntropy());
        System.out.println("FScore: "+eva.getFScore());
        System.out.println("Purity:" +eva.getPurity());
        System.out.println("MutualInformation:" +eva.getMI());
        System.out.println("NMI:" +eva.getNMI());
        System.out.println("Geometry NMI:" +eva.getGeometryNMI());
        printResult(eva,runName, outFile);
    }

    private void printResult(ClusteringEva ce, String runName, String outFile){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFile), true));
            if (runName != null && runName.length() > 0)
                bw.write(runName + "\t");
            bw.write(ce.getEntropy() + "\t" + ce.getFScore() + "\t" + ce.getPurity() + "\t" + ce.getMI() + "\t" +
                     ce.getNMI() + "\t" + ce.getGeometryNMI() + "\n");
            bw.flush();
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void printHeader(String outputFile){
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(new File(outputFile), true));
            bw.write("Run\tEntropy\tFScore\tPurity\tMI\tNMI\tG-NMI\n");
            bw.flush();
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private DocClusterSet readHumanClusterSet(IRDoc[] arrDoc, int clusterNum) {
        DocClusterSet docClusterSet;
        int i;

        docClusterSet = new DocClusterSet(clusterNum);
        for (i = 0; i < arrDoc.length; i++)
            docClusterSet.addDoc(arrDoc[i].getCategory(), arrDoc[i]);
        for(i=0;i<clusterNum;i++)
            docClusterSet.getDocCluster(i).setClusterName((String)labelList.get(i));
        return docClusterSet;
    }

    private IRDoc[] getValidDocs(IndexReader indexReader, SimpleElementList docKeyList, String answerKeyFile){
        BufferedReader br;
        SortedArray answerKeyList;
        IRDoc irDoc;
        IRDoc[] arrDoc;
        Integer curCategory;
        String line, arrTopic[];
        int i;

        try{
            br = FileUtil.getTextReader(answerKeyFile);
            answerKeyList = new SortedArray(Integer.parseInt(br.readLine()), new IndexComparator());
            while((line=br.readLine())!=null){
                arrTopic = line.split("\t");
                if(indexReader!=null){
	                irDoc = indexReader.getDoc(arrTopic[1]);
	                if(irDoc==null || irDoc.getTermNum()<1)
	                    continue;
                }
                else{
                	irDoc=new IRDoc(arrTopic[1]);
                	irDoc.setIndex(docKeyList.search(arrTopic[1]));
                }
                curCategory=(Integer)map.get(arrTopic[0]);
                if(curCategory==null){
                    curCategory=new Integer(maxCategory);
                    maxCategory++;
                    map.put(arrTopic[0],curCategory);
                    labelList.add(arrTopic[0]);
                }
                irDoc.setCategory(curCategory.intValue());
                answerKeyList.add(irDoc);
            }
            br.close();
            arrDoc=new IRDoc[answerKeyList.size()];
            for (i = 0; i < answerKeyList.size(); i++)
                arrDoc[i] = (IRDoc) answerKeyList.get(i);
            return arrDoc;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
