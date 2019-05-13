package com.buding.task.network.cmd;

import com.buding.db.model.Chat;
import com.buding.db.model.Club;
import com.buding.db.model.ClubUser;
import com.buding.db.model.Friend;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.friend.dao.FriendDao;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.task.common.TaskConstants;
import com.buding.task.helper.TaskPushHelper;
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
public class FriendSynCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	UserService userService;

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
		CLUB.FriendSyn ur = CLUB.FriendSyn.parseFrom(data.packet.getData());
        //业务逻辑
		int synType = ur.getSynType();
		switch (synType){
			case TaskConstants.SYN_TYPE_DELETE: {//删除好友
				if(ur.getType() == TaskConstants.CHAT_TYPE_PRIVATE) {
                    Friend friend = friendDao.selectFriend(data.session.userId, (int) ur.getPlayerId());
                    if(friend == null){
						pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "sorry你俩还不是好友");
						return;
					}
                    //删除聊天列表
                    CLUB.ChatListSyn.Builder chatSyn = CLUB.ChatListSyn.newBuilder();
                    chatSyn.setSynType(TaskConstants.SYN_TYPE_DELETE);
                    chatSyn.setChatId(friend.getChatId());
                    chatSyn.setType(TaskConstants.CHAT_TYPE_PRIVATE);
                    pushHelper.pushChatListSyn(data.session.userId,chatSyn.build());
                    pushHelper.pushChatListSyn((int) ur.getPlayerId(),chatSyn.build());

					friendDao.deleteFriend(data.session.userId, (int) ur.getPlayerId());
					chatDao.deleteChatContent(friend.getChatId());
					pushHelper.pushFriendSyn(data.session.userId, ur.toBuilder().build());
					pushHelper.pushFriendSyn((int) ur.getPlayerId(), ur.toBuilder().build());

				}else if(ur.getType() == TaskConstants.CHAT_TYPE_CLUB){
					return;
				}else{
					return;
				}
			}
			break;
			case TaskConstants.SYN_TYPE_MODIFY: {//修改备注,暂时无这个功能

			}
			break;
			default: {

			}
		}
	}

	@Override
	public PacketType getKey() {
		return PacketType.FriendSyn;
	}

}
