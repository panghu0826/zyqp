package com.buding.rank.model;

import java.util.Date;

import com.buding.common.db.model.IEntity;

public class RankModel implements IEntity<Integer> {
	public long id;
	public int playerId;
	public int rankPoint;
	public String name;
	public Date mtime;
	public int rank; //当前排名
	public String awardDesc;
	public int vipType;
	public String img;
	public String gameId;

	@Override
	public Integer getIdentity() {
		return playerId;
	}
}
