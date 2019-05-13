package com.buding.hall.module.status.service;

import java.util.Map;


public interface StatusService {
	//更新每个游戏的在线情况
	public void updateGameOnline(String serverIns, Map<String, Integer> onlineMap);
}
