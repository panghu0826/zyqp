package com.buding.battle.logic.module.cluster;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.hall.module.cluster.model.UserGaming;
import com.buding.hall.module.ws.HallPortalService;

@Component
public class ClusterStubService {
	private Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	HallPortalService portalService;
	
	public UserGaming checkPlayInOtherGame(BattleSession session, String gameId) {
		try {
			UserGaming gaming = portalService.getUserGaming(session.userId);
			if(gaming != null && gaming.gameId.equals(gameId) == false) {
				return gaming;
			}
			return null;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
	
	public void notifyUserPlaying(int userId, String gameId, String instanceId) {
		try {
			portalService.updateUserGaming(userId, gameId, instanceId);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public void removeUserPlaying(int userId, String gameId, String instanceId) {
		try {
			portalService.removeUserGaming(userId, gameId, instanceId);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
}
