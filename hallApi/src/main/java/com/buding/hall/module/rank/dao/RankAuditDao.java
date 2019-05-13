package com.buding.hall.module.rank.dao;

import com.buding.db.model.RankAudit;

public interface RankAuditDao {
	public RankAudit getByParams(String gameId, long date);
	public long insert(RankAudit audit);
}
