package com.buding.battle.logic.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class IDUtil implements InitializingBean {
	public static IDUtil instance = null;
	
	public Map<String, Integer> indexMap = new HashMap<String, Integer>();
	
	public synchronized String genId(String key) {
		if(!indexMap.containsKey(key)) {
			indexMap.put(key, 0);
		}
		int ind = indexMap.get(key);
		ind ++;
		indexMap.put(key, ind);
		return key + ind;
	}
	
	public synchronized String genIntId(String key) {
		if(!indexMap.containsKey(key)) {
			indexMap.put(key, 0);
		}
		int ind = indexMap.get(key);
		ind ++;
		indexMap.put(key, ind);
		return ind+"";
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}
}
