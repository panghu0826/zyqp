package com.buding.hall.network;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User;

import java.io.IOException;

@Sharable
public class OldHallServerHandler extends SimpleChannelInboundHandler<byte[]> {
    private Logger LOG = LogManager.getLogger(getClass());

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        PacketBase.Builder pb = PacketBase.newBuilder();
        pb.setCode(0);
        pb.setPacketType(PacketType.NeedUpdate);
        pb.setData(User.NeedUpdate.newBuilder().build().toByteString());
        ctx.channel().writeAndFlush(pb.build().toByteArray());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("channelActive:{}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("channelInactive:{}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException && cause.getMessage() != null && cause.getMessage().equals("远程主机强迫关闭了一个现有的连接")) {
            LOG.error("远程自动关闭连接");
            return;
        }
        LOG.error("ChannelException:", cause);
    }
}
