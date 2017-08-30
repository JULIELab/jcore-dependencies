package dragon.ir.kngbase;

import dragon.ir.index.*;
import dragon.matrix.*;
import dragon.nlp.*;
import dragon.nlp.extract.*;
import dragon.onlinedb.*;
import dragon.util.*;
import java.io.File;
import java.util.*;

/**
 * <p>Hyper Analogue to Language Space</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class HALSpace implements KnowledgeBase{
    private SimpleElementList termList;
    private TokenExtractor te;
    private int windowSize;
    private IntSparseMatrix cooccurMatrix;
    private DoubleSparseMatrix halMatrix;
    private boolean fileBasedMatrix;
    private SortedArray relationCache;
    private boolean showProgress, useExternalTokenIndex;

    public HALSpace(TokenExtractor te, int windowSize){
         this(new SimpleElementList(),te,windowSize);
    }

    public HALSpace(SimpleElementList termList, TokenExtractor te, int windowSize) {
        this.termList=termList;
        this.useExternalTokenIndex =(termList.size()>0);
        this.te=te;
        te.setFilteringOption(false);
        this.windowSize =windowSize;
        this.halMatrix=new DoubleFlatSparseMatrix();
        this.fileBasedMatrix=false;
        this.cooccurMatrix=new IntFlatSparseMatrix(true,true);
        relationCache=new SortedArray();
        showProgress=false;
    }

    public HALSpace(SimpleElementList termList, TokenExtractor te, int windowSize, String indexFilename, String matrixFilename) {
        this.termList=termList;
        this.useExternalTokenIndex =(termList.size()>0);
        this.te=te;
        te.setFilteringOption(false);
        this.windowSize =windowSize;
        halMatrix=new DoubleGiantSparseMatrix(indexFilename,matrixFilename, false, false);
        ((DoubleGiantSparseMatrix)halMatrix).setFlushInterval(Integer.MAX_VALUE);
        this.fileBasedMatrix=true;
        this.cooccurMatrix=new IntGiantSparseMatrix(indexFilename+".tmp",matrixFilename+".tmp",true,true);
        relationCache=new SortedArray();
        showProgress=false;
    }

    public DoubleSparseMatrix getKnowledgeMatrix(){
        return halMatrix;
    }

    public SimpleElementList getRowKeyList(){
        return termList;
    }

    public SimpleElementList getColumnKeyList(){
        return termList;
    }

    public void setShowProgress(boolean option){
        this.showProgress =option;
    }

    public void add(ArrayList articleList){
        int i;

        for(i=0;i<articleList.size();i++)
            addArticle((Article)articleList.get(i));
    }

    public void add(CollectionReader collectionReader){
        Article article;
        int count;

        count=0;
        article=collectionReader.getNextArticle();
        while(article!=null)
        {
            addArticle(article);
            count++;
            if(showProgress && count%10==0){
                System.out.println((new java.util.Date()).toString()+" Processed Articles: "+count);
            }
            article=collectionReader.getNextArticle();
        }
    }

    public void finalizeData(){
        int i, j, row,len;
        int[] arrCol, arrFreq;
        double mean, sum;

        cooccurMatrix.finalizeData();
        row=cooccurMatrix.rows();
        for(i=0;i<row;i++){
            arrCol=cooccurMatrix.getNonZeroColumnsInRow(i);
            arrFreq=cooccurMatrix.getNonZeroIntScoresInRow(i);
            len=arrFreq.length;
            sum=cooccurMatrix.getRowSum(i);
            mean=sum/len;
            sum=0; //for renormalization purpose
            for(j=0;j<len;j++){
                if(arrFreq[j]>=mean)
                    sum+=arrFreq[j];
            }

            for(j=0;j<len;j++){
                if(arrFreq[j]>=mean)
                    halMatrix.add(i,arrCol[j],arrFreq[j]/sum);
            }
            if(showProgress && i%1000==0) System.out.println("Processed Rows: "+i);
            if(fileBasedMatrix && i%5000==0) ((DoubleGiantSparseMatrix)halMatrix).flush();
        }
        halMatrix.finalizeData();
    }

    public DoubleSparseMatrix getHALMatrix(){
        return halMatrix;
    }

    public void close(){

        halMatrix.close();
        cooccurMatrix.close();
        if(fileBasedMatrix){
            (new File(((IntGiantSparseMatrix)cooccurMatrix).getIndexFilename())).delete();
            (new File(((IntGiantSparseMatrix)cooccurMatrix).getMatrixFilename())).delete();
        }
    }

    private void addArticle(Article article){
        SortedArray cache;
        ArrayList tokenList;
        Token[] arrToken;
        IRRelation relation;
        StringBuffer sb;
        int i, j, first, second, pos;

        sb=new StringBuffer();
        if(article.getTitle()!=null){
            sb.append(article.getTitle());
            sb.append(' ');
        }
        if(article.getAbstract()!=null){
            sb.append(article.getAbstract());
            sb.append(' ');
        }
        if(article.getBody()!=null){
            sb.append(article.getBody());
            sb.append(' ');
        }
       if(sb.length()<=20)
           return;

        tokenList=te.extractFromDoc(sb.toString().trim());
        if(tokenList==null || tokenList.size()<windowSize)
            return;

        arrToken=new Token[tokenList.size()];
        cache=new SortedArray();
        for(i=0;i<tokenList.size();i++){
            arrToken[i]=(Token)tokenList.get(i);
            pos=cache.binarySearch(arrToken[i]);
            if(pos>=0)
                arrToken[i].setIndex(((Token)cache.get(pos)).getIndex());
            else{
                arrToken[i].setIndex(tokenSearch(arrToken[i].getValue()));
                if(arrToken[i].getIndex()>=0)
                    cache.add(pos*-1-1,arrToken[i]);
            }
        }
        cache.clear();
        tokenList.clear();

        for(i=0;i<=arrToken.length-windowSize;i++){
            first=arrToken[i].getIndex();
            if(first==-1) continue;

            for(j=1;j<windowSize;j++){
                second=arrToken[i+j].getIndex();
                if(second!=-1){
                    addRelation(first, second, windowSize - j);
                    addRelation(second,first,windowSize-j);
                }
            }
        }

        for(i=0;i<relationCache.size();i++){
            relation=(IRRelation)relationCache.get(i);
            cooccurMatrix.add(relation.getFirstTerm(),relation.getSecondTerm(),relation.getFrequency());
            cooccurMatrix.add(relation.getSecondTerm(),relation.getFirstTerm(),relation.getFrequency());
        }
        relationCache.clear();
    }

    private int tokenSearch(String token){
        if(useExternalTokenIndex)
            return termList.search(token);
        else
            return termList.add(token);
    }

    private boolean addRelation(int first,int second, int score){
        IRRelation cur;

        cur=new IRRelation(first,second,score);
        if(!relationCache.add(cur))
        {
            cur=(IRRelation)relationCache.get(relationCache.insertedPos());
            cur.addFrequency(score);
        }
        return true;
    }
}