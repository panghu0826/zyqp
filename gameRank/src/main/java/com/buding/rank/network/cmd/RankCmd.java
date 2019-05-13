package com.buding.rank.network.cmd;

import com.buding.common.token.TokenServer;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.service.UserService;
import com.buding.rank.helper.RankPushHelper;
import com.buding.rank.service.UserRankManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.rank.Rank;
import packet.rank.Rank.RankSyn;

/**
 * @author jaime qq_1094086610
 * @Description:
 *
 */
@Component
public class RankCmd extends RankBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	UserService userService;

	@Autowired
	UserDao userDao;

	@Autowired
	TokenServer tokenServer;

	@Autowired
	UserRankManager userRankManager;

	@Autowired
	RankPushHelper pushHelper;


	@Override
	public void execute(CmdData data) throws Exception {
		PacketBase packet = data.packet;
		Rank.RankRequest ur = Rank.RankRequest.parseFrom(packet.getData());
		RankSyn rankSynModel = userRankManager.buldRankSyn(data.session.userId,ur.getGameId(),ur.getRankType());
		pushHelper.pushPBMsg(data.session, PacketType.RankSyn, rankSynModel.toByteString());
	}

	@Override
	public PacketType getKey() {
		return PacketType.RankRequest;
	}

}
