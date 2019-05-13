package com.buding.hall.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class RankAward {
	public int rankFrom;
	public int rankTo;
	public String awardDesc;
	public List<ItemPkg> items;
	
	public String persistenceAwardStr() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(ItemPkg conf : items) {
			map.put(conf.itemId, conf.count);
		}
		return new Gson().toJson(map);
	}
}
