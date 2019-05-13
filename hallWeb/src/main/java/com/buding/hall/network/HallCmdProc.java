package com.buding.hall.network;

import java.util.Map;
import java.util.concurrent.*;

import io.netty.channel.Channel;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.loop.ServerLoop;
import com.buding.common.network.command.Cmd;
import com.buding.hall.network.cmd.CmdData;
import com.buding.hall.network.cmd.HallCmdMapper;


/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class HallCmdProc{
	private Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	HallCmdMapper hallCmdMapper;

//	@Autowired
//	@Qualifier("HallServerNetMsgLoop")
//	ServerLoop serverLoop;

	@Autowired
	HallSessionManager hallSessionManager;
//
//	BlockingQueue<CmdData> msgQueue = new LinkedBlockingQueue<CmdData>();
//
//	Map<Channel,Integer> channelMsgNum = new ConcurrentHashMap<>();
//
//	Map<Channel,Long> channelLoginTime = new ConcurrentHashMap<>();
//
//	Map<Channel,Boolean> channelHertBeat = new ConcurrentHashMap<>();

	private ExecutorService executor = Executors.newCachedThreadPool();

//	@Override
//	public void afterPropertiesSet() throws Exception {
//		serverLoop.register(this);
//	}

//	@Override
//	public void loop() throws Exception {
//		while(true) {
//			CmdData cmdData = msgQueue.take();
//			if(cmdData == null || !cmdData.session.channel.isOpen()) {
//				channelMsgNum.remove(cmdData.session.channel);
//				channelLoginTime.remove(cmdData.session.channel);
//				channelLoginTime.remove(cmdData.session.channel);
//				return;
//			}
//			execute(cmdData);
//			int num =channelMsgNum.get(cmdData.session.channel)-1;
//			if(num == 0){
//				if(channelMsgNum.get(cmdData.session.channel) != null){
//					channelMsgNum.remove(cmdData.session.channel);
//					return;
//				}
//			}
//			channelMsgNum.put(cmdData.session.channel,num);
//
////			logger.warn("msgQueueSize:" + msgQueue.size());
//		}
//	}

//	public void loopTest(){
//		while(true) {
//			try {
//				long t1 = System.currentTimeMillis();
//				CmdData cmdData = msgQueue.take();
//				long t2 = System.currentTimeMillis();
////				logger.info("耗时------"+(t2-t1));
//				if (cmdData == null || !cmdData.session.channel.isOpen()) {
//					return;
//				}
//				execute(cmdData);
//			}catch (Exception e){
//				e.printStackTrace();
//			}
//		}
//	}

	public void handleMsg(PacketBase packet, HallSession session) throws Exception {
//		if(packet.getPacketType() == PacketType.HEARTBEAT) {
//			channelHertBeat.putIfAbsent(session.channel,true);
//			execute(new CmdData(session,packet));
//			return;
//		}
//		if(packet.getPacketType() == PacketType.LoginRequest || packet.getPacketType() == PacketType.ReconnetLogin) {
//			channelLoginTime.put(session.channel, System.currentTimeMillis());
//		}
//		//单IP消息堆积15个关闭
//		int num = channelMsgNum.get(session.channel)==null? 1: (channelMsgNum.get(session.channel)+1);
//		channelMsgNum.put(session.channel,num);
//		if(num >= 15) {
//			logger.error("单IP消息堆积15个关闭,玩家id:"+session.userId+"玩家/攻击者ip:"+session.channel.remoteAddress()+"消息类型:"+packet.getPacketType());
//			session.channel.close();
//			return;
//		}
//
//
//		//单ip登陆前发送消息直接关闭
//		long loginTime = 0l;
//		if(channelLoginTime.get(session.channel) == null){
//			if(packet.getPacketType() != PacketType.LoginRequest
//					&& packet.getPacketType() != PacketType.SubmitInviteCodeRequest
//					&& packet.getPacketType() != PacketType.RegisterRequest){
//				logger.error("单ip登陆前发送消息直接关闭,玩家id:"+session.userId+"玩家/攻击者ip:"+session.channel.remoteAddress()+"消息类型:"+packet.getPacketType());
//				session.channel.close();
//				return;
//			}else {
//				loginTime = System.currentTimeMillis();
//			}
//		}else{
//			loginTime = channelLoginTime.get(session.channel);
//		}
//
//		//单ip登陆后8秒内不发心跳强关
//		if((System.currentTimeMillis() - loginTime > 8 * 1000)
//				&& (channelHertBeat.get(session.channel) == null || !channelHertBeat.get(session.channel))){
//			logger.error("单ip登陆后5秒内不发心跳强关,玩家id:"+session.userId+"玩家/攻击者ip:"+session.channel.remoteAddress()+"消息类型:"+packet.getPacketType());
//			session.channel.close();
//			return;
//		}
////
//		if(msgQueue.size()>1000){
//			logger.info("大厅服务器---1000"+"真实长度--"+msgQueue.size());
//		}
//		if(msgQueue.size()>5000){
//			logger.info("大厅服务器---5000");
//		}
//		if(msgQueue.size()>10000){
//			logger.info("大厅服务器---10000");
//		}

		CmdData cmd = new CmdData(session, packet);
//		executor.submit(() -> {
//            try {
//                execute(cmd);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
		execute(cmd);
//		msgQueue.put(cmd);
		
	}

	private void execute(CmdData cmdData) throws Exception {
		cmdData.startExecuteTime = System.currentTimeMillis();
		Cmd<PacketType, CmdData> cmd = hallCmdMapper.get(cmdData.packet.getPacketType());
		if(cmd == null) {
			logger.info("命令无法处理");
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
