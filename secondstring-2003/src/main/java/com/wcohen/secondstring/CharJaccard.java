package com.wcohen.secondstring;

import com.wcohen.secondstring.tokens.NGramTokenizer;
import com.wcohen.secondstring.tokens.SimpleTokenizer;

/**
 * Character-based Jaccard distance: the distance between two strings
 * is the Jaccard distance of the letters in them.  Plausibly useful for
 * short strings.
 */

public class CharJaccard extends Jaccard
{
	public CharJaccard() { super(new NGramTokenizer(1,1,false,SimpleTokenizer.DEFAULT_TOKENIZER)); }
	public String toString() { return "[CharJaccard]"; }
	
	static public void main(String[] argv) {
		doMain(new CharJaccard(), argv);
	}
}
