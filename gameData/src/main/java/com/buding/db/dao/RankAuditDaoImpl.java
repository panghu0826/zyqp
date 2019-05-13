package com.buding.db.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.db.model.RankAudit;
import com.buding.hall.module.rank.dao.RankAuditDao;


public class RankAuditDaoImpl extends CachedServiceAdpter implements RankAuditDao{
	@Autowired
	DbService dbService;

	@Override
	public RankAudit getByParams(String gameId, long date) {
		List<RankAudit> list = this.commonDao.selectList("select * from rank_audit where group_id = ? and plan_settle_date = ? ", RankAudit.class, gameId, date);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public long insert(RankAudit audit) {
		this.commonDao.save(audit);
		return audit.getId();
	}
}
