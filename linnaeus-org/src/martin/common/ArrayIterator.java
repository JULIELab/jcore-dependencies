package martin.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Class for easily constructing an iterator from an array
 * @author Martin
 *
 * @param <E> the class of the array
 */
public class ArrayIterator<E> implements Iterator<E> {
	private E[] array;
	int nextItem = 0;

	public ArrayIterator(E[] array){
		this.array = array;
	}

	public boolean hasNext() {
		return nextItem < array.length;
	}

	public E next() {
		if (!hasNext())
			throw new NoSuchElementException();
		return array[nextItem++];
	}

	public void remove() {
		if  (nextItem == 0)
			throw new NoSuchElementException();
		
		array[nextItem-1] = null;		
	}
}
