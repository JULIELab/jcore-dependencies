package com.wcohen.secondstring;

import com.wcohen.secondstring.tokens.SimpleTokenizer;

/**
 * "Level 2" recursive field matching algorithm, based on SLIM
 * distance.
 */

public class Level2SLIMWinkler extends Level2
{
	private static final StringDistance MY_SLIM_WINKLER = new SLIMWinkler();

	public Level2SLIMWinkler() { super( SimpleTokenizer.DEFAULT_TOKENIZER, MY_SLIM_WINKLER) ; }
	public String toString() { return "[Level2SLIMWinkler]"; }
	
	static public void main(String[] argv) {
		doMain(new Level2SLIMWinkler(), argv);
	}
}
