package com.buding.task.network.cmd;

import com.buding.task.network.TaskSession;
import packet.msgbase.MsgBase.PacketBase;

/**
 * @author jaime qq_1094086610
 * @Description:
 */
public class CmdData {
    public TaskSession session;
    public PacketBase packet;

    public long startWatingTime;
    public long startExecuteTime;
    public long endExecuteTime;
    public byte[] result;

    public CmdData(TaskSession session, PacketBase packet) {
        this.session = session;
        this.packet = packet;
        this.startWatingTime = System.currentTimeMillis();
    }
}
