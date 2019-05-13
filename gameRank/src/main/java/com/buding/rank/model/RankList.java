package com.buding.rank.model;

import java.util.Collection;

import com.buding.common.collection.FrozenListMap;

public class RankList extends FrozenListMap<Integer, RankModel> {
	public RankList(Collection<RankModel> paramList) {
		super(paramList);
	}
}
