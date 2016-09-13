///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
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

package de.julielab.gnu.trove;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Arrays;

/**
 * An implementation of the Map interface which uses an open addressed
 * hash table to store its contents.
 *
 * Created: Sun Nov  4 08:52:45 2001
 *
 * @author Eric D. Friedman
 * @version $Id: THashMap.java,v 1.19 2005/12/27 21:32:04 ericdf Exp $
 */
public class THashMap extends TObjectHash implements Map, Serializable {

    static final long serialVersionUID = 5182945951854128074L;

    /** the values of the map */
    protected transient Object[] _values;

    /**
     * Creates a new <code>THashMap</code> instance with the default
     * capacity and load factor.
     */
    public THashMap() {
        super();
    }

    /**
     * Creates a new <code>THashMap</code> instance with the default
     * capacity and load factor.
     * @param strategy used to compute hash codes and to compare objects.
     */
    public THashMap(TObjectHashingStrategy strategy) {
        super(strategy);
    }

    /**
     * Creates a new <code>THashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public THashMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Creates a new <code>THashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     * @param strategy used to compute hash codes and to compare objects.
     */
    public THashMap(int initialCapacity, TObjectHashingStrategy strategy) {
        super(initialCapacity, strategy);
    }

    /**
     * Creates a new <code>THashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the specified load factor.
     *
     * @param initialCapacity an <code>int</code> value
     * @param loadFactor a <code>float</code> value
     */
    public THashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Creates a new <code>THashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the specified load factor.
     *
     * @param initialCapacity an <code>int</code> value
     * @param loadFactor a <code>float</code> value
     * @param strategy used to compute hash codes and to compare objects.
     */
    public THashMap(int initialCapacity, float loadFactor, TObjectHashingStrategy strategy) {
        super(initialCapacity, loadFactor, strategy);
    }

    /**
     * Creates a new <code>THashMap</code> instance which contains the
     * key/value pairs in <tt>map</tt>.
     *
     * @param map a <code>Map</code> value
     */
    public THashMap(Map map) {
        this(map.size());
        putAll(map);
    }

    /**
     * Creates a new <code>THashMap</code> instance which contains the
     * key/value pairs in <tt>map</tt>.
     *
     * @param map a <code>Map</code> value
     * @param strategy used to compute hash codes and to compare objects.
     */
    public THashMap(Map map, TObjectHashingStrategy strategy) {
        this(map.size(), strategy);
        putAll(map);
    }

    /**
     * @return a shallow clone of this collection
     */
    public Object clone() {
        THashMap m = (THashMap)super.clone();
        m._values = (Object[])this._values.clone();
        return m;
    }

    /**
     * initialize the value array of the map.
     *
     * @param initialCapacity an <code>int</code> value
     * @return an <code>int</code> value
     */
    protected int setUp(int initialCapacity) {
        int capacity;
        
        capacity = super.setUp(initialCapacity);
        _values = new Object[capacity];
        return capacity;
    }
    
    /**
     * Inserts a key/value pair into the map.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     * @return the previous value associated with <tt>key</tt>,
     * or null if none was found.
     */
    public Object put(Object key, Object value) {
        Object previous = null;
        Object oldKey = null;
        int index = insertionIndex(key);
	boolean isNewMapping = true;
        if (index < 0) {
            index = -index -1;
            previous = _values[index];
	    isNewMapping = false;
        }
        oldKey = _set[index];
        _set[index] = key;
        _values[index] = value;
        if (isNewMapping) {
            postInsertHook(oldKey == FREE);
        }

        return previous;
    }

    /**
     * Compares this map with another map for equality of their stored
     * entries.
     *
     * @param other an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean equals(Object other) {
        if (! (other instanceof Map)) {
            return false;
        }
        Map that = (Map)other;
        if (that.size() != this.size()) {
            return false;
        }
        return forEachEntry(new EqProcedure(that));
    }

    public int hashCode() {
        HashProcedure p = new HashProcedure();
        forEachEntry(p);
        return p.getHashCode();
    }

    public String toString() {
        final StringBuffer buf = new StringBuffer("{");
        forEachEntry(new TObjectObjectProcedure() {
                public boolean execute(Object key, Object value) {
                    buf.append(key);
                    buf.append("=");
                    buf.append(value);
                    buf.append(", ");
                    return true;
                }
            });
        buf.append("}");
        return buf.toString();
    }
    
    private final class HashProcedure implements TObjectObjectProcedure {
        private int h = 0;
        
        public int getHashCode() {
            return h;
        }
        
        public final boolean execute(Object key, Object value) {
            h += (_hashingStrategy.computeHashCode(key) ^ (value == null ? 0 : value.hashCode()));
            return true;
        }
    }

    private static final class EqProcedure implements TObjectObjectProcedure {
        private final Map _otherMap;
        
        EqProcedure(Map otherMap) {
            _otherMap = otherMap;
        }
        
        public final boolean execute(Object key, Object value) {
            Object oValue = _otherMap.get(key);
            if (oValue == value || (oValue != null && oValue.equals(value))) {
                return true;
            }
            return false;
        }
    }

    /**
     * Executes <tt>procedure</tt> for each key in the map.
     *
     * @param procedure a <code>TObjectProcedure</code> value
     * @return false if the loop over the keys terminated because
     * the procedure returned false for some key.
     */
    public boolean forEachKey(TObjectProcedure procedure) {
        return forEach(procedure);
    }

    /**
     * Executes <tt>procedure</tt> for each value in the map.
     *
     * @param procedure a <code>TObjectProcedure</code> value
     * @return false if the loop over the values terminated because
     * the procedure returned false for some value.
     */
    public boolean forEachValue(TObjectProcedure procedure) {
        Object[] values = _values;
        Object[] set = _set;
        for (int i = values.length; i-- > 0;) {
            if (set[i] != FREE
                && set[i] != REMOVED
                && ! procedure.execute(values[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Executes <tt>procedure</tt> for each key/value entry in the
     * map.
     *
     * @param procedure a <code>TObjectObjectProcedure</code> value
     * @return false if the loop over the entries terminated because
     * the procedure returned false for some entry.
     */
    public boolean forEachEntry(TObjectObjectProcedure procedure) {
        Object[] keys = _set;
        Object[] values = _values;
        for (int i = keys.length; i-- > 0;) {
            if (keys[i] != FREE
                && keys[i] != REMOVED
                && ! procedure.execute(keys[i],values[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retains only those entries in the map for which the procedure
     * returns a true value.
     *
     * @param procedure determines which entries to keep
     * @return true if the map was modified.
     */
    public boolean retainEntries(TObjectObjectProcedure procedure) {
        boolean modified = false;
        Object[] keys = _set;
        Object[] values = _values;
        for (int i = keys.length; i-- > 0;) {
            if (keys[i] != FREE
                && keys[i] != REMOVED
                && ! procedure.execute(keys[i],values[i])) {
                removeAt(i);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Transform the values in this map using <tt>function</tt>.
     *
     * @param function a <code>TObjectFunction</code> value
     */
    public void transformValues(TObjectFunction function) {
        Object[] values = _values;
        Object[] set = _set;
        for (int i = values.length; i-- > 0;) {
            if (set[i] != FREE && set[i] != REMOVED) {
                values[i] = function.execute(values[i]);
            }
        }
    }

    /**
     * rehashes the map to the new capacity.
     *
     * @param newCapacity an <code>int</code> value
     */
    protected void rehash(int newCapacity) {
        int oldCapacity = _set.length;
        Object oldKeys[] = _set;
        Object oldVals[] = _values;

        _set = new Object[newCapacity];
        Arrays.fill(_set, FREE);
        _values = new Object[newCapacity];

        for (int i = oldCapacity; i-- > 0;) {
            if(oldKeys[i] != FREE && oldKeys[i] != REMOVED) {
                Object o = oldKeys[i];
                int index = insertionIndex(o);
                if (index < 0) {
                    throwObjectContractViolation(_set[(-index -1)], o);
                }
                _set[index] = o;
                _values[index] = oldVals[i];
            }
        }
    }

    /**
     * retrieves the value for <tt>key</tt>
     *
     * @param key an <code>Object</code> value
     * @return the value of <tt>key</tt> or null if no such mapping exists.
     */
    public Object get(Object key) {
        int index = index(key);
        return index < 0 ? null : _values[index];
    }

    /**
     * Empties the map.
     *
     */
    public void clear() {
        super.clear();
        Object[] keys = _set;
        Object[] vals = _values;

        for (int i = keys.length; i-- > 0;) {
            keys[i] = FREE;
            vals[i] = null;
        }
    }

    /**
     * Deletes a key/value pair from the map.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object remove(Object key) {
        Object prev = null;
        int index = index(key);
        if (index >= 0) {
            prev = _values[index];
            removeAt(index);    // clear key,state; adjust size
        }
        return prev;
    }

    /**
     * removes the mapping at <tt>index</tt> from the map.
     *
     * @param index an <code>int</code> value
     */
    protected void removeAt(int index) {
        super.removeAt(index);  // clear key, state; adjust size
        _values[index] = null;
    }

    /**
     * Returns a view on the values of the map.
     *
     * @return a <code>Collection</code> value
     */
    public Collection values() {
        return new ValueView();
    }

    /**
     * returns a Set view on the keys of the map.
     *
     * @return a <code>Set</code> value
     */
    public Set keySet() {
        return new KeyView();
    }

    /**
     * Returns a Set view on the entries of the map.
     *
     * @return a <code>Set</code> value
     */
    public Set entrySet() {
        return new EntryView();
    }

    /**
     * checks for the presence of <tt>val</tt> in the values of the map.
     *
     * @param val an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean containsValue(Object val) {
        Object[] set = _set;
        Object[] vals = _values;

        // special case null values so that we don't have to
        // perform null checks before every call to equals()
        if (null == val) {
            for (int i = vals.length; i-- > 0;) {
                if ((set[i] != FREE && set[i] != REMOVED) &&
                    val == vals[i]) {
                    return true;
                }
            }
        } else {
            for (int i = vals.length; i-- > 0;) {
                if ((set[i] != FREE && set[i] != REMOVED) &&
                    (val == vals[i] || val.equals(vals[i]))) {
                    return true;
                }
            }
        } // end of else
        return false;
    }

    /**
     * checks for the present of <tt>key</tt> in the keys of the map.
     *
     * @param key an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean containsKey(Object key) {
        return contains(key);
    }

    /**
     * copies the key/value mappings in <tt>map</tt> into this map.
     *
     * @param map a <code>Map</code> value
     */
    public void putAll(Map map) {
        ensureCapacity(map.size());
        // could optimize this for cases when map instanceof THashMap
        for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            put(e.getKey(),e.getValue());
        }
    }

    /**
     * a view onto the values of the map.
     *
     */
    protected class ValueView extends KeyView {
        public Iterator iterator() {
            return new THashIterator(THashMap.this) {
                    protected Object objectAtIndex(int index) {
                        return _values[index];
                    }
                };
        }

        public boolean contains(Object value) {
            return containsValue(value);
        }

        public boolean remove(Object value) {
            boolean changed = false;
            Object[] values = _values;
            Object[] set = _set;
            
            for (int i = values.length; i-- > 0;) {
                if ((set[i] != FREE && set[i] != REMOVED) &&
                    value == values[i] ||
                    (null != values[i] && values[i].equals(value))) {
                    removeAt(i);
                    changed = true;
                }
            }
            return changed;
        }
    }

    /**
     * a view onto the entries of the map.
     *
     */
    protected class EntryView extends KeyView {
        public Iterator iterator() {
            return new EntryIterator(THashMap.this);
        }

        public boolean remove(Object entry) {
            // have to effectively reimplement Map.remove here
            // because we need to return true/false depending on
            // whether the removal took place.  Since the Entry's
            // value can be null, this means that we can't rely
            // on the value of the object returned by Map.remove()
            // to determine whether a deletion actually happened.
            //
            // Note also that the deletion is only legal if
            // both the key and the value match.
            Object key, val;
            int index;

            key = keyForEntry(entry);
            index = index(key);
            if (index >= 0) {
                val = valueForEntry(entry);
                if (val == _values[index] ||
                    (null != val && val.equals(_values[index]))) {
                    removeAt(index);    // clear key,state; adjust size
                    return true;
                }
            }
            return false;
        }

        public boolean retainAll(final Collection collection) {
            final Entry e = new Entry(null, null, -1);
            return retainEntries(new TObjectObjectProcedure() {
                    public boolean execute(Object key, Object value) {
                        e.setKey(key);
                        e.setValue0(value);
                        return collection.contains(e);
                    }
                });
        }

        public boolean contains(Object entry) {
            Object val = get(keyForEntry(entry));
            Object entryValue = ((Map.Entry)entry).getValue();
            return entryValue == val ||
                (null != val && val.equals(entryValue));
        }

        protected Object valueForEntry(Object entry) {
            return ((Map.Entry)entry).getValue();
        }

        protected Object keyForEntry(Object entry) {
            return ((Map.Entry)entry).getKey();
        }
    }
    
    /**
     * a view onto the keys of the map.
     */
    protected class KeyView implements Set {
        public Iterator iterator() {
            return new TObjectHashIterator(THashMap.this);
        }

        public int hashCode() {
            int h = 0;
            for (Iterator i = this.iterator(); i.hasNext();) {
                Object o = i.next();
                if (o != null)
                    h += o.hashCode();
            }
            return h;
        }

        public boolean equals(Object other) {
            if (super.equals(other)) {
                return true;
            } else if (other instanceof Collection) {
                Collection that = (Collection) other;
                return this.size() == that.size() && this.containsAll(that);
            } else {
                return false;
            }
        }

        public boolean remove(Object key) {
            boolean b = contains(key);
            THashMap.this.remove(key);
            return b;
        }

        public boolean contains(Object key) {
            return THashMap.this.contains(key);
        }


        public boolean containsAll(Collection collection) {
            for (Iterator i = collection.iterator(); i.hasNext();) {
                if (! contains(i.next())) {
                    return false;
                }
            }
            return true;
        }

        public boolean removeAll(Collection collection) {
            boolean changed = false;
            for (Iterator i = collection.iterator(); i.hasNext();) {
                if (remove(i.next())) {
                    changed = true;
                }
            }
            return changed;
        }

        public void clear() {
            THashMap.this.clear();
        }

        public boolean add(Object obj) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return THashMap.this.size();
        }

        public Object[] toArray() {
            Object[] result = new Object[size()];
            Iterator e = iterator();
            for (int i=0; e.hasNext(); i++)
                result[i] = e.next();
            return result;
        }

        public Object[] toArray(Object[] a) {
            int size = size();
            if (a.length < size)
                a = (Object[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

            Iterator it = iterator();
            for (int i=0; i<size; i++) {
                a[i] = it.next();
            }

            if (a.length > size) {
                a[size] = null;
            }

            return a;
        }

        public boolean isEmpty() {
            return THashMap.this.isEmpty();
        }

        public boolean addAll(Collection collection) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection collection) {
            boolean changed = false;
            Iterator i = iterator();
            while (i.hasNext()) {
                if (! collection.contains(i.next())) {
                    i.remove();
                    changed = true;
                }
            }
            return changed;
        }

        protected final class EntryIterator extends THashIterator {
            EntryIterator(THashMap map) {
                super(map);
            }
            
            public Object objectAtIndex(final int index) {
                return new Entry(_set[index], _values[index], index);
            }
        }
    }

    final class Entry implements Map.Entry {
        private Object key;
        private Object val;
        private final int index;

        Entry(Object key, Object value, final int index) {
            this.key = key;
            this.val = value;
            this.index = index;
        }

        void setKey(Object aKey) {
            this.key = aKey;
        }

        void setValue0(Object aValue) {
            this.val = aValue;
        }
        
        public Object getKey() {
            return key;
        }
        
        public Object getValue() {
            return val;
        }
        
        public Object setValue(Object o) {
            if (_values[index] != val) {
                throw new ConcurrentModificationException();
            }
            _values[index] = o;
            o = val;            // need to return previous value
            val = o;            // update this entry's value, in case
                                // setValue is called again
            return o;
        }

        public boolean equals(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry e1 = this;
                Map.Entry e2 = (Map.Entry) o;
                return (e1.getKey()==null ? e2.getKey()==null : e1.getKey().equals(e2.getKey()))
                    && (e1.getValue()==null ? e2.getValue()==null : e1.getValue().equals(e2.getValue()));
            }
            return false;
        }

        public int hashCode() {
            return (getKey()==null ? 0 : getKey().hashCode()) ^ (getValue()==null ? 0 : getValue().hashCode());
        }
    }

    private void writeObject(ObjectOutputStream stream)
        throws IOException {
        stream.defaultWriteObject();

        // number of entries
        stream.writeInt(_size);

        SerializationProcedure writeProcedure = new SerializationProcedure(stream);
        if (! forEachEntry(writeProcedure)) {
            throw writeProcedure.exception;
        }
    }

    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        int size = stream.readInt();
        setUp(size);
        while (size-- > 0) {
            Object key = stream.readObject();
            Object val = stream.readObject();
            put(key, val);
        }
    }
} // THashMap
