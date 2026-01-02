/*
 * Copyright 2026 The WhiteHat Project
 *
 * The WhiteHat Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package top.whitehat.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** key-value pair */
public class KeyValue<K, V> implements Map.Entry<K, V>{
	
	/** Create a KeyValue object */
	public static <K, V> KeyValue<K, V> of(K left, V right) {
		return new KeyValue<K, V>(left, right);
	}
	
	/** get key of the KeyValue object */
	public static <K, V> K key(KeyValue<K, V> pair) {
		return pair.getKey();
	}
	
	/** get value of the KeyValue object */
	public static <K, V> V value(KeyValue<K, V> pair) {
		return pair.getValue();
	}
	
	/** set value of the KeyValue object */
	public static <K, V>  KeyValue<K, V> value(KeyValue<K, V> pair, V value) {
		pair.setValue(value);
		return pair;
	}
	
	/** Create a map */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> map(K key, V value, Object...objs) {
		Map<K, V> ret = new HashMap<K, V>();
		ret.put(key, value);
		int i =0;
		while (i < objs.length) {
			Object k = objs[i++];
			Object v = objs[i++];
			ret.put((K)k, (V)v);
			i++;
		}
		return ret;
	}

	private K key;
	
	private V value;

	public KeyValue() {

	}

	public KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getValue() {
		return value;
	}

	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
            return true;
        }
		
		if (obj instanceof KeyValue<?, ?>) {
            final KeyValue<?, ?> other = (KeyValue<?, ?>) obj;
            return Objects.equals(getKey(), other.getKey()) &&
                   Objects.equals(getValue(), other.getValue());
        }
		
		return false;
	}

	@Override
	public int hashCode() {
		return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(key).append(", ").append(value).append(")");
		return sb.toString();
	}
}