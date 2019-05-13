package com.buding.common.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class IndexMap<K extends Comparable<K>, E> {
	private List<E> list = new ArrayList<E>();
	private Map<K, Integer> indexMap = new HashMap<K, Integer>();
	
	public IndexMap(int capacity) {
		list = new ArrayList<E>(capacity);
	}
	
	public void addWidthInd(K key, E val, int index) {
		if(getByInd(index) != null || getByKey(key) != null) {
			throw new RuntimeException("重复的元素:" + key + " " + index);
		}
		list.set(index, val);
		indexMap.put(key, index);
	}
	
	public List<E> listByPos() {
		return Collections.unmodifiableList(list);
	}
	
	public E removeByKey(K key) {
		Integer index = indexMap.remove(key);
		if(index == null) {
			return null;
		}
		E val = list.get(index);
		list.set(index, null);
		return val;
	}
	
	public int indexOf(K key) {
		if(indexMap.containsKey(key)) {
			return indexMap.get(key);
		}
		return -1;
	}
	
	public E getByKey(K key) {
		int ind = indexOf(key);
		return getByInd(ind);
	}
	
	public E getByInd(int ind) {
		if(ind == -1) {
			return null;
		}
		return list.get(ind);
	}
	
	public int size() {
		return indexMap.size();
	}
	
	public Set<K> keySet() {
		return indexMap.keySet();
	}
	
	public List<E> subList(int start, int end) {
		if(start >= size()) {
			return new ArrayList<E>();
		}
		return this.list.subList(start, end > this.size()? this.size() : end);
	}
}
