package martin.common.compthreads;

/**
 * Worker thread class for performing concurrent computations. 
 * @author Martin
 *
 * @param <E> the class of the result from the computations.
 */
class Worker<E> implements Runnable{
	private Problem<E> problem;
	private Master<E> master;
	private int id;
	
	/**
	 * 
	 * @param problem the problem which is to be computed
	 * @param master master object to report back to when finished
	 * @param id the id of the problem
	 */
	Worker(Problem<E> problem, Master<E> master, int id){
		this.problem = problem;
		this.master = master;
		this.id = id;
	}
	
	//@Override
	/**
	 * runs the compute() function of the problem, and then reports back to the master object with the result.
	 */
	public void run(){
		E s = problem.compute();
		master.report(s, id);
	}
}
