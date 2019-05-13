package com.buding.battle.logic.module.game;

import java.util.List;
import java.util.Map;

import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.EnrollResult;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.match.MultiMatchImpl;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.hall.config.GameConfig;
import com.buding.hall.module.game.model.DeskModel;

public interface Game {
	public int getPlayerCount();
	public int getMaxPlayerCount();
	public Match getMatch(String matchId);
	public void init(GameConfig config);
	public EnrollResult enroll(BattleContext ctx);
	public Map<String, Match> getMatchMap();
	public String getId();
	public String getName();
	public Map<String, Integer> getPlayerMap();
	public Map<String, Map<String, Map<String, Double>>> getDeskDelayStatus();
	
	//统计需求
	public List<DeskModel> getDeskList();
	public List<DeskModel> getClubCommonDesk(long clubId);
	public List<DeskModel> getClubJiFenDesk(long clubId);
	List<RoomOnlineModel> getRoomOnlineList();
	
	public DeskModel findDesk(int playerId);

	public void triggerByTime();

	List<MultiMatchImpl> findMultiMatch(int userId);

	MultiMatchImpl getMultiMatch(String matchId,int playerid);
}
