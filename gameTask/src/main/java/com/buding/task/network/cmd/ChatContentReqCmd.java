package com.buding.task.network.cmd;

import com.buding.db.model.ChatContent;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.user.dao.UserDao;
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

import java.util.List;

/**
 * @author jaime qq_1094086610
 *
 */
@Component
public class ChatContentReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
    UserDao userDao;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
    ChatDao chatDao;


	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ChatContentReq ur = CLUB.ChatContentReq.parseFrom(data.packet.getData());
        //业务逻辑
        CLUB.ChatContentRsp.Builder bu = CLUB.ChatContentRsp.newBuilder();
        bu.setChatId(ur.getChatId());
        List<ChatContent> list = chatDao.selectChatContent(ur.getChatId());
        if(list != null && !list.isEmpty()) {
            for (ChatContent content : list) {
                CLUB.ChatContentSyn.Builder co = CLUB.ChatContentSyn.newBuilder();
                co.setSynType(TaskConstants.SYN_TYPE_ALL);
                co.setChatId(content.getChatId());
                co.setTime(content.getChatTime().getTime());
                co.setPlayerId(content.getPlayerId());
                co.setPlayerName(content.getPlayerName());
                co.setContent(content.getContent());
                co.setPlayerImg(content.getPlayerImg());
                bu.addContent(co.build());
            }
        }
        //发送消息
		pushHelper.pushChatContentRsp(data.session, bu.build());
	}

	@Override
	public PacketType getKey() {
		return PacketType.ChatContentReq;
	}

}
