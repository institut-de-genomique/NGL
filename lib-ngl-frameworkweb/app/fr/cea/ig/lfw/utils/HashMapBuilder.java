package fr.cea.ig.lfw.utils;

import java.util.HashMap;

/**
 * Static map creation without using an anonymous HashMap
 * subclass.
 *  
 * @author vrd
 *
 * @param <K> key type
 * @param <V> value type
 */
public class HashMapBuilder<K,V> {
	
	private final HashMap<K,V> map;
	
	public HashMapBuilder() {
		map = new HashMap<>();
	}
	
	public HashMapBuilder<K,V> put(K key, V value) {
		map.put(key,value);
		return this;
	}
	
	public HashMap<K,V> asMap() {
		return map;
	}
	
}
