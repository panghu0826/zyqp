package com.buding.msg.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.common.cache.RedisClient;
import com.buding.common.cluster.model.ServerModel;
import com.buding.common.server.NodeState;
import com.buding.common.server.Server;
import com.buding.common.server.ServerConfig;
import com.buding.msg.network.MsgSessionManager;
import com.google.gson.Gson;

@Component
public class MsgClusterServer extends Server {
	@Autowired
	RedisClient redisClient;

	@Autowired
	ServerConfig serverConfig;

	@Autowired
	MsgSessionManager sessionManager;

	@Override
	public void update() {
		String serverType = "msg";
		ServerModel model = serverConfig.serverMap.get(serverType);
		String member = serverConfig.instanceId + "_" + model.addr;
		int score = sessionManager.getCurrentOnlineCount();
		if (ServerConfig.serverState == NodeState.RUNNING) {
			redisClient.zadd("serverSet_" + serverType, score, member);
		} else {
			redisClient.zrem("serverSet_" + serverType, member);
		}

//		ServerModel model = new ServerModel();
//		model.addr = addr;
//		model.instanceId = serverConfig.instanceId;
		model.onlineNum = score;
//		model.serverType = serverType;
		model.status = ServerConfig.serverState.toString();
		model.lastUpdate = System.currentTimeMillis();
		redisClient.hset("serverMap_" + serverType, member, new Gson().toJson(model));
	}
}