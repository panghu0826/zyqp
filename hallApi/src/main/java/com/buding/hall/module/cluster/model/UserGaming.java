package com.buding.hall.module.cluster.model;

import java.io.Serializable;

public class UserGaming implements Serializable {
	private static final long serialVersionUID = -8011061374263995942L;
	
	public int userId;
	public String gameId;
	public String serverInstanceId;
	public long startGamingTime;
}	
