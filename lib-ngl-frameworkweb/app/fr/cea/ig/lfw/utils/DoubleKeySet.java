package fr.cea.ig.lfw.utils;

public class DoubleKeySet<K1,K2> {
	
	private final DoubleKeyMap<K1,K2,Void> elements = new DoubleKeyMap<>();
	
	public void add(K1 k1, K2 k2) {
		elements.put(k1, k2, null);
	}
	
	public void addK2s(K1 k1, @SuppressWarnings("unchecked") K2... k2s) {
		for (K2 k2 : k2s)
			add(k1, k2);
	}
	
	public boolean contains(K1 k1, K2 k2) {
		return elements.contains(k1, k2);
	}
	
}
