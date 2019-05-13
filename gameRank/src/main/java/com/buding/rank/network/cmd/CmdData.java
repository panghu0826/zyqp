package com.buding.rank.network.cmd;

import com.buding.rank.network.RankSession;
import packet.msgbase.MsgBase.PacketBase;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class CmdData {
	public RankSession session;
	public PacketBase packet;
	public long startWatingTime;
	public long startExecuteTime;
	public long endExecuteTime;
	public byte[] result;
	
	public CmdData(RankSession session, PacketBase packet) {
		this.session = session;
		this.packet = packet;
		this.startWatingTime = System.currentTimeMillis();
	}
}
