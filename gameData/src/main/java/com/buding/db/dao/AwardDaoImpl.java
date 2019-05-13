package com.buding.db.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.Award;
import com.buding.db.model.UserAward;
import com.buding.hall.module.award.dao.AwardDao;

public class AwardDaoImpl extends CachedServiceAdpter implements AwardDao {

	@Autowired
	DbService dbService;
	
	@Override
	public long insert(Award award) {
		this.commonDao.save(award);
		return award.getId();
	}

	@Override
	public Award getAward(long id) {
		return this.get(id, Award.class);
	}

	@Override
	public void insertUserAward(UserAward ua) {
		this.commonDao.save(ua);
	}

	@Override
	public void updateUserAward(UserAward ua) {
		this.put2EntityCache(ua);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(ua);
		} else {
			this.dbService.submitUpdate2Queue(ua);
		}
		
	}

	@Override
	public List<UserAward> getUserAward(int userid) {
		return this.getList("select * from user_award where user_id = ? ", UserAward.class, userid);
	}

	@Override
	public Map<Long, UserAward> getUserAwardMap(int userid) {
		List<UserAward> list = getUserAward(userid);
		Map<Long, UserAward> map = new  HashMap<Long, UserAward>();
		for(UserAward a : list) {
			map.put(a.getAwardId(), a);
		}
		return map;
	}

	@Override
	public UserAward get(long awardId, int userid) {
		List<UserAward> list = this.getList("select * from user_award where user_id = ? and award_id = ? ", UserAward.class, userid, awardId);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public void delAwardByDay() {
		this.commonDao.delete(null,Award.class);
		this.commonDao.delete(null,UserAward.class);
	}

}
