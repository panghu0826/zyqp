package com.buding.task.network.cmd;

import com.buding.db.model.Chat;
import com.buding.db.model.Club;
import com.buding.db.model.Friend;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.friend.dao.FriendDao;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.task.common.TaskConstants;
import com.buding.task.helper.TaskPushHelper;
import com.buding.task.network.TaskSessionManager;
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
public class FriendListReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
    TaskSessionManager sessionManager;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
    FriendDao friendDao;

	@Autowired
    ClubDao clubDao;

	@Autowired
    ChatDao chatDao;

	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.FriendListReq ur = CLUB.FriendListReq.parseFrom(data.packet.getData());
        //业务逻辑
        CLUB.FriendListRsp.Builder bu = CLUB.FriendListRsp.newBuilder();
        List<Friend> list = friendDao.selectAllFriend(data.session.userId);
        if(list != null && !list.isEmpty()) {
            for (Friend friend : list) {
                CLUB.FriendSyn.Builder syn = CLUB.FriendSyn.newBuilder();
                syn.setSynType(TaskConstants.SYN_TYPE_ALL);
                syn.setChatId(friend.getChatId());
                syn.setType(TaskConstants.CHAT_TYPE_PRIVATE);
                syn.setPlayerId(friend.getFriendUserId());
                syn.setPlayerName(friend.getFriendUserName());
                syn.setPlayerImg(friend.getFriendUserImg());
                syn.setOnline(sessionManager.isOnline(friend.getFriendUserId())?1:0);
                bu.addFriends(syn.build());
            }
        }
        List<Club> clubs = clubDao.selectClubList(data.session.userId);
        if(clubs != null && !clubs.isEmpty()) {
            for(Club club : clubs){
                CLUB.FriendSyn.Builder syn = CLUB.FriendSyn.newBuilder();
                syn.setOnline(1);
                syn.setSynType(TaskConstants.SYN_TYPE_ALL);
                syn.setType(TaskConstants.CHAT_TYPE_CLUB);
                Chat chat = chatDao.selectClubChat(club.getId());
                syn.setChatId(chat.getId());
                syn.setClubId(club.getId());
                bu.addFriends(syn.build());
            }
        }

        //发送消息
		pushHelper.pushFriendListRsp(data.session, bu.build());
	}

	@Override
	public PacketType getKey() {
		return PacketType.FriendListReq;
	}

}
