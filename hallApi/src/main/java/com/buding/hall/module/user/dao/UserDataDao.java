package com.buding.hall.module.user.dao;

import com.buding.db.model.UserData;

public interface UserDataDao {
	public long insert(UserData data);
	public void update(UserData data);
	public UserData get(int userId, String key);
}
