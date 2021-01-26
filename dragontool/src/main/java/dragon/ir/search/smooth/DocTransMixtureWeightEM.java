package dragon.ir.search.smooth;

import dragon.ir.index.IRDoc;
import dragon.ir.index.IndexReader;
import dragon.ir.query.RelSimpleQuery;
import dragon.ir.query.SimpleTermPredicate;
import dragon.ir.search.FullRankSearcher;
import dragon.ir.search.Searcher;

/**
 * <p>Computes the Mixture Weights for Models using translations</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocTransMixtureWeightEM extends AbstractMixtureWeightEM{
    private RelSimpleQuery query;

    //for query first mode
    private QueryFirstTransSmoother querySmoother;
    private double queryWeight;

    //for document first mode;
    private DocFirstTransSmoother docSmoother;

    public DocTransMixtureWeightEM(IndexReader indexReader, int iterationNum, QueryFirstTransSmoother querySmoother) {
        super(indexReader,2, iterationNum,false);
        this.querySmoother =querySmoother;
    }

    public DocTransMixtureWeightEM(IndexReader indexReader, int iterationNum, DocFirstTransSmoother docSmoother) {
        super(indexReader,2, iterationNum,true);
        this.docSmoother =docSmoother;
    }

    protected void setInitialParameters(double[] arrCoefficient, IRDoc[] arrDoc){
        Searcher searcher;
        IRDoc curDoc;
        double sum;
        int docNum, i;

        arrCoefficient[0]=0.7;
        arrCoefficient[1]=0.3;

        searcher=new FullRankSearcher(indexReader,new OkapiSmoother(indexReader.getCollection()));
        docNum=searcher.search(query);

        sum=0;
        for (i = 0; i < docNum; i++) {
            arrDoc[i] = indexReader.getDoc(i);
            arrDoc[i].setWeight(1.0 / docNum);
            if (arrDoc[i].getTermCount() == 0)
                arrDoc[i].setTermCount(1);
            if (arrDoc[i].getRelationCount() == 0)
                arrDoc[i].setRelationCount(1);
        }

        for(i=0;i<docNum;i++){
            curDoc=searcher.getIRDoc(i);
            arrDoc[curDoc.getIndex()].setWeight(curDoc.getWeight());
            sum+=curDoc.getWeight();
        }
        for(i=0;i<arrDoc.length;i++) arrDoc[i].setWeight(arrDoc[i].getWeight()/sum);
    }

    protected void setDoc(IRDoc curDoc){
        docSmoother.setDoc(curDoc);
    }

    protected void setQueryTerm(SimpleTermPredicate curQueryTerm){
        queryWeight=curQueryTerm.getWeight();
        querySmoother.setQueryTerm(curQueryTerm);
    }

    protected void init(RelSimpleQuery query){
        this.query =query;
    }

    protected void getComponentValue(IRDoc curDoc, int freq, double[] arrComp){
        querySmoother.setDoc(curDoc);
        arrComp[0]=querySmoother.getBasicSmoother().getSmoothedProb(freq)/queryWeight;
        arrComp[1]=querySmoother.getTranslationProb(curDoc.getIndex());
    }

    protected void getComponentValue(SimpleTermPredicate curQueryTerm, int freq, double[] arrComp){
        docSmoother.setQueryTerm(curQueryTerm);
        arrComp[0]=docSmoother.getBasicSmoother().getSmoothedProb(freq)/curQueryTerm.getWeight();
        arrComp[1]=docSmoother.getTranslationProb(curQueryTerm.getIndex());
    }
}