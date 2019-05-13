package com.buding.msg.helper;

import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Msg;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.msg.network.MsgSession;
import com.buding.msg.network.MsgSessionManager;
import com.google.protobuf.ByteString;

import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class MsgPushHelper {
	@Autowired
	MsgSessionManager msgSessionManager;

	private Logger logger = LogManager.getLogger(getClass());

	public void pushErrorMsg(MsgSession session, PacketType type, String msg) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(-1);
		pb.setPacketType(type);
		pb.setMsg(msg);
		msgSessionManager.write(session, pb.build().toByteArray());
	}
	
	public void pushPBMsg(int userId, PacketType type, ByteString data) {
		MsgSession session = msgSessionManager.getIoSession(userId);
		if(session != null) {
			pushPBMsg(session, type, data);
		}
	}

	public void pushPBMsg(MsgSession session, PacketType type, ByteString data) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(0);
		pb.setPacketType(type);
		if(data != null) {
			pb.setData(data);
		}
		msgSessionManager.write(session, pb.build().toByteArray());
	}

	public void pushQuickReciveAwardMsg(MsgSession session, PacketType type, List<Long> msgIdlist) {
		Msg.quickOperResponse.Builder qb = Msg.quickOperResponse.newBuilder();
		qb.addAllMailId(msgIdlist);
		logger.info("userid"+session.userId+"pushQuickReciveAwardMsg"+ JsonFormat.printToString(qb.build()));
		pushPBMsg(session, type,qb.build().toByteString());
	}
	
	public void pushAuthRsp(MsgSession session, PacketType type) {
		pushPBMsg(session, PacketType.AuthRequest, null);
	}
}
