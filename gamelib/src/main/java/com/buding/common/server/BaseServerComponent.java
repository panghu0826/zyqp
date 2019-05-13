package com.buding.common.server;

import org.springframework.beans.factory.InitializingBean;

public abstract class BaseServerComponent implements ServerComponent, InitializingBean {
	Server server;
	
	private NodeState state;
	
	public void afterPropertiesSet() throws Exception {
		server.componentList.add(this);
	}
	
	public void setServer(Server server) {
		this.server = server;
	}

	@Override
	public void start() throws Exception {
		this.state = NodeState.RUNNING;
	}

	@Override
	public void stop() throws Exception {
		this.state = NodeState.STOPED;
	}

	@Override
	public void restart() throws Exception {
		this.state = NodeState.RUNNING;
	}

	@Override
	public NodeState getState() {
		return this.state;
	}
}
