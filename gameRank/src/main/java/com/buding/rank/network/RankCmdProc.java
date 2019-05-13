package com.buding.rank.network;

import com.buding.common.loop.Looper;
import com.buding.common.loop.ServerLoop;

import com.buding.common.network.command.Cmd;
import com.buding.rank.network.cmd.CmdData;
import com.buding.rank.network.cmd.RankCmdMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class RankCmdProc implements Looper, InitializingBean {
	private Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	RankCmdMapper rankCmdMapper;
	
	@Autowired
	@Qualifier("RankServerNetMsgLoop")
	ServerLoop serverLoop;
	
	@Autowired
	RankSessionManager hallSessionManager;
		
	BlockingQueue<CmdData> msgQueue = new LinkedBlockingQueue<CmdData>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		serverLoop.register(this);	
	}

	@Override
	public void loop() throws Exception {
		while(true) {
			CmdData cmdData = msgQueue.take();
			if(cmdData == null) {
				return;
			}
			execute(cmdData);
//			logger.warn("msgQueueSize:" + msgQueue.size());
		}
	}

	public void handleMsg(PacketBase packet, RankSession session) throws Exception {
//		if(packet.getPacketType() == PacketType.HEARTBEAT) { //心跳包直接忽略
////			logger.info(hallSessionManager.getOnlinePlayerIdList().toString());
//			return;
//		}
		if(msgQueue.size()>1000){
			logger.info("大厅服务器---1000");
		}
		if(msgQueue.size()>3000){
			logger.info("大厅服务器---3000");
		}
		if(msgQueue.size()>5000){
			logger.info("大厅服务器---5000");
		}
		if(msgQueue.size() > 100000000) {
			logger.info("大厅服务器忙,请稍后重试");
//			PacketBase.Builder pb = PacketBase.newBuilder();
//			pb.setCode(-1);
//			pb.setPacketType(packet.getPacketType());
//			pb.setMsg("大厅服务器忙,请稍后重试");
//			hallSessionManager.write(session, pb.build().toByteArray());
//			return;
		}
		CmdData cmd = new CmdData(session, packet);
		msgQueue.put(cmd);
		
//		start(cmd);
	}

	private void execute(CmdData cmdData) throws Exception {
		cmdData.startExecuteTime = System.currentTimeMillis();
		Cmd<PacketType, CmdData> cmd = rankCmdMapper.get(cmdData.packet.getPacketType());
		if(cmd == null) {
			PacketBase.Builder pb = PacketBase.newBuilder();
			pb.setCode(-1);
			pb.setPacketType(cmdData.packet.getPacketType());
			pb.setMsg("命令无法处理");
			hallSessionManager.write(cmdData.session, pb.build().toByteArray());
			return;
		}
		cmd.execute(cmdData);
		cmdData.endExecuteTime = System.currentTimeMillis();
		
		if(cmdData.packet.getPacketType() != PacketType.HEARTBEAT) {
			logger.info("type={};wait={};start={}", cmdData.packet.getPacketType(), cmdData.startExecuteTime - cmdData.startWatingTime, cmdData.endExecuteTime - cmdData.startExecuteTime);
		}
	}
}
