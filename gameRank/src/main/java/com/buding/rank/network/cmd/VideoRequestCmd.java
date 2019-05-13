package com.buding.rank.network.cmd;

import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.rank.helper.RankPushHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall;
import packet.msgbase.MsgBase;
import packet.msgbase.MsgBase.PacketBase;

/**
 * @author jaime qq_1094086610
 * @Description:
 *
 */
@Component
public class VideoRequestCmd extends RankBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	RankPushHelper pushHelper;

	@Autowired
	UserRoomDao userRoomDao;


	@Override
	public void execute(CmdData data) throws Exception {
		PacketBase packet = data.packet;
		Hall.VideoRequest ur = Hall.VideoRequest.parseFrom(packet.getData());
		long videoId = ur.getVideoId();
		if(videoId == 0){
			pushHelper.pushErrorMsg(data.session, MsgBase.PacketType.GlobalMsgSyn,"回放码为空");
			return;
		}
		String msg = userRoomDao.getVideoData(videoId);
		if(msg == null) return;
//		logger.info(msg);
		Hall.VideoResponse.Builder pb = Hall.VideoResponse.newBuilder();
		pb.setMsg(msg);
		pb.setVideoId(videoId);
		pushHelper.pushPBMsg(data.session, MsgBase.PacketType.VideoResponse,pb.build().toByteString());
	}

	@Override
	public MsgBase.PacketType getKey() {
		return MsgBase.PacketType.VideoRequest;
	}

}
