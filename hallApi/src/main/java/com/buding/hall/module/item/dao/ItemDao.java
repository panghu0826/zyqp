package com.buding.hall.module.item.dao;

import com.buding.db.model.UserItem;

public interface ItemDao {
	public UserItem getUserItem(int userId, int itemType);
	
	public void updateUserItem(UserItem item);
	
	public UserItem insert(UserItem item);
	
	public UserItem getById(long id);
}
