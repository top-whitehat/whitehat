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

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/** JSON array */
public class JSONArray extends JSON   {  
	private static final long serialVersionUID = -2993314505532225051L;
	
	protected List<Object> list = new ArrayList<Object>();
	
	public List<Object> getList() {
		return list;
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

//	@Override
	public boolean contains(Object o) {		
		return list.contains(o);
	}

//	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

//	@Override
	public Object[] toArray() {
		return list.toArray();
	}

//	@Override
//	public <T> T[] toArray(T[] a) {		
//		return list.toArray(a)
//	}

//	@Override
	public boolean add(Object e) {
		return list.add(e);
	}


//	@Override
	public boolean containsAll(Collection<?> c) {	
		return list.containsAll(c);
	}

//	@Override
	public boolean addAll(Collection<? extends Object> c) {
		return list.addAll(c);
	}

//	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
		return list.addAll(index, c);
	}

//	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

//	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

//	@Override
	public Object get(int index) {
		return list.get(index);
	}
	
	private boolean isInteger(String key) {
		try {
			Integer.parseInt((String)key);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public Object get(Object key) {
		if (key instanceof Integer) {
			return list.get((Integer)key);
		}
		
		if (key instanceof String && isInteger((String)key)) {
			return list.get(Integer.parseInt((String)key)); //TODO
		}
		return null;
	}
	
	@Override
	public JSON put(String key, Object value) {
		if (isInteger(key))
				list.set(Integer.parseInt((String)key), value); //TODO
		return this;
	}
	

//	@Override
	public Object set(int index, Object element) {
		return list.set(index, element);
	}

//	@Override
	public void add(int index, Object element) {
		list.add(index, element);
	}

//	@Override
	public Object remove(int index) {
		return list.remove(index);
	}

//	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

//	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

//	@Override
	public ListIterator<Object> listIterator() {
		return list.listIterator();
	}

//	@Override
	public ListIterator<Object> listIterator(int index) {
		return list.listIterator(index);
	}

//	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}


}