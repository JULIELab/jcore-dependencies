package dragon.ir.search.evaluate;

import dragon.ir.index.*;
import dragon.ir.query.IRQuery;
import dragon.nlp.compare.IndexComparator;
import dragon.util.*;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;

/**
 * <p>Trec IR Peformance Evaluation Program</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TrecEva {
    private String evaResultFolder;
    private double[] evaResult;

    public TrecEva(String evaResultFolder) {
        this.evaResultFolder =evaResultFolder;
    }

    public double[] evaluateQuery(IRQuery query, ArrayList hitlist, ArrayList relevantList){
        return evaluateQuery(query,hitlist,relevantList,null);
    }

    public double[] evaluateQuery(IRQuery query, ArrayList hitlist, ArrayList relevantList, IndexReader indexReader){
        IRDoc curDoc;
        Comparator indexComparator;
        DecimalFormat df, dfWeight;
        PrintWriter out1;
        int topicID;
        int base, relevant, i, retrieved;
        double avgPrecision, overallPrecision, overallRecall;
        double top100Precision, top100Recall, top10Precision, top10Recall;
        double[] result;

        try {
            top100Precision = 0;
            top100Recall = 0;
            top10Precision = 0;
            top10Recall = 0;
            avgPrecision = 0;
            overallPrecision = 0;
            overallRecall = 0;
            result=new double[10];

            if(relevantList==null || relevantList.size()==0 || hitlist==null || hitlist.size()==0) return result;

            df = FormatUtil.getNumericFormat(2, 2);
            dfWeight = FormatUtil.getNumericFormat(2, 4);
            topicID=query.getQueryKey();
            out1=FileUtil.getPrintWriter(evaResultFolder+"/topic_"+topicID+".eva");
            base = relevantList.size();
            retrieved=hitlist.size();
            if(retrieved>1000) retrieved = 1000;
            relevant = 0;
            indexComparator=new IndexComparator();
            Collections.sort(relevantList,indexComparator);

            out1.write(query.toString() + "\n");
            out1.flush();
            for (i = 0; i < retrieved; i++) {
                curDoc=(IRDoc)hitlist.get(i);
                if (SortedArray.binarySearch(relevantList,curDoc,indexComparator)>=0) {
                    relevant++;
                    avgPrecision += 100.0 * relevant / (i + 1);
                }
                if (i == 9) {
                    top10Precision = 100.0 * relevant / (i + 1);
                    top10Recall = 100.0 * relevant / base;
                }
                if (i == 99) {
                    top100Precision = 100.0 * relevant / (i + 1);
                    top100Recall = 100.0 * relevant / base;
                }
                if(indexReader==null)
                    out1.write("Top " + (i + 1) + " #" + curDoc.getIndex() + "(" + relevant + "): ");
                else
                    out1.write("Top " + (i + 1) + " #" + indexReader.getDocKey(curDoc.getIndex())+"(#"+curDoc.getIndex() + ", " + relevant + "): ");
                out1.write(dfWeight.format(curDoc.getWeight()) + " " + df.format(100.0 * relevant / (i + 1)) +
                           "%/" + df.format(100.0 * relevant / base) + "%\r\n");
                out1.flush();
            }
            overallPrecision = 100.0 * relevant / retrieved;
            overallRecall = 100.0 * relevant / base;
            if (retrieved < 10) {
                top10Precision = overallPrecision;
                top10Recall = overallRecall;
            }
            if (retrieved < 100) {
                top100Precision = overallPrecision;
                top100Recall = overallRecall;
            }
            avgPrecision = avgPrecision / base;
            out1.write("Top 10 Precison/Recall:" + df.format(top10Precision) + "%/" + df.format(top10Recall) + "%\r\n");
            out1.write("Top 100 Precison/Recall:" + df.format(top100Precision) + "%/" + df.format(top100Recall) +
                       "%\r\n");
            out1.write("Overall Precison/Recall:" + df.format(overallPrecision) + "%/" + df.format(overallRecall) +
                       "%\r\n");
            out1.write("Average Precison:" + df.format(avgPrecision) + "%\r\n");
            out1.close();

            result[0]=topicID;
            result[1]=retrieved;
            result[2]=relevant;
            result[3]=top10Precision;
            result[4]=top10Recall;
            result[5]=top100Precision;
            result[6]=top100Recall;
            result[7]=overallPrecision;
            result[8]=overallRecall;
            result[9]=avgPrecision;
            evaResult=new double[10];
            System.arraycopy(result,0,evaResult,0,10);
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getTopicID(){
        return (int)evaResult[0];
    }

    public int getRetrievedDocNum(){
        return (int)evaResult[1];
    }

    public int getRelevantDocNum(){
        return (int)evaResult[2];
    }

    public double getTop10Precision(){
        return evaResult[3];
    }

    public double getTop10Recall(){
        return evaResult[4];
    }

    public double getTop100Precision(){
        return evaResult[5];
    }

    public double getTop100Recall(){
        return evaResult[6];
    }

    public double getOverallPrecision(){
        return evaResult[7];
    }

    public double getOverallRecall(){
        return evaResult[8];
    }

    public double getAveragePrecision(){
        return evaResult[9];
    }
}