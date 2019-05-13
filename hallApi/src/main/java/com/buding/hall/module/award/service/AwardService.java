package com.buding.hall.module.award.service;

import com.buding.common.result.Result;
import com.buding.db.model.Award;
import com.buding.db.model.UserMsg;

import java.util.List;

public interface AwardService {
	public long addAward2User(int userId, Award award);
	public void addAward2User(int userId, long awardId);
	public long addAward(Award award);
	public Result receiveAward(int userId, long awardId);

    Result quickReceiveAward(int userId, List<UserMsg> userMsgList);

	List<Long> quickRemoveAward(int userId, List<UserMsg> userMsgList);
}
