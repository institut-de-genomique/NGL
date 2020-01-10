package fr.cea.ig.ngl.utils;

import java.util.HashMap;

// Allow static map creation without using the anonymous 
// hash map constructor that raises a serialVersionUID warning.
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
