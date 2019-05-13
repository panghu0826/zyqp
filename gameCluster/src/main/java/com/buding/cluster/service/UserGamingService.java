package com.buding.cluster.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.buding.common.admin.component.BaseComponent;
import com.buding.hall.module.cluster.model.UserGaming;

@Component
public class UserGamingService extends BaseComponent {
	public ConcurrentMap<Integer, UserGaming> gamingMap = new ConcurrentHashMap<Integer, UserGaming>();
	
	public UserGaming getUserGaming(int userId) {
		UserGaming gaming = gamingMap.get(userId);
		if(isInvalidGaming(gaming)) {
			gamingMap.remove(userId);
			return null;
		}
		return gaming;
	}
	
	public void updateUserGaming(int userId, String gameId, String instanceId) {
		UserGaming game = new UserGaming();
		game.gameId = gameId;
		game.startGamingTime = System.currentTimeMillis();
		game.userId = userId;
		game.serverInstanceId = instanceId;
		gamingMap.put(userId, game);
	}
	
	public void removeUserGaming(int userId, String gameId, String instanceId) {
		UserGaming gaming = gamingMap.get(userId);
		if(gaming != null && gaming.gameId.equals(gameId) && gaming.serverInstanceId.equalsIgnoreCase(instanceId)) {
			gamingMap.remove(userId);
		}		
	}
		
	public boolean isInvalidGaming(UserGaming gaming) {
		return gaming == null || System.currentTimeMillis() - gaming.startGamingTime >= 600000;
	}

	@Override
	public String getComponentName() {
		return "gaming";
	}
	
}
