package martin.common.compthreads;

/**
 * Implement this interface to allow easy parallelization of computational code. Objects of implementing classes can be sent to Master objects for concurrent computations.  
 * @author Martin
 *
 */

public interface Problem<E> {
	/**
	 * This is the method called by the master threads, and should contain the code that should be computed concurrently. 
	 * @return a data object representing output data from the computation that you might want to read after finished computation. If there is no such output (e.g. results are written to file), just return null. 
	 */
	public E compute();
}
