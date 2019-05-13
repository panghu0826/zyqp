package com.buding.battle.logic.module.desk;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.hall.config.DeskConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 负责维护玩家的map信息
 * @author Administrator
 *
 */
public class DeskGuard {
	private Logger logger = LogManager.getLogger(getClass());
	private DeskConfig conf;
	
	//玩家id->座位号
	private Map<Integer, Integer> playerIdMap = new HashMap<Integer, Integer>();
	//座位号->玩家信息
	private Map<Integer, PlayerInfo> seatIndexMap = new TreeMap<Integer, PlayerInfo>();
	//玩家id->时间
	private Map<Integer, Long> sitTimeMap = new HashMap<Integer, Long>();
	//玩家id->玩家信息,报名但未准备(即未坐下玩家)座位号
	private Map<Integer, PlayerInfo> erollNotSitMap = new ConcurrentHashMap<>();
	//玩家id->玩家信息,二八下注玩家
	private Map<Integer, PlayerInfo> erBaXiaZhuOrBankerMap = new ConcurrentHashMap<>();
	
	public DeskGuard(DeskConfig conf) {
		this.conf = conf;
	}

    //打乱座位
    public void shufflePlayer() {
	    logger.info("随机打乱前--playerIdMap--"+playerIdMap+"--seatIndexMap--"+seatIndexMap);
        List<Integer> posList = new ArrayList<>(playerIdMap.values());
        Collections.shuffle(posList);
        Map<Integer, Integer> idMapNew = new HashMap<>();
        List<Integer> playerIdList = new ArrayList<>(playerIdMap.keySet());

        for (int i = 0; i < posList.size(); i++) {
            idMapNew.put(playerIdList.get(i),posList.get(i));
        }

        Map<Integer, PlayerInfo> seatIndexMapNew = new HashMap<>();

        for(PlayerInfo p : seatIndexMap.values()){
            for (int i = 0; i < posList.size(); i++) {
                if(p.playerId == playerIdList.get(i)){
                    p.position = posList.get(i);
                    seatIndexMapNew.put(posList.get(i),p);
                }
            }
        }
        playerIdMap = idMapNew;
        seatIndexMap = new TreeMap<>(seatIndexMapNew);
        logger.info("随机打乱后--playerIdMap--"+playerIdMap+"--seatIndexMap--"+seatIndexMap);
    }

	public List<Integer> getplayerIdList() {
		return new ArrayList<>(playerIdMap.keySet());
	}
	
	public List<PlayerInfo> getPlayerList() {
		List<PlayerInfo> set = new ArrayList<PlayerInfo>(seatIndexMap.values());
		return set;
	}
	public Set<PlayerInfo> getPlayerAllList() {
		Set<PlayerInfo> set = new HashSet<PlayerInfo>(seatIndexMap.values());
		set.addAll(erollNotSitMap.values());
		return set;
	}

	public List<PlayerInfo> getNotSitPlayer() {
		return new ArrayList<>(erollNotSitMap.values());
	}

	public List<Integer> getNotSitPlayerIdList() {
		return new ArrayList<>(erollNotSitMap.keySet());
	}


	public synchronized boolean playerSit(PlayerInfo player, int npos) {
//		Assert.isTrue(playerIdMap.get(player.playerId) == null);
		if(seatIndexMap.get(player.position) == null) {
			player.position = npos;
			seatIndexMap.put(npos, player);
			playerIdMap.put(player.playerId, npos);
			sitTimeMap.put(player.playerId, System.currentTimeMillis());
			erollNotSitMap.remove(player.playerId);
			return true;
		}
		return false;
	}

	public synchronized int mergeSitPos(PlayerInfo player, int deskPos) {
		if(isAlreadySit(player)) return -2;

		if(isFull() && checkAllPlayerOk()){
			return -1;
		}

		//玩家已经坐下,但是由于别的操作造成玩家位置变成-1,强制让他坐下原来的位置
		for(Integer pos : seatIndexMap.keySet()){
			PlayerInfo p = seatIndexMap.get(pos);
			if(p.position < 0) {

				//小于0的玩家是本次坐下玩家
				if(player.playerId == p.playerId){
					//如果请求的位置就是这个位置直接返回
					if(pos == deskPos){
						seatIndexMap.remove(pos);
						sitTimeMap.remove(p.playerId);
						playerIdMap.remove(p.playerId);
						erollNotSitMap.remove(p.playerId);
						return pos;
					}else{
						if(isFull() || deskPos < 0 || deskPos> conf.seatSizeUpper) return -1;
						if(seatIndexMap.containsKey(deskPos)){
							seatIndexMap.remove(pos);
							sitTimeMap.remove(p.playerId);
							playerIdMap.remove(p.playerId);
							erollNotSitMap.remove(p.playerId);
						}
						return deskPos;
					}
				}else{//不是本次坐下玩家
					//重新操作让他坐好
					player.position = pos;
				}
			}
		}
		//玩家已经坐在这了
		if(seatIndexMap.get(deskPos) != null && seatIndexMap.get(deskPos).playerId == player.playerId){
		    player.position = deskPos;
		    playerIdMap.put(player.playerId,deskPos);
		    seatIndexMap.put(deskPos,player);
		    sitTimeMap.put(player.playerId,System.currentTimeMillis());
		    erollNotSitMap.remove(player.playerId);
            return deskPos;
        }

		if(hasThisEmptyPos(deskPos,conf.seatSizeUpper)) return deskPos;
		return -2;
	}

	private boolean checkAllPlayerOk() {
		for(Integer pos : seatIndexMap.keySet()) {
			PlayerInfo p = seatIndexMap.get(pos);
			if(p.position < 0 || p.position != pos) return false;
		}
		return true;
	}

	//清除玩家信息
	public synchronized PlayerInfo playerExit(int playerId, String leaveType) {
		logger.info("act=playerLeaveSeat;type={};userId={}", leaveType, playerId);
		Integer playerPos = this.playerIdMap.remove(playerId);
		this.sitTimeMap.remove(playerId);
		if(playerPos != null) {
			//			erollNotSitMap.put(playerId,p);
			return this.seatIndexMap.remove(playerPos);
		}
        if(erollNotSitMap.get(playerId) != null) {
			return erollNotSitMap.remove(playerId);
        }
		return null;
	}

	public synchronized void playerForceExit(PlayerInfo player) {
//		player.position = -1;
//		removePlayer(player);
//		playerIdMap.remove(player.playerId);
//		sitTimeMap.remove(player.playerId);
		erollNotSitMap.put(player.playerId,player);
	}

	//清除玩家信息
	public synchronized PlayerInfo playerExitPosNotRoom(int playerId, String leaveType) {
		logger.info("act=playerExitPosNotRoom;type={};userId={}", leaveType, playerId);
		Integer playerPos = this.playerIdMap.remove((Object)playerId);
		this.sitTimeMap.remove((Object)playerId);
		if(playerPos != null) {
			PlayerInfo p = this.seatIndexMap.remove((Object)playerPos);
			erollNotSitMap.put(playerId,p);
			return p;
		}
		return null;
	}
	
	public PlayerInfo getPlayerById(int playerId) {
		Integer pos = playerIdMap.get((Object)playerId);
		if(pos != null) {
			return seatIndexMap.get((Object)pos);
		} else {
			return erollNotSitMap.get(playerId);
		}
	}
	
	public long getSitdownTime(int playerId) {
		Long t = sitTimeMap.get((Object)playerId);
		return t == null ? -1 : t;
	}
	
	public PlayerInfo getPlayerByPos(int nPos) {
		return seatIndexMap.get(nPos);
	}

	public List<Integer> getSeatPlayerPosList() {
		return new ArrayList<>(seatIndexMap.keySet());
	}
	
	public boolean isEmpty() {
		if(seatIndexMap.isEmpty()) return false;
		for(PlayerInfo p : seatIndexMap.values()){
			if(p.robot != 1) return false;
		}
		return true;
	}
	
	public boolean isFull() {
		return seatIndexMap.size() >= conf.seatSizeUpper;
	}
	
	public boolean isCanStartGame() {
		return seatIndexMap.size() >= conf.seatSizeLower;
	}
	
	public boolean isSatByPlayer(int npos) {
		return seatIndexMap.containsKey(npos);
	}

	public synchronized void playerEroll(PlayerInfo player) {
		erollNotSitMap.put(player.playerId,player);
	}
	
	public synchronized void ready4NextGame() {
		for(int playerId : sitTimeMap.keySet()) {
			sitTimeMap.put(playerId, System.currentTimeMillis());
		}
	}
	
	public int getPlayerCount() {
		return playerIdMap.size();
	}

	public Map<Integer, PlayerInfo> getSeatIndexMap() {
		return seatIndexMap;
	}
	
	//随机获取空位
	public synchronized int getEmptySeat(CommonDesk<?> desk) {
		if(isFull()) {
			return -1;
		}
		//管理员顺序分配
		if(desk.isAdminUse()) {
			for(int i = 0; i < conf.seatSizeUpper; i++) {
				if(isSatByPlayer(i) == false) {
					return i;
				}
			}
			return -1;
		}
		//非管理员随机分配
//		return randomGetSeat();
		return seqGetSeat();
	}

	//
	public synchronized boolean hasThisEmptyPos(int pos,int seatSizeUpper) {
		if(isFull() || seatIndexMap.containsKey(pos) || pos < 0 || pos >= seatSizeUpper) {
			return false;
		}

		return true;
	}
	//
	public synchronized boolean hasThisPos(int pos) {
		return seatIndexMap.containsKey(pos);
	}

	private int seqGetSeat() {
		for(int i = 0; i < conf.seatSizeUpper; i++) {
			if(isSatByPlayer(i) == false) {
				return i;
			}
		}
		return -1;
	}

	private int randomGetSeat() {
		int pos[] = new int[conf.seatSizeUpper];
		for(int i = 0; i < conf.seatSizeUpper; i++) {
			pos[i] = i;
		}
		logger.info(new Gson().toJson(pos));
		int max = conf.seatSizeUpper;
		while(max > 0) {
			int random = (int)(System.currentTimeMillis()%max);
			int position = pos[random];
			if(isSatByPlayer(position) == false) {
				return position;
			}
			int i = pos[max - 1];
			pos[max - 1] = pos[random];
			pos[random] = i;
			
			logger.info(position + " is sat by " + seatIndexMap.get(position).playerId);
			logger.info(position + ":" + new Gson().toJson(pos));
			
			max --;
		}
		throw new RuntimeException("程序出错");
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public synchronized boolean isAlreadySit(PlayerInfo pl) {
		return sitTimeMap.containsKey(pl.playerId) && pl.position >= 0;
	}


	private void removePlayer(PlayerInfo player) {
		for(Map.Entry<Integer,PlayerInfo> entry : seatIndexMap.entrySet()){
			if(entry.getValue().playerId == player.playerId) seatIndexMap.remove(entry.getKey());
		}
	}

    public int getPosById(Integer id) {
	    for(Map.Entry<Integer,PlayerInfo> entry : seatIndexMap.entrySet()){
	        if(entry.getValue().playerId == id) return entry.getKey();
        }
        if(playerIdMap.get(id) == null) return -1;
        return playerIdMap.get(id);
    }

    public void log() {
        logger.error(seatIndexMap+"--seatIndexMap--mergeSitPos");
        logger.error(sitTimeMap+"--sitTimeMap--mergeSitPos");
        logger.error(playerIdMap+"--playerIdMap--mergeSitPos");
    }

	public static void main(String[] args) {
		HashMap<Integer,Integer> map = new HashMap();
		map.put(1,1);
		map.put(2,1);
		removePlayer2(1,map);
		System.out.println(map);
	}


	private static void removePlayer2(int i, Map<Integer,Integer> map) {
		for(Map.Entry<Integer,Integer> entry : map.entrySet()){
			if(entry.getValue() == i) map.remove(entry.getKey());
		}
	}

    public PlayerInfo getErollNotSitPlayerById(int playerId) {
		return erollNotSitMap.get(playerId);
    }
    public PlayerInfo removeErollNotSitPlayerById(int playerId) {
		return erollNotSitMap.remove(playerId);
    }

	public void erBaXiaZhuOrConfirmBanker(PlayerInfo pl) {
		erBaXiaZhuOrBankerMap.put(pl.playerId, pl);
	}

	public List<Integer> getErBaXiaZhuOrBankerPlayerIds() {
		return new ArrayList<>(erBaXiaZhuOrBankerMap.keySet());
	}


	public void clearXiaZhuPlayer() {
		erBaXiaZhuOrBankerMap.clear();
	}
}