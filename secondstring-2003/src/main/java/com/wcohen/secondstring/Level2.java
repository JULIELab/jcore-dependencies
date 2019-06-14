package com.wcohen.secondstring;

import com.wcohen.secondstring.tokens.SimpleTokenizer;
import com.wcohen.secondstring.tokens.Token;
import com.wcohen.secondstring.tokens.Tokenizer;

import java.util.Iterator;

/**
 * Generic version of Monge & Elkan's "level 2" recursive field
 * matching.  Given strings A, B that are broken into substrings A =
 * A1...Ak and B=B1...Bm, the recursive string matching algorithm
 * scores
 * <p><code>
 * score(A,B) = 1/k [ sum_i max_j score(Ai,Bj) ]
 * </code>
 *
 * For level 1, substrings Ai, Bj are delimited by commas; for level
 * 2, they are tokens.
 */

public class Level2 extends AbstractStringDistance
{
	private Tokenizer tokenizer;
	private StringDistance tokenDistance;
	
	public Level2(Tokenizer tokenizer,StringDistance tokenDistance) {
		this.tokenizer = tokenizer;
		this.tokenDistance = tokenDistance;
	}
	
	public double score(StringWrapper s,StringWrapper t) {
		BagOfTokens sBag = (BagOfTokens)s;
		BagOfTokens tBag = (BagOfTokens)t;
		double sumOverI = 0;
		for (Iterator i = sBag.tokenIterator(); i.hasNext(); ) {
	    Token tokenI = (Token)i.next();
			double maxOverJ = -Double.MAX_VALUE;
			for (Iterator j = tBag.tokenIterator(); j.hasNext(); ) {
				Token tokenJ = (Token)j.next();
				double scoreItoJ = tokenDistance.score( tokenI.getValue(), tokenJ.getValue() );
				maxOverJ = Math.max( maxOverJ, scoreItoJ);
			}
			sumOverI += maxOverJ;
		}
		//System.out.println("sumOverI="+sumOverI+" size="+sBag.size());
		return  sumOverI / sBag.size();
	}
	
	/** Preprocess a string by finding tokens. */ 
	public StringWrapper prepare(String s) {
		return new BagOfTokens(s, tokenizer.tokenize(s));
	}
	
	/** Explain how the distance was computed.
	 */
	public String explainScore(StringWrapper s, StringWrapper t) 
	{
		StringBuffer buf = new StringBuffer();
		BagOfTokens sBag = (BagOfTokens)s;
		BagOfTokens tBag = (BagOfTokens)t;
		double sumOverI = 0;
		for (Iterator i = sBag.tokenIterator(); i.hasNext(); ) {
	    Token tokenI = (Token)i.next();
			buf.append("token="+tokenI);
			double maxOverJ = -Double.MAX_VALUE;
			Token closestToI = null;
			for (Iterator j = tBag.tokenIterator(); j.hasNext(); ) {
				Token tokenJ = (Token)j.next();
				double scoreItoJ = tokenDistance.score( tokenI.getValue(), tokenJ.getValue() );
				buf.append(" dist("+tokenJ.getValue()+")="+scoreItoJ);
				if (scoreItoJ >= maxOverJ) {
					maxOverJ = scoreItoJ;
					closestToI = tokenJ;
				}
			}
			sumOverI += maxOverJ;
			buf.append(" match="+closestToI+" score="+maxOverJ+"\n");
		}
		//System.out.println("common="+numCommon+" |s| = "+sBag.size()+" |t| = "+tBag.size());
		buf.append("total: "+sumOverI+"/"+sBag.size()+" = "+score(s,t)+"\n");
		return buf.toString();
	}

	public String toString() { return "[Level2:tokenizer="+tokenizer+";tokenDist="+tokenDistance+"]"; }
	
	static public void main(String[] argv) {
		doMain(new Level2(SimpleTokenizer.DEFAULT_TOKENIZER, new Levenstein()), argv);
	}
}
