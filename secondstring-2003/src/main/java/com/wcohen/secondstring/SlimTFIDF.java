package com.wcohen.secondstring;

/**
 * Soft TFIDF-based distance metric, extended to use "soft" token-matching
 * with the SLIM distance metric.
 */

public class SlimTFIDF extends SoftTFIDF
{
	public SlimTFIDF() { super(new SLIMWinkler(), 0.0); }
	public String toString() { return "[SlimTFIDF:threshold="+getTokenMatchThreshold()+"]"; }
	
	static public void main(String[] argv) {
		doMain(new SlimTFIDF(), argv);
	}
}
