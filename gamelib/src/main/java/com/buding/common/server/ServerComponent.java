package com.buding.common.server;
public interface ServerComponent {
	public void start() throws Exception;
	public void stop() throws Exception;
	public void restart() throws Exception;
	public String getName();
	public NodeState getState();
	
	public String getStatusDesc();
}