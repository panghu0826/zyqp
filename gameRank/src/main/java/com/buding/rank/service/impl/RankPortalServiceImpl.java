package com.buding.rank.service.impl;

import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.ws.RankPortalService;
import com.buding.rank.network.RankSession;
import com.buding.rank.network.RankSessionManager;
import org.springframework.beans.factory.annotation.Autowired;

public class RankPortalServiceImpl implements RankPortalService {
	@Autowired
	private RankSessionManager rankSessionManager;

	@Autowired
	private ConfigManager configManager;


	@Override
	public void stopService(String instanceId) {
		
	}

	@Override
	public void startService(String instanceId) {

	}

	@Override
	public void closeSocket(int playerId) {
		RankSession session = rankSessionManager.getIoSession(playerId);
		if(session != null && session.channel.isOpen()){
			session.channel.close();
		}
	}
}
