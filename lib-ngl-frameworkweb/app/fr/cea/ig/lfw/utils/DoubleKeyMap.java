package fr.cea.ig.lfw.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author sgas
 *
 * @param <K1>   cle1
 * @param <K2>   cle2
 * @param <V>    valeur de type transition associée à la paire de cles
 */
public class DoubleKeyMap < K1, K2, V> {
	
	private Map < K1, Map<K2, V> > map = new HashMap<>();
	
	public void put(K1 k1, K2 k2 , V v){
		Map<K2, V> tmp = map.get(k1);
		if (tmp == null) {
			tmp = new HashMap<>();
			map.put(k1, tmp);
		}
		tmp.put(k2, v);
	}
	
	public V get(K1 k1, K2 k2) {
		Map<K2, V> tmp = map.get(k1);
		if (tmp == null) {
			return null;
		}
		return tmp.get(k2);
	}
	
	public boolean contains(K1 k1, K2 k2) {
		Map<K2, V> tmp = map.get(k1);
		if (tmp == null) 
			return false;
		return tmp.containsKey(k2);
	}
	
}