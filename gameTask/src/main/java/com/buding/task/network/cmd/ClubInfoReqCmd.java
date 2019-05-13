package com.buding.task.network.cmd;

import com.buding.db.model.*;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.friend.dao.FriendDao;
import com.buding.task.common.TaskConstants;
import com.buding.task.helper.TaskPushHelper;
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
public class ClubInfoReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	ClubDao clubDao;

	@Autowired
    FriendDao friendDao;

	@Override
	public void execute(CmdData data) throws Exception {
        //业务逻辑

		//推送玩家俱乐部列表
        CLUB.ClubInfoRsp.Builder bu = CLUB.ClubInfoRsp.newBuilder();
		List<Club> result = clubDao.selectClubList(data.session.userId);
//		if(result.isEmpty()) return;
		for(Club club : result){
			CLUB.ClubSyn.Builder syn = CLUB.ClubSyn.newBuilder();
			syn.setSynType(TaskConstants.SYN_TYPE_ALL);
			syn.setClubId(club.getId());
			syn.setClubName(club.getClubName());
			bu.addClubs(syn.build());
		}
        //发送消息
		pushHelper.pushClubInfoRsp(data.session, bu.build());

		//推送申请列表
        //申请俱乐部
        CLUB.ApplyInfoRsp.Builder ap = CLUB.ApplyInfoRsp.newBuilder();
        for(Club club : result){
            ClubUser clubUser = clubDao.selectClubUser(club.getId(),data.session.userId);
            if(clubUser != null && clubUser.getClubMemberType() > 0) {
                List<ClubApply> list = clubDao.selectClubALLApply(club.getId());
                if (list == null || list.isEmpty()) continue;
                for (ClubApply apply : list) {
                    CLUB.ApplyInfo.Builder info = CLUB.ApplyInfo.newBuilder();
                    info.setSynType(TaskConstants.SYN_TYPE_ALL);
                    info.setApplyType(TaskConstants.APPLY_TYPE_CLUB);
                    info.setClubId(apply.getClubId());
                    info.setPlayerId(apply.getApplyUserId());
                    info.setPlayerImg(apply.getApplyUserImg());
                    info.setPlayerName(apply.getApplyUserName());
                    ap.addApply(info.build());
                }
            }
        }

        //申请好友
        List<FriendApply> list = friendDao.selectFriendAllApply(data.session.userId);
        if(list != null && !list.isEmpty()) {
            for (FriendApply apply : list) {
                CLUB.ApplyInfo.Builder info = CLUB.ApplyInfo.newBuilder();
                info.setSynType(TaskConstants.SYN_TYPE_ALL);
                info.setApplyType(TaskConstants.APPLY_TYPE_FRIEND);
                info.setPlayerId(apply.getApplyUserId());
                info.setPlayerImg(apply.getApplyUserImg());
                info.setPlayerName(apply.getApplyUserName());
                ap.addApply(info.build());
            }
        }

//        logger.info(JsonFormat.printToString(ap.build()));
        if(ap.getApplyCount() > 0) pushHelper.pushApplyInfoRsp(data.session, ap.build());

	}

	@Override
	public PacketType getKey() {
		return PacketType.ClubInfoReq;
	}

}
