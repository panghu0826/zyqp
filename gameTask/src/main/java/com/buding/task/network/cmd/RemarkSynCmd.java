package com.buding.task.network.cmd;

import com.buding.db.model.*;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.friend.dao.FriendDao;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.task.common.TaskConstants;
import com.buding.task.helper.TaskPushHelper;
import com.buding.task.network.TaskSessionManager;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.msgbase.MsgBase.PacketType;

import java.util.Date;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * 申请结果Req
 */
@Component
public class RemarkSynCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	ClubDao clubDao;

	@Autowired
	FriendDao friendDao;

	@Autowired
	UserDao userDao;

	@Autowired
    ChatDao chatDao;

    @Autowired
    TaskSessionManager sessionManager;

	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.RemarkSyn ur = CLUB.RemarkSyn.parseFrom(data.packet.getData());
		logger.info("ur--"+ JsonFormat.printToString(ur.toBuilder().build()));
		if(data.session.userId <= 0) return;

		UserRemark remark = clubDao.selectUserRemark(data.session.userId, (int) ur.getPlayerId());
		if(remark == null){
			remark = new UserRemark();
			remark.setRemarkUserName(ur.getPlayerName());
			remark.setRemarkUserId((int) ur.getPlayerId());
			remark.setUserId(data.session.userId);
			clubDao.insertUserRemark(remark);
		}else{
			remark.setRemarkUserName(ur.getPlayerName());
			clubDao.updateUserRemark(remark);
		}
		pushHelper.pushRemarkSyn(data.session,ur.toBuilder().build());
	}

	@Override
	public PacketType getKey() {
		return PacketType.RemarkSyn;
	}

}
