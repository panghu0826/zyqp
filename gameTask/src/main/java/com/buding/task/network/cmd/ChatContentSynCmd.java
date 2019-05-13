package com.buding.task.network.cmd;

import com.buding.db.model.Chat;
import com.buding.db.model.ChatContent;
import com.buding.db.model.ClubUser;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.club.dao.ClubDao;
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
 *
 */
@Component
public class ChatContentSynCmd extends TaskBaseCmd {
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


	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ChatContentSyn ur = CLUB.ChatContentSyn.parseFrom(data.packet.getData());
        //业务逻辑
		int synType = ur.getSynType();
		Date date = new Date();
		switch (synType){
			case TaskConstants.SYN_TYPE_ADD: {//发送消息
                ChatContent content = new ChatContent();
                content.setChatId(ur.getChatId());
                content.setChatTime(date);
                content.setPlayerId((int) ur.getPlayerId());
                content.setContent(ur.getContent());
                chatDao.insertChatContent(content);
			}
			break;
			case TaskConstants.SYN_TYPE_DELETE: {//删除消息
                chatDao.deleteChat(ur.getChatId());
			}
			break;
			default: {
			    return;
			}
		}
		logger.info("ur.getChatId()--"+ur.getChatId());
        Chat chat = chatDao.selectChat(ur.getChatId());
        CLUB.ChatContentSyn.Builder syn = ur.toBuilder();
        syn.setTime(date.getTime());
        if(chat.getChatType() == TaskConstants.CHAT_TYPE_PRIVATE) {
            pushHelper.pushChatContentSyn(chat.getUser1Id(), syn.build());
            pushHelper.pushChatContentSyn(chat.getUser2Id(), syn.build());

        }else if(chat.getChatType() == TaskConstants.CHAT_TYPE_CLUB) {
            List<ClubUser> allList = clubDao.selectClubAllUser(chat.getClubId());
            //发送消息
            for(ClubUser clubUser1 : allList) {
                pushHelper.pushChatContentSyn(clubUser1.getClubMemberId(), syn.build());
            }
        }else if(chat.getChatType() == TaskConstants.CHAT_TYPE_HALL) {
            for(int playerId: sessionManager.getOnlinePlayerIdList()){
                pushHelper.pushChatContentSyn(playerId, syn.build());
            }
        }
	}

	@Override
	public PacketType getKey() {
		return PacketType.ChatContentSyn;
	}

}
