package com.buding.task.network.cmd;

import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.task.helper.TaskPushHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.msgbase.MsgBase.PacketType;

/**
 * @author jaime qq_1094086610
 * 申请结果Req
 */
@Component
public class ClubCloseCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	UserService userService;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	UserRoomDao userRoomDao;


	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ClubClose ur = CLUB.ClubClose.parseFrom(data.packet.getData());
		//逻辑校验

        //业务逻辑

	}

	@Override
	public PacketType getKey() {
		return PacketType.ClubClose;
	}

}
