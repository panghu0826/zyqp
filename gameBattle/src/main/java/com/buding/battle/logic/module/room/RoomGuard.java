package com.buding.battle.logic.module.room;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.buding.battle.logic.module.common.Constants;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.DeskStatus;
import com.buding.battle.logic.module.desk.DeskGenerator;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.common.result.TResult;
import com.google.gson.GsonBuilder;

/**
 * 负责维护桌子的map信息
 * @author Administrator
 *
 */
public class RoomGuard {
	//俱乐部桌子
	ConcurrentMap<Long, ConcurrentMap<String, CommonDesk>> clubCommonDeskMap = new ConcurrentHashMap<>();
	ConcurrentMap<Long, ConcurrentMap<String, CommonDesk>> clubJiFenDeskMap = new ConcurrentHashMap<>();
	//所有桌子
	ConcurrentMap<String, CommonDesk> deskMap = new ConcurrentHashMap<String, CommonDesk>();
	//满人的桌子(包括开赛的桌子)
	ConcurrentMap<String, CommonDesk> fullMap = new ConcurrentHashMap<String, CommonDesk>();
	//未满的桌子	
	ConcurrentMap<String, CommonDesk> notFullMap = new ConcurrentHashMap<String, CommonDesk>();
	//桌子生成器
	public transient DeskGenerator deskGenerator;
	
	public RoomGuard(DeskGenerator deskGenerator) {
		this.deskGenerator = deskGenerator;
	}
	
	public TResult<CommonDesk> applyAdminDesk(BattleContext context) throws Exception {
		BattleSession session = context.session;
		Set<CommonDesk> treeSet = new TreeSet<CommonDesk>(new Comparator<CommonDesk>() {
			@Override
			public int compare(CommonDesk o1, CommonDesk o2) {
				return o2.getPlayerCount() - o1.getPlayerCount();
			}
		});
		
		treeSet.addAll(notFullMap.values());
		
		for(CommonDesk desk : treeSet) {
			if(desk.isAdminUse() == true) {
				return TResult.sucess1(desk);
			}
		}
		
		CommonDesk desk = deskGenerator.genDesk(null,context.getWanfa());
		desk.markAsAdminUse();
		notFullMap.put(desk.getDeskID(), desk);
		deskMap.put(desk.getDeskID(), desk);
		addClubDesk(desk);
		return TResult.sucess1(desk);
	}

	private void addClubDesk(CommonDesk desk) {
		if(desk.getClubId() > 0){
			if(desk.getClubRoomType() == Constants.CLUB_COMMON_DESK){
				if(clubCommonDeskMap.get(desk.getClubId()) == null){
                    ConcurrentMap<String, CommonDesk> map = new ConcurrentHashMap<>();
					map.putIfAbsent(desk.getDeskID(),desk);
					clubCommonDeskMap.put(desk.getClubId(),map);
				}else{
					clubCommonDeskMap.get(desk.getClubId()).putIfAbsent(desk.getDeskID(),desk);
				}
			}
			if(desk.getClubRoomType() == Constants.CLUB_JI_FEN_DESK){
				if(clubJiFenDeskMap.get(desk.getClubId()) == null){
                    ConcurrentMap<String, CommonDesk> map = new ConcurrentHashMap<>();
                    map.putIfAbsent(desk.getDeskID(),desk);
					clubJiFenDeskMap.put(desk.getClubId(),map);
				}else{
					clubJiFenDeskMap.get(desk.getClubId()).putIfAbsent(desk.getDeskID(),desk);
				}
			}
		}
	}

	private void removeClubDesk(CommonDesk desk) {
		if(desk.getClubId() > 0){
			if(desk.getClubRoomType() == Constants.CLUB_COMMON_DESK){
				if(clubCommonDeskMap.get(desk.getClubId()) == null){
					return;
				}else{
					clubCommonDeskMap.get(desk.getClubId()).remove(desk.getDeskID());
				}
			}
			if(desk.getClubRoomType() == Constants.CLUB_JI_FEN_DESK){
				if(clubJiFenDeskMap.get(desk.getClubId()) != null){
					clubJiFenDeskMap.get(desk.getClubId()).remove(desk.getDeskID());
				}
			}
		}
	}

	public TResult<CommonDesk> applyEmptyDesk(BattleContext context) throws Exception {
		CommonDesk desk = deskGenerator.genDesk(null,context.getWanfa());
		notFullMap.put(desk.getDeskID(), desk);
		deskMap.put(desk.getDeskID(), desk);
		addClubDesk(desk);
		return TResult.sucess1(desk);
	}
	
	/**
	 * 此处应有策略管理
	 * @return
	 */
	public TResult<CommonDesk> applyDesk(BattleContext context) throws Exception {
		BattleSession session = context.session;
		if(session.isAdmin()) {
			return applyAdminDesk(context);
		}
		Set<CommonDesk> treeSet = new TreeSet<CommonDesk>(new Comparator<CommonDesk>() {
			@Override
			public int compare(CommonDesk o1, CommonDesk o2) {
				return o2.getPlayerCount() - o1.getPlayerCount();
			}
		});
		
		treeSet.addAll(notFullMap.values());
		
		for(CommonDesk desk : treeSet) {
			if(session.recentDeskId.containsKey(desk.getDeskID()) || desk.isAdminUse() == true) {
				continue;
			}			
			return TResult.sucess1(desk);
		}
		
		CommonDesk desk = deskGenerator.genDesk(null,context.getWanfa());
		notFullMap.put(desk.getDeskID(), desk);
		deskMap.put(desk.getDeskID(), desk);
		addClubDesk(desk);
		return TResult.sucess1(desk);
	}
	
	public void playerLeave(CommonDesk desk, PlayerInfo player) {
		if(!desk.isFull()) {
			fullMap.remove(desk.getDeskID());
			notFullMap.put(desk.getDeskID(), desk);
		}
	}
	
	public void playerSit(CommonDesk desk, PlayerInfo player) {
		if(desk.isFull()) {
			notFullMap.remove(desk.getDeskID());
			fullMap.put(desk.getDeskID(), desk);
		}
	}
	
	public void destroyDesk(CommonDesk desk) {
		this.deskMap.remove(desk.getDeskID());
		this.fullMap.remove(desk.getDeskID());
		this.notFullMap.remove(desk.getDeskID());
        removeClubDesk(desk);
	}
	
	public void gameStart(CommonDesk desk) {
		this.notFullMap.remove(desk.getDeskID());
		this.fullMap.put(desk.getDeskID(), desk);
	}
	
	public CommonDesk getDeskById(String deskId) {
		return deskMap.get(deskId);
	}
	
	public synchronized CommonDesk tryAddDesk(CommonDesk desk) {
		if(this.deskMap.containsKey(desk.getDeskID())) {
			return this.deskMap.get(desk.getDeskID());
		}
		this.deskMap.put(desk.getDeskID(), desk);
		this.notFullMap.put(desk.getDeskID(), desk);
		addClubDesk(desk);
		return desk;
	}
	
	public void check() {
		//移除已销毁的桌子
		for(CommonDesk desk : deskMap.values()) {
			if(desk.getStatus() == DeskStatus.DESTROYED) {
				deskMap.remove(desk.getDeskID());
				fullMap.remove(desk.getDeskID());
				notFullMap.remove(desk.getDeskID());
                removeClubDesk(desk);
			}
		}
		
		//将人满的桌子移入fullMap
		for(CommonDesk desk : notFullMap.values()) {
			if(desk.isFull()) {
				notFullMap.remove(desk.getDeskID());
				fullMap.put(desk.getDeskID(), desk);
			}
		}
		
		//将未满的桌子移入notFullMap
		for(CommonDesk desk : fullMap.values()) {
			if(desk.isFull() == false) {
				fullMap.remove(desk.getDeskID());
				notFullMap.put(desk.getDeskID(), desk);
			}
		}
	}

	public ConcurrentMap<String, CommonDesk> getDeskMap() {
		return deskMap;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

    public ConcurrentMap<Long, ConcurrentMap<String, CommonDesk>> getAllClubCommonDeskMap() {
        return clubCommonDeskMap;
    }

    public List<CommonDesk> getClubCommonDeskMap(Long clubId) {
	    if(clubCommonDeskMap.get(clubId) == null) return new ArrayList<>();
        return new ArrayList<>(clubCommonDeskMap.get(clubId).values());
    }

    public List<CommonDesk> getClubJiFenDeskMap(Long clubId) {
        if(clubJiFenDeskMap.get(clubId) == null) return new ArrayList<>();
        return new ArrayList<>(clubJiFenDeskMap.get(clubId).values());
    }

    public ConcurrentMap<Long, ConcurrentMap<String, CommonDesk>> getAllClubJiFenDeskMap() {
        return clubJiFenDeskMap;
    }


}
