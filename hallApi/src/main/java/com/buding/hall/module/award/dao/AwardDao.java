package com.buding.hall.module.award.dao;

import java.util.List;
import java.util.Map;

import com.buding.db.model.Award;
import com.buding.db.model.UserAward;

public interface AwardDao {
	public long insert(Award award);
	public Award getAward(long id);
	public void insertUserAward(UserAward ua);
	public void updateUserAward(UserAward ua);
	public List<UserAward> getUserAward(int userid);
	public Map<Long, UserAward> getUserAwardMap(int userid);
	public UserAward get(long awardId, int userid);
	void delAwardByDay();
}
