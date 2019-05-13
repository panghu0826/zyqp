package com.buding.task.network.cmd;

import com.buding.db.model.Club;
import com.buding.db.model.ClubUser;
import com.buding.hall.module.club.dao.ClubDao;
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

import java.util.List;

/**
 * @author jaime qq_1094086610
 *
 */
@Component
public class ClubMemberReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	ClubDao clubDao;

	@Autowired
	TaskSessionManager sessionManager;

	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ClubMemberReq ur = CLUB.ClubMemberReq.parseFrom(data.packet.getData());
		//逻辑校验
		Club club = clubDao.selectClub(ur.getClubId());
		CLUB.ClubMemberRsp.Builder bu = CLUB.ClubMemberRsp.newBuilder();
		if(club == null) {
			pushHelper.pushClubMemberRsp(data.session, bu.build());
			return;
		}
        //业务逻辑
		List<ClubUser> list = clubDao.selectClubAllUser(ur.getClubId());
		for(ClubUser clubUser : list){
			CLUB.ClubMemberSyn.Builder syn = CLUB.ClubMemberSyn.newBuilder();
			syn.setSynType(TaskConstants.SYN_TYPE_ALL);
			syn.setClubId(clubUser.getClubId());
			syn.setMemberId(clubUser.getClubMemberId());
			syn.setMemberImg(clubUser.getClubMemberImg());
			syn.setMemberName(clubUser.getClubMemberName());
			syn.setMemberScore(clubUser.getClubMemberScore());
			syn.setMemberType(clubUser.getClubMemberType());
			syn.setOnline(sessionManager.isOnline(clubUser.getClubMemberId()) ? 1 : 0);
			bu.addMembers(syn.build());
		}
		logger.info("成员列表--"+ JsonFormat.printToString(bu.build()));

        //发送消息
		pushHelper.pushClubMemberRsp(data.session, bu.build());
	}

	@Override
	public PacketType getKey() {
		return PacketType.ClubMemberReq;
	}

}
