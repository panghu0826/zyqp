package com.buding.msg.network.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Msg.ReadMailMsgRequest;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.token.TokenClient;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.msg.helper.MsgPushHelper;
import com.buding.msg.network.MsgSessionManager;
import com.buding.msg.service.MsgPushService;
import com.buding.msg.service.MsgService;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class ReadMailCmd extends MsgCmd {
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
		ReadMailMsgRequest req = ReadMailMsgRequest.parseFrom(data.packet.getData());
		msgService.markMsgRead(data.session.userId, req.getMsgId());
	}

	@Override
	public PacketType getKey() {
		return PacketType.ReadMailMsgRequest;
	}

}
