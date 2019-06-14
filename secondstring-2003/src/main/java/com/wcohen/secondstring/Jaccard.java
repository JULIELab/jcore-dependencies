package com.wcohen.secondstring;

import com.wcohen.secondstring.tokens.SimpleTokenizer;
import com.wcohen.secondstring.tokens.Token;
import com.wcohen.secondstring.tokens.Tokenizer;

import java.util.Iterator;

/**
 * Jaccard distance implementation.  The Jaccard distance between two
 * sets is the ratio of the size of their intersection to the size of
 * their union.
 */

public class Jaccard extends AbstractStringDistance
{
	private Tokenizer tokenizer;
	
	public Jaccard(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
	public Jaccard() {
		this(SimpleTokenizer.DEFAULT_TOKENIZER);
	}
	
	public double score(StringWrapper s,StringWrapper t) {
		BagOfTokens sBag = (BagOfTokens)s;
		BagOfTokens tBag = (BagOfTokens)t;
		double numCommon = 0.0; 
		for (Iterator i = sBag.tokenIterator(); i.hasNext(); ) {
	    Token tok = (Token)i.next();
	    if (tBag.contains(tok)) numCommon++;
		}
		//System.out.println("common="+numCommon+" |s| = "+sBag.size()+" |t| = "+tBag.size());
		return  numCommon / (sBag.size() + tBag.size() - numCommon);
	}
	
	/** Preprocess a string by finding tokens. */ 
	public StringWrapper prepare(String s) {
		return new BagOfTokens(s, tokenizer.tokenize(s));
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
		buf.append("S: ");
		for (Iterator i = sBag.tokenIterator(); i.hasNext(); ) {
	    Token tok = (Token)i.next();
	    buf.append(" "+tok.getValue());
	    if (tBag.contains(tok)) buf.append("*");
		}
		buf.append("\nT: ");
		for (Iterator i = tBag.tokenIterator(); i.hasNext(); ) {
	    Token tok = (Token)i.next();
	    buf.append(" "+tok.getValue());
	    if (sBag.contains(tok)) buf.append("*");
		}
		buf.append("\nscore = "+score(s,t));
		return buf.toString(); 
	}

	public String toString() { return "[Jaccard]"; }
	
	static public void main(String[] argv) {
		doMain(new Jaccard(), argv);
	}
}
