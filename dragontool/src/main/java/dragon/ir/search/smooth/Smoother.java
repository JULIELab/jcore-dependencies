package dragon.ir.search.smooth;

import dragon.ir.index.IRDoc;
import dragon.ir.query.SimpleTermPredicate;
/**
 * <p>Interface of smoother which returns a score of a searching term in the given document to the searcher</p>
 * <p>The toolkit has implemented various language model smoothing methods as well as traditional probabilistic and vector space models.
 * For language models, the score means the probability of the doucment generating the term.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface Smoother {
    /**
     * @param doc the document
     * @param queryTerm the query term
     * @param termFreq the frequency of the query term in the given document
     * @return a score
     */
    public double getSmoothedProb(IRDoc doc, SimpleTermPredicate queryTerm, int termFreq);

    /**
     * This method is equal to call getSmoothedProb(doc, queryTerm, 0).
     * @param doc the document
     * @param queryTerm the query term
     * @return a score
     */
    public double getSmoothedProb(IRDoc doc, SimpleTermPredicate queryTerm);

    /**
     * Before calling this method, one should call the setQueryTerm method.
     * @param doc the document
     * @param termFreq the frequency of the current query term in the given document
     * @return a score
     */
    public double getSmoothedProb(IRDoc doc, int termFreq);

    /**
     * Before calling this method, one should call the setDoc method.
     * @param queryTerm the query term
     * @param termFreq the frequency of the given query term in the current document
     * @return a score
     */
    public double getSmoothedProb(SimpleTermPredicate queryTerm, int termFreq);

    /**
     *Before calling this method, one should call the setQueryTerm method and the setDoc method.
     * @param termFreq the frequency of the current term in the current document
     * @return a score
     */
    public double getSmoothedProb(int termFreq);

    /**
     * It is equal to calling getSmoothedProb(queryTerm, 0);
     * @param queryTerm the query term
     * @return a score
     */
    public double getSmoothedProb(SimpleTermPredicate queryTerm);

    /**
     * It is equal to calling getSmoothedProb(doc, 0);
     * @param doc the document
     * @return a score
     */
    public double getSmoothedProb(IRDoc doc);

    /**
     * @param params paramteres for the current smoother
     * @return true if successful
     */
    public boolean setParameters(double[] params);

    /**
     * Set the current query term for processing
     * @param queryTerm the query term
     */
    public void setQueryTerm(SimpleTermPredicate queryTerm);

    /**
     * Set the current document for processing
     * @param doc the document
     */
    public void setDoc(IRDoc doc);

    /**
     * If this method returns true, the fullrank searcher will do breadth-frist search, i.e. processing document one by one.
     * @return true or false
     */
    public boolean isDocFirstOptimal();

    /**
     * If this method returns true, the fullrank searcher will do depth-frist search, i.e. processing query terms one by one and then merging
     * the document lists resulted from different query terms.
     * @return true or false
     */
    public boolean isQueryTermFirstOptimal();

    /**
     * If this option is true, smoomther will return a log score
     * @param option true or false
     */
    public void setLogLikelihoodOption(boolean option);
    public boolean getLogLikelihoodOption();
}