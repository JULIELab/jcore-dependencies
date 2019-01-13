package com.wcohen.secondstring;

import java.util.*;

/**
 * Abstract StringDistance implementation, implementing a few useful defaults.
 *
 */

public abstract class AbstractStringDistance implements StringDistance
{
	/** This method needs to be implemented by subclasses. 
	 */
	abstract public double score(StringWrapper s,StringWrapper t);

	/** This method needs to be implemented by subclasses. 
	 */
	abstract public String explainScore(StringWrapper s, StringWrapper t);

/** Strings are scored by converting them to StringWrappers with the
	 * prepare function. 
	 */
	final public double score(String s, String t) {
		return score(prepare(s), prepare(t));
	}
	
	/** Scores are explained by converting Strings to StringWrappers
	 * with the prepare function.
	 */
	final public String explainScore(String s, String t) {
		return explainScore(prepare(s),prepare(t));
	}
	
	/** Default way to preprocess a string for distance computation.  If
	 * this is an expensive operations, then override this method to
	 * return a StringWrapper implementation that caches appropriate
	 * information about s.
	 */
	public StringWrapper prepare(String s) {
		return new StringWrapper(s);
	}
	
	/** Default way to accumulate statistics for a set of related
	 * strings.  This is for distance metrics like TFIDF that use
	 * statistics on unlabeled strings to adjust a distance metric.
	 * Override this method if it's necessary to accumulate statistics.
	 */
	public void accumulateStatistics(Iterator i) { 
		/* Default is to do nothing. */ ; 
	}

	/** Default main routine for testing */
	final protected static void doMain(StringDistance d,String[] argv) 
	{
		if (argv.length!=2) {
	    System.out.println("usage: string1 string2");
		} else {
	    System.out.println(d.explainScore(argv[0],argv[1]));
		}
	}
}
