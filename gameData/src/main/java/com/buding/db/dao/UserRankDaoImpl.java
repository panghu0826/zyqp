package com.buding.db.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.common.util.StrUtil;
import com.buding.db.model.RankAudit;
import com.buding.db.model.UserRank;
import com.buding.db.model.UserRankDetail;
import com.buding.hall.module.rank.dao.UserRankDao;

public class UserRankDaoImpl extends CachedServiceAdpter implements UserRankDao {
	@Autowired
	DbService dbService;

	@Override
	public UserRankDetail getByType(int userId, String gameId, int pointType, long datetime) {
		List<UserRankDetail> rankList = this.commonDao.selectList("select * from user_rank_detail where user_id = ? and game_id = ? and point_type = ? and group_datetime = ? ", UserRankDetail.class,
				userId, StrUtil.null2string(gameId, ""), pointType, datetime);
		if (rankList.isEmpty()) {
			return null;
		}
		UserRankDetail rank = rankList.get(0);
		return this.get(rank.getId(), UserRankDetail.class);
	}

	@Override
	public void update(UserRank model) {
		this.put2EntityCache(model);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(model);
		} else {
			this.dbService.submitUpdate2Queue(model);
		}
	}

	@Override
	public void insert(UserRank rank) {
		this.commonDao.save(rank);
	}

	@Override
	public List<UserRank> assembleUserRank(String gameId, long startTime, long endTime, int pointType) {
		return this.commonDao.selectList("SELECT user_id, SUM(POINT) AS rank_point FROM user_rank_detail where game_id = ? and group_datetime >= ? and group_datetime < ? and point_type = ? GROUP BY user_id ORDER BY rank_point desc ",
				UserRank.class, StrUtil.null2string(gameId, ""), startTime, endTime, pointType);
	}

	@Override
	public Set<Integer> getAwardUserIdSet(long auditId) {
		List<UserRank> rankList = this.commonDao.selectList("select user_id from user_rank where audit_id = ? ", UserRank.class, auditId);
		Set<Integer> useridset = new HashSet<Integer>();
		for(UserRank rank : rankList) {
			useridset.add(rank.getUserId());
		}
		return useridset;
	}

	@Override
	public void update(UserRankDetail model) {
		this.put2EntityCache(model);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(model);
		} else {
			this.dbService.submitUpdate2Queue(model);
		}
	}

	@Override
	public void insert(UserRankDetail rank) {
		this.commonDao.save(rank);
	}

	@Override
	public List<UserRank> assembleOldRank(long auditId) {
		return this.commonDao.selectList("select * from user_rank where audit_id = ? ", UserRank.class, auditId);
	}

	@Override
	public RankAudit getBySettleTime(String groupId, long settleTime) {
		return this.commonDao.selectOne("select * from rank_audit where plan_settle_date = ? and group_id = ?", RankAudit.class, settleTime, groupId);
	}

	@Override
	public UserRank getUserRank(int userId, String rankGrpId, String gameId, long rankGroupTime) {
		return this.commonDao.selectOne("select * from user_rank where user_id = ? and group_id = ? and rank_grp_time = ? and game_id = ?", UserRank.class, userId, rankGrpId, rankGroupTime,gameId);
	}

	@Override
	public List<UserRank> getRankList(String rankGrpId, long rankGroupTime, String gameId, int size) {
		return this.commonDao.selectList("select * from user_rank where group_id = ? and rank_grp_time = ? and game_id = ? order by rank_point desc limit " + size, UserRank.class, rankGrpId, rankGroupTime,gameId);
	}
	
}
