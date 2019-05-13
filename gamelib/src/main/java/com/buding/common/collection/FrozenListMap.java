package com.buding.common.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buding.common.db.model.IEntity;


public class FrozenListMap<K extends Comparable<K>, E extends IEntity<K>> {
	private List<E> list = new ArrayList<E>();
	private Map<K, Integer> indexMap = new HashMap<K, Integer>();
	
	public FrozenListMap(Collection<E> paramList) {
		int ind = 0;
		if(paramList != null) {
			for(E obj : paramList) {
				list.add(obj);
				indexMap.put(obj.getIdentity(), ind++);
			}
		}
		
		if(list.size() != indexMap.size()) {
			throw new RuntimeException("DuplicateKey");
		}
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
		return list.size();
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
	
	public List<E> list() {
		return this.list;
	}
}
