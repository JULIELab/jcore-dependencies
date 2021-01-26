package com.wcohen.secondstring;

import com.wcohen.secondstring.tokens.Token;
import com.wcohen.secondstring.tokens.Tokenizer;

import java.util.Iterator;

/**
 * TFIDF-based distance metric.
 */

public class TFIDF extends AbstractStatisticalTokenDistance
{
	public TFIDF(Tokenizer tokenizer) { super(tokenizer);	}
	public TFIDF() { super(); }

	public double score(StringWrapper s,StringWrapper t) {
		BagOfTokens sBag = (BagOfTokens)s;
		BagOfTokens tBag = (BagOfTokens)t;
		double sim = 0.0;
		for (Iterator i = sBag.tokenIterator(); i.hasNext(); ) {
	    Token tok = (Token)i.next();
	    if (tBag.contains(tok)) {
				sim += sBag.getWeight(tok) * tBag.getWeight(tok);
			}
		}
		//System.out.println("common="+numCommon+" |s| = "+sBag.size()+" |t| = "+tBag.size());
		return sim;
	}
	
	/** Preprocess a string by finding tokens and giving them TFIDF weights */ 
	public StringWrapper prepare(String s) {
		BagOfTokens bag = new BagOfTokens(s, tokenizer.tokenize(s));
		// reweight by tdfidf
		double normalizer = 0.0;
		for (Iterator i=bag.tokenIterator(); i.hasNext(); ) {
			Token tok = (Token)i.next();
			if (collectionSize>0) {
				Integer dfInteger = (Integer)documentFrequency.get(tok);
				// set previously unknown words to df==1, which gives them a high value
				double df = dfInteger==null ? 1.0 : dfInteger.intValue();
				double w = Math.log( bag.getWeight(tok) + 1) * Math.log( collectionSize/df );
				bag.setWeight( tok, w );
				normalizer += w*w;
			} else {
				bag.setWeight( tok, 1.0 );
				normalizer += 1.0;
			}
		}
		normalizer = Math.sqrt(normalizer);
		for (Iterator i=bag.tokenIterator(); i.hasNext(); ) {
			Token tok = (Token)i.next();
			bag.setWeight( tok, bag.getWeight(tok)/normalizer );
		}
		return bag;
	}
	
	/** Explain how the distance was computed. 
	 * In the output, the tokens in S and T are listed, and the
	 * common tokens are marked with an asterisk.
	 */
	public String explainScore(StringWrapper s, StringWrapper t) 
	{
		BagOfTokens sBag = (BagOfTokens)s;
		BagOfTokens tBag = (BagOfTokens)t;
		StringBuffer buf = new StringBuffer("");
		PrintfFormat fmt = new PrintfFormat("%.3f");
		buf.append("Common tokens: ");
		for (Iterator i = sBag.tokenIterator(); i.hasNext(); ) {
	    Token tok = (Token)i.next();
			if (tBag.contains(tok)) {
				buf.append(" "+tok.getValue()+": ");
				buf.append(fmt.sprintf(sBag.getWeight(tok)));
				buf.append("*"); 
				buf.append(fmt.sprintf(tBag.getWeight(tok)));
			}
		}
		buf.append("\nscore = "+score(s,t));
		return buf.toString(); 
	}
	public String toString() { return "[TFIDF]"; }
	
	static public void main(String[] argv) {
		doMain(new TFIDF(), argv);
	}
}
