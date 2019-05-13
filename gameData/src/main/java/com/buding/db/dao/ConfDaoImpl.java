package com.buding.db.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.db.model.MallConf;
import com.buding.db.model.RoomConf;
import com.buding.hall.module.conf.ConfDao;

public class ConfDaoImpl extends CachedServiceAdpter implements ConfDao {
	@Autowired
	private DbService dbService;

	@Override
	public List<RoomConf> getRoomConfList() {
		return this.commonDao.selectList("select * from room_conf ", RoomConf.class, null);
	}

	@Override
	public List<MallConf> getMallConfList() {
		return this.commonDao.selectList("select * from mall_conf where status = 1 and publish = 1", MallConf.class);
	}
	
	
}
