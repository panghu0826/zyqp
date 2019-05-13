package com.buding.rank.processor;

import java.util.Calendar;
import java.util.List;

import com.buding.common.cache.RedisClient;
import com.buding.db.model.UserRank;
import com.buding.rank.model.RankModel;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.buding.common.event.EventBus;
import com.buding.common.event.Receiver;
import com.buding.hall.config.RankConfig;
import com.buding.hall.module.common.constants.RankPointType;
import com.buding.hall.module.task.event.GamePlayedInWeekEvent;
import com.buding.hall.module.task.type.EventType;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.user.dao.UserDao;


/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class GameCountRankInWeekProcessor extends BaseRankProcessor implements Receiver<GamePlayedInWeekEvent> {
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
		eventBus.register(EventType.PLAYED_GAME_WEEK, this);
	}

	@Override
	protected long getRankGroupTime() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int week = c.get(Calendar.WEEK_OF_YEAR);
		int dayWeek = c.get(Calendar.DAY_OF_WEEK);
		if(dayWeek==1) week--;
		return year * 1000 + week;
	}

	@Override
	public void onEvent(GamePlayedInWeekEvent event) throws Exception {
		GamePlayingVo model = event.getBody();
		addRankPoint(model.userId, model.loseCount + model.winCount + model.evenCount, getRankGroupTime(),model.gameId);
	}

	@Override
	public String getEventName() {
		return EventType.PLAYED_GAME_WEEK;
	}


	@Override
	public int getRankType() {
		return RankPointType.gameCountInWeek;
	}

	@Override
	protected RankConfig getRankConfig() {
		return configManager.rankConfMap.get("GAME_COUNT_RANK_IN_WEEK");
	}

	@Override
	public void loadFromDB() {
		RankConfig rankConfig = getRankConfig();
		for (String gameId : configManager.gameMap.keySet()) {
			List<UserRank> list = userRankDao.getRankList(rankConfig.id, getRankGroupTime(), gameId, rankConfig.rankLimit);

			String key = rankConfig.pointType + gameId + "_rank";
			redisClient.zremrangeByRank(key, 0, rankConfig.rankLimit * 2);
			int i = 1;
			for (UserRank rank : list) {
				RankModel rankModel = dbModel2CacheModel(rank);
				rankModel.rank = i++;
				int score = rankModel.rank;
				String member = new Gson().toJson(rankModel);
				redisClient.zadd(key, score, member);
			}
		}

		redisClient.hset("ddz_meta", "rank_version", System.currentTimeMillis()+"");
	}
}
