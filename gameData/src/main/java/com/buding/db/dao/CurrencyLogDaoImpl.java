package com.buding.db.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.db.model.UserCurrencyLog;
import com.buding.hall.module.currency.dao.CurrencyLogDao;


public class CurrencyLogDaoImpl extends CachedServiceAdpter implements CurrencyLogDao {
	@Autowired
	DbService dbService;
	
	@Override
	public void insertLog(UserCurrencyLog log) {
		this.commonDao.save(log);
	}
}
