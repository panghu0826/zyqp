package com.buding.task.network.cmd;

import com.buding.db.model.*;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.friend.dao.FriendDao;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.task.common.TaskConstants;
import com.buding.task.helper.TaskPushHelper;
import com.buding.task.network.TaskSessionManager;
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
public class ClubAuthorityManageReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
    UserDao userDao;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
    FriendDao friendDao;

    @Autowired
    ClubDao clubDao;

    @Autowired
    ChatDao chatDao;

    @Autowired
    TaskSessionManager sessionManager;

	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ClubAuthorityManageReq ur = CLUB.ClubAuthorityManageReq.parseFrom(data.packet.getData());
		int type = ur.getType();
        User user = userDao.getUser((int) ur.getPlayerId());
        if(user == null){
            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家未找到");
            return;
        }
        ClubUser clubOwnerUser = clubDao.selectClubOwnerUser(ur.getClubId());
        ClubUser opPlayer = clubDao.selectClubUser(ur.getClubId(), (int) ur.getPlayerId());
        if(clubOwnerUser.getClubMemberId() != data.session.userId){
            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "您们不是该俱乐部群主无权操作");
            return;
        }
		switch (type){
            case TaskConstants.AUTH_TYPE_TRANSFER_OWNER: {
                if (clubOwnerUser.getClubMemberId() == ur.getPlayerId()) {
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家就是该群群主");
                    return;
                }
                int scoreModify = -clubOwnerUser.getClubMemberScore();

                clubOwnerUser.setClubMemberId((int) ur.getPlayerId());
                clubDao.updateClubUser(clubOwnerUser);

                if(opPlayer == null) {
                    ClubUser clubUserOld = new ClubUser();
                    clubUserOld.setClubId(ur.getClubId());
                    clubUserOld.setClubMemberId(data.session.userId);
                    clubUserOld.setClubMemberScore(0);
                    clubUserOld.setClubMemberType(TaskConstants.CLUB_MEMEBER_TYPE_COMMON);
                    clubUserOld.setCtime(new Date());
                    clubDao.insertClubUser(clubUserOld);
                }else{
                    opPlayer.setClubMemberId(data.session.userId);
                    opPlayer.setClubMemberScore(0);
                    clubDao.updateClubUser(opPlayer);
                }

                //记录日志
                ClubScoreLog log = new ClubScoreLog();
                log.setClubId(ur.getClubId());
                log.setPlayerId(data.session.userId);
                log.setScoreModify(scoreModify);
                log.setScoreLeft(0);
                log.setMtime(new Date());
                log.setType(TaskConstants.SCORE_LOG_TRANSFER_OWNER);
                log.setInfo("转让群主积分清零");
                clubDao.insertClubScoreLog(log);

                //聊天列表
                {
                    Chat chat = chatDao.selectClubChat(ur.getClubId());
                    CLUB.ChatListSyn.Builder chatBu = CLUB.ChatListSyn.newBuilder();
                    chatBu.setSynType(TaskConstants.SYN_TYPE_ADD);
                    chatBu.setChatId(chat.getId());
                    chatBu.setClubId(ur.getClubId());
                    chatBu.setType(chat.getChatType());
                    pushHelper.pushChatListSyn((int) ur.getPlayerId(), chatBu.build());
                }
                //俱乐部列表
                {
                    Club club = clubDao.selectClub(ur.getClubId());
                    CLUB.ClubSyn.Builder clubSyn = CLUB.ClubSyn.newBuilder();
                    clubSyn.setClubId(ur.getClubId());
                    clubSyn.setSynType(TaskConstants.SYN_TYPE_ADD);
                    clubSyn.setClubName(club.getClubName());
                    pushHelper.pushClubSyn((int) ur.getPlayerId(), clubSyn.build());
                }
                //申请列表
                {
                    CLUB.ApplyInfoRsp.Builder ap = CLUB.ApplyInfoRsp.newBuilder();
                    List<ClubApply> list = clubDao.selectClubALLApply(ur.getClubId());
                    if (list != null && !list.isEmpty()) {
                        for (ClubApply apply : list) {
                            CLUB.ApplyInfo.Builder info = CLUB.ApplyInfo.newBuilder();
                            info.setSynType(TaskConstants.SYN_TYPE_ALL);
                            info.setApplyType(TaskConstants.APPLY_TYPE_CLUB);
                            info.setClubId(apply.getClubId());
                            info.setPlayerId(apply.getApplyUserId());
                            info.setPlayerImg(apply.getApplyUserImg());
                            info.setPlayerName(apply.getApplyUserName());
                            ap.addApply(info.build());

                            info.setSynType(TaskConstants.SYN_TYPE_DELETE);
                            pushHelper.pushApplyInfo(data.session.userId,info.build());
                        }
                    }
                    pushHelper.pushApplyInfoRsp((int) ur.getPlayerId(),ap.build());
                }

            }
            break;
            case TaskConstants.AUTH_TYPE_ADD_MANAGER:{

                if(opPlayer == null){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家不是俱乐部成员");
                    return;
                }
                if(opPlayer.getClubMemberType() != TaskConstants.CLUB_MEMEBER_TYPE_COMMON){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家不是普通成员");
                    return;
                }

                opPlayer.setClubMemberType(TaskConstants.CLUB_MEMEBER_TYPE_MANAGER);
                clubDao.updateClubUser(opPlayer);
                //申请列表
                CLUB.ApplyInfoRsp.Builder ap = CLUB.ApplyInfoRsp.newBuilder();
                List<ClubApply> list = clubDao.selectClubALLApply(ur.getClubId());
                if (list != null && !list.isEmpty()) {
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
                pushHelper.pushApplyInfoRsp((int) ur.getPlayerId(),ap.build());
            }
            break;
            case TaskConstants.AUTH_TYPE_DELETE_MANAGER:{
                if(opPlayer == null){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家不是俱乐部成员");
                    return;
                }
                if(opPlayer.getClubMemberType() != TaskConstants.CLUB_MEMEBER_TYPE_MANAGER){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家不是管理员");
                    return;
                }
                opPlayer.setClubMemberType(TaskConstants.CLUB_MEMEBER_TYPE_COMMON);
                clubDao.updateClubUser(opPlayer);
                CLUB.ApplyInfoRsp.Builder ap = CLUB.ApplyInfoRsp.newBuilder();
                List<ClubApply> list = clubDao.selectClubALLApply(ur.getClubId());
                if (list != null && !list.isEmpty()) {
                    for (ClubApply apply : list) {
                        CLUB.ApplyInfo.Builder info = CLUB.ApplyInfo.newBuilder();
                        info.setSynType(TaskConstants.SYN_TYPE_ALL);
                        info.setApplyType(TaskConstants.APPLY_TYPE_CLUB);
                        info.setClubId(apply.getClubId());
                        info.setPlayerId(apply.getApplyUserId());
                        info.setPlayerImg(apply.getApplyUserImg());
                        info.setPlayerName(apply.getApplyUserName());
                        ap.addApply(info.build());

                        info.setSynType(TaskConstants.SYN_TYPE_DELETE);
                        pushHelper.pushApplyInfo((int) ur.getPlayerId(),info.build());
                    }
                }
            }
            break;
            default:{
                return;
            }
        }
        //成员列表(全量同步)
        {
            CLUB.ClubMemberRsp.Builder bu = CLUB.ClubMemberRsp.newBuilder();
            List<ClubUser> list = clubDao.selectClubAllUser(ur.getClubId());
            for (ClubUser clubUser2 : list) {
                CLUB.ClubMemberSyn.Builder syn = CLUB.ClubMemberSyn.newBuilder();
                syn.setSynType(TaskConstants.SYN_TYPE_ALL);
                syn.setClubId(clubUser2.getClubId());
                syn.setMemberId(clubUser2.getClubMemberId());
                syn.setMemberImg(clubUser2.getClubMemberImg());
                syn.setMemberName(clubUser2.getClubMemberName());
                syn.setMemberScore(clubUser2.getClubMemberScore());
                syn.setMemberType(clubUser2.getClubMemberType());
                syn.setOnline(sessionManager.isOnline(clubUser2.getClubMemberId()) ? 1 : 0);
                bu.addMembers(syn.build());
            }
            for(ClubUser clubUser2 : list){
                pushHelper.pushClubMemberRsp(clubUser2.getClubMemberId(),bu.build());
            }
        }

	}

	@Override
	public PacketType getKey() {
		return PacketType.ClubAuthorityManageReq;
	}

}
