package com.buding.db.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.UserItem;
import com.buding.hall.module.item.dao.ItemDao;

public class ItemDaoImpl extends CachedServiceAdpter implements ItemDao {

	@Autowired
	DbService dbService;
	
	@Override
	public UserItem getUserItem(int userId, int itemType) {
		String sql = "select * from user_item where user_id = ? and item_type = ? ";
		UserItem item = this.commonDao.selectOne(sql, UserItem.class, userId, itemType);
		if(item != null) {
			item = getById(item.getId());
		}
		return item;
	}

	@Override
	public void updateUserItem(UserItem item) {
		this.put2EntityCache(item);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(item);
		} else {
			this.dbService.submitUpdate2Queue(item);
		}
	}

	@Override
	public UserItem insert(UserItem item) {
		this.commonDao.save(item);
		return item;
	}

	@Override
	public UserItem getById(long id) {
		return this.get(id, UserItem.class);
	}

}
