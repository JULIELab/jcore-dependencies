package com.wcohen.secondstring;

import com.wcohen.secondstring.tokens.SimpleTokenizer;
import com.wcohen.secondstring.tokens.Token;
import com.wcohen.secondstring.tokens.Tokenizer;

import java.util.*;

/**
 * Distance metrics based on Jensen-Shannon distance of two smoothed
 * unigram language models.
 */

abstract public class JensenShannonDistance extends AbstractStringDistance
{
	private Tokenizer tokenizer;

	// maps tokens to document frequency
	private Map backgroundFrequency = new HashMap(); 
	// count number of tokens
	int totalTokens = 0;

	// to save space, allocate the small numbers only once in the backgroundFrequency map
	private static final Integer ONE = new Integer(1);
	private static final Integer TWO = new Integer(2);
	private static final Integer THREE = new Integer(3);

	public JensenShannonDistance(Tokenizer tokenizer) { this.tokenizer = tokenizer; }
	public JensenShannonDistance() { this(SimpleTokenizer.DEFAULT_TOKENIZER); }
	
	/** Accumulate statistics on how often each token occurs. */
	final public void accumulateStatistics(Iterator i) 
	{
		Set seenTokens = new HashSet();
		while (i.hasNext()) {
			StringWrapper s = (StringWrapper)i.next();
			Token[] toks = tokenizer.tokenize(s.unwrap());
			for (int j=0; j<toks.length; j++) {
				totalTokens++;
				// increment backgroundFrequency counts
				Integer freq = (Integer)backgroundFrequency.get(toks[j]);
				if (freq==null) backgroundFrequency.put(toks[j],ONE); 
				else if (freq==ONE) backgroundFrequency.put(toks[j],TWO);
				else if (freq==TWO) backgroundFrequency.put(toks[j],THREE);
				else backgroundFrequency.put(toks[j], new Integer(freq.intValue()+1));
			}
		}
	}

	/** Preprocess a string by finding tokens and giving them weights W
	 * such that W is the smoothed probability of the token appearing
	 * in the document.
	 */ 
	final public StringWrapper prepare(String s) {
		BagOfTokens bag = new BagOfTokens(s, tokenizer.tokenize(s));
		double totalWeight = bag.getTotalWeight();
		for (Iterator i=bag.tokenIterator(); i.hasNext(); ) {
			Token tok = (Token)i.next();
			double freq = bag.getWeight(tok);
			bag.setWeight( tok, smoothedProbability(tok, freq, totalWeight) );
		}
		return bag;
	}

	/** Smoothed probability of the token with frequency freq in a bag with the given totalWeight */
	abstract protected double smoothedProbability(Token tok, double freq, double totalWeight);

	/** Probability of token in the background language model */
	protected double backgroundProb(Token tok) 
	{
		Integer freqInteger = (Integer)backgroundFrequency.get(tok);
		double freq = freqInteger==null ? 0 : freqInteger.intValue();
		return freq/totalTokens;
	}
	
	/** Jensen-Shannon distance between distributions. */
	final public double score(StringWrapper s,StringWrapper t) 
	{
		BagOfTokens sBag = (BagOfTokens)s;
		BagOfTokens tBag = (BagOfTokens)t;
		double sum = 0;
		for (Iterator i = sBag.tokenIterator(); i.hasNext(); ) {
	    Token tok = (Token)i.next();
	    if (tBag.contains(tok)) {
				double ps = sBag.getWeight(tok);
				double pt = tBag.getWeight(tok);
				sum -= h(ps + pt) - h(ps) - h(pt);
			}
		}
		return 0.5*sum/Math.log(2);
	}
	private double h(double p) { return - p * Math.log(p); }

	final public String explainScore(StringWrapper s,StringWrapper t) 
	{
		StringBuffer buf = new StringBuffer();
		PrintfFormat fmt = new PrintfFormat("%.3f");
		BagOfTokens sBag = (BagOfTokens)s;
		BagOfTokens tBag = (BagOfTokens)t;
		buf.append("Common tokens: ");
		for (Iterator i = sBag.tokenIterator(); i.hasNext(); ) {
	    Token tok = (Token)i.next();
	    if (tBag.contains(tok)) {
				double ps = sBag.getWeight(tok);
				double pt = tBag.getWeight(tok);
				buf.append(" "+tok.getValue()+": ");
				buf.append(fmt.sprintf(ps));
				buf.append("*"); 
				buf.append(fmt.sprintf(pt));
				buf.append(":delta=");
				buf.append(fmt.sprintf((h(ps + pt) - h(ps) - h(pt))));
			}
		}
		buf.append("\nscore = "+score(s,t));
		return buf.toString(); 
	}

}
