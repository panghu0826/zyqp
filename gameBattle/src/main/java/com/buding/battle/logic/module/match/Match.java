package com.buding.battle.logic.module.match;

import java.util.List;
import java.util.Map;

import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.EnrollResult;
import com.buding.battle.logic.module.common.EnterRoomResult;
import com.buding.battle.logic.module.common.ParentAware;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.common.result.Result;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.module.game.model.DeskModel;

public interface Match extends ParentAware<Game> {
	/**
	 * 申请进入房间,返回房间id
	 * @return
	 */
	public EnrollResult enroll(BattleContext ctx);
	
	/**
	 * 进入房间,返回桌子id
	 * @return
	 */
	public EnterRoomResult enterRoom(BattleContext ctx);
	
	/**
	 * 玩家推出
	 * @param playerId
	 */
	public Result playerExit(int playerId, PlayerExitType reason);
		
	/**
	 * 玩家已退出
	 * @param playerId
	 */
	public void onPlayerCountIncr(int playerId);
	
	public void onPlayerCountDecr(int playerId);
	
	/**
	 * 是否场地已满
	 * @return
	 */
	public boolean isFull();
	
	/**
	 * 是否场地为空
	 * @return
	 */
	public boolean isEmpty();
	
	/**
	 * 初始化比赛
	 * @param conf
	 */
	public void init(MatchConfig conf);
	
	/**
	 * 销毁比赛
	 */
	public void destroy();
	
	/**
	 * 根据房间id获取房间
	 * @param roomId
	 * @return
	 */
	public Room getRoom(String roomId);
	
	public String getId();
	public void setId(String id);
	public void setStartTime(long startTime);
	public void setInitTime(long initTime);
	public String getName();
	
	public MatchConfig getMatchConfig();
	
	public int getPlayerCount();
	
	public Map<String, Integer> getPlayerMap();
	public Map<String, Map<String, Double>> getDeskDelayStatus();
	public List<DeskModel> getDeskList();
	public List<DeskModel> getClubJiFenDesk(long clubId);
	public List<DeskModel> getClubCommonDesk(long clubId);
	public List<RoomOnlineModel> getRoomOnlineList();
	
	public CommonDesk findDesk(String deskId);
	
	public DeskModel findDesk(int playerId);

	void triggerByTime();

    Result unEnroll(int playerId);
}
