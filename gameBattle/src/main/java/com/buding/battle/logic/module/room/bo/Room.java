package com.buding.battle.logic.module.room.bo;

import java.util.List;
import java.util.Map;

import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.EnterRoomResult;
import com.buding.battle.logic.module.common.ParentAware;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import com.buding.battle.logic.module.match.Match;
import com.buding.common.monitor.Monitorable;
import com.buding.common.result.Result;
import com.buding.hall.config.RoomConfig;
import com.buding.hall.module.game.model.DeskModel;

/**
 * 房间
 * @author Administrator
 *
 */
public interface Room extends Monitorable, ParentAware<Match> {
	/**
	 * 在此房间报名
	 * @param playerId
	 * @return
	 */
	public Result playerEnroll(BattleContext ctx);
	
	/**
	 * 进入房间，如果房间未满,返回桌子id和座位号
	 * @return
	 */
	public EnterRoomResult playerTryEnter(BattleContext ctx);
	
	/**
	 * 找位置坐下
	 * @param ctx
	 * @return
	 */
	public EnterRoomResult playerTrySit(BattleContext ctx);
	
	/**
	 * 房间是否已满
	 */
	public boolean isFull();
	
	/**
	 * 房间是否已空
	 */
	public boolean isEmpty();
	
	/**
	 * 房间ID
	 * @return
	 */
	public String getRoomId();
	
	/**
	 * 离开房间
	 * @param playerId
	 */
	public void playerExit(int playerId, PlayerExitType reason);
	
	/**
	 * 换桌
	 * @param playerId
	 * @return
	 */
	public CommonDesk changeDesk(int playerId);
	
	public RoomConfig getRoomConfig();
	
	public void init(RoomConfig conf);
	
	public int getPlayerCount();
	
	public Map<String, Integer> getPlayerMap();
	
	public Map<String, Double> getDeskDelayStatsu();
	
	public CommonDesk getById(String id);
	
	public DeskModel findDesk(int playerId);
	
	public List<DeskModel> getDeskList();
	public List<DeskModel> getClubCommonDesk(long clubId);
	public List<DeskModel> getClubJiFenDesk(long clubId);

}
