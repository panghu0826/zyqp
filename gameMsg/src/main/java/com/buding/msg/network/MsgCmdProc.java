package com.buding.msg.network;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.alibaba.dubbo.remoting.exchange.support.header.HeartbeatHandler;
import com.buding.common.loop.Looper;
import com.buding.common.loop.ServerLoop;
import com.buding.common.network.command.Cmd;
import com.buding.msg.network.cmd.CmdData;
import com.buding.msg.network.cmd.MsgCmdMapper;


/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class MsgCmdProc implements Looper, InitializingBean {
	private Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	MsgCmdMapper msgCmdMapper;
	
	@Autowired
	MsgSessionManager msgSessionManager;
	
	@Autowired
	@Qualifier("MsgServerNetMsgLoop")
	ServerLoop serverLoop;
	
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
	
	public void handleMsg(PacketBase packet, MsgSession session) throws Exception {
//		if(packet.getPacketType() == PacketType.HEARTBEAT) { //心跳包直接忽略
//			return;
//		}
		if(msgQueue.size()>1000){
			logger.info("消息服务器---1000");
		}
		if(msgQueue.size()>3000){
			logger.info("消息服务器---3000");
		}
		if(msgQueue.size()>5000){
			logger.info("消息服务器---5000");
		}
		if(msgQueue.size() > 100000000) {
			logger.info("消息服务器忙,请稍后重试");
//			PacketBase.Builder pb = PacketBase.newBuilder();
//			pb.setCode(-1);
//			pb.setPacketType(packet.getPacketType());
//			pb.setMsg("消息服务器忙,请稍后重试");
//			msgSessionManager.write(session, pb.build().toByteArray());
//			return;
		}
		CmdData cmd = new CmdData(session, packet);
		msgQueue.put(cmd);
		
//		execute(cmd);
	}
	
	private void execute(CmdData cmdData) throws Exception {
		cmdData.startExecuteTime = System.currentTimeMillis();
		Cmd<PacketType, CmdData> cmd = msgCmdMapper.get(cmdData.packet.getPacketType());
		if(cmd == null) {
			PacketBase.Builder pb = PacketBase.newBuilder();
			pb.setCode(-1);
			pb.setPacketType(cmdData.packet.getPacketType());
			pb.setMsg("命令无法处理");
			msgSessionManager.write(cmdData.session, pb.build().toByteArray());
			return;
		}
		
		cmd.execute(cmdData);
		cmdData.endExecuteTime = System.currentTimeMillis();
		
		if(cmdData.packet.getPacketType() != PacketType.HEARTBEAT) {
//			if(cmdData.packet.getPacketType() == PacketType.AuthRequest){
//				logger.info("111111111111111111111111111111111111111111111111");
//			}
//			logger.info("type={};wait={};execute={}", cmdData.packet.getPacketType(), cmdData.startExecuteTime - cmdData.startWatingTime, cmdData.endExecuteTime - cmdData.startExecuteTime);
		}
		
	}
}
