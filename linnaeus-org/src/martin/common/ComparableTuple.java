package martin.common;

/**
 * Tuple class implementing the Comparable interface. Objects are compared primarily on their first component, and secondariliy on their second component.
 * @author Martin
 *
 * @param <E>
 * @param <T>
 */
public class ComparableTuple<E extends Comparable<E>,T extends Comparable<T>> implements Comparable<ComparableTuple<E,T>>{
	private E a;
	private T b;

	public ComparableTuple(E a, T b){
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

	public int compareTo(ComparableTuple<E,T> o){
		if (a != null && o.a == null)
			return 1;
		if (a == null && o.a != null)
			return -1;

		if (a != null){
			int x = a.compareTo(o.getA());
			if (x != 0)
				return x;
		}

		if (b != null && o.b == null)
			return 1;
		if (b == null && o.b != null)
			return -1;

		if (b != null)
			return b.compareTo(o.getB());

		return 0;
	}

	public boolean equals(ComparableTuple<E,T> o){
		if (a == null && o.a != null)
			return false;
		if (a != null && o.a == null)
			return false;
		if (b == null && o.b != null)
			return false;
		if (b != null && o.b == null)
			return false;
		if (a != null && !a.equals(o.a))
			return false;
		if (b != null && !b.equals(o.b))
			return false;

		return true;
	}
	
	public String toString(){
		return "(" + a.toString() + ", " + b.toString() + ")";
	}
}
