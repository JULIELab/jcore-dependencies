package com.wcohen.secondstring;

import java.util.*;
import com.wcohen.secondstring.tokens.*;

/**
 * Jensen-Shannon distance of two unsmoothed unigram language models.
 */

public class UnsmoothedJS extends JensenShannonDistance
{
	public String toString() { return "[UnsmoothedJS]"; }

	/** Unsmoothed probability of the token */
	protected double smoothedProbability(Token tok, double freq, double totalWeight) 
	{
		return freq/totalWeight;
	}

	static public void main(String[] argv) {
		doMain(new UnsmoothedJS(), argv);
	}
}
