package com.buding.task.network;

import com.buding.common.model.Message;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import java.io.IOException;

@Sharable
public class TaskServerHandler extends SimpleChannelInboundHandler<Message> {
    /**
     *
     */
    private static final AttributeKey<TaskSession> SESSION = AttributeKey.newInstance("NettyHandler.TaskSessionKey");
    @Autowired
    TaskSessionManager taskSessionManager;

    @Autowired
    TaskCmdProc taskCmdProc;
    private Logger LOG = LogManager.getLogger(getClass());

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        // LOG.info("收到消息");
        PacketBase packet = PacketBase.parseFrom(msg.getData());
        TaskSession session = ctx.attr(SESSION).get();
        if (LOG.isDebugEnabled() && packet.getPacketType() != PacketType.HEARTBEAT) {
            LOG.debug("收到消息:" + packet.getPacketType().toString());
        }

        taskCmdProc.handleMsg(packet, session);
    }

//	@Override
//	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//		// IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
//		if (evt instanceof IdleStateEvent) {
//			IdleStateEvent e = (IdleStateEvent) evt;
//			switch (e.state()) {
//				case READER_IDLE:
//					handleReaderIdle(ctx);
//					break;
//				case WRITER_IDLE:
//					handleWriterIdle(ctx);
//					break;
//				case ALL_IDLE:
//					handleAllIdle(ctx);
//					break;
//				default:
//					break;
//			}
//		}
//	}

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        LOG.error("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
        ctx.close();
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        LOG.error("---WRITER_IDLE---");
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        LOG.error("---ALL_IDLE---");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        cleanWhenClosed(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("channelActive:{}", ctx.channel().remoteAddress());
        initWhenConnected(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("channelInactive:{}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException && cause.getMessage() != null && cause.getMessage().equals("远程主机强迫关闭了一个现有的连接")) {
            TaskSession session = (TaskSession) ctx.attr(SESSION).get();
            int playerId = session == null ? -1 : session.userId;
            LOG.error("远程自动关闭连接:{}", playerId);
            return;
        }
        LOG.error("ChannelException:", cause);
    }

    public void initWhenConnected(ChannelHandlerContext ctx) {
        TaskSession session = new TaskSession();
        session.channel = ctx.channel();
        session.initTime = System.currentTimeMillis();
        taskSessionManager.put2AnonymousList(session);
        ctx.attr(SESSION).set(session);
    }

    public void onSessionClosed(TaskSession session, ChannelHandlerContext ctx) {
        taskSessionManager.schedule2Remove(session);
    }

    public void cleanWhenClosed(ChannelHandlerContext ctx) {
        TaskSession session = ctx.attr(SESSION).get();
        if (session != null) {
            onSessionClosed(session, ctx);
            ctx.attr(SESSION).set(null);
        }
    }
}
