package com.buding.cluster.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.hall.module.cluster.model.ServerState;
import com.buding.hall.module.cluster.model.UserGaming;
import com.buding.hall.module.cluster.service.ClusterService;

public class ClusterExternalService implements ClusterService {
	@Autowired
	UserGamingService userGamingService;

	@Autowired
	GameStatusService gameStatusService;

	public void updateUserGaming(int userId, String gameId, String serverInstanceId) {
		this.userGamingService.updateUserGaming(userId, gameId, serverInstanceId);
	}

	public void removeUserGaming(int userId, String gameId, String serverInstanceId) {
		this.userGamingService.removeUserGaming(userId, gameId, serverInstanceId);
	}

	public void updateServerStatus(ServerState server) {
		this.gameStatusService.updateServerStatus(server.instanceId, server.gameId, server);
	}

	@Override
	public UserGaming getUserGaming(int userId) {
		return this.userGamingService.getUserGaming(userId);
	}
	
	
}
