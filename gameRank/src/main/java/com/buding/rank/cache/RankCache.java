package com.buding.rank.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.buding.db.model.UserRank;
import com.buding.rank.model.MyRankRsp;
import com.buding.rank.model.RankItem;
import com.buding.rank.model.RankList;
import com.buding.rank.model.RankModel;
import com.buding.rank.model.RankRsp;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class RankCache {
	RankList rankList = null;
	
	volatile long version = 0;
	public String groupId;

	private static final ConcurrentLinkedHashMap.Builder<Integer, List<RankModel>> BUILDER = new ConcurrentLinkedHashMap.Builder<Integer, List<RankModel>>();
	private static final ConcurrentLinkedHashMap<Integer, List<RankModel>> RANK_CACHE = BUILDER.maximumWeightedCapacity(2000).build();

	private int maxRank = 0;
	
	public void reinit(List<UserRank> list) {		
		List<RankModel> ll = sort(list);

		rankList = new RankList(ll);
 
		version = System.currentTimeMillis();
		
		if(list.isEmpty()) {
			return;
		}
		
		maxRank = rankList.getByInd(rankList.size() - 1).rank;
		
//		printRankList(ll);
	}

	private List<RankModel> sort(List<UserRank> list) {
		List<RankModel> ll = new ArrayList<RankModel>();
		for (UserRank rank : list) {
			RankModel model = new RankModel();
			model.playerId = rank.getUserId();
			model.rankPoint = rank.getRankPoint();
			model.name = rank.getUserName();
			model.mtime = rank.getMtime();
			model.awardDesc = rank.getAwardDesc();
			model.img = "";
			ll.add(model);
		}

		Collections.sort(ll, new Comparator<RankModel>() {
			@Override
			public int compare(RankModel o1, RankModel o2) {
				int diff = o1.rankPoint - o2.rankPoint;
				if (diff == 0) {
					return -1;
				}
				return -1 * diff / Math.abs(diff);
			}
		});

		int i = 0;
		for (RankModel model : ll) {
			model.rank = ++i;
		}
		return ll;
	}
	
	public Set<Integer> getShowIndexs(int totalSize, int myIdex, int aheadSize, int afterSize, int firstSize, int middelSize) {
		Set<Integer> set = new TreeSet<Integer>();
		int showCount = aheadSize + afterSize + firstSize + middelSize + 1;
		if(showCount >= totalSize) {
			for(int i = 0; i < totalSize; i++) {
				set.add(i);
			}
			return set;
		}
		
		//保证有200个名次
		//1-10一定要显示
		//我前10个，后10个一定要显示.
		
		//如果我在榜首
		{
			
		}
		
		//如果我在榜中间
		//如果我在榜尾
		//如果我没出现在榜中
		return set;
	}
	
	public MyRankRsp getNewestRankRsp(int userId) {
		RankModel model = rankList.getByKey(userId);
		MyRankRsp rsp = new MyRankRsp();
		rsp.groupId = groupId;
		
		if(model == null) {
			rsp.rank = 0;
			rsp.rankPoint = 0;
		} else {
			rsp.rank = model.rank;
			rsp.rankPoint = model.rankPoint;
		}		
		return rsp;
	}
	
	public RankRsp getUserRankRsp(int userId, long version) {
		RankRsp rsp = new RankRsp();
		RankModel m = rankList.getByKey(userId);
		if(m == null) {
			rsp.myRank.awardDesc = "";
			rsp.myRank.rankPoint = 0;//TODO
			rsp.myRank.rank = -1 * maxRank;
		} else {
			rsp.myRank.awardDesc = m.awardDesc;
			rsp.myRank.rank = m.rank;
			rsp.myRank.rankPoint = m.rankPoint;
		}
		
		RankModel model = rankList.getByKey(userId);
		if(model != null) {
			rsp.myRank.rank = model.rank;
			rsp.myRank.rankPoint = model.rankPoint;
		} else {
			rsp.myRank.rank = -1;
			rsp.myRank.rankPoint = -1;
		}
		
		rsp.version = this.version;
		rsp.groupId = groupId;
		
		if(this.version == version) {
			return rsp;
		}
				
		List<RankModel> list = getUserRankList(userId);
		for(RankModel rm : list) {
			RankItem item = new RankItem();
			item.name = rm.name;
			item.rank = rm.rank;
			item.rankPoint = rm.rankPoint;
			item.img = rm.img;
			item.awardDesc = rm.awardDesc;
			rsp.data.add(item);
		}
		return rsp;
	}

	public List<RankModel> getUserRankList(int userId) {
		List<RankModel> list = RANK_CACHE.get(userId);
		if (list != null) {
			printRankList(list);
			return list;
		}

		RankModel model = rankList.getByKey(userId);
		if (model == null) {
			List<RankModel> subList = rankList.subList(0, 100);
			printRankList(subList);
			return subList;
		}

		int aheadSize = 5;
		int afterSize = 5;
		int firstSize = 5;
		int middelSize = 20;
		int showCount = aheadSize + afterSize + firstSize + middelSize + 1;
		
		if(showCount >= rankList.size()) {
			List<RankModel> subList = rankList.subList(0, rankList.size() + 1);
			printRankList(subList);
			return subList;
		}
		
		int myInd = rankList.indexOf(userId);
		LinkedList<RankModel> ret = new LinkedList<RankModel>();
		ret.add(model);
		for (int ind = myInd + 1; ind < rankList.size() && ind - myInd <= afterSize; ind++) {// 我后面的5个排名
			ret.addLast(rankList.getByInd(ind));
		}

		int ahead = 0;
		for (ahead = myInd - 1; ahead >= 0 && myInd - ahead <= aheadSize; ahead--) {
			ret.addFirst(rankList.getByInd(ahead));
		}

		LinkedList<RankModel> ret2 = new LinkedList<RankModel>();
		for (int first = 0; first < firstSize && first < rankList.size() && first < ahead; first++) {
			ret2.add(first, rankList.getByInd(first));
		}
		
		int delta = 1;
		delta = (ahead - 10)/middelSize;
		if(delta <= 10) {
			delta = 1;
		}
		for (int mid = ahead - delta; mid >= 10 && mid < rankList.size(); mid -= delta) {
			ret.addFirst(rankList.getByInd(mid-1));
		}

		ret2.addAll(ret);

		RANK_CACHE.put(userId, ret2);

		printRankList(ret2);

		return ret2;
	}
	
	public void printRankList(List<RankModel> list) {
		for(RankModel model : list) {
			System.out.println(model.rank + " " + model.playerId+" " + model.rankPoint);
		}
	}

	public RankList getRankList() {
		return rankList;
	}	
}
