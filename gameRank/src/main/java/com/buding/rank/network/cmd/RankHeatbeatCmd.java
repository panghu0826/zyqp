package com.buding.rank.network.cmd;

import com.buding.rank.helper.RankPushHelper;
import com.buding.rank.network.RankSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.msgbase.MsgBase;
import packet.msgbase.MsgBase.PacketType;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class RankHeatbeatCmd extends RankBaseCmd {

	@Autowired
	RankSessionManager sessionManager;

	@Autowired
	RankPushHelper pushHelper;

	@Override
	public void execute(CmdData data) throws Exception {
		MsgBase.PacketBase.Builder pb = MsgBase.PacketBase.newBuilder();
		pb.setCode(0);
		pb.setPacketType(getKey());
		sessionManager.write(data.session, pb.build().toByteArray());
	}

	@Override
	public PacketType getKey() {
		return PacketType.HEARTBEAT;
	}
}
