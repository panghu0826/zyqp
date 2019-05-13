package com.buding.common.network.codec;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.List;

public class NettyServerInitializerOld extends NettyServerInitializer {
    private List<ChannelHandler> handlers;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new Netty4CodecOld());
        for (ChannelHandler chr : handlers) {
            pipeline.addLast(chr);
        }
    }

    @Override
    public List<ChannelHandler> getHandlers() {
        return handlers;
    }

    @Override
    public void setHandlers(List<ChannelHandler> handlers) {
        this.handlers = handlers;
    }

}

