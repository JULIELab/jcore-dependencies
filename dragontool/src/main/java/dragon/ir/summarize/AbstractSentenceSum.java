package dragon.ir.summarize;

import dragon.ir.clustering.*;
import dragon.ir.index.*;
import dragon.matrix.vector.DoubleVector;
import dragon.nlp.compare.*;
import dragon.onlinedb.Article;
import dragon.util.SortedArray;
import java.util.*;

/**
 * <p>The class implement basic function of building summary given a sentence set. </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public abstract class AbstractSentenceSum {

    protected String buildSummary(IndexReader indexReader, ArrayList sentSet, int summaryLength, DoubleVector weightVector){
        ArrayList list;
        IRDoc curDoc;
        Article article;
        TopicSummary summary;
        StringBuffer autoSum;
        String curSentence;
        int i, curLength;

        curLength=0;
        summary=new TopicSummary(TextUnit.UNIT_SENTENCE);
        list=new ArrayList(sentSet.size());
        for(i=0;i<sentSet.size();i++){
            curDoc=(IRDoc)sentSet.get(i);
            curDoc.setWeight(weightVector.get(i));
            list.add(curDoc);
        }
        Collections.sort(list,new WeightComparator(true));

        for(i=0;i<list.size() && curLength<summaryLength; i++){
            curDoc=(IRDoc)list.get(i);
            article=indexReader.getOriginalDoc(curDoc.getIndex());
            if(article==null || (curSentence=article.getTitle())==null)
                continue;
            if(summary.contains(new TextUnit(curSentence)))
                continue;
            if(curLength<summaryLength){
                summary.addText(new TextUnit(curSentence,curDoc.getIndex(),curDoc.getWeight()));
                curLength+=curSentence.length();
            }
        }
        summary.sortByWegiht();
        if(summary.size()==0)
            return null;

        autoSum = new StringBuffer(summary.getTextUnit(0).getText());
        for (i = 1; i < summary.size(); i++) {
            autoSum.append("\n");
            autoSum.append(summary.getTextUnit(i).getText());
        }
        if(autoSum.length()<=summaryLength)
            return autoSum.toString();
        else
            return autoSum.substring(0,summaryLength);
    }

    protected String buildSummary(IndexReader indexReader, ArrayList sentSet, int summaryLength, DoubleVector weightVector,
                                  DocClusterSet clusters){
        SortedArray list;
        IRDoc curDoc;
        Article article;
        TopicSummary summary;
        DocCluster curCluster;
        StringBuffer autoSum;
        String curSentence;
        boolean[] usedDoc, usedCluster;
        int i, j, pos,curLength;


        list=new SortedArray(sentSet.size(), new IndexComparator());
        for(i=0;i<sentSet.size();i++){
            curDoc=(IRDoc)sentSet.get(i);
            curDoc.setWeight(weightVector.get(i));
            list.add(curDoc);
        }

        for(i=0;i<clusters.getClusterNum();i++){
            curCluster=clusters.getDocCluster(i);
            for(j=0;j<curCluster.getDocNum();j++){
                curDoc=curCluster.getDoc(j);
                pos=list.binarySearch(curDoc);
                if(pos<0)
                    continue;
                curDoc=(IRDoc)list.get(pos);
                curDoc.setCategory(i);
            }
        }

        list.setComparator(new WeightComparator(true));
        usedDoc=new boolean[list.size()];
        usedCluster=new boolean[clusters.getClusterNum()];
        curLength=0;
        summary=new TopicSummary(TextUnit.UNIT_SENTENCE);

        //extract one sentence with highest score from each cluster
        for(i=0;i<list.size() && curLength<summaryLength; i++){
            curDoc=(IRDoc)list.get(i);
            if(usedCluster[curDoc.getCategory()])
                continue;
            article=indexReader.getOriginalDoc(curDoc.getIndex());
            if(article==null || (curSentence=article.getTitle())==null)
                continue;
            if(summary.contains(new TextUnit(curSentence)))
                continue;
            if(curLength<summaryLength){
                summary.addText(new TextUnit(curSentence,curDoc.getIndex(),curDoc.getWeight()));
                curLength+=curSentence.length();
                usedCluster[curDoc.getCategory()]=true;
                usedDoc[i]=true;
            }
        }

        //extract remaining sentences
        for(i=0;i<list.size() && curLength<summaryLength; i++){
            if(usedDoc[i])
                continue;
            curDoc=(IRDoc)list.get(i);
            article=indexReader.getOriginalDoc(curDoc.getIndex());
            if(article==null || (curSentence=article.getTitle())==null)
                continue;
            if(summary.contains(new TextUnit(curSentence)))
                continue;
            if(curLength<summaryLength){
                summary.addText(new TextUnit(curSentence,curDoc.getIndex(),curDoc.getWeight()));
                curLength+=curSentence.length();
                usedDoc[i]=true;
            }
        }

        summary.sortByWegiht();
        if(summary.size()==0)
            return null;

        autoSum = new StringBuffer(summary.getTextUnit(0).getText());
        for (i = 1; i < summary.size(); i++) {
            autoSum.append("\n");
            autoSum.append(summary.getTextUnit(i).getText());
        }
        if(autoSum.length()<=summaryLength)
            return autoSum.toString();
        else
            return autoSum.substring(0,summaryLength);
    }

}