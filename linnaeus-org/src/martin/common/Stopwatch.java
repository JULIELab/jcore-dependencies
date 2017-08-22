package martin.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Class useful for profiling the time usage of java software.
 * Consecutive calls using the same label are added together to a total sum.
 * 
 * Example:
 * Stopwatch s = new Stopwatch("Method 1");
 * method1();
 * s.stop();
 * 
 * Stopwatch.printStatistics();
 * 
 * @author Martin
 */
public class Stopwatch {
	private static Map<String,Long> sums = new HashMap<String,Long>();
	private static Map<String,Integer> hits = new HashMap<String,Integer>();

	private static Semaphore totalSem = new Semaphore(1);

	private long startTime;
	private String label;

	/**
	 * Returns a running Stopwatch object. 
	 * @param label
	 */
	public Stopwatch(String label){
		this.startTime = -1;
		this.label = label;
	}

	/**
	 * Starts the stopwatch, storing the time when this function is called.
	 * @return a reference to the stopwatch
	 */
	public static Stopwatch startNew(String label){
		Stopwatch s = new Stopwatch(label);
		s.start();
		return s;		
	}
	
	/**
	 * Starts the stopwatch, storing the time when this function is called.
	 */
	public void start(){
		this.startTime = System.currentTimeMillis();
	}
	
	/**
	 * Stops the stopwatch, without adding the elapsed time to the global data store.
	 */
	public void cancel(){
		this.startTime = -1;
	}

	/**
	 * Stops the Stopwatch, and adds the elapsed time to a global sum for the label specified on construction. 
	 * @return the number of milliseconds elapsed since this stopwatch was last started.
	 */
	public long stop(){
		if (this.startTime == -1)
			throw new IllegalStateException("This stopwatch needs to be restarted before stop() is called a second time.");
		
		long time = System.currentTimeMillis() - startTime;

		try {
			totalSem.acquire();

			if (!sums.containsKey(label)){
				sums.put(label, time);
				hits.put(label, 1);
			} else {
				sums.put(label, sums.get(label) + time);
				hits.put(label, hits.get(label) + 1);
			}

			totalSem.release();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		startTime = -1;
		
		return time;
	}

	public static void printStats(){
		try {
			totalSem.acquire();

			ComparableTuple<Long,String>[] sums_list = new ComparableTuple[sums.size()];
			
			//store the total times in an array so we can sort them.
			//store -time so they are sorted in descending order of time, but ascending order of label. 
			
			int c = 0;
			for (String l : sums.keySet())
				sums_list[c++] = new ComparableTuple<Long, String>(-sums.get(l), l);
			
			Arrays.sort(sums_list);
			
			System.out.println("Label\tTime (ms)\tHits");
			
			for (ComparableTuple<Long, String> t : sums_list)
				System.out.println(t.getB() + "\t" + (-t.getA()) + "\t" + hits.get(t.getB()));

			totalSem.release();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
