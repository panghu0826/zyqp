package com.buding.hall.module.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buding.api.player.PlayerInfo;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class DeskModel implements Serializable {
	private static final long serialVersionUID = -8011061375263995942L;
	
	public String gameId;
	public String gameName;
	public String matchId;
	public String matchName;
	public String roomId;
	public String roomName;
	public String deskId;
	public List<PlayerInfo> players = new ArrayList<PlayerInfo>();
	public String deskStatus;
	public int gameCount;
	public String instanceId;
	public String createTime;
	public int wanfa;
	public int totalJuNum;
	public int limitMax;
	public long clubId;
}
