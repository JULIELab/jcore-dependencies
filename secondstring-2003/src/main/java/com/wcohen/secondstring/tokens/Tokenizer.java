package com.wcohen.secondstring.tokens;

/**
 * Split a string into tokens.
 */

public interface Tokenizer 
{
	/**  Return tokenized version of a string */
	public Token[] tokenize(String input);

	/** Convert a given string into a token */
	public Token intern(String s);
}

