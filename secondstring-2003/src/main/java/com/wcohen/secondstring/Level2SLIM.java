package com.wcohen.secondstring;

import java.util.*;
import com.wcohen.secondstring.tokens.*;

/**
 * "Level 2" recursive field matching algorithm, based on SLIM
 * distance.
 */

public class Level2SLIM extends Level2
{
	private static final StringDistance MY_JARO = new SLIM();

	public Level2SLIM() { super( SimpleTokenizer.DEFAULT_TOKENIZER, MY_JARO) ; }
	public String toString() { return "[Level2SLIM]"; }
	
	static public void main(String[] argv) {
		doMain(new Level2SLIM(), argv);
	}
}
