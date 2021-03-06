package com.buding.rank.processor;

import java.util.Date;
import java.util.List;

import com.buding.common.cache.RedisClient;
import com.buding.rank.model.RankModel;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.buding.common.event.EventBus;
import com.buding.common.event.Receiver;
import com.buding.db.model.User;
import com.buding.db.model.UserRank;
import com.buding.hall.config.RankConfig;
import com.buding.hall.module.common.constants.RankPointType;
import com.buding.hall.module.task.event.CoinChangeEvent;
import com.buding.hall.module.task.type.EventType;
import com.buding.hall.module.user.dao.UserDao;


/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class CoinRankProcessor extends BaseRankProcessor implements Receiver<CoinChangeEvent> {
	@Autowired
	@Qualifier("hallEventBus")
	EventBus eventBus;
	
	@Autowired
	UserDao userDao;

	@Autowired
	private RedisClient redisClient;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		eventBus.register(EventType.COIN_CHANGE, this);
	}
	
	@Override
	public int getRankType() {
		return RankPointType.coin;
	}

	@Override
	protected long getRankGroupTime() {
		return 1;
	}

	@Override
	protected RankConfig getRankConfig() {
		return configManager.rankConfMap.get("GAME_COIN_RANK");
	}

	@Override
	public void onEvent(CoinChangeEvent paramEvent) throws Exception {
		int userId = paramEvent.getBody().getUserId();
		int coin = paramEvent.getBody().getCoin();
		String gameid = paramEvent.getBody().getGameId();
		addRankPoint(userId, coin, getRankGroupTime(),gameid);
	}
	
	protected UserRank initUserRank(int userId, int rankPoint, String gameId) {
		User user = userDao.getUser(userId);
		UserRank rank = new UserRank();
		rank.setAuditId(0L);
		rank.setCtime(new Date());
		rank.setMtime(new Date());
		rank.setRank(0);
		rank.setRankPoint(user.getCoin());
		rank.setUserId(userId);
		rank.setGroupId(getRankConfig().id);
		rank.setGameId(gameId);
		rank.setRankGrpTime(getRankGroupTime());
		userRankDao.insert(rank);
		return rank;
	}

	@Override
	public String getEventName() {
		return EventType.COIN_CHANGE;
	}

	@Override
	public void loadFromDB() {
		RankConfig rankConfig = getRankConfig();
		List<UserRank> list = userRankDao.getRankList(rankConfig.id, getRankGroupTime(), "ALL", rankConfig.rankLimit);

		String key = rankConfig.pointType + "ALL" + "_rank";
		redisClient.zremrangeByRank(key, 0, rankConfig.rankLimit * 2);
		int i = 1;
		for (UserRank rank : list) {
			RankModel rankModel = dbModel2CacheModel(rank);
			rankModel.rank = i++;
			int score = rankModel.rank;
			String member = new Gson().toJson(rankModel);
			redisClient.zadd(key, score, member);
		}

		redisClient.hset("ddz_meta", "rank_version", System.currentTimeMillis()+"");
	}
}
