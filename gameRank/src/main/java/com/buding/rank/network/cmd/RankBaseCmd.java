package com.buding.rank.network.cmd;

import com.buding.common.network.command.BaseCmd;
import com.buding.common.network.command.CmdMapper;
import org.springframework.beans.factory.annotation.Autowired;
import packet.msgbase.MsgBase.PacketType;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public abstract class RankBaseCmd extends BaseCmd<PacketType, CmdData>{
	@Autowired
	RankCmdMapper cmdMapper;

	@Override
	public CmdMapper<PacketType, CmdData> getCmdMapper() {
		return cmdMapper;
	}
}
