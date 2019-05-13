package com.buding.common.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.cluster.service.ServerManager;

/**
 * 服务管理器
 * 
 * @author admin
 * 
 */
public class Server implements InitializingBean {
	@Autowired
	ServerManager serverManager;
	
	List<ServerComponent> componentList = new ArrayList<ServerComponent>();
		
	public List<ServerComponent> getComponentList() {
		return componentList;
	}

	public void setComponentList(List<ServerComponent> componentList) {
		this.componentList = componentList;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		serverManager.serverList.add(this);
	}
	
	public void restartAllServerComponent() throws Exception {
		stopServerComponents();
		startServerComponents();
	}
	
	public void restartServerComponent(String serverName) throws Exception {
		ServerComponent server = getServerComponentByName(serverName);
		if(server != null) {
			server.restart();
		}
	}
	
	public ServerComponent getServerComponentByName(String name) {
		if(componentList == null) {
			return null;
		}
		
		for(ServerComponent server : componentList) {
			if(server.getName().equals(name)) {
				return server;
			}
		}
		return null;
	}
	
	public NodeState stopServerComponents() throws Exception{
		NodeState state = NodeState.STOPED;
		for(ServerComponent sc : componentList) {
			sc.stop();
			if(sc.getState() != NodeState.STOPED) {
				state = NodeState.STOPING;
			}
		}
		return state;
	}

	public NodeState startServerComponents() throws Exception {
		NodeState state = NodeState.RUNNING;
		for(int i = componentList.size() - 1; i >=0; i--) {
			ServerComponent sc = componentList.get(i);
			sc.start();
			if(sc.getState() != NodeState.RUNNING) {
				state = NodeState.STARTING;
			}
		}
		return state;
	}
	
	public void update() {
		//TODO
	}
	
	public String getStatus() {
		StringBuilder sb = new StringBuilder();
		for(int i = componentList.size() - 1; i >=0; i--) {
			ServerComponent sc = componentList.get(i);
			sb.append(sc.getName()).append("\r\n");
			sb.append(sc.getStatusDesc());
		}
		return sb.toString();
	}
	
	public String getServerName() {
		return "UNKNOWN";
	}
}
