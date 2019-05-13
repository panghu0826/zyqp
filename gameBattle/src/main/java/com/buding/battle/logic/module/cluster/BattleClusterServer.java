package com.buding.battle.logic.module.cluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.game.service.GameService;
import com.buding.common.cache.RedisClient;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.common.cluster.model.ServerModel;
import com.buding.common.server.NodeState;
import com.buding.common.server.Server;
import com.buding.common.server.ServerConfig;
import com.google.gson.Gson;

@Component
public class BattleClusterServer extends Server {
	@Autowired
	RedisClient redisClient;
	
	@Autowired
	ServerConfig serverConfig;
	
	@Autowired
	BattleSessionManager sessionManager;
	
	@Autowired
	GameService gameService;
	
	@Override
	public void update() {
		updateServerOnlineStatus();
		updateRoomOnlineStatus();
	}

	private void updateRoomOnlineStatus() {
		String key = "RoomOnlineDetailMap";
		for(RoomOnlineModel model : this.gameService.getRoomOnlineList()) {
			redisClient.hset(key, serverConfig.instanceId+"_"+model.roomId, new Gson().toJson(model));
		}
		
		//负责更新数据
		if(serverConfig.mainServer) {
			Map<String, Integer> roomOnlineMap = new HashMap<String, Integer>();
			for(String field : redisClient.hkeys(key)) {
				String json = redisClient.hget(key, field);
				if(StringUtils.isBlank(json)) {
					continue;
				}
				RoomOnlineModel model = new Gson().fromJson(json, RoomOnlineModel.class);
				if(System.currentTimeMillis() - model.lastUpdate > 3*60*1000) {
					redisClient.hdel(key, field);
					continue;
				}
				if(roomOnlineMap.get(model.roomId) == null) {
					roomOnlineMap.put(model.roomId, model.onlineNum);
				} else {
					roomOnlineMap.put(model.roomId, model.onlineNum + roomOnlineMap.get(model.roomId));
				}
			}
			redisClient.set("RoomOnlineList", new Gson().toJson(roomOnlineMap));
			redisClient.expire("RoomOnlineList", 3*60*1000);
		}
	}
	
	private void updateServerOnlineStatus() {
		String serverType = "battle";
		ServerModel model = serverConfig.serverMap.get(serverType);
		String member = serverConfig.instanceId + "_" + model.addr;
		int score = sessionManager.getCurrentOnlineCount();
		if (ServerConfig.serverState == NodeState.RUNNING) {
			redisClient.zadd("serverSet_" + serverType, score, member);
		} else {
			redisClient.zrem("serverSet_" + serverType, member);
		}
		
		model.onlineNum = score;
		model.status = ServerConfig.serverState.toString();
		model.lastUpdate = System.currentTimeMillis();
		redisClient.hset("serverMap_" + serverType, member, new Gson().toJson(model));
		
		//负责清理已经失效的数据
		if(ServerConfig.mainServer) {
			Set<String> servers = redisClient.zrange("serverSet_" + serverType, 0, 100);
			for(String server : servers) {
				String ret = redisClient.hget("serverMap_" + serverType, server);
				if(StringUtils.isNotBlank(ret)) {
					ServerModel sm = new Gson().fromJson(ret, ServerModel.class);
					if(System.currentTimeMillis() - sm.lastUpdate < 15*1000) {
						continue;
					}
				}
				//无效的服务，清除
				redisClient.hdel("serverMap_" + serverType, server);
				redisClient.zrem("serverSet_" + serverType, server);
			}
		}
	}
}