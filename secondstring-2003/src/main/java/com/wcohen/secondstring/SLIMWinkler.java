package com.wcohen.secondstring;

/**
 * SLIM distance metric, with extensions proposed by Winkler for the
 * Jaro metric.
 */
public class SLIMWinkler extends WinklerRescorer
{
	public SLIMWinkler() { super(new SLIM()); }

	static public void main(String[] argv) {
		doMain(new SLIMWinkler(), argv);
	}
}
