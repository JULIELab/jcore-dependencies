package com.wcohen.secondstring;

import java.util.*;
import com.wcohen.secondstring.tokens.*;

/**
 * Abstract token distance metric that uses frequency statistics.
 */

abstract public class AbstractStatisticalTokenDistance extends AbstractStringDistance
{
	protected Tokenizer tokenizer;

	// maps tokens to document frequency
	protected Map documentFrequency = new HashMap(); 
	// to save space, allocate the small numbers only once in the documentFrequency map
	private static final Integer ONE = new Integer(1);
	private static final Integer TWO = new Integer(2);
	private static final Integer THREE = new Integer(3);
	// count number of documents
	protected int collectionSize = 0;
	// count number of tokens
	protected int totalTokenCount = 0;

	public AbstractStatisticalTokenDistance(Tokenizer tokenizer) { this.tokenizer = tokenizer; }
	public AbstractStatisticalTokenDistance() { this(SimpleTokenizer.DEFAULT_TOKENIZER); }
	
	/** Accumulate statistics on how often each token value occurs 
	 */
	public void accumulateStatistics(Iterator i) 
	{
		Set seenTokens = new HashSet();
		while (i.hasNext()) {
			StringWrapper s = (StringWrapper)i.next();
			Token[] toks = tokenizer.tokenize(s.unwrap());
			seenTokens.clear();
			for (int j=0; j<toks.length; j++) {
				totalTokenCount++;
				if (!seenTokens.contains(toks[j])) {
					seenTokens.add(toks[j]);
					// increment documentFrequency counts
					Integer df = (Integer)documentFrequency.get(toks[j]);
					if (df==null) documentFrequency.put(toks[j],ONE); 
					else if (df==ONE) documentFrequency.put(toks[j],TWO);
					else if (df==TWO) documentFrequency.put(toks[j],THREE);
					else documentFrequency.put(toks[j], new Integer(df.intValue()+1));
				}
			}
			collectionSize++;
		}
	}
	public int getDocumentFrequency(Token tok) {
		Integer freqInteger = (Integer)documentFrequency.get(tok);
		if (freqInteger==null) return 0;
		else return freqInteger.intValue();
	}
}
