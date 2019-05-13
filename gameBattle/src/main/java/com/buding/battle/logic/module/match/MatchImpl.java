package com.buding.battle.logic.module.match;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.BaseParent;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.EnrollResult;
import com.buding.battle.logic.module.common.EnterRoomResult;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.battle.logic.module.room.bo.RoomImpl;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.common.monitor.Monitor;
import com.buding.common.result.Result;
import com.buding.common.server.ServerConfig;
import com.buding.hall.config.EnterCondition;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.config.RoomConfig;
import com.buding.hall.module.game.model.DeskModel;
import com.google.gson.GsonBuilder;

public class MatchImpl extends BaseParent<Game> implements Match {
	protected Logger logger = LogManager.getLogger(getClass());

	ConcurrentMap<String, Room> roomMap = new ConcurrentHashMap<String, Room>();
	public int playerCount = 0;
	transient MatchConfig matchConf;

	public String id;

	public MatchImpl(Game parent) {
		super(parent);
	}

	@Override
	public synchronized EnrollResult enroll(BattleContext ctx) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
		if (session == null) {
			logger.error("act=enroll;error=sessionMiss;playerId={};matchId={};", ctx.playerId, getId());
			return EnrollResult.fail("会话超时,请重新登录");
		}
		PlayerInfo player = session.player;

		EnterCondition condition = matchConf.conditionInfo.enterCondition;

		if (isFull()) {
			logger.error("act=enroll;error=full;playerId={};matchId={};", ctx.playerId, ctx.matchId);
			return EnrollResult.fail("赛场人数已满");
		}

//		if (player.coin <= 0) {
//			logger.info("act=enroll;error=bankrupt;playerId={};matchId={}", ctx.playerId, ctx.matchId);
//			return EnrollResult.fail("您已破产，可以前往大厅每日任务中领取破产救济，每天可领取三次！");
//		}

		if (condition.minCoinLimit > 0 && player.coin < condition.minCoinLimit) {
			logger.info("act=enroll;error=coinLess;expect={};actual={};playerId={};matchId={};", condition.minCoinLimit, player.coin, ctx.playerId, matchConf.matchID);
			return EnrollResult.fail("你的金币数不足,还差" + (condition.minCoinLimit - player.coin) + "金币");
		}

		if (condition.maxCoinLimit > 0 && player.coin > condition.maxCoinLimit) {
			logger.info("act=enroll;error=coinFlowout;expect={};actual={};playerId={};matchId={};", condition.maxCoinLimit, player.coin, ctx.playerId, matchConf.matchID);
			return EnrollResult.fail("你的金币数已经超过本场最大限制,推荐前往其它场");
		}

		for (Room room : roomMap.values()) {
			//校验下金币是否足够
			Result ret = room.playerEnroll(ctx);
			if (ret.isOk()) {
				return EnrollResult.success(ctx.matchId,room.getRoomId());
			}
		}

		return EnrollResult.fail("进入失败,没有满足报名条件的房间");
	}

	public static void main(String[] args) {
		Runnable runnable = new Runnable() {
			public void run() {
				// task to run goes here
				System.out.println("Hello !!");
			}
		};
		ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
		// 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
		service.scheduleAtFixedRate(runnable, 10, 1, TimeUnit.SECONDS);
	}
	@Override
	public synchronized EnterRoomResult enterRoom(BattleContext ctx) {
//		if (isFull()) {
//			logger.error("act=enterRoom;error=matchfull;playerId={};roomId={};matchId={};", ctx.playerId, ctx.roomId, ctx.matchId);
//			return EnterRoomResult.fail("赛场人数已满");
//		}
		Room room = roomMap.get(ctx.getRoomId());
		if (room == null) {
			logger.error("act=enterRoom;error=roomMiss;playerId={};roomId={};matchId={};", ctx.playerId, ctx.roomId, ctx.matchId);
			return EnterRoomResult.fail("房间不存在");
		}
		EnterRoomResult res = room.playerTryEnter(ctx);
		if (res.isOk()) {
			res = room.playerTrySit(ctx);
		}

		return res;
	}

	@Override
	public void onPlayerCountDecr(int playerId) {
		playerCount--;
	}

	@Override
	public Result playerExit(int playerId, PlayerExitType reason) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session == null) {
			logger.error("act=playerExit;error=sessionMiss;playerId={};matchId={};", playerId, getId());
			return Result.fail("用户会话失效");
		}
		Room room = session.getRoom();
		if (room == null) {
			// playerCount --;
			session.leaveMatch();
			return Result.success();
		}

		room.playerExit(playerId, reason);

		return Result.success();
	}

	@Override
	public boolean isFull() {
		return playerCount >= matchConf.playerCountLimit;
	}

	@Override
	public boolean isEmpty() {
		return playerCount == 0;
	}

	@Override
	public void init(MatchConfig conf) {
		this.matchConf = conf;
		this.id = matchConf.matchID;
		
		logger.info("act=matchInit;matchId={};", matchConf.matchID);

		for (RoomConfig roomConf : conf.conditionInfo.roomArray) {
			try {
				Room room = roomMap.get(roomConf.roomId);
				if(room == null) {
					if (StringUtils.isNotBlank(roomConf.roomClassFullName)) {
						Class<?> cls = getClass().getClassLoader().loadClass(roomConf.roomClassFullName);
						Constructor<?> c = cls.getConstructor(Match.class);
						room = (Room) c.newInstance(this);
					} else {
						room = new RoomImpl(this);
					}
					Monitor.add2Monitor(room);
				}
				room.init(roomConf);
				roomMap.put(room.getRoomId(), room);				
			} catch (Exception e) {
				logger.error("act=matchInit;error=exception;", e);
			}
		}
	}

	@Override
	public void destroy() {

	}

	@Override
	public Room getRoom(String roomId) {
		return roomMap.get(roomId);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setStartTime(long startTime) {

	}

	@Override
	public void setInitTime(long initTime) {

	}


	@Override
	public String getName() {
		return matchConf.matchName;
	}

	@Override
	public List<DeskModel> getDeskList() {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(Room room : this.roomMap.values()) {
			list.addAll(room.getDeskList());
		}
		return list;
	}

	@Override
	public List<DeskModel> getClubCommonDesk(long clubId) {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(Room room : this.roomMap.values()) {
			list.addAll(room.getClubCommonDesk(clubId));
		}
		return list;
	}

	@Override
	public List<DeskModel> getClubJiFenDesk(long clubId) {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(Room room : this.roomMap.values()) {
			list.addAll(room.getClubJiFenDesk(clubId));
		}
		return list;
	}

	@Override
	public MatchConfig getMatchConfig() {
		return this.matchConf;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	@Override
	public int getPlayerCount() {
		return playerCount;
	}

	@Override
	public void onPlayerCountIncr(int playerId) {
		playerCount++;
	}

	@Override
	public Map<String, Integer> getPlayerMap() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String matchId : roomMap.keySet()) {
			map.put(matchId, roomMap.get(matchId).getPlayerCount());
		}
		return map;
	}

	@Override
	public Map<String, Map<String, Double>> getDeskDelayStatus() {
		Map<String, Map<String, Double>> map = new HashMap<String, Map<String, Double>>();
		for (String deskId : roomMap.keySet()) {
			map.put(deskId + "", roomMap.get(deskId).getDeskDelayStatsu());
		}
		return map;
	}

	@Override
	public CommonDesk findDesk(String deskId) {
		for(Room r : this.roomMap.values()) {
			CommonDesk desk = r.getById(deskId);
			if(desk != null) {
				return desk;
			}
		}
		return null;
	}
	
	@Override
	public DeskModel findDesk(int playerId) {
		for(Room r : this.roomMap.values()) {
			DeskModel desk = r.findDesk(playerId);
			if(desk != null) {
				return desk;
			}
		}
		return null;
	}

	@Override
	public void triggerByTime() {

	}

	@Override
	public Result unEnroll(int playerId) {
		return new Result(0);
	}

	@Override
	public List<RoomOnlineModel> getRoomOnlineList() {
		List<RoomOnlineModel> list = new ArrayList<RoomOnlineModel>();
		for(Room r : this.roomMap.values()) {
			 RoomOnlineModel model = new RoomOnlineModel();
			 model.serverInstanceId = ServiceRepo.serverConfig.instanceId;
			 model.gameId = this.getParent().getId();
			 model.matchId = this.getId();
			 model.roomId = r.getRoomId();
			 model.lastUpdate = System.currentTimeMillis();
			 model.onlineNum = r.getPlayerCount();
			 list.add(model);
		}
		return list;
	}
	
}
