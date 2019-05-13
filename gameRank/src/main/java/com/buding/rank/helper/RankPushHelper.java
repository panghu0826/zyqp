package com.buding.rank.helper;

import com.buding.db.model.User;
import com.buding.db.model.UserGameOutline;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.type.UserRole;
import com.buding.rank.network.RankSession;
import com.buding.rank.network.RankSessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall.RoomResultResponse;
import packet.game.Hall.VistorRegisterResponse;
import packet.game.Msg;
import packet.msgbase.MsgBase;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.LoginResponse;
import packet.user.User.UserInfoSyn;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class RankPushHelper {
	@Autowired
	RankSessionManager rankSessionManager;
	
	@Autowired
	UserDao userDao;
	
	private Logger logger = LogManager.getLogger(getClass());
	
	public void pushErrorMsg(RankSession session, PacketType type, String msg) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(-1);
		pb.setPacketType(type);
		pb.setMsg(msg);
		rankSessionManager.write(session, pb.build().toByteArray());
	}
	
	public void pushPBMsg(RankSession session, PacketType type, ByteString data) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(0);
		pb.setPacketType(type);
		if(data != null) {
			pb.setData(data);
		}
		rankSessionManager.write(session, pb.build().toByteArray());
	}

	public void pushPBMsg(Integer playerId, PacketType type, ByteString data) {
		RankSession session = rankSessionManager.getIoSession(playerId);
		pushPBMsg(session,type,data);
	}

    public void pushAuthRsp(RankSession session, PacketType authRequest) {
		pushPBMsg(session, PacketType.AuthRequest, null);
    }
}
