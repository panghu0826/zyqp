package com.buding.msg.network.cmd;

import com.buding.common.result.Result;
import com.buding.db.model.UserMsg;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.award.service.AwardService;
import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.msg.helper.MsgPushHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Msg.ReceiveMailAttachRequest;
import packet.msgbase.MsgBase.PacketType;

@Component
public class ReceiveMailAttachCmd extends MsgCmd {

	protected Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	protected MsgPushHelper pushHelper;
		
	@Autowired
	ConfigManager configManager;
	
	@Autowired
	MsgDao msgDao;
	
	@Autowired
	AwardService awardService;
	
	@Override
	public void execute(CmdData data) throws Exception {
		ReceiveMailAttachRequest req = ReceiveMailAttachRequest.parseFrom(data.packet.getData());
		int userId = data.session.userId;
		UserMsg userMsg = msgDao.getUserMsg(userId, req.getMailId());
		if(userMsg == null) {
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "邮件不存在");
			return;
		}
		
		long awardId = userMsg.getAwardId();
		if(awardId  == 0) {
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "邮件没有附件");
			return;
		}
		
		Result ret = awardService.receiveAward(userId, awardId);
		if(ret.isFail()) {
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, ret.msg);
		}
	}

	@Override
	public PacketType getKey() {
		return PacketType.ReceiveMailAttachRequest;
	}	
}
