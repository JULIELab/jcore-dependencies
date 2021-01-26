package martin.common;

import java.util.*;
import java.util.logging.Logger;

public class CacheMap <K,V extends Sizeable> implements Iterable<K>{

	private HashMap<K, V> hashMap = new HashMap<K, V>();
	private HashMap<K, Long> sizes = new HashMap<K, Long>();

	private LinkedList<K> priority = new LinkedList<K>();
	private long maxSize;
	private long currentSize = 0;
	private Function<V> factory = null;
	private Logger logger;

	public CacheMap(long maxSize){
		this(maxSize,null, null);
	}

	public CacheMap(long maxSize, Function<V> factory, Logger logger){
		this.maxSize = maxSize;
		this.factory  = factory;
		this.logger = logger;
	}

	public V get(K key){
		V v = remove(key);
		put(key,v);
		return v;
	}

	public void put(K key, V value){
		if (containsKey(key))
			remove(key);

		hashMap.put(key,value);
		priority.addFirst(key);

		long size = value.sizeof();
		currentSize += size;
		sizes.put(key, size);

		free();
	}

	public String listKeys(){
		Object[] a = priority.toArray();
		return Misc.implode(a, ",");
	}
	
	private void free() {
		while (priority.size() > 1 && currentSize > maxSize){
			K k = priority.removeLast();

			if (logger != null)
				logger.info("Unloading cache map data: " + k + "\n");
			
			long s = sizes.remove(k);
			hashMap.remove(k);
			currentSize -= s;
		}
		System.gc();
	}

	public V remove(K key){
		if (!containsKey(key))
			throw new NoSuchElementException();

		priority.remove(key);
		currentSize -= sizes.remove(key);

		return hashMap.remove(key);		
	}

	public boolean containsKey(K key){
		return hashMap.containsKey(key);
	}

	public Iterator<K> iterator(){
		return priority.iterator();
	}

	public Set<K> keySet(){
		return hashMap.keySet();
	}

	public Collection<V> values(){
		return hashMap.values();
	}

	public static void main(String[] args){
		Logger l = Loggers.getDefaultLogger(null);

		CacheMap<Integer,SimpleClass<String>> c = new CacheMap<Integer,SimpleClass<String>>(5,null,l);
		
		for (int i = 0; i < 10; i++){
			c.put(i, new SimpleClass<String>("x"+i,i));

			for (Integer k : c)
				System.out.print(k + ", ");
			System.out.println();
		}
	}
	
	

	public V getOrCreate(K key){
		if (containsKey(key))
			return get(key);
		else if (factory != null){
			if (logger != null)
				logger.info("Loading cache map data: " + key + "\n");

			V v = factory.function(new Object[]{key});
			put(key,v);
			
			if (logger != null)
			logger.info("Loaded " + key + ", size: " + sizes.get(key) + "\n");
			
			return v;
		} else {
			throw new IllegalStateException("getOrCreate can only be called if a factory function has been specified.");
		}
	}

	public void clear() {
		this.priority.clear();
		this.hashMap.clear();
		this.sizes.clear();
		this.currentSize=0;
		System.gc();
	}
}
