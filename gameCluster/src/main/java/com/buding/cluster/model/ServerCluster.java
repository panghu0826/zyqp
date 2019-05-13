package com.buding.cluster.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.buding.hall.module.cluster.model.ServerState;


public class ServerCluster {
	public ConcurrentMap<String, ServerState> addrMap = new ConcurrentHashMap<String, ServerState>();	
	public String gameId;
	
	public ServerState getServerAddr(String gameId) {
		for(ServerState server : addrMap.values()) {
			if(server.serverOnline == false) {
				continue;
			}
			if(server.curOnline >= server.maxOnline) {
				continue;
			}
			return server;
		}
		return null;
	}
	
	public void updateServerStatus(String serverInstanceId, ServerState server) {
		addrMap.put(serverInstanceId, server);
	}
	
	public ServerState getByInstanceId(String serverInstance) {
		return this.addrMap.get(serverInstance);
	}
}
