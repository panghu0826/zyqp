package com.buding.battle.logic.module.game;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.match.MultiMatchImpl;
import com.buding.common.loop.Looper;
import com.buding.common.loop.ServerLoop;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.EnrollResult;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.match.MatchImpl;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.common.server.BaseServerComponent;
import com.buding.hall.config.GameConfig;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.module.game.model.DeskModel;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import packet.game.MsgGame;


public class GameImpl extends BaseServerComponent implements Game {
	ConcurrentMap<String, Match> matchMap = new ConcurrentHashMap<String, Match>();
	transient GameConfig gameConfig;
	private Logger logger = LogManager.getLogger(getClass());
	private long lastLoopTime=System.currentTimeMillis();
	private Thread multiMatchPlayerNumThread;

	@Override
	public void triggerByTime() {
		multiMatchPlayerNumThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					long currTime = System.currentTimeMillis();
					if(currTime-lastLoopTime<5*1000) continue;
//					logger.info("同步赛场玩家人数消息===========");
					boolean hasMultiMatch=false;
					for (Map.Entry<String, Match> entry : matchMap.entrySet()) {
						String matchId = entry.getKey();
						if (!matchId.startsWith("G_DQMJ_MATCH_MULTI")) continue;
						if (!(entry.getValue() instanceof MultiMatchImpl)) continue;
						MultiMatchImpl match = (MultiMatchImpl) entry.getValue();
						if (currTime - match.startTime >= 60 * 1000) continue;
						hasMultiMatch=true;
						PushService.instance.sendMultiMatchPlayerNum(1, match.playerList.size());
					}
					if(!hasMultiMatch){
						PushService.instance.sendMultiMatchPlayerNum(1, 0);
					}
					lastLoopTime = currTime;
				}
			}
		};
		multiMatchPlayerNumThread.start();
	}

	@Override
	public List<MultiMatchImpl> findMultiMatch(int userId) {
		List<MultiMatchImpl> list = new ArrayList<>();
		for(Match match : this.matchMap.values()) {
			if(match instanceof MultiMatchImpl){
				MultiMatchImpl multiMatch = (MultiMatchImpl) match;
				if(multiMatch.playerList.contains(userId)) {
					list.add(multiMatch);
				}
			}
		}
		return list;
	}

	@Override
	public MultiMatchImpl getMultiMatch(String matchId,int playerId) {
		for(Match match : this.matchMap.values()) {
			if(match instanceof MultiMatchImpl){
				MultiMatchImpl multiMatch = (MultiMatchImpl) match;
				if(multiMatch.id.startsWith(matchId)&&multiMatch.playerList.contains(playerId)) {
					return multiMatch;
				}
			}
		}
		return null;
	}

	public  GameImpl() {

	}
	public void init(GameConfig config) {
		this.gameConfig = config;
		
		logger.info("act=gameInit;gameId={};gameName={};", gameConfig.gameId, gameConfig.gameName);
		for(MatchConfig conf : gameConfig.matchs) {
			try {
				Match match = matchMap.get(conf.matchID);
				if(match == null) {
					if(StringUtils.isNotBlank(conf.matchClassFullName)) {
						Class<?> cls = getClass().getClassLoader().loadClass(conf.matchClassFullName);
						Constructor<?> c = cls.getConstructor(Game.class);
						match = (Match)c.newInstance(this);
					} else {
						match = new MatchImpl(this);
					}
				}
				match.init(conf);
				if(conf.matchID.equalsIgnoreCase("G_DQMJ_MATCH_MULTI")){
					SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
					String currentMinutes = sdf.format(new Date());
					long initTime =  sdf.parse(currentMinutes).getTime();
					long startTime =  sdf.parse(currentMinutes).getTime()+60*1000;

					match.setId(conf.matchID+currentMinutes);
					match.setInitTime(initTime);
					match.setStartTime(startTime);
					matchMap.put(conf.matchID+currentMinutes, match);
					match.triggerByTime();
				}else {
					matchMap.put(conf.matchID, match);
				}
			} catch(Exception e) {
				logger.error("act=init;error=exception;", e);
			}
		}
		
	}
	
	
	public Match getMatch(String matchId) {
		return matchMap.get(matchId);
	}
	
	@Override
	public int getPlayerCount() {
		int c = 0;
		for(Match m : matchMap.values()) {
			c += m.getPlayerCount();
		}
		return c;
	}
	
	@Override
	public int getMaxPlayerCount() {
		int c = 0;
		for(Match m : matchMap.values()) {
			c += m.getMatchConfig().playerCountLimit;
		}
		return c;
	}

	@Override
	public EnrollResult enroll(BattleContext ctx) {
		Match match =  matchMap.get(ctx.matchId);
		if(match == null) {
			if(StringUtils.equals(ctx.matchId,"G_DQMJ_MATCH_MULTI")){
				SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
				String currentMinutes = sdf.format(new Date());
				Match match1 = matchMap.get(ctx.matchId+currentMinutes);
				if(match1==null) {
					logger.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
					match1 = initMultiMatch(ctx.matchId);
				}
				logger.info("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
				if(match1 == null){
					logger.info("act=enroll;error=matchMiss;playerId={};matchId={};", ctx.playerId, ctx.matchId);
					return EnrollResult.fail("赛场不存在");
				}
				EnrollResult ret = match1.enroll(ctx);
				return ret;
			}else {
				logger.info("act=enroll;error=matchMiss;playerId={};matchId={};", ctx.playerId, ctx.matchId);
				return EnrollResult.fail("赛场不存在");
			}
		}
		
		EnrollResult ret = match.enroll(ctx);
		return ret;
	}

	private Match initMultiMatch(String matchId) {
		Match match = null;
		for(MatchConfig conf : gameConfig.matchs) {
			if(!StringUtils.equals(matchId,conf.matchID)) continue;
			try {
				match = matchMap.get(conf.matchID);
				if(match == null) {
					if(StringUtils.isNotBlank(conf.matchClassFullName)) {
						Class<?> cls = getClass().getClassLoader().loadClass(conf.matchClassFullName);
						Constructor<?> c = cls.getConstructor(Game.class);
						match = (Match)c.newInstance(this);
					}
				}else{
					logger.info("======================initMultiMatch error========================");
				}
				match.init(conf);
				if(conf.matchID.equalsIgnoreCase("G_DQMJ_MATCH_MULTI")){
					SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
					String currentMinutes = sdf.format(new Date());
					long initTime =  sdf.parse(currentMinutes).getTime();
					long startTime =  sdf.parse(currentMinutes).getTime()+60*1000;
					match.setId(conf.matchID+currentMinutes);
					match.setInitTime(initTime);
					match.setStartTime(startTime);
					matchMap.put(conf.matchID+currentMinutes, match);
					match.triggerByTime();
				}
			} catch(Exception e) {
				logger.error("act=initMultiMatch;error=exception;", e);
			}
		}
		return match;
	}

	@Override
	public Map<String, Match> getMatchMap() {
		return matchMap;
	}

	@Override
	public String getId() {
		return gameConfig.gameId;
	}

	@Override
	public String getName() {
		return gameConfig.gameName;
	}

	@Override
	public List<DeskModel> getDeskList() {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(Match room : this.matchMap.values()) {
			list.addAll(room.getDeskList());
		}
		return list;
	}

	@Override
	public List<DeskModel> getClubJiFenDesk(long clubId) {
		List<DeskModel> list = new ArrayList<>();
		for(Match room : this.matchMap.values()) {
			list.addAll(room.getClubJiFenDesk(clubId));
		}
		return list;
	}

	@Override
	public List<DeskModel> getClubCommonDesk(long clubId) {
		List<DeskModel> list = new ArrayList<>();
		for(Match room : this.matchMap.values()) {
			list.addAll(room.getClubCommonDesk(clubId));
		}
		return list;
	}

	@Override
	public Map<String, Integer> getPlayerMap() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(String matchId : matchMap.keySet()) {
			map.put(matchId, matchMap.get(matchId).getPlayerCount());
		}
		return map;
	}

	@Override
	public Map<String, Map<String, Map<String, Double>>> getDeskDelayStatus() {
		Map<String, Map<String, Map<String, Double>>> map = new HashMap<String, Map<String, Map<String, Double>>>();
		for(String key : matchMap.keySet()) {
			Map<String, Map<String, Double>> a = matchMap.get(key).getDeskDelayStatus();
			map.put(key, a);
		}
		return map;
	}

	@Override
	public String getStatusDesc() {
		String status = new GsonBuilder().setPrettyPrinting().create().toJson(getDeskDelayStatus());
		return status;
	}

	@Override
	public DeskModel findDesk(int playerId) {
		for(Match m : this.matchMap.values()) {
			DeskModel desk = m.findDesk(playerId);
			if(desk != null) {
				return desk;
			}
		}
		return null;
	}

	@Override
	public List<RoomOnlineModel> getRoomOnlineList() {
		List<RoomOnlineModel> list = new ArrayList<RoomOnlineModel>();
		for(Match m : this.matchMap.values()) {
			list.addAll(m.getRoomOnlineList());
		}
		return list;
	}

}
