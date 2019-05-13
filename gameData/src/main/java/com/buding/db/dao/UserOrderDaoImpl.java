package com.buding.db.dao;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.db.model.UserOrder;
import com.buding.hall.module.order.dao.UserOrderDao;

public class UserOrderDaoImpl extends CachedServiceAdpter implements UserOrderDao {

	@Override
	public void insert(UserOrder order) {
		this.commonDao.save(order);
	}

	@Override
	public void update(UserOrder order) {
		this.commonDao.update(order);
	}

	@Override
	public UserOrder getByOrderId(String id) {
		return this.commonDao.selectOne("select * from user_order where order_id = ? ", UserOrder.class, id);
	}
}
