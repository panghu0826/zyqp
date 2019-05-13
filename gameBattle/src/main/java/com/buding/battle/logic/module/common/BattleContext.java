package com.buding.battle.logic.module.common;

import java.util.*;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.db.model.UserRoom;
import org.apache.commons.lang.StringUtils;

public class BattleContext {
	public String gameId;
	public String matchId;
	public String roomId;
	public String deskId;
	public int playerId;
	public BattleSession session;
	public Map<String, Object> params = new HashMap<String, Object>();
	
	public BattleContext(int playerId, BattleSession session) {
		this.playerId = playerId;
		this.session = session;
	}
	
	public static BattleContext create(BattleSession session) {
		return new BattleContext(session.userId, session);
	}

	public String getGameId() {
		return gameId;
	}

	public BattleContext setGameId(String gameId) {
		this.gameId = gameId;
		return this;
	}

	public String getMatchId() {
		return matchId;
	}

	public BattleContext setMatchId(String matchId) {
		this.matchId = matchId;
		return this;
	}

	public String getRoomId() {
		return roomId;
	}

	public BattleContext setRoomId(String roomId) {
		this.roomId = roomId;
		return this;
	}

	public String getDeskId() {
		return deskId;
	}

	public BattleContext setDeskId(String deskId) {
		this.deskId = deskId;
		return this;
	}

	public int getPlayerId() {
		return playerId;
	}

	public BattleContext setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}

	public int getWanfa() {
		for(Map.Entry<String, Object> e:params.entrySet()){
			UserRoom room = (UserRoom) e.getValue();
			if(room.getRoomCode()==deskId){
				return StringUtils.isBlank(room.getWanfa())?0:Integer.valueOf(room.getWanfa());
			}
		}
		return 0;
	}
}

