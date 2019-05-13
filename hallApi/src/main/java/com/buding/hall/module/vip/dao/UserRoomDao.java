package com.buding.hall.module.vip.dao;

import com.buding.db.model.*;

import java.util.List;

public interface UserRoomDao {
	public UserRoom getUserRoom(int userId, String matchId);
	public void updateUserRoom(UserRoom room);
	public long addUserRoom(UserRoom room);
	public UserRoom get(long roomId);
	public UserRoom getByCode(String roomCode);
	public boolean isRoomExists(String roomCode);
	public List<UserRoom> getMyRoomList(int playerId);
	public int getMyRoomListCount(int playerId);
	public void updateLastActiveTime(String roomCode);
	
	public long insertUserRoomResultDetail(UserRoomResultDetail detail);
	
	public void insertUserRoomResult(UserRoomResult detail);
	
	public List<UserRoomResult> getUserRoomResultList(long userId);

	List<UserRoomResult> getClubRoomResultList(long clubId);

	public List<UserRoomResultDetail> getUserRoomResultDetailList(long roomId);
	
	public void insertUserRoomGameTrack(UserRoomGameTrack track);

	List<UserRoomResultDetail> getUserPlayingRoomResultDetailList(long roomId);

	void insertFuliCount(FuLiCount count);

	void delUserRoomByDay();

	String getVideoData(long videoId);

    List<Integer> getfuLiPlayerList(String gameId);

	Integer getFuliCount(Integer userId,String startTime,String endTime);
}
