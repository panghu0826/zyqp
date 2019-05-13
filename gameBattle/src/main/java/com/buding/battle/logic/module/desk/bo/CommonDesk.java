package com.buding.battle.logic.module.desk.bo;

import com.buding.api.desk.Desk;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.DeskStatus;
import com.buding.battle.logic.module.common.ParentAware;
import com.buding.battle.logic.module.desk.DeskGuard;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.common.monitor.Monitorable;
import com.buding.hall.config.DeskConfig;
import com.buding.hall.module.game.model.DeskModel;

import java.util.List;
import java.util.Map;

public interface CommonDesk<MsgType> extends Desk<MsgType>, Monitorable, ParentAware<Room> {   

	public int playerSitPre(BattleContext ctx);

    boolean canZhongTuNotEnter();

    public int playerSitAfter(BattleContext ctx);
	
	/**
	 * 玩家离开
	 * @param playerId
	 */
	@Deprecated
	public void playerExit(int playerId, PlayerExitType type);
	
	/**
	 * 销毁
	 */
	public void destroy(DeskDestoryReason type);
	
	/**
	 * 桌子是否已空
	 * @return
	 */
	public boolean isEmpty();
	
	/**
	 * 桌子是否已满
	 * @return
	 */
	public boolean isFull();


    /**
	 * 检查是否可以开赛
	 */
	public void tryStartGame();
	
	/**
	 * 收到用户准备数据包
	 * @param playerId
	 */
	public void onPlayerReadyPacketReceived(int playerId);
	
	/**
	 * 换桌
	 * @param playerId
	 */
	public void onPlayerChangeDeskPacketReceived(int playerId);

	void multiMatchResetAndStart(List<PlayerInfo> playerInfoList);

	/**
	 * 重连
	 * @param playerId
	 */
	public void onPlayerReconnectPacketReceived(int playerId);
	
	/**
	 * 退出游戏
	 * @param playerId
	 */
	public void onPlayerExitPacketReceived(int playerId);
	
	/**
	 * 离开游戏
	 * @param playerId
	 */
	public void onPlayerAwayPacketReceived(int playerId);
	
	/**
	 * 回到游戏
	 * @param playerId
	 */
	public void onPlayerComeBackPacketReceived(int playerId);
	
	/**
	 * 断线
	 * @param playerId
	 */
	public void onPlayerOfflinePacketReceived(int playerId);
	
	/**
	 * 收到玩家解散请求
	 * @param playerId
	 */
	public void onPlayerDissVotePacketReceived(int playerId, boolean agree);
	
	/**
	 * 收到游戏数据包
	 * @param playerID
	 * @param content
	 */
	public void onGameMsgPacketReceived(int playerID, MsgType content);
	
	/**
	 * 收到聊天数据包
	 * @param playerID
	 * @param content
	 */
	public void onChatMsgPacketReceived(int playerID, int contentType, byte[] content);

	void playerExitPosNotExitRoom(int playerId, int deskPos);

	public void onPlayerHangupPacketReceived(int playerID);

	public void onStartGameMsgReceived(int playerId);

	public void onPlayerCancelHangupPacketReceived(int playerID);
	
	/**
	 * 收到踢人数据包
	 * @param playerId
	 * @param targetPlayerId
	 */
	public void onKickoutPacketReceived(int playerId, int targetPlayerId);
	
	/**
	 * 获取玩家数量
	 * @return
	 */
	public int getPlayerCount();
	
	/**
	 * 获取桌子状态
	 * @return
	 */
	public DeskStatus getStatus();
	
	/**
	 * 是否自动换桌
	 * @return
	 */
	public boolean isAutoChangeDesk();
	
	/**
	 * 重置桌子状态
	 */
	public void reset();
	
	public List<PlayerInfo> getPlayers();
	
	public DeskConfig getDeskConfig();
	
	public void setDeskConfig(DeskConfig conf);
	
	public void setDeskOwner(int ownerId);
	
	public double getDeskDelayStatus();

    public void onDismissPacketRequest();

    public void setDeskId(String id);
	
	public boolean isAutoReady();
	
	public void onSetGamingDataReq(String json);
	
	public void markAsAdminUse();
	
	public boolean isAdminUse();
	
	public String dumpGameData(long videoId);
	
	public int getGameCount();
	
	public DeskModel getDeskInfo();
	
	public String printGameDetail();
	
	public boolean isHasPlayer(int playerId);

    long genVideoId();

	List<Integer> getplayerIdList();

	void onPlayerExitPosNotExitRoomRequstReceived(int playerId, int deskPos);

	void onPlayerSitMsgReceived(int userId, int deskPos);

	public int getTotalQuan();
	public int getWanfa();
	DeskGuard getGuard();
	long getClubId();
	int getClubRoomType();
	int getEnterScore();
	int getCanFufen();
	int getChoushuiScore();
	int getChoushuiNum();
	int getZengsongNum();
	int getQiangZhuangNum();
	Map<Integer, Integer> getNiuFanStr();

    boolean isStart();

	List<Integer> getNotSitPlayerIdList();

	boolean isClubJiFenDesk();
	List<Integer> getfuLiPlayerList(String gameId);

    void onViewGuanZhanReqPacketReceived(int userId);

	void gameCountIncr();

	public int getErBaGameType();

	void erBaXiaZhuOrConfirmBanker(PlayerInfo pl);

	List<Integer> getErBaXiaZhuOrBankerPlayerIds();

	int getStarterId();
}
