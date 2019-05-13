package com.buding.cluster.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.buding.cluster.model.ServerCluster;
import com.buding.common.admin.component.BaseComponent;
import com.buding.common.result.TResult;
import com.buding.common.util.IOUtil;
import com.buding.hall.module.cluster.model.ServerState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class GameStatusService extends BaseComponent implements InitializingBean {	
	ConcurrentMap<String, ServerCluster> gameClusterMap = new ConcurrentHashMap<String, ServerCluster>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initServer2Cluster();
	}

	public TResult<ServerState> getServerAddr(String gameId, String instanceId) {
		ServerCluster sm = gameClusterMap.get(gameId);
		if(sm == null) {
			return TResult.fail1("游戏不存在");
		}
		
		ServerState addr = null;
		if(instanceId != null) {
			addr = sm.getByInstanceId(instanceId);
		} else {
			addr = sm.getServerAddr(gameId);
		}
		if(addr == null) {
			return TResult.fail1("服务器已满");
		}
		return TResult.sucess1(addr);
	}
	
	public void updateServerStatus(String serverInstanceId, String gameId, ServerState server) {
		ServerCluster sm = gameClusterMap.get(gameId);
		if(sm == null) {
			sm = new ServerCluster();
			gameClusterMap.putIfAbsent(gameId, sm);
			sm = gameClusterMap.get(gameId);
		}
		
		sm.updateServerStatus(serverInstanceId, server);
	}
	
	public void initServer2Cluster() throws Exception {
		String json = IOUtil.getClassPathResourceAsString("/server.state", "utf8");
		List<ServerState> serverList = new Gson().fromJson(json, new TypeToken<List<ServerState>>(){}.getType());
		for(ServerState state : serverList) {
			if(state.serverOnline) {
				updateServerStatus(state.instanceId, state.gameId, state);
			}
		}
	}

	@Override
	public String getComponentName() {
		return "cluster";
	}
	
}
