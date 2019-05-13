package com.buding.task.network.cmd;

import com.buding.db.model.User;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.task.helper.TaskPushHelper;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall;
import packet.msgbase.MsgBase.PacketType;

@Component
public class SignSynCmd extends TaskBaseCmd {
	protected Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	protected TaskPushHelper pushHelper;
	
	@Autowired
	ConfigManager configManager;

	@Autowired
	UserDao userDao;
	
	@Override
	public void execute(CmdData data) throws Exception {
		User user = userDao.getUser(data.session.userId);
		int signNumLeft = user.getCanSign()-user.getSignNums();
		int LunPanNumLeft = user.getCanLunpan()-user.getLunpanNums();

		Hall.SignSynResponse.Builder syn = Hall.SignSynResponse.newBuilder();
		syn.setSignNum(signNumLeft);
		syn.setSignDays(user.getSignDay());
		syn.setLunPanNum(LunPanNumLeft);
		logger.info("同步签到消息------"+ JsonFormat.printToString(syn.build()));
		pushHelper.pushPBMsg(data.session,PacketType.SignSynResponse,syn.build().toByteString());
	}

	@Override
	public PacketType getKey() {
		return PacketType.SignSynRequest;
	}	
}
