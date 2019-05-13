package com.buding.task.network.cmd;

import com.buding.db.model.*;
import com.buding.hall.module.club.dao.ClubDao;
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
public class ApplyReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	ClubDao clubDao;

	@Autowired
	FriendDao friendDao;

	@Autowired
	UserDao userDao;

	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ApplyReq ur = CLUB.ApplyReq.parseFrom(data.packet.getData());
		//逻辑校验
		if(data.session.userId <= 0) return;
        //业务逻辑
        CLUB.ApplyInfo.Builder bu = CLUB.ApplyInfo.newBuilder();
		if(ur.getType() == TaskConstants.APPLY_TYPE_CLUB){//申请加入俱乐部
			Club club = clubDao.selectClub(ur.getClubId());
			if(club == null){
				pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷您要找的俱乐部地球上找不到");
				return;
			}

			ClubUser clubUser = clubDao.selectClubUser(ur.getClubId(),data.session.userId);
			if(clubUser != null) {
				pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷您已经加入该俱乐部");
				return;
			}

			ClubApply apply = clubDao.selectClubApply(ur.getClubId(),data.session.userId);
			if(apply != null){
                pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷您已经申请了请等待结果");
                return;
            }
			apply = new ClubApply();
			apply.setClubId(ur.getClubId());
			apply.setApplyUserId(data.session.userId);
			apply.setCtime(new Date());
			clubDao.insertClubApply(apply);

			User user = userDao.getUser(data.session.userId);
			bu.setSynType(TaskConstants.SYN_TYPE_ADD);
			bu.setApplyType(ur.getType());
			bu.setClubId(ur.getClubId());
			bu.setPlayerId(user.getId());
			bu.setPlayerName(user.getNickname());
			bu.setPlayerImg(user.getHeadImg());
			List<ClubUser> manageList = clubDao.selectClubAllManageUser(ur.getClubId());
			for(ClubUser manager : manageList){
				pushHelper.pushApplyInfo(manager.getClubMemberId(), bu.build());
			}

			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "申请成功");

		}else if(ur.getType() == TaskConstants.APPLY_TYPE_FRIEND){//申请加好友
			if (data.session.userId == ur.getPlayerId()){
				pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷您不能和自己搞基");
				return;
			}
			User duiFang = userDao.getUser((int) ur.getPlayerId());
			if(duiFang == null) {
				pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷您要找的人地球上找不到");
				return;
			}

			Friend friend = friendDao.selectFriend(data.session.userId, (int) ur.getPlayerId());
			if(friend != null){
				pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷您已经是他的好友");
				return;
			}

            FriendApply apply = friendDao.selectFriendApply((int) ur.getPlayerId(),data.session.userId);
            if(apply != null){
                pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "大爷您已经申请了请等待结果");
                return;
            }
			User user = userDao.getUser(data.session.userId);
            apply = new FriendApply();
			apply.setApplyUserId(user.getId());
			apply.setCtime(new Date());
			apply.setUserId((int) ur.getPlayerId());
			friendDao.insertFriendApply(apply);

			bu.setSynType(TaskConstants.SYN_TYPE_ADD);
			bu.setApplyType(ur.getType());
			bu.setPlayerId(user.getId());
			bu.setPlayerName(user.getNickname());
			bu.setPlayerImg(user.getHeadImg());
			pushHelper.pushApplyInfo((int) ur.getPlayerId(), bu.build());
		}
	}

	@Override
	public PacketType getKey() {
		return PacketType.ApplyReq;
	}

}
