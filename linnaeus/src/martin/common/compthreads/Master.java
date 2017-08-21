package martin.common.compthreads;

public abstract class Master<E> implements Runnable{
	private boolean reportProgress = false;

	abstract void report(E solution, int id);
	public abstract void run();
	
	/**
	 * Set whether the object should report once each computational process is finished.
	 * @param v
	 */
	public void setReportProgress(boolean v){
		reportProgress = v;
	}

	/**
	 * @return the reportProgress
	 */
	public boolean getReportProgress() {
		return reportProgress;
	}
	
	/**
	 * Spawns a new thread for the master object and starts it.
	 * @return A reference to the executing thread.
	 */
	public Thread startThread() {
		Thread t = new Thread(this);
		t.start();
		return t;
	}
}
