package com.buding.battle.common.network.session;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.logic.module.common.AwayStatus;
import com.buding.battle.logic.module.common.OnlineStatus;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.battle.logic.network.module.Module;
import com.buding.common.network.session.BaseSession;
import com.buding.common.network.session.SessionStatus;
import com.buding.db.model.User;
import com.buding.hall.config.DeskConfig;
import com.buding.poker.constants.PokerConstants;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BattleSession extends BaseSession { 
	private Logger logger = LogManager.getLogger(getClass());

	public transient Module<PacketType, PacketBase> currentModule;
	public PlayerInfo player;
	public User user;	
	private transient Game game;//当前所在的游戏
	private transient Match match;//当前所在的赛场
	private transient Room room;//当前所在的房间
	private transient Map<String,CommonDesk> deskMap = new ConcurrentHashMap();//当前所在的桌子
	public OnlineStatus onlineStatus = OnlineStatus.ONLINE;
	public AwayStatus awayStatus = AwayStatus.BACK;
	private PlayerStatus status = PlayerStatus.IN_HALL; //当前玩家的状态
	public ConcurrentLinkedHashMap<String, String> recentDeskId = new ConcurrentLinkedHashMap.Builder<String, String>().maximumWeightedCapacity(3).build();
	public List<Integer> debugData;//调试数据
	
	@Override
	public boolean isCanRemove() {
		//已经计划移除&&不在游戏中&&等待时间3分钟已到
//		return sessionStatus == SessionStatus.INVALID && status != PlayerStatus.GAMING && System.currentTimeMillis() - planRemoveTime >= 3*60*1000;
		return sessionStatus == SessionStatus.INVALID && status != PlayerStatus.GAMING && System.currentTimeMillis() - planRemoveTime >= DeskConfig.gamePauseTimeout * 1000;
	}
	
	public void enterRoom(Room room) {
		if(room != null) {
			this.enterMatch(room.getParent());
		}
		
		this.room = room;		
	}
	
	public void enterMatch(Match match) {
		if(match != null) {
			this.game = match.getParent();
		}
		this.match = match;		
	}
	
	public void enterDesk(CommonDesk desk) {
		if(desk != null) {
			this.enterRoom((Room)desk.getParent());
		}
		this.deskMap.put(desk.getDeskID(),desk);
	}
	
	public void leaveDesk(CommonDesk desk) {
	    logger.info("leaveDesk----"+desk.getDeskID());
		this.deskMap.remove(desk.getDeskID());
		this.status = PlayerStatus.UNREADY;
		if(getPlayingOrReadyDesk(userId) == null) this.player.position = -1;
	}
	
	public void leaveMatch() {
        this.room = null;
        this.match = null;
		this.game = null;
	}

    public void setDeskMap(Map<String, CommonDesk> deskMap) {
        this.deskMap = deskMap;
    }

    public Game getGame() {
		return game;
	}

	public Match getMatch() {
		return match;
	}

	public Room getRoom() {
		return room;
	}

	public Map<String,CommonDesk> getDeskMap() {
		return deskMap;
	}
	
	public PlayerStatus getStatus() {
		return status;
	}
	
	public void setStatus(PlayerStatus status, StatusChangeReason reason) {
		logger.info("act=setStatus;userid={};status={};reason={};sessionid={};gameid={};matchid={};roomid={}", userId, status, reason, sessionId, getGameId(), getMatchId(), getRoomId());
		this.status = status;
	}

	public String getGameId() {
		return this.game == null ? null : this.game.getId();
	}

	public String getMatchId() {
		return this.match == null ? null : this.match.getId();
	}

	public String getRoomId() {
		return this.room == null ? null : this.room.getRoomId();
	}

	public boolean isAdmin() {
		return user.getRole() != null && (user.getRole() & 1) == 1;
	}

	public CommonDesk getPlayingDesk(int userId) {
		for(CommonDesk desk : deskMap.values()){
			if(desk.isStart() && desk.getplayerIdList().contains(userId))return desk;
		}
		return null;
	}
	public CommonDesk getChuanTongErBaPlayingDesk(int userId) {
		for(CommonDesk desk : deskMap.values()){
            if (!desk.getplayerIdList().contains(userId) && desk.getNotSitPlayerIdList().contains(userId) && desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                return desk;
            }
		}
		return null;
	}

	public CommonDesk getPlayingOrReadyDesk(int userId){
		for(CommonDesk desk : deskMap.values()){
			if(desk.getplayerIdList().contains(userId))return desk;
			if (desk.getErBaXiaZhuOrBankerPlayerIds().contains(userId)) return desk;
		}
		return null;
	}

    public List<CommonDesk> getNotSitDesk(int userId) {
		List<CommonDesk> list = new ArrayList<>();
		for(CommonDesk desk : deskMap.values()) {
			if (!desk.getplayerIdList().contains(userId) && desk.getNotSitPlayerIdList().contains(userId) && !desk.getErBaXiaZhuOrBankerPlayerIds().contains(userId)) {
				list.add(desk);
			}
		}
        return list;
    }
}
