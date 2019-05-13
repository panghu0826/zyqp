package com.buding.common.network.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.springframework.beans.factory.InitializingBean;

import com.buding.common.network.codec.NettyServerInitializer;

/**
 * @author jaime qq_1094086610
 * @Description:netty服务器
 * 
 */
public class NettyServer implements InitializingBean, Runnable {

	private int port;

	private NettyServerInitializer protocolInitalizer;

	@Override
	public void afterPropertiesSet() throws Exception {
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	public void run() {
		//服务类
		ServerBootstrap bootstrap = new ServerBootstrap();

		//boss和worker 线程池
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		try {
			//设置线程池
			bootstrap.group(boss, worker);

			//设置socket工厂
			bootstrap.channel(NioServerSocketChannel.class);

			//设置管道工厂
			bootstrap.childHandler(protocolInitalizer);

			/*netty3中对应设置如下
			bootstrap.setOption("backlog", 1024);
			bootstrap.setOption("tcpNoDelay", true);
			bootstrap.setOption("keepAlive", true);*/
			//设置TCP参数,mina,twist等都需设置(物理机参数)
			bootstrap.option(ChannelOption.SO_BACKLOG, 2048);//serverSocketchannel的设置，链接缓冲池的大小
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);//socketchannel的设置,维持链接的活跃，清除死链接
			bootstrap.childOption(ChannelOption.TCP_NODELAY, true);//socketchannel的设置,关闭延迟发送

			System.out.println("NettyServer 启动了:" + port);

			// 绑定端口，开始接收进来的连接
			ChannelFuture f = bootstrap.bind(port).sync();

			//等待服务端关闭
			f.channel().closeFuture().sync();

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			//释放资源
			boss.shutdownGracefully();
			worker.shutdownGracefully();
			System.out.println("NettyServer 关闭了:" + port);
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public NettyServerInitializer getProtocolInitalizer() {
		return protocolInitalizer;
	}

	public void setProtocolInitalizer(NettyServerInitializer protocolInitalizer) {
		this.protocolInitalizer = protocolInitalizer;
	}
}
