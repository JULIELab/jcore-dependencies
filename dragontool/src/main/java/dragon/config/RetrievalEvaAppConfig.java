package dragon.config;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.ir.query.IRQuery;
import dragon.ir.query.RelSimpleQuery;
import dragon.ir.search.Searcher;
import dragon.ir.search.evaluate.TrecEva;
import dragon.nlp.SimpleElement;
import dragon.util.FileUtil;
import dragon.util.FormatUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * <p>Retrieval evaluation configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class RetrievalEvaAppConfig {
    public RetrievalEvaAppConfig() {
    }

    public static void main(String[] args) {
        RetrievalEvaAppConfig retrievalApp;
        ConfigureNode root,retrievalAppNode;
        ConfigUtil util;

        if(args.length!=2){
            System.out.println("Please input two parameters: configuration xml file and retrieval evaluation id");
            return;
        }

        root=new BasicConfigureNode(args[0]);
        util=new ConfigUtil();
        retrievalAppNode=util.getConfigureNode(root,"retrievalevaapp",Integer.parseInt(args[1]));
        if(retrievalAppNode==null)
            return;
        retrievalApp=new RetrievalEvaAppConfig();
        retrievalApp.evaluate(retrievalAppNode);
    }

    public void evaluate(ConfigureNode node){
        SearcherConfig config;
        Searcher searcher;
        ArrayList excludedQueries;
        String  judgmentFile, queryFile, resultFolder, excludedTopics, arrTopic[];
        int i, searcherID, start, end;

        config=new SearcherConfig();
        searcherID=node.getInt("searcher",0);
        if(searcherID<=0)
            return;
        searcher=config.getSearcher(node,searcherID);
        judgmentFile=node.getString("judgmentfile",null);
        queryFile=node.getString("queryfile",null);
        resultFolder=node.getString("resultfolder",null);
        if(judgmentFile==null || queryFile==null || resultFolder==null)
            return;
        start=node.getInt("startopic",-1);
        end=node.getInt("endtopic",-1);
        excludedTopics=node.getString("excludedtopics",null);
        if(excludedTopics==null || excludedTopics.trim().length()==0)
            excludedQueries=null;
        else{
            arrTopic=excludedTopics.trim().split(";");
            excludedQueries=new ArrayList(arrTopic.length);
            for(i=0;i<arrTopic.length;i++)
                excludedQueries.add(new Integer(arrTopic[i]));
        }
        evaluate(searcher,judgmentFile,queryFile,resultFolder,start,end,excludedQueries);
    }

    public void evaluate(Searcher searcher, String judgmentFile, String queryFile, String outputFolder,
                         int start, int end, ArrayList excludedQueries) {
        ArrayList list;
        TrecEva eva;
        TreeMap arrRelevant;
        SimpleElement[] arrQuery;
        IRQuery irQuery;
        PrintWriter out;
        DecimalFormat df;
        double[] result, resultSum;
        int i, j, total, queryNo;

        try {
            (new File(outputFolder)).mkdirs();
            arrQuery=readQuery(queryFile);
            arrRelevant=loadJudgmentFile(searcher.getIndexReader(),judgmentFile);
            eva=new TrecEva(outputFolder);
            resultSum=new double[10];
            for(i=0;i<10;i++) resultSum[i]=0;
            df = FormatUtil.getNumericFormat(2, 2);
            total=0;
            out = FileUtil.getPrintWriter(outputFolder + "/all.eva");
            out.write("Query\tRetrieved\tRelevant\tP@10\tR@10\tP@100\tR@100\tP\tR\tAP\r\n");
            for (i= 0; i <arrQuery.length; i++) {
                queryNo=arrQuery[i].getIndex();
                if(queryNo<start && start>=0 || queryNo>end && end>=0) continue;
                list=(ArrayList)arrRelevant.get(new Integer(queryNo));
                if(list==null || list.size()==0) continue;
                if(excludedQueries!=null && excludedQueries.contains(new Integer(queryNo))) continue;
                total=total+1;

                System.out.print("Processing Query #" + queryNo);
                irQuery=new RelSimpleQuery(arrQuery[i].getKey());
                irQuery.setQueryKey(queryNo);
                searcher.search(irQuery);
                result=eva.evaluateQuery(irQuery,searcher.getRankedDocumentList(), list, searcher.getIndexReader());
                printEvaStat(result,out);
                System.out.print(" "+df.format(result[9])+"%\n");
                for(j=0;j<10;j++) resultSum[j]+=result[j];
            }

            for(i=0;i<10;i++) resultSum[i]=resultSum[i]/total;
            System.out.println("MAP:  " +df.format(resultSum[9])+"%");
            System.out.println("P@10: " +df.format(resultSum[3])+"%");
            System.out.println("P@100:" +df.format(resultSum[5])+"%");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printEvaStat(double[] result, PrintWriter out) {
        DecimalFormat df, dfInteger;
        int i;

        try {
            df = FormatUtil.getNumericFormat(2, 2);
            dfInteger=FormatUtil.getNumericFormat(0,0);

            for(i=0;i<3;i++){
                if(i>0) out.write('\t');
                out.write(dfInteger.format(result[i]));
            }
            for(i=3;i<=9;i++){
                out.write('\t');
                out.write(df.format(result[i]));
            }
            out.write("\r\n");
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SimpleElement[] readQuery(String queryFile){
        SimpleElement[] arrQuery;
        String line;
        int total, j, count,lineNo;

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(queryFile)));
            line = br.readLine();
            total = Integer.parseInt(line);
            arrQuery = new SimpleElement[total];
            count=0;
            while ( (line = br.readLine()) != null) {
                j = line.indexOf('\t');
                lineNo = Integer.parseInt(line.substring(0, j));
                arrQuery[count] = new SimpleElement(line.substring(j + 1),lineNo);
                count++;
            }
            br.close();
            return arrQuery;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private TreeMap loadJudgmentFile(IndexReader reader, String filename) {
        TreeMap arrRelevant;
        ArrayList list;
        BufferedReader br;
        IRDoc cur;
        int start, end, count;
        int topicID, relevance;
        String pmid, line;


        try {
            arrRelevant=new TreeMap();
            count = 0;
            br=FileUtil.getTextReader(filename);
            while ((line=br.readLine())!=null ) {
                count = count + 1;
                start = line.indexOf('\t');
                end = line.indexOf('\t', start + 1);
                topicID = Integer.parseInt(line.substring(0, start));
                pmid = line.substring(start + 1, end);
                relevance = Integer.parseInt(line.substring(end + 1));
                if(relevance>0 && relevance<3){
                    cur=reader.getDoc(pmid);
                    if(cur!=null){
                        list = (ArrayList) arrRelevant.get(new Integer(topicID));
                        if (list == null){
                            list = new ArrayList();
                            arrRelevant.put(new Integer(topicID),list);
                        }
                        list.add(cur);
                    }
                }
            }
            return arrRelevant;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}