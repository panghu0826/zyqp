package com.buding.rank.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.cache.RedisClient;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.User;
import com.buding.db.model.UserRank;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.RankConfig;
import com.buding.hall.module.rank.dao.UserRankDao;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.rank.model.RankModel;
import com.buding.rank.service.UserRankManager;
import com.google.gson.Gson;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public abstract class BaseRankProcessor implements RankProcessor, InitializingBean {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	protected UserRankManager rankManager;
 
	@Autowired
	protected ConfigManager configManager;

	@Autowired
	protected UserRankDao userRankDao;

	@Autowired
	protected UserDao userDao;

	private long version = 0;
	
	@Autowired
	private RedisClient redisClient;

	@Override
	public void afterPropertiesSet() throws Exception {
		rankManager.registerProcessor(getRankType(), this);
	}

	public void loadFromDB() {
//		logger.info("////// " + getClass() + " load rank from db //////");
	}

	@Override
	public boolean refresh() {
		//主服务器负责定时刷新排行榜  测试完记得解开！！！
		if(ServerConfig.mainServer) {
			loadFromDB();
		}
		
		String ver = redisClient.hget("ddz_meta", "rank_version");
		if(StringUtils.isBlank(ver)) {
			return false;
		}
		if(this.version != Long.valueOf(ver)) {
			this.version = Long.valueOf(ver);
			return true;
		}
		return false;
	}

	public void addRankPoint(int userId, int rankPoint, long rankGroupTime,String gameId) {
		UserRank rank = userRankDao.getUserRank(userId, getRankConfig().id,gameId, rankGroupTime);
		if (rank == null) {
			rank = initUserRank(userId, rankPoint,gameId);
		} else {
			rank.setMtime(new Date());
			rank.setRankPoint(rank.getRankPoint() + rankPoint);
			userRankDao.update(rank);
		}
	}

	protected UserRank initUserRank(int userId, int rankPoint, String gameId) {
		UserRank rank;
		rank = new UserRank();
		rank.setAuditId(0L);
		rank.setCtime(new Date());
		rank.setMtime(new Date());
		rank.setRank(0);
		rank.setRankPoint(rankPoint);
		rank.setUserId(userId);
		rank.setGroupId(getRankConfig().id);
		rank.setGameId(gameId);
		rank.setRankGrpTime(getRankGroupTime());
		userRankDao.insert(rank);
		return rank;
	}

	public RankModel dbModel2CacheModel(UserRank rank) {
		User user = userDao.getUser(rank.getUserId());
		RankModel rankModel = new RankModel();
		rankModel.playerId = rank.getUserId();
		rankModel.rankPoint = rank.getRankPoint();
		rankModel.name = user.getNickname();
		rankModel.mtime = new Date();
		rankModel.img = user.getHeadImg();
		rankModel.gameId = rank.getGameId();
		return rankModel;
	}

	@Override
	public List<RankModel> getRank(int userId, String gameId) {
		RankConfig rankConfig = getRankConfig();
		String key = rankConfig.pointType + gameId + "_rank";
		Set<String> items = redisClient.zrange(key, 0, rankConfig.rankLimit);
		TreeSet<RankModel> set = new TreeSet<RankModel>(new Comparator<RankModel>() {
			@Override
			public int compare(RankModel o1, RankModel o2) {
				return o1.rank - o2.rank;
			}			
		});
		for(String item : items) {
			RankModel model = new Gson().fromJson(item, RankModel.class);
			set.add(model);
		}
		return new ArrayList<RankModel>(set);
	}

	protected abstract long getRankGroupTime();

	protected abstract RankConfig getRankConfig();
}
