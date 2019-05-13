package com.buding.battle.logic.module.game.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.match.MultiMatchImpl;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.common.result.Result;
import com.buding.hall.module.game.model.DeskModel;

public interface GameService {

	public Result enroll(BattleSession session, BattleContext ctx);

	public void requestReady(BattleSession session);

	public void changeDesk(BattleSession session);
	
	public void quickStart(BattleSession session);
	
	public void checkCoinInMath(BattleSession session, Match match);
	
	public ConcurrentMap<String, Game> gameMap();
	
	public Map<String, Map<String, Map<String, Map<String, Double>>>> getDeskDelayStatus();
	
	public List<RoomOnlineModel> getRoomOnlineList();
	
	public Game getById(String gameId);
	
	public List<DeskModel> getDeskList();
	
	public void reload();
	
	public CommonDesk findDesk(String gameId, String matchId, String deskId);
	
	public DeskModel searchDesk(int playerId);

    List<MultiMatchImpl> getMultiMatchPlayer(int userId);

	MultiMatchImpl getMultiMatch(String gameId, String matchId, int playerId);

	List<DeskModel> getClubCommonDesk(long clubId);

	List<DeskModel> getClubJiFenDesk(long clubId);
}
