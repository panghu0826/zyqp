package com.buding.common.network.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class CommHanler extends SimpleChannelInboundHandler<byte[]> {
	public NetWorkListener listner;

	public CommHanler(NetWorkListener l) {
		this.listner = l;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
		this.listner.msgRead(msg);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		listner.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {		
		super.channelInactive(ctx);
		listner.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		listner.exceptionCaught(cause);
	}

	public NetWorkListener getListner() {
		return listner;
	}

	public void setListner(NetWorkListener listner) {
		this.listner = listner;
	}
}
