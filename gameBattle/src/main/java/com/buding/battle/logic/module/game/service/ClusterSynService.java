package com.buding.battle.logic.module.game.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.battle.logic.module.game.Game;
import com.buding.common.server.ServerConfig;
import com.buding.hall.module.cluster.model.ServerState;
import com.buding.hall.module.cluster.service.ClusterService;

//@Component
public class ClusterSynService implements InitializingBean, Runnable {

	@Autowired
	GameService gameService;
	
	@Autowired
	ClusterService clusterService;
	
	@Autowired
	ServerConfig serverConfig;
	
	public boolean serverOnline = true;
	
	ScheduledExecutorService pool;

	@Override
	public void afterPropertiesSet() throws Exception {
		start();
	}
	
	@Override
	public void run() {
		syn();
	}

	public void start() {
		if(pool != null && pool.isShutdown() == false) {
			pool.shutdown();
		}
		pool = Executors.newScheduledThreadPool(1);
		pool.scheduleAtFixedRate(this, 10, 10, TimeUnit.SECONDS);
	}

	public void syn() {
		for (Game game : gameService.gameMap().values()) {
			ServerState st = new ServerState();
			st.curOnline = game.getPlayerCount();
			st.maxOnline = game.getMaxPlayerCount();
			st.instanceId = serverConfig.instanceId;
//			st.ip = serverConfig.serverIp;
//			st.port = serverConfig.serverPort;
			st.serverOnline = serverOnline;
			st.gameId = game.getId();
			clusterService.updateServerStatus(st);
		}
	}
}
