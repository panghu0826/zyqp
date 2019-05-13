package com.buding.task.network.cmd;

import com.buding.db.model.UserRemark;
import com.buding.db.model.UserRoom;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.friend.dao.FriendDao;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.task.helper.TaskPushHelper;
import com.buding.task.network.TaskSessionManager;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.msgbase.MsgBase.PacketType;

import java.util.List;

/**
 * @author jaime qq_1094086610
 * 申请结果Req
 */
@Component
public class UpdateRemarkReqCmd extends TaskBaseCmd {
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
		CLUB.UpdateRemarkReq ur = CLUB.UpdateRemarkReq.parseFrom(data.packet.getData());
		logger.info("ur--"+ JsonFormat.printToString(ur.toBuilder().build()));

		List<UserRemark> list = clubDao.selectUserRemarkByUserId(data.session.userId);
		CLUB.UpdateRemarkRsp.Builder rsp = CLUB.UpdateRemarkRsp.newBuilder();
		if(list != null && !list.isEmpty()){
			for(UserRemark remark : list){
				CLUB.RemarkSyn.Builder bu = CLUB.RemarkSyn.newBuilder();
				bu.setPlayerId(remark.getRemarkUserId());
				bu.setPlayerName(remark.getRemarkUserName());
				rsp.addRemark(bu.build());
			}
		}

		pushHelper.pushUpdateRemarkRsp(data.session,rsp.build());

	}

	@Override
	public PacketType getKey() {
		return PacketType.UpdateRemarkReq;
	}

}
