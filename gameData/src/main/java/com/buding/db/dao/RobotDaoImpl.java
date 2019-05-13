package com.buding.db.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.RobotSetting;
import com.buding.hall.module.robot.dao.RobotDao;

public class RobotDaoImpl extends CachedServiceAdpter implements RobotDao {
	@Autowired
	DbService dbService;
	
	@Override
	public List<RobotSetting> loadRobotSettingList() {
		return commonDao.selectList("select * from robot_setting ", RobotSetting.class);
	}

	@Override
	public void update(RobotSetting model) {
		this.put2EntityCache(model);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(model);
		} else {
			this.dbService.submitUpdate2Queue(model);
		}
	}

	@Override
	public RobotSetting getByMatchId(String matchId) {
		List<RobotSetting> list = this.commonDao.selectList("select * from robot_setting where match_id = ?", RobotSetting.class, matchId);
		if(list.isEmpty()) {
			return null;
		}
		int id = list.get(0).getId();
		return get(id, RobotSetting.class);
	}
}
