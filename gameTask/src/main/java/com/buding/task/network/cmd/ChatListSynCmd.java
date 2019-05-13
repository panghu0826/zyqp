package com.buding.task.network.cmd;

import com.buding.db.model.*;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.friend.dao.FriendDao;
import com.buding.hall.module.user.dao.UserDao;
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
public class ChatListSynCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	UserDao userDao;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	ChatDao chatDao;

	@Autowired
	FriendDao friendDao;


	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ChatListSyn ur = CLUB.ChatListSyn.parseFrom(data.packet.getData());
        //业务逻辑
		int synType = ur.getSynType();
		switch (synType){
			case TaskConstants.SYN_TYPE_ADD: {
				if(ur.getType() == TaskConstants.CHAT_TYPE_PRIVATE) {
					//校验两人是否是好友
					Friend friend = friendDao.selectFriend(data.session.userId, (int) ur.getReciverId());
					if(friend == null){
						pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "sorry你俩还不是好友");
						return;
					}

					Chat chat= chatDao.selectChatList(data.session.userId, (int) ur.getReciverId());
					if(chat == null) {
						chat = new Chat();
						chat.setChatType(ur.getType());
						int minId = data.session.userId > ur.getReciverId() ? (int) ur.getReciverId() : data.session.userId;
						int maxId = data.session.userId == minId ? (int) ur.getReciverId() : data.session.userId;
						chat.setUser1Id(minId);
						chat.setUser2Id(maxId);
						chat.setCtime(new Date());
						long chatId = chatDao.insertChat(chat);
						chat.setId(chatId);
					}

					User currentUser = userDao.getUser(data.session.userId);
					User otherUser = userDao.getUser((int) ur.getReciverId());

					CLUB.ChatListSyn.Builder syn = CLUB.ChatListSyn.newBuilder();
					syn.setSynType(ur.getSynType());
					syn.setChatId(chat.getId());
					syn.setType(ur.getType());
					syn.setReciverId(otherUser.getId());
					syn.setReciverImg(otherUser.getHeadImg());
					syn.setReciverName(otherUser.getNickname());
					pushHelper.pushChatListSyn(currentUser.getId(),syn.build());

                    syn.setReciverId(currentUser.getId());
                    syn.setReciverImg(currentUser.getHeadImg());
                    syn.setReciverName(currentUser.getNickname());
                    pushHelper.pushChatListSyn(otherUser.getId(),syn.build());

				}else if(ur.getType() == TaskConstants.CHAT_TYPE_CLUB){
				    //实际上没有这个操作,因为创建俱乐部自动添加了
				    return;
				}else{
					return;
				}
			}
			break;
			default: {
			}
		}
	}

	@Override
	public PacketType getKey() {
		return PacketType.ChatListSyn;
	}

}
