package com.buding.rank.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.hall.module.rank.service.RankService;

public class RankExternalService implements RankService {
	@Autowired
	UserRankManager userRankManager;
		
	@Override
	public void addUserRankPoint(int userId, String userName, String gameId, int rankPoint, int rankType, Date date) throws Exception {
//		userRankManager.addPoint(rankType, gameId, userId, rankPoint);
	}

}
