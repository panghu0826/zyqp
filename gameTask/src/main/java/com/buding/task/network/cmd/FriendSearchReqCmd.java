package com.buding.task.network.cmd;

import com.buding.db.model.User;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.task.helper.TaskPushHelper;
import com.buding.task.network.TaskSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.msgbase.MsgBase.PacketType;

/**
 * @author jaime qq_1094086610
 *
 */
@Component
public class FriendSearchReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	UserDao userDao;

	@Autowired
	TaskSessionManager sessionManager;

	@Autowired
	TaskPushHelper pushHelper;

	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.FriendSearchReq ur = CLUB.FriendSearchReq.parseFrom(data.packet.getData());
        //业务逻辑
        CLUB.FriendSearchRsp.Builder bu = CLUB.FriendSearchRsp.newBuilder();
		User user = userDao.getUser((int) ur.getPlayerId());
		if(user == null){
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "您找的人不存在");
			return;
		}
		CLUB.Player.Builder pl = CLUB.Player.newBuilder();
		pl.setPlayerId(user.getId());
		pl.setPlayerName(user.getNickname());
		pl.setPlayerImg(user.getHeadImg());
		pl.setDiamond(user.getDiamond());
		pl.setUnionid(user.getWxunionid());
		pl.setOnline(sessionManager.isOnline((int) ur.getPlayerId()) ?1:0);
		bu.addPlayers(pl.build());
        //发送消息
		pushHelper.pushFriendSearchRsp(data.session, bu.build());
	}

	@Override
	public PacketType getKey() {
		return PacketType.FriendSearchReq;
	}

}
