package martin.common;

public class Tuple<E,T> {
	private E a;
	private T b;

	public Tuple(E a, T b){
		this.a = a;
		this.b = b;
	}

	/**
	 * @return the a
	 */
	public E getA() {
		return a;
	}

	/**
	 * @param a the a to set
	 */
	public void setA(E a) {
		this.a = a;
	}

	/**
	 * @return the b
	 */
	public T getB() {
		return b;
	}

	/**
	 * @param b the b to set
	 */
	public void setB(T b) {
		this.b = b;
	}

	
	public String toString(){
		return "(" + a.toString() + ", " + b.toString() + ")";
	}
}
