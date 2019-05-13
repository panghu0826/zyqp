package com.buding.hall.module.vip.service;

import java.util.Date;

import com.buding.common.network.session.SessionManager;
import com.googlecode.protobuf.format.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.common.util.DesUtil;
import com.buding.db.model.UserRoom;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.config.RoomConfig;
import com.buding.hall.module.common.constants.RoomState;
import com.buding.hall.module.vip.dao.UserRoomDao;
import packet.game.MsgGame;
import packet.msgbase.MsgBase;

@Component
public class UserRoomServiceImpl implements UserRoomService  {
	@Autowired
	UserRoomDao userRoomDao;
	
	@Autowired
	ConfigManager configManager;

	@Autowired
	SessionManager hallSessionManager;
	
	public Result addRoom(UserRoom room) {
//		UserRoom old = userRoomDao.getUserRoom(room.getOwnerId(), room.getMatchId());
//		if(old != null) {
//			return Result.fail("你已经拥有房间，不能重复创建");
//		}
		room.setCtime(new Date());
		room.setMtime(new Date());
		userRoomDao.addUserRoom(room);
				
		return Result.success();
	}	 
	
	public Result upsertRoom(UserRoom room) {
		if(room.getId() != null) {
			userRoomDao.updateUserRoom(room);
			return Result.success();
		}
		return addRoom(room);
	}
	
	public UserRoom getMyRoom(int userId, String matchId) {
		return userRoomDao.getUserRoom(userId, matchId);
	}
	
	public UserRoom getMyRoom(int userId, long roomId) {
		UserRoom room = userRoomDao.get(roomId);
		return room == null || room.getOwnerId() != userId ? null : room;
	}

	@Override
	public UserRoom getByRoomCode(String roomCode) {
		String code = DesUtil.md5(roomCode, 16);
		return this.userRoomDao.getByCode(roomCode);
	}

	@Override
	public String genUniqCode() {
		int tryCount = 0;
		do {
			String random = System.nanoTime()+"";
			String code = random.substring(random.length() - 6);
			if(userRoomDao.isRoomExists(code) == false) {
				return code;
			}
		} while (tryCount ++ < 100);
		return null;
	}

}
