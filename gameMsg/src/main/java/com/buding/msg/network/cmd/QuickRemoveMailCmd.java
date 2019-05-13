package com.buding.msg.network.cmd;

import com.buding.db.model.Msg;
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

import java.util.ArrayList;
import java.util.List;

@Component
public class QuickRemoveMailCmd extends MsgCmd {

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

		List<Long> msgList = new ArrayList<>();
		for(UserMsg userMsg:userMsgList){
			Msg msg = this.msgDao.getMsg(userMsg.getMsgId());
			if(msg!=null&&msg.getStatus()==10) continue;

			if((userMsg.getAwardId()==null||userMsg.getAwardId()==0) && userMsg.getReaded()){
				msgList.add(userMsg.getMsgId());
			}
		}
		List<Long> msgIdlist = awardService.quickRemoveAward(userId, userMsgList);
		logger.info("msgiD2Remove1111============"+msgList);
		logger.info("msgiD2Remove2222============"+msgIdlist);
		msgList.addAll(msgIdlist);
		if(msgList==null || msgList.size()==0) {
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "无邮件可删除");
			return;
		}
		for(Long msgId:msgList){
			Msg msg = this.msgDao.getMsg(msgId);
			msg.setStatus(10);
			this.msgDao.update(msg);
		}
		pushHelper.pushQuickReciveAwardMsg(data.session, PacketType.quickRemoveMail,msgList);
	}

	@Override
	public PacketType getKey() {
		return PacketType.quickRemoveMail;
	}	
}
