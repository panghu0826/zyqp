package com.buding.msg.network.cmd;

import packet.msgbase.MsgBase.PacketBase;

import com.buding.msg.network.MsgSession;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class CmdData {
	public MsgSession session;
	public PacketBase packet;
	
	public long startWatingTime;
	public long startExecuteTime;
	public long endExecuteTime;
	public byte[] result;
	
	public CmdData(MsgSession session, PacketBase packet) {
		this.session = session;
		this.packet = packet;
		this.startWatingTime = System.currentTimeMillis();
	}
}
