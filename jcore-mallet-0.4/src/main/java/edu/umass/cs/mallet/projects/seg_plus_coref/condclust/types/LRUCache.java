/* Copyright (C) 2002 Dept. of Computer Science, Univ. of Massachusetts, Amherst

   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet

   This program toolkit free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  For more
   details see the GNU General Public License and the file README-LEGAL.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
   02111-1307, USA. */


/**
	 @author Aron Culotta
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.condclust.types;
import edu.umass.cs.mallet.base.util.*;
import java.util.*;

/** Stores objects in a hashmap. Uses Least-recently used list to keep
 * size of cache reasonable.  */
public class LRUCache  {

	HashMap hash;
	/** maximum size of cache*/
	int maxSize;
	/** number of lru to remove when size > maxSize*/
	int toRemove;
	/** List of keys in order of use (head = LRU) */
	LinkedList lru;
	
	public LRUCache (int _maxSize, int _toRemove) {
		this.maxSize = _maxSize;
		this.toRemove = _toRemove;
		lru = new LinkedList ();
		hash = new HashMap ();
	}

	public LRUCache () {
		this (1000, 100);
	}
	
	public void put (Object key, Object value) {
		hash.put (key, value);
		moveToEnd (key);
		if (hash.size() > maxSize) 
			removeLRU ();
	}

	public Object get (Object key) {
		Object val = hash.get (key);
		if (val != null)
			moveToEnd (key);
		return val;
	}

	private void moveToEnd (Object key) {
		lru.remove (key);
		lru.addLast (key);
	}

	private void removeLRU () {
		for (int i=0; i < this.toRemove && lru.size() > 0; i++) {
			Object key = lru.removeFirst();
			hash.remove (key);
		}
	}

	public int size () { return hash.size(); }
}

