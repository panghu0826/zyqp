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
import packet.msgbase.MsgBase.PacketType;

import java.util.List;

@Component
public class QuickReceiveAwardCmd extends MsgCmd {

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
		int userId = data.session.userId;
		List<UserMsg> userMsgList = msgDao.getAllUserMsg(userId);
		if(userMsgList == null || userMsgList.size()==0) {
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "无附件可领取");
			return;
		}

		Result ret = awardService.quickReceiveAward(userId, userMsgList);
		if(ret.isFail()) {
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, ret.msg);
		}
	}

	@Override
	public PacketType getKey() {
		return PacketType.quickReciveAward;
	}	
}
