package edu.upenn.cit594.datamanagement;

import java.util.HashMap;

/**
 * Data storage class to hold results of user input 
 * @author andrii podhornyi 
 *
 * @param <K> user input 
 * @param <V> result for input 
 */
public class ResultHolder<K, V> {

	/**
	 * internal HashMap stores all results.
	 */
	private HashMap<K, V> results = new HashMap<>();
	
	/**
	 * Checks if value for the given key exists in results 
	 * @param key Key to search for 
	 * @return true if value for given key exists, false otherwise 
	 */
	public boolean contains(K key) {
		return results.containsKey(key);
	}
	
	/**
	 * Places new key-value pair to the map 
	 * @param key user input 
	 * @param value result for input 
	 */
	public void put(K key, V value) {
		results.put(key, value);
	}
	
	/**
	 * Returns the value for the given key
	 * @param key user input 
	 * @return result of input if exists. Null otherwise 
	 */
	public V get(K key) {
		return results.get(key);
	}
}
