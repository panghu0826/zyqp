package com.buding.common.network.codec;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface NetWorkListener {

	public void msgRead(byte[] msg);

	public void channelActive(ChannelHandlerContext ctx) throws Exception;

	public void channelInactive(ChannelHandlerContext ctx) throws Exception;

	public void exceptionCaught(Throwable cause) throws Exception;
}
