package com.buding.task.network.cmd;

import com.buding.common.token.TokenClient;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.task.helper.TaskPushHelper;
import com.buding.task.network.TaskSession;
import com.buding.task.network.TaskSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.AuthRequest;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class TaskAuthCmd extends TaskBaseCmd {
	@Autowired
	TaskSessionManager sessionManager;

//	@Autowired
//	MsgService msgService;
//
//	@Autowired
//	MsgPushService msgPushService;

	@Autowired
	TokenClient tokenClient;

	@Autowired
	UserSecurityHelper userSecurityHelper;
	
	@Autowired
	TaskPushHelper pushHelper;

	@Override
	public void execute(CmdData data) throws Exception {
		AuthRequest req = AuthRequest.parseFrom(data.packet.getData());

		String token = req.getToken();
//		String token = userSecurityHelper.decrypt(secToken);

		// 会话验证
		TaskSession session = data.session;
		if (!tokenClient.verifyToken(req.getUserId(), token)) {
			pushHelper.pushErrorMsg(session, PacketType.AuthRequest, "token无效");			
			return;
		}

		int playerId = req.getUserId();
		session.userId = playerId;
		sessionManager.put2OnlineList(playerId, session);
		pushHelper.pushAuthRsp(session, PacketType.AuthRequest);

//		msgPushService.onLoginListener(playerId);
//		msgService.onLoginListener(playerId);
	}

	@Override
	public PacketType getKey() {
		return PacketType.AuthRequest;
	}

}
