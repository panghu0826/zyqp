package com.buding.hall.module.cluster.model;

import java.io.Serializable;

public class ServerState implements Serializable {
	private static final long serialVersionUID = -8011061375263995942L;
	
	public String ip;
	public int port;
	public long maxOnline;
	public long curOnline;
	public boolean serverOnline = true;
	public String instanceId;
	public String gameId;
}
