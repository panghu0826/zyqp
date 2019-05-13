package com.buding.hall.module.rank.service;

import java.util.Date;

public interface RankService {
	public void addUserRankPoint(int userId, String userName, String gameId, int rankPoint, int rankType, Date date) throws Exception;
}
