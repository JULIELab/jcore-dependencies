package com.wcohen.secondstring;

import com.wcohen.secondstring.tokens.SimpleTokenizer;

/**
 * "Level 2" recursive field matching algorithm, based on Jaro
 * distance.
 */

public class Level2Jaro extends Level2
{
	private static final StringDistance MY_JARO = new Jaro();

	public Level2Jaro() { super( SimpleTokenizer.DEFAULT_TOKENIZER, MY_JARO) ; }
	public String toString() { return "[Level2Jaro]"; }
	
	static public void main(String[] argv) {
		doMain(new Level2Jaro(), argv);
	}
}
