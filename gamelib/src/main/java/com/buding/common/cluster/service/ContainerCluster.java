package com.buding.common.cluster.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import redis.clients.jedis.Tuple;

import com.buding.common.cache.RedisClient;
import com.buding.common.cluster.model.ServerModel;
import com.buding.common.loop.Looper;
import com.buding.common.loop.ServerLoop;
import com.buding.common.server.NodeState;
import com.buding.common.server.ServerConfig;
import com.buding.common.zk.client.ZkClientBase;
import com.buding.common.zk.serializer.ZkStringSerializer;
import com.google.gson.Gson;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@SuppressWarnings("all")
public class ContainerCluster extends ZkClientBase implements InitializingBean, ICluster, Looper {
	public String containerType;

	@Autowired
	public ServerManager serverMgr;

	@Autowired
	public RedisClient redisClient;

	@Autowired
	ServerConfig serverConfig;

	@Autowired
	@Qualifier("ServerBgTaskLoop")
	ServerLoop serverLoop;

	@Override
	public void afterPropertiesSet() throws Exception {
		initZkClient(new ZkStringSerializer());

		register();

		tryStart();

		serverLoop.register(this);
	}

	private void register() {
		try {
			if (zkClient.exists("/container") == false) {
				zkClient.createPersistent("/container");
			}
			if (zkClient.exists("/container/" + containerType) == false) {
				zkClient.createPersistent("/container/" + containerType);
			}
			zkClient.createEphemeralSequential("/container/" + containerType + "/node", serverConfig.instanceId);
			logger.info("create container : " + containerType);
		} catch (Exception e) {
			logger.error("", e);
		}

		// List<ServerModel> list = getAllServer(containerType);
		// logger.info("clusterContainerList[{}]:{}", containerType, new
		// GsonBuilder().setPrettyPrinting().create().toJson(list));
	}

	// @Override
	// public List<ServerModel> getAllServer(String serverType) {
	// List<String> childrenList = zkClient.getChildren("/server/" +
	// serverType);
	// List<ServerModel> list = new ArrayList<ServerModel>();
	// for (String node : childrenList) {
	// String data = zkClient.readData("/server/" + serverType + "/" + node,
	// true);
	// if (data != null) {
	// ServerModel model = new ServerModel();
	// model.instance = data;
	// list.add(model);
	// }
	// }
	// return list;
	// }

	@Override
	public void stop(String instance) {
		if (this.serverConfig.instanceId.equals(instance)) {
			serverConfig.serverState = NodeState.STOPING;

			for (String serverType : serverConfig.serverMap.keySet()) {
				String key = "serverSet_" + serverType;
				String member = serverConfig.instanceId + "_" + serverConfig.serverMap.get(serverType);
				redisClient.zrem(key, member);
			}

			tryStop();
		}
	}

	private void tryStop() {
		NodeState state = NodeState.STOPED;
		if (serverMgr != null) {
			try {
				state = serverMgr.tryStop();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		serverConfig.serverState = state;
		serverMgr.update();
	}

	@Override
	public void start(String instanceId) {
		if (this.serverConfig.instanceId.equals(instanceId)) {

			serverConfig.serverState = NodeState.STARTING;

			tryStart();
		}
	}

	private void tryStart() {
		NodeState state = NodeState.RUNNING;
		if (serverMgr != null) {
			try {
				state = serverMgr.tryStart();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		serverConfig.serverState = state;
		
		if(state == NodeState.RUNNING) {
			logger.info("{} 服务器已恢复对外服务", serverConfig.instanceId);
		}
		serverMgr.update();
	}

	@Override
	public void loop() throws Exception {
		if (serverConfig.serverState == NodeState.STARTING) {
			logger.info("{} 服务器正在恢复对外服务", serverConfig.instanceId);
			tryStart();
		}

		if (serverConfig.serverState == NodeState.STOPING) {
			logger.info("{} 服务器正在停止对外服务", serverConfig.instanceId);
			tryStop();
		}

		serverMgr.update();
		
		if (serverConfig.serverState == NodeState.STOPED) {
			logger.info("{} 服务器已停止对外服务", serverConfig.instanceId);
		}
	}

	public void setContainerType(String containerType) {
		this.containerType = containerType;
	}

	@Override
	public ServerModel getFreeServer(String serverType) {
		Set<Tuple> servers = redisClient.zrangeWithScores("serverSet_" + serverType, 0, 0);
		if (servers.isEmpty()) {
			return null;
		}
		Tuple t = servers.iterator().next();
		String server = t.getElement();
		String addr = server.split("_")[1];
		ServerModel model = new ServerModel();
		model.addr = addr;
		model.instanceId = server.split("_")[0];
		model.serverType = serverType;
		String online = redisClient.hget("serverMap_" + serverType, server);
		model.onlineNum = (int)t.getScore();
		return model;
	}

	@Override
	public List<ServerModel> getAllServer(String serverType) {
		Map<String, String> mapAll = new HashMap<String, String>();
		if(serverType == null) {
			mapAll.putAll(redisClient.hgetAll("serverMap_hall"));
			mapAll.putAll(redisClient.hgetAll("serverMap_msg"));
			mapAll.putAll(redisClient.hgetAll("serverMap_battle"));
		} else {
			mapAll.putAll(redisClient.hgetAll("serverMap_"+serverType));
		}
		
		List<ServerModel> list = new ArrayList<ServerModel>();
		for(Entry<String, String> e : mapAll.entrySet()) {
			ServerModel model = new Gson().fromJson(e.getValue(), ServerModel.class);
			list.add(model);
		}
		return list;
	}

	@Override
	public String getStatus() {
		return serverMgr.getStatus();
	}
}
