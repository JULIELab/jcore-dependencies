package dragon.ir.search.expand;

import dragon.ir.index.*;
import dragon.ir.kngbase.HALSpace;
import dragon.ir.query.*;
import dragon.nlp.*;
import dragon.nlp.compare.*;
import dragon.matrix.*;
import java.util.*;

/**
 * <p>Query Expansion based on Information Flow</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class InformationFlowQE extends AbstractQE implements QueryExpansion{
    private DoubleSparseMatrix halMatrix;
    private SimpleElementList halTermList;
    private IndexReader indexReader;
    private int expandTerm;
    private double expandCoeffi;
    private double dominance1;
    private double dominance2;
    private double multiplier;
    private double threshold1;
    private double threshold2;

    public InformationFlowQE(IndexReader indexReader, int expandTermNum, double expandCoeffi) {
        super(indexReader);
        this.indexReader=indexReader;
        this.expandTerm=expandTermNum;
        this.expandCoeffi=expandCoeffi;
        this.dominance1 = 0.5;
        this.dominance2 = 0.3;
        this.multiplier = 2;
        this.threshold1 = 0;
        this.threshold2 = 0;
    }

    public InformationFlowQE(HALSpace halSpace, IndexReader indexReader, int expandTermNum, double expandCoeffi) {
        super(indexReader);
        this.halMatrix = halSpace.getKnowledgeMatrix();
        this.halTermList =halSpace.getRowKeyList();
        this.indexReader=indexReader;
        this.expandTerm=expandTermNum;
        this.expandCoeffi=expandCoeffi;
        this.dominance1 = 0.5;
        this.dominance2 = 0.3;
        this.multiplier = 2;
        this.threshold1 = 0;
        this.threshold2 = 0;
    }

    public void setHALSpace(HALSpace halSpace){
        this.halMatrix = halSpace.getKnowledgeMatrix();
        this.halTermList =halSpace.getRowKeyList();
    }

    public void setMultiplier(double multiplier){
        if(multiplier<1)
            return;
        this.multiplier =multiplier;
    }

    public double getMultiplier(){
        return multiplier;
    }

    public void setDominantVectorWeight(double weight){
        if(weight<0 || weight>1)
            return;
        this.dominance1 =weight;
    }

    public double getDominantVectorWeight(){
        return dominance1;
    }

    public void setSubordinateVectorWeight(double weight){
        if(weight<0 || weight>1)
            return;
        this.dominance2 =weight;
    }

    public double getSubordinateVectorWeight(){
        return dominance2;
    }

    public void setDominantVectorThreshold(double threshold){
        if(threshold<0) return;
        this.threshold1=threshold;
    }

    public double getDominantVectorThreshold(){
        return threshold1;
    }

    public void setSubordinateVectorThreshold(double threshold){
        if(threshold<0) return;
        this.threshold2=threshold;
    }

    public double getSubordinateVectorThreshold(){
        return threshold2;
    }

    public IRQuery expand(IRQuery initQuery){
        SimpleTermPredicate[] arrPredicate, arrExpand;
        ArrayList oldList, newList;
        Token token;
        int docNum;
        int i, index,halIndex;

        if(!initQuery.isRelSimpleQuery()) return null;
        arrPredicate=checkSimpleTermQuery((RelSimpleQuery)initQuery);
        if(arrPredicate==null || arrPredicate.length==0 ||halMatrix==null)
            return initQuery;

        oldList=new ArrayList();
        docNum=indexReader.getCollection().getDocNum();
        for(i=0;i<arrPredicate.length;i++){
            index=arrPredicate[i].getIndex();
            //map query term to hal term
            halIndex=halTermList.search(arrPredicate[i].getKey());
            if(halIndex>=0 && halMatrix.getNonZeroNumInRow(halIndex)>0){
                token=new Token(arrPredicate[i].getKey());
                token.setIndex(halIndex);
                token.setWeight(arrPredicate[i].getWeight()*Math.log(docNum*1.0/indexReader.getIRTerm(index).getDocFrequency()));
                oldList.add(token);
            }
        }
        if(oldList.size()==0) return initQuery;

        newList=expand(oldList,expandTerm);
        arrExpand=new SimpleTermPredicate[newList.size()];
        for(i=0;i<arrExpand.length;i++){
            token=(Token)newList.get(i);
            arrExpand[i]=buildSimpleTermPredicate(token.getIndex(), token.getWeight());
        }

        return buildQuery(arrPredicate,arrExpand,expandCoeffi);
    }

    //select top n information flows for the specified query
    private ArrayList expand(ArrayList queryList, int top){
        ArrayList candidates;
        ArrayList topList;
        DoubleRow first, second;
        IRTerm term;
        Token token;
        double sum;
        int i, rowIndex;

        candidates = new ArrayList(halMatrix.rows());
        topList = new ArrayList(top);

        //sort tokens by weight (qtf*idf);
        Collections.sort(queryList,new WeightComparator(true));
        token = (Token) queryList.get(0);
        rowIndex = token.getIndex();

        first=getVector(rowIndex);
        //combining query term vector
        for(i=1;i<queryList.size();i++){
            rowIndex=token.getIndex();
            second=getVector(rowIndex);
            first=combineVector(first,second);
        }

        //calculate degree between combined query concept "i" and corpus concept "j"
        first=filterVector(first,getMeanScoreInVector(first));
        sum=getSumScoreInVector(first);

        for (i = 0; i < halMatrix.rows(); i++) {
            if(halMatrix.getNonZeroNumInRow(i)==0) continue;

            second=getVector(i);
            token = new Token(null);
            token.setIndex(i);
            token.setWeight(intersect(first,second)/sum);
            candidates.add(token);
        }

        Collections.sort(candidates,new WeightComparator(true));
        for(i=0;i<top;i++){
            token=(Token)candidates.get(i);
            //map HAL term to query term
            term=indexReader.getIRTerm(halTermList.search(token.getIndex()));
            if(term!=null){
                token.setIndex(term.getIndex());
                topList.add(token);
            }
        }
        candidates.clear();
        return topList;
    }

    private DoubleRow getVector(int index){
       return new DoubleRow(index,halMatrix.getNonZeroNumInRow(index),halMatrix.getNonZeroColumnsInRow(index), halMatrix.getNonZeroDoubleScoresInRow(index));
    }

    private double intersect(DoubleRow first, DoubleRow second){
        int i, j, len1, len2;
        int x, y;
        double sum;

        i=0;
        j=0;
        sum=0;
        len1=first.getNonZeroNum();
        len2=second.getNonZeroNum();
        while(i<len1 && j<len2){
            x=first.getNonZeroColumn(i);
            y=second.getNonZeroColumn(j);
            if(x==y){
                sum+=first.getNonZeroDoubleScore(i);
                i++;
                j++;
            }
            else if(x<y){
                i++;
            }
            else{
                j++;
            }
        }
        return sum;
    }

    private DoubleRow combineVector(DoubleRow first, DoubleRow second){
        DoubleRow vector;

        first=reweightVector(first,dominance1);
        second=reweightVector(second,dominance2);
        strengthenCommonProperty(first,threshold1,second,threshold2, multiplier);
        vector=addVector(first,second);
        return normalizeVector(vector);
    }

    private DoubleRow filterVector(DoubleRow vector, double threshold){
        ArrayList tokenList;
        Token curToken;
        int[] arrCol;
        double[] arrScore;
        int i, len;
        double score;

        len=vector.getNonZeroNum();
        tokenList=new ArrayList();
        for(i=0;i<len;i++){
            score=vector.getNonZeroDoubleScore(i);
            if(score>=threshold){
                curToken=new Token(null);
                curToken.setIndex(vector.getNonZeroColumn(i));
                curToken.setWeight(score);
                tokenList.add(curToken);
            }
        }

        arrCol=new int[tokenList.size()];
        arrScore=new double[tokenList.size()];
        for(i=0;i<tokenList.size();i++){
            curToken=(Token)tokenList.get(i);
            arrCol[i]=curToken.getIndex();
            arrScore[i]=curToken.getWeight();
        }
        return new DoubleRow(vector.getRowIndex(),tokenList.size(),arrCol,arrScore);
    }

    private double getMaxScoreInVector(DoubleRow vector){
        double max;
        int i,len;

        max=Double.MIN_VALUE;
        len=vector.getNonZeroNum();
        for(i=0;i<len;i++)
            if (max < vector.getNonZeroDoubleScore(i) ) max=vector.getNonZeroDoubleScore(i);
        return max;
    }

    private double getMeanScoreInVector(DoubleRow vector){
        double mean;
        int i, len;

        mean=0;
        len=vector.getNonZeroNum();
        for(i=0;i<len;i++)
            mean+=vector.getNonZeroDoubleScore(i);
        return mean/len;
    }

    private double getSumScoreInVector(DoubleRow vector){
        double sum;
        int i, len;

        sum=0;
        len=vector.getNonZeroNum();
        for(i=0;i<len;i++)
            sum+=vector.getNonZeroDoubleScore(i);
        return sum;
    }

    private DoubleRow reweightVector(DoubleRow vector, double scale){
        int i,len;
        double max;

        len=vector.getNonZeroNum();
        max=getMaxScoreInVector(vector);
        for(i=0;i<len;i++)
            vector.setNonZeroDoubleScore(i,scale+vector.getNonZeroDoubleScore(i)*scale/max);
        return vector;
    }

    private DoubleRow normalizeVector(DoubleRow vector){
        double sum;

        sum=getSumScoreInVector(vector);
        return reweightVector(vector,1.0/sum);
    }

    private void strengthenCommonProperty(DoubleRow first, double thresholdFirst, DoubleRow second, double thresholdSecond, double multiplier){
        int i, j, len1, len2;
        int x, y;
        double a,b;

        i=0;
        j=0;
        len1=first.getNonZeroNum();
        len2=second.getNonZeroNum();
        while(i<len1 && j<len2){
            a=first.getNonZeroDoubleScore(i);
            if(a<thresholdFirst){
                i++;
                continue;
            }
            b=second.getNonZeroDoubleScore(j);
            if(b<thresholdSecond){
                j++;
                continue;
            }

            x=first.getNonZeroColumn(i);
            y=second.getNonZeroColumn(j);
            if(x==y){
                first.setNonZeroDoubleScore(i,a*multiplier);
                second.setNonZeroDoubleScore(j,b*multiplier);
                i++;
                j++;
            }
            else if(x<y){
                i++;
            }
            else{
                j++;
            }
        }
    }

    private DoubleRow addVector(DoubleRow first, DoubleRow second){
        ArrayList tokenList;
        Token curToken;
        int[] arrCol;
        double[] arrScore;
        int i, j, k, len1, len2;
        int x, y;

        i = 0;
        j = 0;
        len1 = first.getNonZeroNum();
        len2 = second.getNonZeroNum();
        tokenList=new ArrayList();
        while (i < len1 && j < len2) {
            x = first.getNonZeroColumn(i);
            y = second.getNonZeroColumn(j);
            if (x == y) {
                curToken=new Token(null);
                curToken.setIndex(x);
                curToken.setWeight(first.getNonZeroDoubleScore(i)+second.getNonZeroDoubleScore(j));
                i++;
                j++;
            }
            else if (x < y) {
                curToken=new Token(null);
                curToken.setIndex(x);
                curToken.setWeight(first.getNonZeroDoubleScore(i));
                i++;
            }
            else {
                curToken=new Token(null);
                curToken.setIndex(y);
                curToken.setWeight(second.getNonZeroDoubleScore(j));
                j++;
            }
            tokenList.add(curToken);
        }

        for(k=i;k<len1;k++){
            curToken=new Token(null);
            curToken.setIndex(first.getNonZeroColumn(k));
            curToken.setWeight(first.getNonZeroDoubleScore(k));
            k++;
        }
        for(k=j;k<len2;k++){
            curToken=new Token(null);
            curToken.setIndex(second.getNonZeroColumn(k));
            curToken.setWeight(second.getNonZeroDoubleScore(k));
            k++;
        }

        arrCol=new int[tokenList.size()];
        arrScore=new double[tokenList.size()];
        for(i=0;i<tokenList.size();i++){
            curToken=(Token)tokenList.get(i);
            arrCol[i]=curToken.getIndex();
            arrScore[i]=curToken.getWeight();
        }
        return new DoubleRow(-1,tokenList.size(),arrCol,arrScore);
    }
}

