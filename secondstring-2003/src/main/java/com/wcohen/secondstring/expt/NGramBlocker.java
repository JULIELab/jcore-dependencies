package com.wcohen.secondstring.expt;

import com.wcohen.secondstring.tokens.NGramTokenizer;
import com.wcohen.secondstring.tokens.SimpleTokenizer;
import com.wcohen.secondstring.tokens.Tokenizer;

/**
 * Finds all pairs that share a not-too-common character n-gram.
 */

public class NGramBlocker extends TokenBlocker 
{
	static private Tokenizer tokenizer = 
	  new NGramTokenizer(4,4,false,SimpleTokenizer.DEFAULT_TOKENIZER);

	public NGramBlocker() { super( tokenizer, 1.0 ); }
	public String toString() { return "[NGramBlocker]"; }
}
