package martin.common.compthreads;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Class for facilitating parallelized computations. 
 * This class provides functionality when the "problem" instances are stored in an array, while the IteratorBasedMaster class is better suited when the problems are given by an iterator.
 * Usage example: new Thread(new ArrayBasedMaster(problems, 4)).start(); Object[] solutions = master.getSolutions();
 * @author Martin
 *
 */
public class ArrayBasedMaster<E> extends Master<E> implements Runnable{
	private Problem<E>[] problems;
	private Object[] solutions;

	private Semaphore threadsem;
	private Semaphore solutionsem = new Semaphore(0,true);

	public ArrayBasedMaster(Problem<E>[] problems, int numThreads){
		this.problems = problems;
		this.solutions = new Object[problems.length];
		threadsem = new Semaphore(numThreads,true);
	}

	/**
	 * The method which is run as the master thread starts. Should not be run directly by the user. 
	 */
	//@Override
	public void run(){
		for (int i = 0; i < problems.length; i++){
			try {
				threadsem.acquire();
				Worker<E> w = new Worker<E>(problems[i],this,i);
				new Thread(w).start();
			} catch (Exception e){
				System.err.println(e);
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	/**
	 * This method will block until all problems have been processed and computed.
	 * Returned solutions will be in the same order as the order of the problems given to the constructor.
	 * @return the solutions to the problems specified (i.e. an array with the objects that are returned from the problem compute() methods).
	 */
	public Object[] getSolutions(){
		try {
			for (int i = 0; i < problems.length; i++)
				solutionsem.acquire();
		} catch (InterruptedException e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return solutions;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<E> getArrayListSolutions(){
		Object[] sols = getSolutions();
		ArrayList<E> res = new ArrayList<E>(sols.length);
		for (int i = 0; i < res.size(); i++)
			res.add((E) sols[i]);
		return res;
	}

	/**
	 * Called by the worker threads as they complete.
	 */
	void report(E solution, int id){
		solutions[id] = solution;
		
		if (getReportProgress()){
			if (solution != null)
				System.out.println("\tThread " + id + " finished.");
			else
				System.out.println("\tThread finished.");
		}

		solutionsem.release();
		threadsem.release();
	}
}
