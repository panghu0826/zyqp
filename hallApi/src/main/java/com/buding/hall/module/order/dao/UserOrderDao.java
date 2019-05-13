package com.buding.hall.module.order.dao;

import com.buding.db.model.UserOrder;

public interface UserOrderDao {
	public void insert(UserOrder order);
	public void update(UserOrder order);
	public UserOrder getByOrderId(String id);
}
