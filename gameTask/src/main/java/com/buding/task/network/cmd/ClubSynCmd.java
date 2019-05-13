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

import java.util.Date;
import java.util.List;

/**
 * @author jaime qq_1094086610
 *
 */
@Component
public class ClubSynCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	TaskPushHelper pushHelper;

    @Autowired
    ClubDao clubDao;

    @Autowired
    ChatDao chatDao;

    @Override
	public void execute(CmdData data) throws Exception {
		CLUB.ClubSyn ur = CLUB.ClubSyn.parseFrom(data.packet.getData());
		//逻辑校验
        CLUB.ClubSyn.Builder clubSyn =  ur.toBuilder();
        //业务逻辑
		int synType = ur.getSynType();
        long clubId = ur.getClubId();
        long chatId = 0L;

        List<ClubUser> allList = clubDao.selectClubAllUser(ur.getClubId());
        switch (synType){
			case TaskConstants.SYN_TYPE_ADD: {//创建俱乐部
                Club club = new Club();
                club.setClubName(ur.getClubName());
                club.setClubWanfa(TaskConstants.CLUB_CREATE_DEFAULT_WANFA_DDZ);
                club.setClubNotice(TaskConstants.CLUB_CREATE_DEFAULT_NOTICE);
                club.setCreateRoomMode(TaskConstants.CLUB_DEFAULT_CREATE_ROOM_MODE);
                club.setEnterScore(TaskConstants.CLUB_DEFAULT_ENTER_SCORE);
                club.setCanFufen(TaskConstants.CLUB_DEFAULT_CAN_FU_FEN);
                club.setChoushuiScore(TaskConstants.CLUB_DEFAULT_CHOU_SHUI_SCORE);
                club.setChoushuiNum(TaskConstants.CLUB_DEFAULT_CHOU_SHUI_NUM);
                club.setZengsongNum(TaskConstants.CLUB_DEFAULT_ZENG_SONG_NUM);
                club.setCtime(new Date());
                clubId = clubDao.insertClub(club);

                ClubUser clubUser = new ClubUser();
                clubUser.setClubId(clubId);
                clubUser.setClubMemberId(data.session.userId);
                clubUser.setClubMemberType(TaskConstants.CLUB_MEMEBER_TYPE_OWNER);
                clubUser.setClubMemberScore(0);
                clubUser.setCtime(new Date());
                clubDao.insertClubUser(clubUser);

                Chat chat = chatDao.selectClubChat(clubId);
                if(chat == null) {
                    chat = new Chat();
                    chat.setChatType(TaskConstants.CHAT_TYPE_CLUB);
                    chat.setClubId(clubId);
                    chat.setCtime(new Date());
                    chatId = chatDao.insertChat(chat);
                    chat.setId(chatId);
                }

                allList = clubDao.selectClubAllUser(clubId);
                clubSyn.setClubId(clubId);
			}
			break;
			case TaskConstants.SYN_TYPE_DELETE: {//解散俱乐部
			    ClubUser clubUser = clubDao.selectClubOwnerUser(ur.getClubId());
			    if(clubUser.getClubMemberId() != data.session.userId){
                    pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷您不是群主");
                    return;
                }
                Chat chat = chatDao.selectClubChat(ur.getClubId());

                chatDao.deleteChat(chat.getId());
                chatDao.deleteChatContent(chat.getId());
                clubDao.deleteClub(ur.getClubId());
                clubDao.deleteClubAllApply(ur.getClubId());
                clubDao.deleteClubAllUser(ur.getClubId());
                CLUB.ChatListSyn.Builder syn = CLUB.ChatListSyn.newBuilder();
                syn.setSynType(ur.getSynType());
                syn.setChatId(chat.getId());
                syn.setClubId(ur.getClubId());
                syn.setType(TaskConstants.CHAT_TYPE_CLUB);
                for(ClubUser clubUser1 : allList) {
                    pushHelper.pushChatListSyn(clubUser1.getClubMemberId(),syn.build());
                }
                chatId = chat.getId();
            }
			break;
			case TaskConstants.SYN_TYPE_MODIFY: {//俱乐部改名字
                Club club = clubDao.selectClub(ur.getClubId());
			    club.setClubName(ur.getClubName());
			    clubDao.updateClub(club);

                Chat chat = chatDao.selectClubChat(ur.getClubId());
                chatId = chat.getId();
            }
			break;
			default: {
				return;
			}
		}

//		logger.info(allList);

        CLUB.ChatListSyn.Builder syn = CLUB.ChatListSyn.newBuilder();
        syn.setSynType(ur.getSynType());
        syn.setChatId(chatId);
        syn.setClubId(clubId);
        syn.setType(TaskConstants.CHAT_TYPE_CLUB);
        //发送消息
        for(ClubUser clubUser1 : allList) {
            pushHelper.pushChatListSyn(clubUser1.getClubMemberId(),syn.build());
            pushHelper.pushClubSyn(clubUser1.getClubMemberId(), clubSyn.build());
        }
    }

	@Override
	public PacketType getKey() {
		return PacketType.ClubSyn;
	}

}
