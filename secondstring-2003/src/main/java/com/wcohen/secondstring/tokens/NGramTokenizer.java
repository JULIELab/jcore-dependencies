package com.wcohen.secondstring.tokens;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps another tokenizer, and adds all computes all ngrams of
 * characters from a single token produced by the inner tokenizer.
 */

public class NGramTokenizer implements Tokenizer
{
	private int minNGramSize;
	private int maxNGramSize;
	private boolean keepOldTokens;
	private Tokenizer innerTokenizer;
	
	public static NGramTokenizer DEFAULT_TOKENIZER = new NGramTokenizer(3,5,true,SimpleTokenizer.DEFAULT_TOKENIZER);

	public NGramTokenizer(int minNGramSize,int maxNGramSize,boolean keepOldTokens,Tokenizer innerTokenizer) {
		this.minNGramSize = minNGramSize;
		this.maxNGramSize = maxNGramSize;
		this.keepOldTokens = keepOldTokens;
		this.innerTokenizer = innerTokenizer;
	}

	/**  Return tokenized version of a string.  Tokens are sequences
	 * of alphanumerics, or any single punctuation character. */
	public Token[] tokenize(String input) 
	{
		Token[] initialTokens = innerTokenizer.tokenize(input);
		List tokens = new ArrayList();
		for (int i=0; i<initialTokens.length; i++) {
			Token tok = initialTokens[i];
			String str = "^"+tok.getValue()+"$";
			if (keepOldTokens) tokens.add( intern(str) );
			for (int lo=0; lo<str.length(); lo++) {
				for (int len=minNGramSize; len<=maxNGramSize; len++) {
					if (lo+len<str.length()) {
						tokens.add( innerTokenizer.intern( str.substring(lo,lo+len) )); 
					}
				}
			}
		}
		return (Token[]) tokens.toArray(new Token[tokens.size()]);
	}
	
	public Token intern(String s) { return innerTokenizer.intern(s); }

	/** Test routine */
	public static void main(String[] argv) 
	{
		NGramTokenizer tokenizer = NGramTokenizer.DEFAULT_TOKENIZER;
		//NGramTokenizer tokenizer = new NGramTokenizer(1,1,false,SimpleTokenizer.DEFAULT_TOKENIZER);
		int n = 0;
		for (int i=0; i<argv.length; i++) {
	    System.out.println("argument "+i+": '"+argv[i]+"'");
	    Token[] tokens = tokenizer.tokenize(argv[i]);
	    for (int j=0; j<tokens.length; j++) {
				System.out.println("token "+(++n)+":"
													 +" id="+tokens[j].getIndex()
													 +" value: '"+tokens[j].getValue()+"'");
	    }
		}
	}
}
