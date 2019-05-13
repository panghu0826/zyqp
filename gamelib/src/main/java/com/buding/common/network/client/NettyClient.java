package com.buding.common.network.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.buding.common.network.codec.CommHanler;
import com.buding.common.network.codec.Netty4Codec;
import com.buding.common.network.codec.NettyServerInitializer;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class NettyClient {
	public void connect(String strHost, int nPort, final CommHanler h) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		NettyServerInitializer protocolInitalizer = new NettyServerInitializer();
		protocolInitalizer.setHandlers(new ArrayList<ChannelHandler>());
		protocolInitalizer.getHandlers().add(new Netty4Codec());
		protocolInitalizer.getHandlers().add(h);
		try {
			Bootstrap b = new Bootstrap();
			b.group(group) // 注册线程池
					.channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
					.remoteAddress(new InetSocketAddress(strHost, nPort)) // 绑定连接端口和host信息
					.handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
								@Override
								protected void initChannel(SocketChannel ch) throws Exception {
									System.out.println("connected...");
									ch.pipeline().addLast(new Netty4Codec());
									ch.pipeline().addLast(h);
								}
							});

			ChannelFuture f = b.connect().sync();
			f.channel().closeFuture().sync();
		} finally {
			 group.shutdownGracefully().sync();
		}
	}
}
