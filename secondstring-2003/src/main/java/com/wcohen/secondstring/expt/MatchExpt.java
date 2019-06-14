package com.wcohen.secondstring.expt;

import com.wcohen.secondstring.DistanceFactory;
import com.wcohen.secondstring.PrintfFormat;
import com.wcohen.secondstring.StringDistance;

import java.io.*;
import java.util.Arrays;

/**
 * Perform a matching experiment using a data file, distance function
 * and blocker.
 */

public class MatchExpt implements Serializable
{
	public static final String BLOCKER_PACKAGE = "com.wcohen.secondstring.expt.";
	public static final String DISTANCE_PACKAGE = "com.wcohen.secondstring.";

	// for serialization control
	private static final long serialVersionUID = 1;
	private static int CURRENT_SERIALIZED_VERSION_NUMBER = 1;

	transient private Blocker.Pair[] pairs; 	// transient, because this is serialized specially
	private int numCorrectPairs;
	private double preparationTime;
	private double blockingTime;
	private double matchingTime;
	private double sortingTime;

	public MatchExpt(MatchData data,StringDistance dist,Blocker blocker) { setUpExperiment(data,dist,blocker); }
	public MatchExpt(MatchData data,StringDistance dist) { this(data,dist,new NullBlocker()); }
	
	/** Serialization
	 */
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		// only write correct pairs
		out.writeInt(pairs.length);
		for (int i=0; i<pairs.length; i++) {
			if (correctPair(i)) {
				out.writeInt(i);
				out.writeObject( pairs[i] );
			}
		}
		// sentinal, marks end of correct pairs
		out.writeInt(-1);
	}
	private void readObject(ObjectInputStream out) throws IOException,ClassNotFoundException
	{
		out.defaultReadObject();
		int n = out.readInt();		
		pairs = new Blocker.Pair[n];
		int nextIndex = out.readInt();
		while (nextIndex>=0) {
			pairs[nextIndex] = (Blocker.Pair)out.readObject();
			nextIndex = out.readInt();
		}
	}

	/** Initialize for later analysis.
	 */
	private void setUpExperiment(MatchData data,StringDistance dist,Blocker blocker) 
	{
		System.out.println("setting up expt: "+dist+" "+blocker+" file: "+data.getFilename());
		long startTime = System.currentTimeMillis();
		data.prepare(dist);
		preparationTime = (System.currentTimeMillis()-startTime)/1000.0;

		startTime = System.currentTimeMillis();
		blocker.block(data);
		blockingTime = (System.currentTimeMillis()-startTime)/1000.0;

		numCorrectPairs = blocker.numCorrectPairs();
		pairs = new Blocker.Pair[blocker.size()];
		startTime = System.currentTimeMillis();
		System.out.println("Pairs: "+pairs.length+" Correct: "+blocker.numCorrectPairs());
		for (int i=0; i<blocker.size(); i++) {
	    pairs[i] = blocker.getPair(i);
	    pairs[i].setDistance( dist.score( pairs[i].getA().getText(), pairs[i].getB().getText()) ); 
			if (i>0 && i%100000==0) {
				System.out.println("First "+i+" distances in "+
													 ((System.currentTimeMillis()-startTime)/1000.0)+" sec");
			}
		}
		matchingTime = (System.currentTimeMillis()-startTime)/1000.0;

		startTime = System.currentTimeMillis();
		Arrays.sort( pairs );
		sortingTime = (System.currentTimeMillis()-startTime)/1000.0;
		System.out.println("Matching time: "+matchingTime);
	}
	
	/** Return total time to process data. */
	public Double time() { 
		return new Double(preparationTime+blockingTime+matchingTime+sortingTime); 
	}

	/** Return total time to process data, divided by the number of pairs */
	public Double pairsPerSecond() {
		return new Double( pairs.length / (preparationTime+blockingTime+matchingTime+sortingTime) );
	}

	/** non-interpolated average precision */
	public Double averagePrecision() 
	{
		double n = 0;
		double sumPrecision = 0;
		for (int i=0; i<pairs.length; i++) {
			if (correctPair(i)) {
				n++;
				double precisionAtRankI = n/(i+1.0);
				sumPrecision += precisionAtRankI;
			}
		}
		return new Double(sumPrecision / numCorrectPairs);
	}
	
	/** max F1 for any threshold */
	public Double maxF1() 
	{
		double maxF1 = -Double.MAX_VALUE;
		double n = 0;
		for (int i=0; i<pairs.length; i++) {
			if (correctPair(i)) {
				n++;
				double precisionAtRankI = n/(i+1.0);
				double recallAtRankI = n/numCorrectPairs;
				if (precisionAtRankI>0 && recallAtRankI>0) {
					double f1 = 2*(precisionAtRankI*recallAtRankI) / (precisionAtRankI + recallAtRankI);
					maxF1 = Math.max( f1, maxF1 );
				}
			}
		}
		return new Double(maxF1);
	}
	
	/** performance of the blocker */
	public Double blockerRecall()
	{
		double n = 0;
		for (int i=0; i<pairs.length; i++) {
			if (correctPair(i)) {
				n++;
			}
		}
		return new Double(n/numCorrectPairs);
	}

	//
	// compute 11-pt interpolated precision/recall 
	//

	private static double[] elevenPoints = new double[] { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 };

	/** Return recall levels associated with the precision levels returned by interpolated11PointPrecision. */
	static public double[] interpolated11PointRecallLevels() { return elevenPoints; }

	/** Return an array of interpolated precision at various different recall levels. */
	public double[] interpolated11PointPrecision()
	{
		double[] interpolatedPrecision = new double[11];
		int numCorrectAtRankI = 0;
		for (int i=0; i<pairs.length; i++) {
			if (correctPair(i)) ++numCorrectAtRankI;
			double recall = numCorrectAtRankI/((double)numCorrectPairs);
			double precision = numCorrectAtRankI/(i+1.0);
			for (int j=0; j<elevenPoints.length; j++) {
				if (recall>=elevenPoints[j]) {
					interpolatedPrecision[j] = Math.max(interpolatedPrecision[j], precision);
				}
			}
		}
		return interpolatedPrecision;
	}

	/** Graph interpolated precision vs recall */
	public void graphPrecisionRecall(PrintStream out) throws IOException 
	{
		/** find interpolated precision - max precision at any rank point after i */
		double[] interpolatedPrecision = new double[pairs.length];
		double n = numCorrectPairs;
		double maxPrecision = n/pairs.length;
		for (int i=pairs.length-1; i>=0; i--) {
			if (correctPair(i)) {
				interpolatedPrecision[i] = maxPrecision;
				n--;
				maxPrecision = Math.max(maxPrecision, n/(i+1));
			}
		}
		/** plot points on the graph */
		n = 0;
		for (int i=0; i<pairs.length; i++) {
			if (correctPair(i)) {
				n++;
				double recallAtRankI = n/numCorrectPairs;
				out.println(recallAtRankI+"\t"+interpolatedPrecision[i]);
			}
		}
	}


	/** Show results in a very simple format.
	 */
	public void displayResults(boolean showMismatches,PrintStream out) throws IOException 
	{
		PrintfFormat fmt = new PrintfFormat("%s %3d %7.2f | %30s | %30s\n");
		for (int i=0; i<pairs.length; i++) {
			if (pairs[i]!=null) {
				String label = pairs[i].getA().getId().equals( pairs[i].getB().getId() ) ? "+" : "-";
				String aText = (pairs[i].getA()==null) ? "***" : pairs[i].getA().getText().unwrap();
				String bText = (pairs[i].getB()==null) ? "***" : pairs[i].getB().getText().unwrap();
				if (showMismatches || "+".equals(label)) {
					out.print( fmt.sprintf( new Object[] { 
						label,
						new Integer(i+1),
						new Double(pairs[i].getDistance()),
						aText,
						bText
					}));
				}
			}
		}
	}
	
	//
	// utility - since after a restore, incorrect pairs are saved as nulls
	//
	private boolean correctPair(int i) { return pairs[i]!=null && pairs[i].isCorrect(); }

	/**
	 * Command-line interface.
	 */
	static public void main(String[] argv) 
	{
		try {
	    Blocker blocker = (Blocker)Class.forName(BLOCKER_PACKAGE+argv[0]).newInstance();
	    StringDistance dist = DistanceFactory.build( argv[1] );
	    MatchData data = new MatchData(argv[2]);
	    MatchExpt expt = new MatchExpt(data,dist,blocker);
			for (int i=3; i<argv.length; ) {
				String c = argv[i++];
				if (c.equals("-display")) {
					expt.displayResults(true,System.out);
				} else if (c.equals("-shortDisplay")) {
					expt.displayResults(false,System.out);
				} else if (c.equals("-graph")) {
					expt.graphPrecisionRecall(System.out);
				} else if (c.equals("-summarize")) {
					System.out.println("maxF1:\t" + expt.maxF1());
					System.out.println("avgPrec:\t" + expt.averagePrecision());
				} else {
					throw new RuntimeException("illegal command "+c);
				}
			}
		} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("\nusage: <blocker> <distanceClass> <matchDataFile> [commands]\n");
		}
	}
}
