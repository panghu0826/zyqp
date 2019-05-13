package com.buding.hall.module.exchange.dao;

import java.util.List;

import com.buding.db.model.DayExchange;
import com.buding.db.model.UserExchange;

public interface ExchangeDao {
	public void insertExchange(UserExchange ue);
	public void insertDayExchange(DayExchange de);
	public void updateExchange(UserExchange ue);
	public void updateDayExchange(DayExchange de);
	public UserExchange get(long id);
	public List<UserExchange> getByUserId(int userId);
	public List<DayExchange> getDayChangeList();
	public DayExchange getByConfAndDay(String confId, int day);
}
