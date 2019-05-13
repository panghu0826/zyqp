package com.buding.task.network.cmd;

import com.buding.db.model.*;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.game.model.DeskModel;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.ws.BattlePortalBroadcastService;
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
 *
 */
@Component
public class ClubMemberSynCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
    UserDao userDao;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
    ClubDao clubDao;

	@Autowired
    ChatDao chatDao;

	@Autowired
	TaskSessionManager sessionManager;

	@Autowired
    BattlePortalBroadcastService battleService;

	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ClubMemberSyn ur = CLUB.ClubMemberSyn.parseFrom(data.packet.getData());
		//逻辑校验
        Club club = clubDao.selectClub(ur.getClubId());
        if(club == null){
            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该俱乐部不存在");
            return;
        }
        //业务逻辑
        int playerId = (int) ur.getMemberId();
        int synType = ur.getSynType();
        ClubUser clubUserTemp = clubDao.selectClubUser(club.getId(), playerId);
        List<ClubUser> allList = clubDao.selectClubAllUser(club.getId());
        switch (synType){
            case TaskConstants.SYN_TYPE_ADD: {//邀请玩家直接拉进来
                User user = userDao.getUser(playerId);
                if(user == null || user.getNickname() == null) {
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家不存在");
                    return;
                }
                ClubUser clubUserOp = clubDao.selectClubUser(club.getId(),playerId);
                if(clubUserOp != null){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家已经加入俱乐部");
                    return;
                }
                ClubUser clubUser = new ClubUser();
                clubUser.setClubId(club.getId());
                clubUser.setClubMemberId(playerId);
                clubUser.setClubMemberType(TaskConstants.CLUB_MEMEBER_TYPE_COMMON);
                clubUser.setClubMemberScore(ur.getMemberScore());
                clubUser.setCtime(new Date());
                clubDao.insertClubUser(clubUser);
                clubUserTemp = clubDao.selectClubUser(club.getId(), playerId);
            }
            break;
            case TaskConstants.SYN_TYPE_DELETE: {//管理踢人或者玩家自己退出

                ClubUser clubUserOp = clubDao.selectClubUser(club.getId(),playerId);
                if(clubUserOp == null){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "您不是该俱乐部成员");
                    return;
                }
                if(data.session.userId == playerId && clubUserOp.getClubMemberType() == TaskConstants.CLUB_MEMEBER_TYPE_OWNER){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "自己不能踢自己");
                    return;
                }
                if(data.session.userId != playerId){//管理踢人
                    ClubUser currentUser = clubDao.selectClubUser(club.getId(),data.session.userId);
                    if(currentUser.getClubMemberType() <= TaskConstants.CLUB_MEMEBER_TYPE_COMMON){
                        pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "您无权踢人");
                        return;
                    }
                    if(clubUserOp.getClubMemberType() !=TaskConstants.CLUB_MEMEBER_TYPE_COMMON) {
                        if (currentUser.getClubMemberType() > clubUserOp.getClubMemberType()) {
                            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "您的权限不够");
                            return;
                        }
                    }
                }

                clubDao.deleteClubUser(club.getId(),playerId);
            }
            break;
            case TaskConstants.SYN_TYPE_MODIFY: {//修改玩家信息(积分)
                ClubUser currentUser = clubDao.selectClubUser(club.getId(),data.session.userId);
                if(currentUser.getClubMemberType() <= TaskConstants.CLUB_MEMEBER_TYPE_COMMON){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "您无权操作");
                    return;
                }
                DeskModel deskModel = battleService.getUserReadyClubJifenDesk("all",club.getId(),playerId);
                if(deskModel != null){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "玩家在桌子"+deskModel.deskId+"已经坐下不能修改,请让他离座再改");
                    return;
                }

                ClubUser clubUser = clubDao.selectClubUser(club.getId(), playerId);
                if(clubUser == null) return;
                int scoreModify = ur.getMemberScore() - clubUser.getClubMemberScore();
                clubUser.setClubId(club.getId());
                clubUser.setClubMemberId(playerId);
                clubUser.setClubMemberScore(ur.getMemberScore());
                clubUser.setCtime(new Date());
                clubDao.updateClubUser(clubUser);
                clubUserTemp = clubUser;

                //记录日志
                ClubScoreLog log = new ClubScoreLog();
                log.setClubId(club.getId());
                log.setPlayerId(playerId);
                log.setScoreModify(scoreModify);
                log.setScoreLeft(ur.getMemberScore());
                log.setMtime(new Date());
                log.setType(TaskConstants.SCORE_LOG_MANAGER_MODIFY);
                String info = "";
                if(currentUser.getClubMemberType() == TaskConstants.CLUB_MEMEBER_TYPE_OWNER){
                    info += "群主(id:"+currentUser.getClubMemberId()+")"+currentUser.getClubMemberName();
                }else if(currentUser.getClubMemberType() == TaskConstants.CLUB_MEMEBER_TYPE_MANAGER){
                    info += "管理员(id:"+currentUser.getClubMemberId()+")"+currentUser.getClubMemberName();
                }
                if(scoreModify > 0){
                    info += "增加";
                }else if(scoreModify < 0){
                    info += "减少";
                }else {
                    info += "修改";
                }
                log.setInfo(info);
                clubDao.insertClubScoreLog(log);
            }
            break;
            default: {
                return;
            }
        }

        if(synType != TaskConstants.SYN_TYPE_MODIFY) {
            //聊天列表
            CLUB.ChatListSyn.Builder chatSyn = CLUB.ChatListSyn.newBuilder();
            Chat chat = chatDao.selectClubChat(club.getId());
            chatSyn.setSynType(ur.getSynType());
            chatSyn.setChatId(chat.getId());
            chatSyn.setClubId(club.getId());
            chatSyn.setType(TaskConstants.CHAT_TYPE_CLUB);
            pushHelper.pushChatListSyn(playerId, chatSyn.build());

            //好友列表
//            CLUB.FriendSyn.Builder friendSyn = CLUB.FriendSyn.newBuilder();
//            friendSyn.setSynType(ur.getSynType());
//            friendSyn.setClubId(club.getId());
//            friendSyn.setChatId(chat.getId());
//            friendSyn.setType(TaskConstants.CHAT_TYPE_PRIVATE);
//            pushHelper.pushFriendSyn(playerId, friendSyn.build());

            //俱乐部列表
            CLUB.ClubSyn.Builder clubSyn = CLUB.ClubSyn.newBuilder();
            clubSyn.setSynType(ur.getSynType());
            clubSyn.setClubId(club.getId());
            clubSyn.setClubName(club.getClubName());
            pushHelper.pushClubSyn(playerId, clubSyn.build());
        }

        CLUB.ClubMemberSyn.Builder syn = CLUB.ClubMemberSyn.newBuilder();
        syn.setSynType(ur.getSynType());
        syn.setClubId(clubUserTemp.getClubId());
        syn.setMemberId(clubUserTemp.getClubMemberId());
        syn.setMemberImg(clubUserTemp.getClubMemberImg());
        syn.setMemberName(clubUserTemp.getClubMemberName());
        syn.setMemberScore(clubUserTemp.getClubMemberScore());
        syn.setMemberType(clubUserTemp.getClubMemberType());
        syn.setOnline(sessionManager.isOnline(clubUserTemp.getClubMemberId()) ? 1 : 0);

        //发送消息
        for(ClubUser clubUser1 : allList){
            pushHelper.pushClubMemberSyn(clubUser1.getClubMemberId(), syn.build());
        }

	}

	@Override
	public PacketType getKey() {
		return PacketType.ClubMemberSyn;
	}

}
