package com.buding.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import packet.game.MsgGame.EnrollResponse;
import packet.game.MsgGame.PlayerSitSyn;
import packet.game.MsgGame.ReadySyn;
import packet.mj.MJ.GameOperPlayerActionSyn;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class GamingData {
//	public EnrollResponse enrollData;
	public boolean enroll = false;
	public boolean ready = false;
//	public ReadySyn readyData;
	public int position = -1;
//	public int playerId = -1;
	public String nickName = null;
	public Map<Integer, GameOperPlayerActionSyn> posSynMap = new HashMap<Integer, GameOperPlayerActionSyn>();
	public Map<Integer, PlayerSitSyn> idMap = new HashMap<Integer, PlayerSitSyn>();
	public Map<Integer, PlayerSitSyn> posMap = new HashMap<Integer, PlayerSitSyn>();
	public List<Integer> handCards = new ArrayList<Integer>();
	public List<Integer> cardsBefore = new ArrayList<Integer>();
	public List<Integer> downCards = new ArrayList<Integer>();
	
	public List<List<Integer>> handCardHistory = new ArrayList<List<Integer>>();
	public List<List<Integer>> downCardHistory = new ArrayList<List<Integer>>();
}
