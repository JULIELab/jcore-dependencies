package cc.mallet.pipe.iterator;

import cc.mallet.types.Instance;

import java.util.Iterator;

public class EmptyInstanceIterator implements Iterator<Instance> {

	public boolean hasNext() { return false; }
	public Instance next () { throw new IllegalStateException ("This iterator never has any instances.");	}
	public void remove () { throw new IllegalStateException ("This iterator does not support remove().");	}
}
