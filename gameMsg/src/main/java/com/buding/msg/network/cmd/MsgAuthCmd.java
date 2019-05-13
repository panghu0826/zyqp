package com.buding.msg.network.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketType;
import packet.user.User.AuthRequest;

import com.buding.common.token.TokenClient;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.msg.helper.MsgPushHelper;
import com.buding.msg.network.MsgSession;
import com.buding.msg.network.MsgSessionManager;
import com.buding.msg.service.MsgPushService;
import com.buding.msg.service.MsgService;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class MsgAuthCmd extends MsgCmd {
	@Autowired
	MsgSessionManager sessionManager;

	@Autowired
	MsgService msgService;

	@Autowired
	MsgPushService msgPushService;

	@Autowired
	TokenClient tokenClient;

	@Autowired
	UserSecurityHelper userSecurityHelper;
	
	@Autowired
	MsgPushHelper pushHelper;

	@Override
	public void execute(CmdData data) throws Exception {
		AuthRequest req = AuthRequest.parseFrom(data.packet.getData());

		String token = req.getToken();
//		String token = userSecurityHelper.decrypt(secToken);

		// 会话验证
		MsgSession session = data.session;
		if (!tokenClient.verifyToken(req.getUserId(), token)) {
			pushHelper.pushErrorMsg(session, PacketType.AuthRequest, "token无效");			
			return;
		}

		int playerId = req.getUserId();
		session.userId = playerId;
		sessionManager.put2OnlineList(playerId, session);

		pushHelper.pushAuthRsp(session, PacketType.AuthRequest);

		msgPushService.onLoginListener(playerId);
		msgService.onLoginListener(playerId);
	}

	@Override
	public PacketType getKey() {
		return PacketType.AuthRequest;
	}

}
