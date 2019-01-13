package com.wcohen.secondstring;

import junit.framework.*;
import java.io.*;
import java.util.*;

public class TestPackage extends TestSuite 
{
	private static final boolean DEBUG = false;

	public TestPackage(String name) { super(name); }

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTest( new MyFixture("testLevenstein") );
		suite.addTest( new MyFixture("testJaccard") );
		suite.addTest( new MyFixture("testJaro") );
		suite.addTest( new MyFixture("testJaroWinkler") );
		suite.addTest( new MyFixture("testDirichletJS") );
		suite.addTest( new MyFixture("testJelinekMercerJS") );
		suite.addTest( new MyFixture("testJaroWinklerTFIDF") );
		suite.addTest( new MyFixture("testSoftTokenFelligiSunter") );
		suite.addTest( new MyFixture("testTFIDF") );
		suite.addTest( new MyFixture("testTokenFelligiSunter") );
		return suite;
	}
		
	public static class MyFixture extends TestCase {
		private String s = "william w. cohen";
		private String t = "w. liam chen";
		private static List corpus;
		static {
			corpus = new ArrayList();
			String[] words = { "william", "w", "cohen", "liam", "chen", "liam", "o", "furniture", 
												 "george", "w", "bush", "wei", "chen" };
			for (int i=0; i<words.length; i++) {
				corpus.add(new StringWrapper(words[i]));
			}
		}

		public MyFixture(String name) { super(name); }

		public void check(StringDistance dist, double expected, double epsilon) {
			dist.accumulateStatistics( corpus.iterator() );
			double actual = dist.score(s,t);
			if (DEBUG) {
				System.out.println("dist="+dist+" s='"+s+"' t='"+t+"' actual="+actual+" expected="+expected);
			}
			assertEquals( expected, actual, epsilon);
		}
		// distance-measure specific tests
		public void testJaccard() { check( new Jaccard(), 0.2, 0); }
		public void testJaro() { check( new Jaro(), 0.833, 0.01); }
		public void testJaroWinkler() { check( new JaroWinkler(), 0.85, 0.01); }
		public void testLevel2Jaro() { check( new Level2Jaro(), 0.902, 0.01); }
		public void testLevel2MongeElkan() { check( new Level2MongeElkan(), 0.9167, 0.001); }
		public void testLevenstein() { check( new Levenstein(), -6, 0); }
		public void testMongeElkan() { check( new MongeElkan(), 0.55, 0.01); }
		public void testDirichletJS() { check( new DirichletJS(), 0.2884, 0.01); }
		public void testJelinekMercerJS() { check( new JelinekMercerJS(), 0.2974, 0.01); }
		public void testJaroWinklerTFIDF() { check( new JaroWinklerTFIDF(), 0.886, 0.01); }
		public void testSoftTokenFelligiSunter() { check( new SoftTokenFelligiSunter(), 3.154, 0.01); }
		public void testTFIDF() { check( new TFIDF(), 0.2647, 0.01); }
		public void testTokenFelligiSunter() { check( new TokenFelligiSunter(), -0.6931, 0.01); }
	}

	static public void main(String[] argv) {
		junit.textui.TestRunner.run(suite());
	}
}
