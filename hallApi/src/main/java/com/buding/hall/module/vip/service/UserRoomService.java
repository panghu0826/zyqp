package com.buding.hall.module.vip.service;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.UserRoom;

public interface UserRoomService {
	public Result addRoom(UserRoom room) ;
	
	public Result upsertRoom(UserRoom room);
	
	public UserRoom getMyRoom(int userId, String matchId);
	public UserRoom getMyRoom(int userId, long roomId) ;
		
	public UserRoom getByRoomCode(String roomCode);
	
	public String genUniqCode();
}
