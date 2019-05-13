package com.buding.hall.network.cmd;

import packet.msgbase.MsgBase.PacketBase;

import com.buding.hall.network.HallSession;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class CmdData{
	public HallSession session;
	public PacketBase packet;
	public long startWatingTime;
	public long startExecuteTime;
	public long endExecuteTime;
	public byte[] result;
	
	public CmdData(HallSession session, PacketBase packet) {
		this.session = session;
		this.packet = packet;
		this.startWatingTime = System.currentTimeMillis();
	}

}
