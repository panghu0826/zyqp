package com.buding.hall.network;

import com.buding.common.network.command.Cmd;
import com.buding.hall.network.cmd.CmdData;
import com.buding.hall.network.cmd.HallCmdMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import packet.msgbase.MsgBase;

public class Task implements  Runnable{

    private Logger logger = LogManager.getLogger(getClass());

    @Autowired
    HallCmdMapper hallCmdMapper;

    @Autowired
    HallSessionManager hallSessionManager;

    private CmdData cmdData;

    public Task(CmdData cmdData) {
        this.cmdData = cmdData;
    }

    @Override
    public void run() {
        try {
            execute(cmdData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void execute(CmdData cmdData) throws Exception {
        cmdData.startExecuteTime = System.currentTimeMillis();
        Cmd<MsgBase.PacketType, CmdData> cmd = hallCmdMapper.get(cmdData.packet.getPacketType());
        if(cmd == null) {
            logger.info("命令无法处理");
            MsgBase.PacketBase.Builder pb = MsgBase.PacketBase.newBuilder();
            pb.setCode(-1);
            pb.setPacketType(cmdData.packet.getPacketType());
            pb.setMsg("命令无法处理");
            hallSessionManager.write(cmdData.session, pb.build().toByteArray());
            return;
        }
        cmd.execute(cmdData);
        cmdData.endExecuteTime = System.currentTimeMillis();

        if(cmdData.packet.getPacketType() != MsgBase.PacketType.HEARTBEAT) {
            logger.info("type={};wait={};start={}", cmdData.packet.getPacketType(), cmdData.startExecuteTime - cmdData.startWatingTime, cmdData.endExecuteTime - cmdData.startExecuteTime);
        }
    }
}
