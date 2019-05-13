package com.buding.hall.module.rank.dao;

import java.util.List;
import java.util.Set;

import com.buding.db.model.RankAudit;
import com.buding.db.model.UserRank;
import com.buding.db.model.UserRankDetail;

public interface UserRankDao {
	public UserRankDetail getByType(int userId, String gameId, int pointType, long datetime);
	public void update(UserRank rank);
	public void insert(UserRank rank);
	public void update(UserRankDetail rank);
	public void insert(UserRankDetail rank);
	
	public UserRank getUserRank(int userId, String rankGrpId, String gameId, long rankGroupTime);
	public List<UserRank> getRankList(String rankGrpId, long rankGroupTime, String gameId, int size);

	public List<UserRank> assembleUserRank(String gameId, long startTime, long endTime, int pointType);
	public List<UserRank> assembleOldRank(long auditId);
	public Set<Integer> getAwardUserIdSet(long auditId);
	public RankAudit getBySettleTime(String groupId, long settleTime);
	
}
