package com.buding.hall.module.status.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.admin.component.BaseComponent;
import com.buding.common.util.DateUtil;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.StatusItemConf;
import com.buding.hall.module.status.model.OnlineModel;
import com.google.gson.Gson;

/**
 * 维护每个服务器实例的在线人数
 * @author Administrator
 *
 */
public class StatusServiceImpl extends BaseComponent implements StatusService {
	@Autowired
	ConfigManager configManager;
	
	public boolean mockOnlineUser = true;
	
	//每个服务器实例-对应的在线人数
	ConcurrentMap<String, Integer> onlineMap = new ConcurrentHashMap<String, Integer>();
	//每个服务器实例-对应的虚构在线人数
	ConcurrentMap<String, Integer> onlineMockMap = new ConcurrentHashMap<String, Integer>();
	//每个服务器实例下每个游戏对应的人数
	ConcurrentMap<String, OnlineModel> onlineServerMap = new ConcurrentHashMap<String, OnlineModel>();

	public Map<String, Integer> getOnlineMap() {
		return onlineMockMap;
	}
	
	public void mock() {
		ConcurrentMap<String, Integer> onlineMap = this.onlineMap;
		ConcurrentMap<String, Integer> onlineMockMap = new ConcurrentHashMap<String, Integer>();
		
		int date = DateUtil.getHHmmss(new Date());
		for(String gameId : onlineMap.keySet()) {
			Integer num = onlineMap.get(gameId);
			if(num == null) {
				num = 0;
			}
			
			if(mockOnlineUser) {
				StatusItemConf item = configManager.getStatusConf(gameId, date);
				if(item != null) {
					if(num < item.randomNumStart) {
						num = new Random().nextInt(item.randomNumEnd - item.randomNumStart) + item.randomNumStart;
					}
				}
			}
			onlineMockMap.put(gameId, num);
		}
		this.onlineMockMap = onlineMockMap;
	}

	@Override
	public void updateGameOnline(String serverIns, Map<String, Integer> onlineMap) {
		OnlineModel model = this.onlineServerMap.get(serverIns);
		if(model == null) {
			model = new OnlineModel();
			onlineServerMap.putIfAbsent(serverIns, model);
			model = this.onlineServerMap.get(serverIns);
		}
		model.gameMap = onlineMap;
		
		ConcurrentMap<String, Integer> tmpMap = new ConcurrentHashMap<String, Integer>();
		for(OnlineModel m : this.onlineServerMap.values()) {
			for(Entry<String, Integer> e : m.gameMap.entrySet()) {
				String gameId = e.getKey();
				int count = e.getValue();
				if(tmpMap.containsKey(gameId)) {
					tmpMap.put(gameId, count + tmpMap.get(gameId));
				} else {
					tmpMap.put(gameId, count);
				}
			}
		}
		this.onlineMap = tmpMap;
		this.mock();
	}
	
	public static void main(String[] args) {
		Map<String, Integer> map = new HashMap<String,Integer>();
		map.put("1", 1);
		map.put("2", 1);
		System.out.println(new Gson().toJson(map));
	}

	@Override
	public String getComponentName() {
		return "status";
	}
}
