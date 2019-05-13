package com.buding.rank.model;

import java.util.ArrayList;
import java.util.List;

import com.buding.common.network.model.BaseRsp;

public class RankRsp extends BaseRsp {
	public long version;
	public String groupId;
	public MyRank myRank = new MyRank();
	public List<RankItem> data = new ArrayList<RankItem>();

	public long getVersion() {
		return version;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}

	public MyRank getMyRank() {
		return myRank;
	}

	public void setMyRank(MyRank myRank) {
		this.myRank = myRank;
	}

	public List<RankItem> getData() {
		return data;
	}

	public void setData(List<RankItem> data) {
		this.data = data;
	}
}
