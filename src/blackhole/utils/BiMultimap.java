/* 
 * The MIT License
 *
 * Copyright 2020 fabian.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package blackhole.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for mapping multiple keys to a single value and multiple values to a
 * single key. Implements the {@link Map} interface and uses {@link HashMap}s to
 * store the mappings.
 *
 * @author fabian
 * @param <K> Type of the keys
 * @param <V> Type of the values
 *
 * @see Map
 * @see HashMap
 */
public class BiMultimap<K, V> implements Map<K, V> {

	/**
	 * {@link HashMap} mapping a list of values to each key
	 */
	private final HashMap<K, List<V>> keyMap;

	/**
	 * {@link HashMap} mapping a list of keys to each value
	 */
	private final HashMap<V, List<K>> valMap;

	/**
	 * Creates a new empty {@link BiMultimap}
	 */
	public BiMultimap() {
		keyMap = new HashMap<>();
		valMap = new HashMap<>();
	}

	/**
	 * Returns the number of keys in this map
	 *
	 * @return the number of keys in this map
	 */
	@Override
	public int size() {
		return keyMap.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return keyMap.isEmpty();
	}

	/**
	 * Returns {@code true} if this map contains a mapping for the specified key
	 *
	 * @param key key whose presence in this map is to be tested
	 * @return {@code true} if this map contains at least one mapping for the
	 * specified key
	 * @throws ClassCastException if the key is of an inappropriate type for
	 * this map
	 * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	@Override
	public boolean containsKey(Object key) {
		return keyMap.containsKey(key);
	}

	/**
	 * Returns {@code true} if this map contains a mapping for the specified
	 * value
	 *
	 * @param val value whose presence in this map is to be tested
	 * @return {@code true} if this map contains at least one mapping for the
	 * specified value
	 * @throws ClassCastException if the value is of an inappropriate type for
	 * this map
	 * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	@Override
	public boolean containsValue(Object val) {
		return valMap.containsKey(val);
	}

	/**
	 * Returns the first value associated with the given key. This means the
	 * value that was mapped to the given key first after the key had no
	 * mappings will be returned.
	 *
	 * @param key the key whose first associated value will be returned or
	 * {@code null} if the map does not contain the key
	 * @return the first value associated with the given key
	 */
	@Override
	public V get(Object key) {
		List<V> vals = keyMap.get(key);
		if (vals != null && vals.size() > 0) {
			return vals.get(0);
		}
		return null;
	}

	/**
	 * Returns the first key associated with the given value. This means the key
	 * that was mapped to the given value first after the value had no mappings
	 * will be returned.
	 *
	 * @param val the value whose first associated key will be returned or
	 * {@code null} if the map does not contain the value
	 * @return the first key associated with the given value
	 */
	public K getKey(Object val) {
		List<K> keys = valMap.get(val);
		if (keys != null && keys.size() > 0) {
			return keys.get(0);
		}
		return null;
	}

	/**
	 * Returns all values associated with the given key.
	 *
	 * @param key the key whose first associated value will be returned or
	 * {@code null} if the map does not contain the key
	 * @return all values associated with the given key
	 */
	public List<V> getAll(K key) {
		return keyMap.get(key);
	}

	/**
	 * Returns all keys associated with the given value.
	 *
	 * @param val all keys associated with the given value or {@code null} if
	 * the map does not contain the value
	 * @return the first key associated with the given value
	 */
	public List<K> getAllKeys(V val) {
		return valMap.get(val);
	}

	/**
	 * Associates the specified value with the specified key in this map
	 * (optional operation). If the map previously contained a mapping for the
	 * key, the specified value will be added to the mappings for the key.
	 * Furthermore, the key will be added to the mappings for the value.
	 *
	 * @param key key with which the specified value is to be associated
	 * @param val value to be associated with the specified key
	 * @return the specified value added to the map
	 * @throws ClassCastException if the class of the specified key or value
	 * prevents it from being stored in this map
	 * @throws IllegalArgumentException if some property of the specified key or
	 * value prevents it from being stored in this map
	 */
	@Override
	public V put(K key, V val) {
		if (!keyMap.containsKey(key)) {
			keyMap.put(key, new ArrayList<>());
		}
		if (!valMap.containsKey(val)) {
			valMap.put(val, new ArrayList<>());
		}
		keyMap.get(key).add(val);
		valMap.get(val).add(key);
		return val;
	}

	/**
	 * Removes the mappings for the specified key from this map
	 *
	 * @param key key whose mappings are to be removed from the map
	 * @return the previous first value associated with {@code key}, or
	 * {@code null} if there was no mapping for {@code key}.
	 * @throws ClassCastException if the key is of an inappropriate type for
	 * this map
	 * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	@Override
	public V remove(Object key) {
		V val = get(key);
		if (keyMap.containsKey(key)) {
			Iterator<V> it = keyMap.get(key).iterator();
			while (it.hasNext()) {
				valMap.get(it.next()).remove(key);
			}
			keyMap.remove(key);
		}
		return val;
	}

	/**
	 * Removes the mappings for the specified value from this map
	 *
	 * @param val value whose mappings are to be removed from the map
	 * @return the previous first value associated with {@code val}, or
	 * {@code null} if there was no mapping for {@code val}.
	 * @throws ClassCastException if the value is of an inappropriate type for
	 * this map
	 * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	public K removeValue(V val) {
		K key = getKey(val);
		if (valMap.containsKey(val)) {
			Iterator<K> it = valMap.get(val).iterator();
			while (it.hasNext()) {
				keyMap.get(it.next()).remove(val);
			}
			valMap.remove(val);
		}
		return key;

	}

	/**
	 * Removes the specified mapping for the specified key and value from this map
	 *
	 * @param key key whose mapping is to be removed from the map
	 * @param val value whose mapping is to be removed from the map
	 * @return {@code true} if the operation was succesful
	 * @throws ClassCastException if the key is of an inappropriate type for
	 * this map
	 * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	@Override
	public boolean remove(Object key, Object val) {
		if (keyMap.containsKey(key) && valMap.containsKey(val)) {
			return keyMap.get(key).remove(val) && valMap.get(val).remove(key);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		Iterator<? extends Entry<? extends K, ? extends V>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<? extends K, ? extends V> entry = it.next();
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		keyMap.clear();
		valMap.clear();
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public Set<K> keySet() {
		return keyMap.keySet();
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public Collection<V> values() {
		return valMap.keySet();
	}

	/**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is not backed by the map.
     *
     * @return a set view of the mappings contained in this map
     */
	@Override
	public Set<Entry<K, V>> entrySet() {
		HashSet<Entry<K, V>> set = new HashSet<>();
		Iterator<Entry<K, List<V>>> it = keyMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<K, List<V>> entry = it.next();
			for (int i = 0; i < entry.getValue().size(); i++) {
				set.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().get(i)));
			}
		}
		return set;
	}

}
