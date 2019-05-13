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
public class ApplyResultReqCmd extends TaskBaseCmd {
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
		CLUB.ApplyResultReq ur = CLUB.ApplyResultReq.parseFrom(data.packet.getData());
//		logger.info("ur.getPlayerId()--"+ur.getPlayerId());
        User user = userDao.getUser((int) ur.getPlayerId());
        if(user == null){
            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家不存在");
            return;
        }
		if(ur.getType() == TaskConstants.APPLY_TYPE_CLUB){
            //如果已经是该俱乐部成员,则返回
            ClubUser clubUser = clubDao.selectClubUser(ur.getClubId(), (int) ur.getPlayerId());
            if(clubUser != null) {
                ClubApply clubApply = clubDao.selectClubApply(ur.getClubId(), (int) ur.getPlayerId());
                if(clubApply !=null){
                    clubDao.deleteClubApplyById(clubApply.getId());
                }
                pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "该玩家已经加入该俱乐部");
                return;
            }

            boolean isGuanLi = false;
            List<ClubUser> manageList = clubDao.selectClubAllManageUser(ur.getClubId());
            for(ClubUser manager : manageList){
                if(manager.getClubMemberId() == data.session.userId) isGuanLi = true;
            }
            if(!isGuanLi){
                pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷你不是管理别凑热闹");
                return;
            }

            //删除该次申请信息
            clubDao.deleteClubApply(ur.getClubId(), (int) ur.getPlayerId());
            CLUB.ApplyInfo.Builder bu = CLUB.ApplyInfo.newBuilder();
            bu.setSynType(TaskConstants.SYN_TYPE_DELETE);
            bu.setApplyType(ur.getType());
            bu.setClubId(ur.getClubId());
            bu.setPlayerId(user.getId());
            bu.setPlayerName(user.getNickname());
            bu.setPlayerImg(user.getHeadImg());
            for(ClubUser manager : manageList){
                pushHelper.pushApplyInfo(manager.getClubMemberId(), bu.build());
            }

            if(ur.getIsAgree()){
                clubUser = new ClubUser();
                clubUser.setClubId(ur.getClubId());
                clubUser.setClubMemberId((int) ur.getPlayerId());
                clubUser.setClubMemberScore(0);
                clubUser.setClubMemberType(TaskConstants.CLUB_MEMEBER_TYPE_COMMON);
                clubUser.setCtime(new Date());
                clubDao.insertClubUser(clubUser);

                CLUB.ClubMemberSyn.Builder syn = CLUB.ClubMemberSyn.newBuilder();
                syn.setSynType(TaskConstants.SYN_TYPE_ADD);
                syn.setClubId(clubUser.getClubId());
                syn.setMemberId(clubUser.getClubMemberId());
                syn.setMemberImg(user.getHeadImg());
                syn.setMemberName(user.getNickname());
                syn.setMemberScore(clubUser.getClubMemberScore());
                syn.setMemberType(clubUser.getClubMemberType());
                syn.setOnline(sessionManager.isOnline(clubUser.getClubMemberId()) ? 1 : 0);

                //給俱樂部所有人推送成員添加
                List<ClubUser> allList = clubDao.selectClubAllUser(ur.getClubId());
                for(ClubUser clubUser1 : allList){
                    pushHelper.pushClubMemberSyn(clubUser1.getClubMemberId(), syn.build());
                }

                //給申請人添加聊天列表
                CLUB.ChatListSyn.Builder chatSyn = CLUB.ChatListSyn.newBuilder();
                Chat chat = chatDao.selectClubChat(ur.getClubId());
                chatSyn.setSynType(TaskConstants.SYN_TYPE_ADD);
                chatSyn.setChatId(chat.getId());
                chatSyn.setClubId(ur.getClubId());
                chatSyn.setType(TaskConstants.CHAT_TYPE_CLUB);
                pushHelper.pushChatListSyn((int) ur.getPlayerId(),chatSyn.build());

                //給申請人添加俱樂部列表
                CLUB.ClubSyn.Builder clubSyn = CLUB.ClubSyn.newBuilder();
                Club club = clubDao.selectClub(ur.getClubId());
                clubSyn.setSynType(TaskConstants.SYN_TYPE_ADD);
                clubSyn.setClubId(club.getId());
                clubSyn.setClubName(club.getClubName());
                pushHelper.pushClubSyn((int) ur.getPlayerId(),clubSyn.build());
            }

        }else if(ur.getType() == TaskConstants.APPLY_TYPE_FRIEND){
            Friend friend = friendDao.selectFriend(data.session.userId, (int) ur.getPlayerId());
            if(friend != null){
                pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷您已经是他的好友");
                return;
            }

            //删除该次申请信息
            friendDao.deleteFriendApply(data.session.userId, (int) ur.getPlayerId());
            CLUB.ApplyInfo.Builder bu = CLUB.ApplyInfo.newBuilder();
            bu.setSynType(TaskConstants.SYN_TYPE_DELETE);
            bu.setApplyType(ur.getType());
            bu.setPlayerId(user.getId());
            bu.setPlayerName(user.getNickname());
            bu.setPlayerImg(user.getHeadImg());
            pushHelper.pushApplyInfo(data.session.userId, bu.build());

            if(ur.getIsAgree()){
                Chat chat = new Chat();
                chat.setChatType(TaskConstants.CHAT_TYPE_PRIVATE);
                int minId = data.session.userId > ur.getPlayerId()? (int) ur.getPlayerId() : data.session.userId;
                int maxId = data.session.userId == minId ? (int) ur.getPlayerId() : data.session.userId;
                chat.setUser1Id(minId);
                chat.setUser2Id(maxId);
                chat.setCtime(new Date());
                long chatId = chatDao.insertChat(chat);

                friend = new Friend();
                friend.setCtime(new Date());
                friend.setUserId(data.session.userId);
                friend.setFriendUserId(user.getId());
                friend.setChatId(chatId);
                friendDao.insertFriend(friend);

                Friend friend1 = new Friend();
                friend1.setCtime(new Date());
                friend1.setUserId(user.getId());
                friend1.setFriendUserId(data.session.userId);
                friend1.setChatId(chatId);
                friendDao.insertFriend(friend1);

                CLUB.FriendSyn.Builder syn = CLUB.FriendSyn.newBuilder();
                User currentUser = userDao.getUser(data.session.userId);
                syn.setSynType(TaskConstants.SYN_TYPE_ADD);
                syn.setChatId(chatId);
                syn.setType(TaskConstants.CHAT_TYPE_PRIVATE);
                syn.setPlayerId(currentUser.getId());
                syn.setPlayerName(currentUser.getNickname());
                syn.setPlayerImg(currentUser.getHeadImg());
                pushHelper.pushFriendSyn(user.getId(),syn.build());

                CLUB.FriendSyn.Builder syn2 = CLUB.FriendSyn.newBuilder();
                syn2.setSynType(TaskConstants.SYN_TYPE_ADD);
                syn2.setChatId(chatId);
                syn2.setType(TaskConstants.CHAT_TYPE_PRIVATE);
                syn2.setPlayerId(user.getId());
                syn2.setPlayerName(user.getNickname());
                syn2.setPlayerImg(user.getHeadImg());
                pushHelper.pushFriendSyn(data.session.userId,syn.build());

            }
        }
	}

	@Override
	public PacketType getKey() {
		return PacketType.ApplyResultReq;
	}

}
