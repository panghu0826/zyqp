package com.buding.common.cluster.service;

import java.util.ArrayList;
import java.util.List;

import com.buding.common.server.NodeState;
import com.buding.common.server.Server;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class ServerManager {
	public List<Server> serverList = new ArrayList<Server>();
	
	public NodeState tryStop() throws Exception {
		NodeState state = NodeState.STOPED;
		for(Server server : serverList) {
			NodeState tmpState = server.stopServerComponents();
			if(tmpState != NodeState.STOPED) {
				state = tmpState; 
			}
		}
		return state;
	}
	
	public NodeState tryStart() throws Exception {
		NodeState state = NodeState.RUNNING;
		for(Server server : serverList) {
			NodeState tmpState = server.startServerComponents();
			if(tmpState != NodeState.RUNNING) {
				state = tmpState; 
			}
		}
		return state;
	}
	
	public void update() {
		for(Server server : serverList) {
			server.update();
		}
	}
	
	public String getStatus() {
		StringBuilder sb = new StringBuilder();
		for(Server server : serverList) {
			sb.append(server.getServerName()).append("\r\n");
			sb.append(server.getStatus());
		}
		return sb.toString();
	}
}
