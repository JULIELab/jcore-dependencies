package martin.common.compthreads;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

import martin.common.ArrayIterator;

/**
 * Class for facilitating parallelized computations. 
 * This class provides functionality when the "problem" instances are given by an iterator, while the ArrayBasedMaster class is better suited when the problems are stored in an array.
 * Solutions can be retrieved as they complete through an iterator.
 * Usage example: new Thread(new ArrayBasedMaster(problems, 4)).start(); Object[] solutions = master.getSolutions();
 * 
 * @author Martin
 * @param <E> The class representing solutions to computational problems
 */
public class IteratorBasedMaster<E> extends Master<E> implements Runnable, Iterator<E>, Iterable<E>{
	private Iterator<Problem<E>> problemIterator;

	private Map<Integer,E> solutions;
	private Map<Integer,Semaphore> solutionSemaphores;

	private Semaphore threadsem;
	private Semaphore solutionsem = new Semaphore(1,true);
	private Semaphore addedsem = new Semaphore(0,true);
	private int nextJobToRelease = 0;

	private Semaphore storageLimitSem;

	/**
	 * Creates a new IteratorBasedMaster.
	 * @param problemIterator specifies an iterator which gives the problems that are to be computed.
	 * @param numThreads specifies the maximum number of problems that should be computed concurrently
	 * @param maxStorageLength specifies the maximum number of result objects that can be help in buffer memory
	 */
	public IteratorBasedMaster(Iterator<Problem<E>> problemIterator, int numThreads, Integer maxStorageLength){
		this.problemIterator = problemIterator;

		this.solutions = Collections.synchronizedMap(new HashMap<Integer,E>());
		this.solutionSemaphores = Collections.synchronizedMap(new HashMap<Integer,Semaphore>());

		threadsem = new Semaphore(numThreads,true);
		this.storageLimitSem = maxStorageLength != null ? new Semaphore(maxStorageLength,true) : null;
	}

	/**
	 * Creates a new IteratorBasedMaster.
	 * @param problemIterator specifies an iterator which gives the problems that are to be computed.
	 * @param numThreads specifies the maximum number of problems that should be computed concurrently
	 */
	public IteratorBasedMaster(Iterator<Problem<E>> problemIterator, int numThreads){
		this(problemIterator, numThreads, null);
	}

	public IteratorBasedMaster(Problem<E>[] array, int numThreads){
		this(new ArrayIterator<Problem<E>>(array),numThreads, null);
	}

	public IteratorBasedMaster(Collection<Problem<E>> collection, int numThreads){
		this(collection.iterator(), numThreads, null);
	}

	//@Override
	/**
	 * Runs the thread. This should not be called by the user - call startThread() instead.
	 */
	public void run(){
		int nextJob = 0;

		while (problemIterator.hasNext()){
			try {
				threadsem.acquire();
				
				if (storageLimitSem != null)
					storageLimitSem.acquire();
				
				solutionSemaphores.put(nextJob, new Semaphore(0,true));

				Worker<E> w = new Worker<E>(problemIterator.next(), this, nextJob++);

				new Thread(w).start();

				addedsem.release();
			} catch (Exception e){
				System.err.println(e);
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	void report(E solution, int id){
		try {
			solutionsem.acquire();
			solutions.put(id,solution);
			solutionsem.release();
		} catch (InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);			
		}

		if (getReportProgress())
			System.out.println("\tThread " + id + " finished.");

		solutionSemaphores.get(id).release();
		threadsem.release();
	}

	/**
	 * @return whether all solutions (already computed or not) have already been returned by the next() function
	 */
	public boolean hasNext() {
		if (problemIterator.hasNext())
			return true;

		if (solutionSemaphores.containsKey(nextJobToRelease))
			return true;
		else 
			return false;
	}

	/**
	 * If no computed solutions exist that have not already been returned, this method will block until the next problem finishes.
	 * The problems are returned in the same order as they come in the problem iterator given to the constructor.
	 * @return the solution to the next problem in line to be returned.
	 * @throws NoSuchElementException if called when hasNext() == false
	 */
	public E next(){
		return next(true);
	}

	/**
	 * If no computed solutions exist that have not already been returned, this method will block until the next problem finishes.
	 * The problems are returned in the same order as they come in the problem iterator given to the constructor.
	 * @param remove whether to also delete the returned object from the underlying storage data structures
	 * @return the solution to the next problem in line to be returned.
	 * @throws NoSuchElementException if called when hasNext() == false
	 */
	public E next(boolean remove) {
		if (!hasNext())
			throw new NoSuchElementException();

		E res = null;

		int job = nextJobToRelease++;
		try {
			addedsem.acquire();
			solutionSemaphores.get(job).acquire();
			res = solutions.get(job);

			if (remove){
				solutions.remove(job);
				solutionSemaphores.remove(job);
				if (storageLimitSem != null)
					storageLimitSem.release();
			} else {
				solutionSemaphores.get(job).release();
			}
		} catch (InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);			
		}

		return res;
	}

	public void reset(){
		addedsem.release(nextJobToRelease);
		nextJobToRelease = 0;
	}

	/**
	 * Does not do anything, as next() also removes elements from storage after returning them.
	 */
	public void remove() {
	}

	public Iterator<E> iterator() {
		return this;
	}
}
