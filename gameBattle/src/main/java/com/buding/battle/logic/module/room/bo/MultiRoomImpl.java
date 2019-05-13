package com.buding.battle.logic.module.room.bo;

import com.buding.api.game.Game;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.*;
import com.buding.battle.logic.module.desk.DeskGenerator;
import com.buding.battle.logic.module.desk.bo.*;
import com.buding.battle.logic.module.desk.listener.DeskListener;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.room.RoomGuard;
import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.hall.config.DeskConfig;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.config.RoomConfig;
import com.buding.hall.module.game.model.DeskModel;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class MultiRoomImpl extends BaseParent<Match> implements Room, DeskListener, DeskGenerator {
	transient Logger logger = LogManager.getLogger(getClass());

	protected RoomGuard guard;

	public int playerCount = 0;
	public int deskCount = 0;
	public transient RoomConfig conf;
	public String roomId;

	public MultiRoomImpl(Match parent) {
		super(parent);
	}

	@Override
	public synchronized EnterRoomResult playerTryEnter(BattleContext ctx) {
		if (isFull()) {
			logger.error("act=playerTryEnter;error=roomfull;playerId={};roomId={};deskId={};", ctx.playerId, roomId, getParent().getId());
			return EnterRoomResult.fail("房间已满");
		}

		// 0. 推送赛场id
		PushService.instance.pushMatchInfo(ctx.playerId, this.getParent().getId(), null);

		// 1.推送房间信息
		PushService.instance.pushRoomInfo(ctx.playerId, this.roomId, null);
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
		session.enterRoom(this);

		return EnterRoomResult.ok(null, -1);
	}

	@Override
	public synchronized EnterRoomResult playerTrySit(BattleContext ctx) {
		try {
			// 就位
			TResult<CommonDesk> ret = applyDesk(ctx);
			if(ret.isFail()) {
				logger.error("act=playerTrySit;error={};playerId={};roomId={};", ret.msg, ctx.playerId, ctx.roomId);
				return EnterRoomResult.fail(ret.msg);
			}
			
			CommonDesk desk = ret.t;
			if (desk == null) {
				logger.error("player {} enter room {} fail, apply desk return null;", ctx.playerId, ctx.roomId);
				return EnterRoomResult.fail("无法分配桌子");
			}
			if(desk.getStatus() == DeskStatus.DESTROYED) {
				logger.error("act=playerTrySit;error=applyDestoryedDesk;deskId={};", desk.getDeskID());
				return EnterRoomResult.fail("桌子状态出错");
			}

			int position = desk.playerSitPre(ctx);

			if (position == -1) {
				logger.info("act=playerTrySit;error=deskFull;playerId={};deskId={};", ctx.playerId, desk.getDeskID());
				return EnterRoomResult.fail("桌子已满");
			}
			
//			//推送正在游戏的消息
//			int wanfa = desk instanceof MJDesk ? ((MJDesk)desk).getWanfa() : 0; //TODO 不好的设计
//			PushService.instance.pushPlayerGamingInfo(ctx.session.userId, ctx.gameId, ctx.matchId, ctx.getRoomId(), desk.getDeskID()+"", wanfa);
			
			ctx.setDeskId(desk.getDeskID());
			ctx.session.enterDesk(desk);
			return EnterRoomResult.ok(desk.getDeskID(), position);
		} catch (Exception e) {
			logger.error("act=playerTrySit;error=exception;", e);
			return EnterRoomResult.fail("无法分配桌子");
		}
	}

	public synchronized TResult<CommonDesk> applyDesk(BattleContext ctx) throws Exception {
		return guard.applyDesk(ctx);
	}

	@Override
	public boolean isFull() {
		return playerCount >= conf.deskCountLimit * getMatchConf().conditionInfo.deskConf.seatSizeUpper;
	}

	@Override
	public boolean isEmpty() {
		return playerCount == 0;
	}

	@Override
	public String getRoomId() {
		return roomId;
	}

	@Override
	public void playerExit(int playerId, PlayerExitType reason) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		CommonDesk desk = session.getPlayingOrReadyDesk(playerId);
		if (desk == null) {
			// playerCount--;
			// this.getParent().onPlayerCountDecr(playerId);
			return;
		}
		desk.playerExit(playerId, reason);
	}

	@Override
	public void onPlayerSit(CommonDesk desk, PlayerInfo player) {
		playerCount++;
		this.getParent().onPlayerCountIncr(player.playerId);
		guard.playerSit(desk, player);
	}

	@Override
	public void onPlayerLeave(CommonDesk desk, PlayerInfo player) {
		playerCount--;
		this.getParent().onPlayerCountDecr(player.playerId);
		guard.playerLeave(desk, player);
	}

	@Override
	public void onDeskGameStart(CommonDesk desk, Game game) {
		guard.gameStart(desk);
	}

	@Override
	public void onDeskGameFinish(CommonDesk desk, Game game) {
		this.destroyDesk(desk.getDeskID(), DeskDestoryReason.GAME_OVER);
	}

	@Override
	public void onPlayerReady(CommonDesk desk, PlayerInfo player) {

	}

	@Override
	public void onDeskDestroy(CommonDesk desk) {
		guard.destroyDesk(desk);
		deskCount--;
	}

	@Override
	public Result playerEnroll(BattleContext ctx) {
		if (!needCheckEnroll(ctx)) {
			return Result.success();
		}
		return Result.success();
	}

	protected int getCurrencyAmount(BattleContext ctx) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
		int coin = session.player.coin;
		return coin;
	}

	/**
	 * 是否检查报名条件
	 * 
	 * @param ctx
	 * @return
	 */
	protected boolean needCheckEnroll(BattleContext ctx) {
		return false;
	}

	public void destroyDesk(String deskId, DeskDestoryReason reason) {
		CommonDesk desk = guard.getDeskById(deskId);
		if (desk != null) {
			desk.destroy(reason);
		}
	}

	public void destroyAllDesk(DeskDestoryReason reason) {
		for(String deskId:this.guard.getDeskMap().keySet()) {
			CommonDesk desk = guard.getDeskById(deskId);
			if (desk != null) {
				desk.destroy(reason);
			}
		}
	}

	@Override
	public void check() {
		guard.check();

		for (CommonDesk desk : guard.getDeskMap().values()) {
			desk.check();
		}
	}

	@Override
	public void onDeskCreate(CommonDesk desk) {
		deskCount++;
	}

	@Override
	public CommonDesk genDesk(String id,int wanfa) throws Exception {
		DeskConfig deskConf = getMatchConf().conditionInfo.deskConf;
		if (deskConf.deskClassFullName != null) {
			Class<?> cls = getClass().getClassLoader().loadClass(deskConf.deskClassFullName);
			Constructor<?> c = cls.getConstructor(DeskListener.class, Room.class, DeskConfig.class, String.class);
			CommonDesk desk = (CommonDesk)c.newInstance(this, this, deskConf, id);
			return desk;
		} else if (deskConf.supportRobot) {
			CommonDesk desk = new RobotSupportDeskImpl(this, this, deskConf, id);
			return desk;
		} else {
			CommonDesk desk = new DeskImpl(this, this, deskConf, id);
			return desk;
		}
	}

	@Override
	public CommonDesk changeDesk(int playerId) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session == null) {
			logger.error("act=changeDesk;error=sessionMiss;playerId={};roomId={};", playerId, getRoomId());
			return null;
		}

		PlayerStatus status = session.getStatus();
		if (status == PlayerStatus.GAMING) {
			logger.error("act=changeDesk;error=gaming;playerId={};roomId={};", playerId);
			return null;
		}

		CommonDesk desk = session.getPlayingOrReadyDesk(playerId);
		if (desk == null) {
			logger.error("act=changeDesk;error=deskMiss;playerId={};roomId={};", playerId);
			return null;
		}

		// 退出原来的桌子
		desk.playerExit(playerId, PlayerExitType.CHANGE_DESK);

		// 再次进入. TODO 是否需要跳到其它房间去.
		BattleContext ctx = BattleContext.create(session).setPlayerId(playerId);
		EnterRoomResult deskResult = playerTrySit(ctx);

		if (deskResult.isFail()) {
			// TODO 推送其它的房间.....
			return null;
		}

		// 2.自动进入准备状态
		desk.onPlayerReadyPacketReceived(playerId);

		// 3.推送换桌成功消息
		PushService.instance.pushChangeDeskRsp(session.userId, true, null);
		return null;
	}

	@Override
	public void init(RoomConfig conf) {		
		if(guard == null) {
			guard = new RoomGuard(this);
		}
		this.conf = conf;
		this.roomId = conf.roomId;
		logger.info("act=roomInit;roomId={};roomName={};", roomId, conf.roomName);
		for(CommonDesk desk : this.guard.getDeskMap().values()) {
			DeskConfig deskConf = getMatchConf().conditionInfo.deskConf;
			desk.setDeskConfig(deskConf);
		}
	}

	protected MatchConfig getMatchConf() {
		return getParent().getMatchConfig();
	}

	@Override
	public RoomConfig getRoomConfig() {
		return conf;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	@Override
	public int getPlayerCount() {
		return this.playerCount;
	}

	@Override
	public Map<String, Integer> getPlayerMap() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		ConcurrentMap<String, CommonDesk> deskMap = guard.getDeskMap();

		for (String deskId : deskMap.keySet()) {
			map.put(deskId, deskMap.get(deskId).getPlayerCount());
		}
		return map;
	}

	@Override
	public Map<String, Double> getDeskDelayStatsu() {
		Map<String, Double> map = new HashMap<String, Double>();
		ConcurrentMap<String, CommonDesk> deskMap = guard.getDeskMap();

		for (String deskId : deskMap.keySet()) {
			map.put(deskId, deskMap.get(deskId).getDeskDelayStatus());
		}
		return map;
	}

	@Override
	public CommonDesk getById(String id) {
		return this.guard.getDeskById(id);
	}

	@Override
	public List<DeskModel> getDeskList() {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(CommonDesk desk : this.guard.getDeskMap().values()) {
			list.add(desk.getDeskInfo());
		}
		return list;
	}

	@Override
	public List<DeskModel> getClubJiFenDesk(long clubId) {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(CommonDesk desk : this.guard.getClubJiFenDeskMap(clubId)) {
			list.add(desk.getDeskInfo());
		}
		return list;
	}

	@Override
	public List<DeskModel> getClubCommonDesk(long clubId) {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(CommonDesk desk : this.guard.getClubCommonDeskMap(clubId)) {
			list.add(desk.getDeskInfo());
		}
		return list;
	}
	@Override
	public DeskModel findDesk(int playerId) {
		for(CommonDesk desk : guard.getDeskMap().values()) {
			if(desk.isHasPlayer(playerId)) {
				return desk.getDeskInfo();
			}
		}
		return null;
	}
	
}
