package com.wcohen.secondstring.expt;

import com.wcohen.secondstring.tokens.Tokenizer;

/**
 * TokenBlocker for clustering.
 */

public class ClusterTokenBlocker extends TokenBlocker
{
	public ClusterTokenBlocker() {
		super();
		setClusterMode(true);
	}
	public ClusterTokenBlocker(Tokenizer tokenizer, double maxFraction) {
		super(tokenizer,maxFraction);
		setClusterMode(true);
	}
	public String toString() { return "[ClusterTokenBlocker]"; }
}
