package martin.common;

public class SimpleClass<E> implements Sizeable{
	private E e;
	private long s;
	public SimpleClass(E e, long s){this.e = e;this.s=s;}
	public E get(){return e;}
	public long sizeof(){return s;}
	public String toString() { return e.toString(); }
}
