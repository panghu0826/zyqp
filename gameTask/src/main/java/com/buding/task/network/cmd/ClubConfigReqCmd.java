package com.buding.task.network.cmd;

import com.buding.db.model.Club;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.task.helper.TaskPushHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.msgbase.MsgBase.PacketType;

/**
 * @author jaime qq_1094086610
 *
 */
@Component
public class ClubConfigReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
    ClubDao clubDao;


	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ClubConfigReq ur = CLUB.ClubConfigReq.parseFrom(data.packet.getData());
        //业务逻辑
        CLUB.ClubConfigSyn.Builder bu = CLUB.ClubConfigSyn.newBuilder();
        Club club = clubDao.selectClub(ur.getClubId());
        if(club == null){
//            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该俱乐部不存在");
            pushHelper.pushClubConfigSyn(data.session.userId, bu.build());
            return;
        }
        bu.setClubId(club.getId());
        bu.setWanfa(club.getClubWanfa());
        bu.setCreateRoomMode(club.getCreateRoomMode());
        bu.setNotice(club.getClubNotice());
        bu.setClubName(club.getClubName());
        bu.setClubRoomCanFuFen(club.getCanFufen());
        bu.setClubRoomEnterScore(club.getEnterScore());
        bu.setClubRoomChouShuiScore(club.getChoushuiScore());
        bu.setClubRoomChouShuiNum(club.getChoushuiNum());
        bu.setClubRoomZengSongNum(club.getZengsongNum());
        //发送消息
		pushHelper.pushClubConfigSyn(data.session.userId, bu.build());
	}

	@Override
	public PacketType getKey() {
		return PacketType.ClubConfigReq;
	}

}
