package com.buding.rank.processor;

import java.util.List;

import com.buding.rank.model.RankModel;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface RankProcessor {
	public boolean refresh();	
	public List<RankModel> getRank(int userId,String gameId);
	public int getRankType();

//	List<RankModel> getRank(int userId, String gameId);
}
