package com.buding.test;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.network.codec.NetWorkListener;
import com.google.protobuf.ByteString;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public abstract class BaseListener implements NetWorkListener {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Channel channel;
	protected Player player;
	protected PlayerData data;

	public BaseListener(Player player, PlayerData data) {
		this.player = player;
		this.data = data;
	}

	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.channel = ctx.channel();
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

	}

	public void exceptionCaught(Throwable cause) throws Exception {
		logger.error("", cause);
	}

	public void sendPacket(PacketType type, ByteString data) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setPacketType(type);
		if(data != null) {
			pb.setData(data);
		}
		
//		logger.info("发送消息:" + pb.getPacketType());
		
		byte[] bytes = pb.build().toByteArray();
		this.channel.writeAndFlush(bytes);
	}
	
	public boolean isInit() {
		return this.channel != null && this.channel.isOpen();
	}
	
	public void tick() {
		
	}
}
