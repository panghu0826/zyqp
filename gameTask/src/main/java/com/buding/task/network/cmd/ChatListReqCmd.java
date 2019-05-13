package com.buding.task.network.cmd;

import com.buding.db.model.Chat;
import com.buding.db.model.Club;
import com.buding.db.model.ClubUser;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.task.common.TaskConstants;
import com.buding.task.helper.TaskPushHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.msgbase.MsgBase.PacketType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaime qq_1094086610
 *
 */
@Component
public class ChatListReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	TaskPushHelper pushHelper;

    @Autowired
    ChatDao chatDao;

    @Autowired
    ClubDao clubDao;

	@Override
	public void execute(CmdData data) throws Exception {
        //业务逻辑
        CLUB.ChatListRsp.Builder bu = CLUB.ChatListRsp.newBuilder();
        List<Chat> privateChatList = chatDao.selectAllPrivateChatList(data.session.userId);
        List<ClubUser> clubs = clubDao.selectUserClub(data.session.userId);
        List<Chat> chatList = new ArrayList<>(privateChatList);
        for(ClubUser clubUser : clubs){
            chatList.add(this.chatDao.selectClubChat(clubUser.getClubId()));
        }
        logger.info(chatList);
        if(!chatList.isEmpty()) {
            for (Chat chat : chatList) {
                if (chat == null) continue;
                CLUB.ChatListSyn.Builder chatBu = CLUB.ChatListSyn.newBuilder();
                chatBu.setSynType(TaskConstants.SYN_TYPE_ALL);
                chatBu.setChatId(chat.getId());
                chatBu.setType(chat.getChatType());
                if (chat.getChatType() == TaskConstants.CHAT_TYPE_CLUB) {
                    chatBu.setClubId(chat.getClubId());
                } else {
                    int reciverId = 0;
                    String reciverName = "";
                    String reciverImg = "";
                    if (data.session.userId == chat.getUser1Id()) {
                        reciverId = chat.getUser2Id();
                        reciverName = chat.getUser2Name();
                        reciverImg = chat.getUser2Img();
                    } else {
                        reciverId = chat.getUser1Id();
                        reciverName = chat.getUser1Name();
                        reciverImg = chat.getUser1Img();
                    }
                    chatBu.setReciverId(reciverId);
                    chatBu.setReciverName(reciverName);
                    chatBu.setReciverImg(reciverImg);
                }
                bu.addChatList(chatBu.build());
            }
        }
        //发送消息
		pushHelper.pushChatListRsp(data.session, bu.build());
	}

	@Override
	public PacketType getKey() {
		return PacketType.ChatListReq;
	}

}
