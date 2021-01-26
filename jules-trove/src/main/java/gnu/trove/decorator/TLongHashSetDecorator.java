///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2002, Eric D. Friedman All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package gnu.trove.decorator;

import de.julielab.gnu.trove.TLongHashSet;
import de.julielab.gnu.trove.TLongIterator;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Wrapper class to make a TLongHashSet conform to the <tt>java.util.Set</tt> API.
 * This class simply decorates an underlying TLongHashSet and translates the Object-based
 * APIs into their Trove primitive analogs.
 *
 * <p>
 * Note that wrapping and unwrapping primitive values is extremely inefficient.  If
 * possible, users of this class should override the appropriate methods in this class
 * and use a table of canonical values.
 * </p>
 *
 * Created: Tue Sep 24 22:08:17 PDT 2002
 *
 * @author Eric D. Friedman
 * @version $Id: TLongHashSetDecorator.java,v 1.5 2004/11/09 15:48:50 ericdf Exp $
 * @since trove 0.1.8
 */
public class TLongHashSetDecorator extends AbstractSet implements Set, Cloneable {
    /** the wrapped primitive set */
    protected TLongHashSet _set;

    /**
     * Creates a wrapper that decorates the specified primitive set.
     */
    public TLongHashSetDecorator(TLongHashSet set) {
	super();
	this._set = set;
    }

    /** 
     * Clones the underlying trove collection and returns the clone wrapped in a new
     * decorator instance.  This is a shallow clone except where primitives are 
     * concerned.
     *
     * @return a copy of the receiver
     */
    public Object clone() {
        try {
            TLongHashSetDecorator copy = (TLongHashSetDecorator) super.clone();
            copy._set = (TLongHashSet) _set.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            // assert(false);
            throw new InternalError(); // we are cloneable
        }
    }

    /**
     * Inserts a value into the set.
     *
     * @param true if the set was modified by the insertion
     */
    public boolean add(Object value) {
	return _set.add(unwrap(value));
    }

    /**
     * Compares this set with another set for equality of their stored
     * entries.
     *
     * @param other an <code>Object</code> value
     * @return true if the sets are identical
     */
    public boolean equals(Object other) {
	if (_set.equals(other)) {
	    return true;	// comparing two trove sets
	} else if (other instanceof Set) {
	    Set that = (Set)other;
	    if (that.size() != _set.size()) {
		return false;	// different sizes, no need to compare
	    } else {		// now we have to do it the hard way
		Iterator it = that.iterator();
		for (int i = that.size(); i-- > 0;) {
		    Object val = it.next();
		    if (val instanceof Long) {
			long v = unwrap(val);
			if (_set.contains(v)) {
			    // match, ok to continue
			} else {
			    return false; // no match: we're done
			}
		    } else {
			return false; // different type in other set
		    }
		}
		return true;	// all entries match
	    }
	} else {
	    return false;
	}
    }

    /**
     * Empties the set.
     */
    public void clear() {
	this._set.clear();
    }

    /**
     * Deletes a value from the set.
     *
     * @param value an <code>Object</code> value
     * @return true if the set was modified
     */
    public boolean remove(Object value) {
	return _set.remove(unwrap(value));
    }

    /**
     * Creates an iterator over the values of the set.
     *
     * @return an iterator with support for removals in the underlying set
     */
    public Iterator iterator() {
	return new Iterator() {
		private final TLongIterator it = _set.iterator();
			    
		public Object next() {
		    return wrap(it.next());
		}

		public boolean hasNext() {
		    return it.hasNext();
		}

		public void remove() {
		    it.remove();
		}
	    };
    }

    /**
     * Returns the number of entries in the set.
     * @return the set's size.
     */
    public int size() {
	return this._set.size();
    }

    /**
     * Indicates whether set has any entries.
     * @return true if the set is empty
     */
    public boolean isEmpty() {
	return (size() == 0);
    }

    /**
     * Wraps a value
     *
     * @param a value in the underlying set
     * @return an Object representation of the value
     */
    protected Long wrap(long k) {
	return new Long(k);
    }

    /**
     * Unwraps a value
     *
     * @param a wrapped value
     * @return an unwrapped representation of the value
     */
    protected long unwrap(Object value) {
	return ((Long)value).longValue();
    }
} // TLongHashSetDecorator
