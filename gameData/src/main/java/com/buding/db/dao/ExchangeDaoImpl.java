package com.buding.db.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.DayExchange;
import com.buding.db.model.UserExchange;
import com.buding.hall.module.exchange.dao.ExchangeDao;

public class ExchangeDaoImpl extends CachedServiceAdpter implements ExchangeDao {
	@Autowired
	DbService dbService;
	
	@Override
	public void insertExchange(UserExchange ue) {
		this.commonDao.save(ue);
	}

	@Override
	public void updateExchange(UserExchange ue) {
		this.put2EntityCache(ue);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(ue);
		} else {
			this.dbService.submitUpdate2Queue(ue);
		}		
	}

	@Override
	public UserExchange get(long id) {
		return this.get(id, UserExchange.class);
	}

	@Override
	public List<UserExchange> getByUserId(int userId) {
		return this.getList("select * from user_exchange where user_id = ? ", UserExchange.class, userId);
	}

	@Override
	public List<DayExchange> getDayChangeList() {
		return this.getList("select * from day_exchange ", DayExchange.class);
	}

	@Override
	public DayExchange getByConfAndDay(String confId, int day) {
		List<DayExchange> list = this.getList("select * from day_exchange where conf_id = ? and day = ? ", DayExchange.class, confId, day);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public void insertDayExchange(DayExchange de) {
		this.commonDao.save(de);
	}

	@Override
	public void updateDayExchange(DayExchange de) {
		this.put2EntityCache(de);
		this.dbService.submitUpdate2Queue(de);
	}
}
