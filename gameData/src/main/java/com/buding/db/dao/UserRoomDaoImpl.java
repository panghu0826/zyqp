package com.buding.db.dao;

import com.buding.api.context.PokerZJHFinalResult;
import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.*;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserRoomDaoImpl extends CachedServiceAdpter implements UserRoomDao {
	@Autowired
	DbService dbService;
	
	@Override
	public UserRoom getUserRoom(int userId, String matchId) {
		return this.getOne("select * from user_room where owner_id = ? and match_id = ? and room_state = 1 ", UserRoom.class, userId, matchId);
	}
	
	@Override
	public UserRoom getByCode(String roomCode) {
		return this.getOne("select * from user_room where room_code = ? and room_state = 1", UserRoom.class, roomCode);
	}

	@Override
	public void updateUserRoom(UserRoom model) {
		this.put2EntityCache(model);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(model);
		} else {
			this.dbService.submitUpdate2Queue(model);
		}
	}

	@Override
	public long addUserRoom(UserRoom room) {
		this.commonDao.save(room);
		return room.getId();		
	}

	@Override
	public UserRoom get(long roomId) {
		return this.commonDao.get(roomId, UserRoom.class);
	}

	@Override
	public List<UserRoom> getMyRoomList(int playerId) {
		return this.getList("select * from user_room where owner_id = ? and room_state = 1", UserRoom.class, playerId);
	}

	@Override
	public int getMyRoomListCount(int playerId) {
		return this.commonDao.count("select ifnull(count(*), 0) as c from user_room where owner_id = ? and room_state = 1",  playerId);
	}

	@Override
	public boolean isRoomExists(String roomCode) {
		return this.commonDao.count("select ifnull(count(*), 0) as c from user_room where room_code = ?", roomCode) > 0;
	}

	@Override
	public void updateLastActiveTime(String roomCode) {
		UserRoom room = this.getByCode(roomCode);
		if(room != null) {
			room.setLastActiveTime(new Date());
			this.updateUserRoom(room);
		}
	}

	@Override
	public long insertUserRoomResultDetail(UserRoomResultDetail detail) {
		this.commonDao.save(detail);
		return detail.getId();
	}
	
	@Override
	public void insertUserRoomResult(UserRoomResult detail) {
		this.commonDao.save(detail);
	}

	@Override
	public void insertUserRoomGameTrack(UserRoomGameTrack track) {
		this.commonDao.save(track);
	}

	@Override
	public List<UserRoomResultDetail> getUserPlayingRoomResultDetailList(long userId) {
		UserRoomGameTrack t = this.getOne("select * from user_room_game_track where user_id = ? order by game_time desc limit 1", UserRoomGameTrack.class, userId);
		if(t==null) return null;
		List<UserRoomResult> list = this.getList("select * from user_room_result where room_id = ? ", UserRoomResult.class, t.getRoomId());
		if(list==null||list.isEmpty()){
			return this.getList("select * from user_room_result_detail where room_id = ? order by start_time desc", UserRoomResultDetail.class, t.getRoomId());
		}
		return null;
	}


	@Override
	public List<UserRoomResult> getUserRoomResultList(long userId) {
		String sql = "select * from user_room_game_track where user_id = ? order by game_time desc limit 15";
		List<UserRoomGameTrack> list = this.getList(sql, UserRoomGameTrack.class, userId);
		List<UserRoomResult> retList = new ArrayList<UserRoomResult>();
		for(UserRoomGameTrack t : list) {
			UserRoomResult model = this.getOne("select * from user_room_result where room_id = ? order by start_time limit 1", UserRoomResult.class, t.getRoomId());
			if(model!=null){
				retList.add(model);
			}
		}
		return retList;
	}

	@Override
	public List<UserRoomResult> getClubRoomResultList(long clubId) {
		return this.getList("select * from user_room_result where club_id = ? and club_room_type = 1", UserRoomResult.class, clubId);
	}

	@Override
	public List<UserRoomResultDetail> getUserRoomResultDetailList(long roomId) {
		return this.getList("select * from user_room_result_detail where room_id = ? order by start_time desc", UserRoomResultDetail.class, roomId);
	}

	@Override
	public String getVideoData(long videoId) {
		UserRoomResultDetail model = this.getOne("select * from user_room_result_detail where video_id = ?", UserRoomResultDetail.class, videoId);
		if(model == null) return null;
		return model.getVideoDetail();
	}
	@Override
	public List<Integer> getfuLiPlayerList(String gameId){
        List<FuLi> list  = this.commonDao.selectList("select * from t_fuli where game_id = ? ", FuLi.class,gameId);
        List<Integer> r = new ArrayList<>();
        if(list == null || list.isEmpty()) return r;
        for(FuLi fuLi : list){
            r.add(fuLi.getPlayerId());
        }
		return r;
	}

	@Override
	public void insertFuliCount(FuLiCount count) {
		this.commonDao.save(count);
	}

	@Override
	public Integer getFuliCount(Integer userId,String startTime,String endTime) {
		Gson gson = new Gson();
		String sql = "SELECT " +
				" c.detail " +
				" FROM " +
				" ( " +
				" SELECT " +
				" b.game_count, " +
				" b.room_id, " +
				" b.detail, " +
				" b.end_time " +
				" FROM " +
				" user_room_game_track a, " +
				" user_room_result_detail b " +
				" WHERE " +
				" a.room_id = b.room_id " +
				" AND a.user_id = ? " +
				" ) c " +
				" INNER JOIN ( " +
				" SELECT " +
				" max(e.game_count) game_count, " +
				" e.room_id, " +
				" e.detail, " +
				" e.end_time " +
				" FROM " +
				" user_room_game_track d, " +
				" user_room_result_detail e " +
				" WHERE " +
				" d.room_id = e.room_id " +
				" AND d.user_id = ? " +
				" GROUP BY " +
				" room_id " +
				" ) f ON c.game_count = f.game_count " +
				" AND c.room_id = f.room_id " +
				" AND c.end_time >= ? " +
				" AND c.end_time <= ? ";
		Object[] args = new Object[4];
		args[0] = userId;
		args[1] = userId;
		args[2] = startTime;
		args[3] = endTime;
		List<FuliCountNum> list = this.commonDao.selectList(sql, FuliCountNum.class,args);
		int all = 0;
		for (FuliCountNum countNum : list) {
		    int num = 0;
		    try {
                List<PokerZJHFinalResult> results = gson.fromJson(countNum.getDetail(), new TypeToken<List<PokerZJHFinalResult>>(){}.getType());
                PokerZJHFinalResult result = null;
                for (PokerZJHFinalResult r : results) {
                    if (r.playerId == userId) result = r;
                }
                if (result != null) num = result.allScore;
            }catch (Exception e) {
		        e.printStackTrace();
            }
            all += num;
		}
		return all;
	}

	@Override
	public void delUserRoomByDay() {
		this.commonDao.delete(null,UserRoom.class);
		this.commonDao.delete(null,UserRoomGameTrack.class);
		this.commonDao.delete(null,UserRoomResult.class);
		this.commonDao.delete(null,UserRoomResultDetail.class);
	}
}
