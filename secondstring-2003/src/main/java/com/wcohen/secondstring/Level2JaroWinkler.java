package com.wcohen.secondstring;

import com.wcohen.secondstring.tokens.SimpleTokenizer;

/**
 * "Level 2" recursive field matching algorithm, based on Jaro
 * distance.
 */

public class Level2JaroWinkler extends Level2
{
	private static final StringDistance MY_JARO_WINKLER = new JaroWinkler();

	public Level2JaroWinkler() { super( SimpleTokenizer.DEFAULT_TOKENIZER, MY_JARO_WINKLER) ; }
	public String toString() { return "[Level2JaroWinkler]"; }
	
	static public void main(String[] argv) {
		doMain(new Level2JaroWinkler(), argv);
	}
}
