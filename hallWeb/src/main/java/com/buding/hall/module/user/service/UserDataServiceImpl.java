package com.buding.hall.module.user.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.db.model.UserData;
import com.buding.hall.module.user.dao.UserDataDao;

/**
 * 存放用户数据
 * @author Administrator
 *
 */
public class UserDataServiceImpl implements UserDataService {
	@Autowired
	UserDataDao userDataDao;
	
	public int getIntVal(String key, int userId) {
		UserData item = getUserDataItem(key, userId);
		return item == null ? 0 : Integer.valueOf(item.getVal());
	}

	public void incrIntVal(String key, int userId, int val) {
		UserData item = getUserDataItem(key, userId);
		if(item == null) {
			item = new UserData();
			item.setCtime(new Date());
			item.setVkey(key);
			item.setUserId(userId);
			item.setMtime(new Date());
			item.setVal(val+"");
			userDataDao.insert(item);
		} else {
			item.setMtime(new Date());
			item.setVal((Integer.valueOf(item.getVal()) + val) + "");
			userDataDao.update(item);
		}
	}

	public UserData getUserDataItem(String key, int userId) {
		return userDataDao.get(userId, key);
	}
}