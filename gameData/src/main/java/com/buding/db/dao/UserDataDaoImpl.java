package com.buding.db.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.UserData;
import com.buding.hall.module.user.dao.UserDataDao;

public class UserDataDaoImpl extends CachedServiceAdpter implements UserDataDao {
	@Autowired
	DbService dbService;

	@Override
	public long insert(UserData data) {
		this.commonDao.save(data);
		return data.getId();
	}

	@Override
	public void update(UserData model) {
		this.put2EntityCache(model);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(model);
		} else {
			this.dbService.submitUpdate2Queue(model);
		}
	}

	@Override
	public UserData get(int userId, String key) {
		return this.getOne("select * from user_data where user_id = ? and vkey = ? ", UserData.class, userId, key);
	}

}
