package com.buding.battle.logic.network;

import com.buding.common.model.Message;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import packet.game.Hall.ServerChangeSyn;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.cluster.BattleClusterServer;
import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.common.cluster.model.ServerModel;
import com.buding.common.cluster.service.ICluster;
import com.buding.common.server.NodeState;
import com.buding.common.server.ServerComponent;

@Sharable
public class BattleServerHandler extends SimpleChannelInboundHandler<Message> implements ServerComponent, InitializingBean {
	private Logger LOG = LogManager.getLogger(getClass());

	// @Autowired
	// LoginModule loginModuel;

	@Autowired
	BattleSessionManager sessionManager;

	@Autowired
	ICluster serverClusterService;

	@Autowired
	BattleClusterServer server;

	/**
	 * 
	 */
	private static final AttributeKey<BattleSession> SESSION = AttributeKey.newInstance("NettyHandler.BattleSessionKey");

	@Override
	public void afterPropertiesSet() throws Exception {
		server.getComponentList().add(this);
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
		PacketBase packet = PacketBase.parseFrom(msg.getData());
		BattleSession session = (BattleSession) ctx.attr(SESSION).get();
		if(packet.getPacketType()!=PacketType.HEARTBEAT){
//			LOG.info("rtype={}",packet.getPacketType());
		}
		session.currentModule.handleMsg(ctx, session, packet.getPacketType(), packet);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOG.info("channelActive:{}", ctx.channel().remoteAddress());
		initWhenConnected(ctx);
		super.channelActive(ctx);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		LOG.info("handlerRemoved:{}", ctx.channel().remoteAddress());
		super.handlerRemoved(ctx);
		cleanWhenClosed(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOG.info("channelInactive:{}", ctx.channel().remoteAddress());
		super.channelInactive(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			switch (e.state()) {
			case READER_IDLE:
				handleReaderIdle(ctx);
				break;
			case WRITER_IDLE:
				handleWriterIdle(ctx);
				break;
			case ALL_IDLE:
				handleAllIdle(ctx);
				break;
			default:
				break;
			}
		}
	}
	
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
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof IOException && cause.getMessage() != null && cause.getMessage().equals("远程主机强迫关闭了一个现有的连接")) {
			BattleSession session = (BattleSession) ctx.attr(SESSION).get();
			int playerId = session == null ? -1 : session.userId;
			LOG.error("远程自动关闭连接:{}", playerId);
			ctx.channel().close();
			return;
		}
		LOG.error("ChannelException:", cause);
	}

	public void initWhenConnected(ChannelHandlerContext ctx) {
		BattleSession session = new BattleSession();
		session.channel = ctx.channel();
		session.initTime = System.currentTimeMillis();
		session.currentModule = ServiceRepo.matchModule;
		sessionManager.put2AnonymousList(session);
		ctx.attr(SESSION).set(session);
	}

	public void cleanWhenConnected(ChannelHandlerContext ctx) {
		BattleSession session = ctx.attr(SESSION).get();
		if (session != null) {
			sessionManager.removeFromAnonymousList(session.getSessionId());
			ctx.attr(SESSION).set(null);
		}
	}

	public void onSessionClosed(BattleSession session, ChannelHandlerContext ctx) {
		sessionManager.schedule2Remove(session);
	}

	public void cleanWhenClosed(ChannelHandlerContext ctx) {
		BattleSession session = ctx.attr(SESSION).get();
		if (session != null) {
			onSessionClosed(session, ctx);
			ctx.attr(SESSION).set(null);
		}
	}

	NodeState state = NodeState.RUNNING;

	@Override
	public void start() throws Exception {
		state = NodeState.RUNNING;
	}

	@Override
	public void stop() throws Exception {
		state = NodeState.STOPING;
		int num = 0;
		for (int playerId : sessionManager.getOnlinePlayerIdList()) {
			BattleSession session = sessionManager.getIoSession(playerId);
			if (session.channel == null) {
				continue;
			}
			ServerModel model = serverClusterService.getFreeServer("battle");
			if (model == null) {
				throw new RuntimeException("没有空余battle服务器");
			}
			ServerChangeSyn.Builder sb = ServerChangeSyn.newBuilder();
			sb.setGameServerAddr(model.addr);
			PushService.instance.pushPBMsg(playerId, PacketType.ServerChangeSyn, sb.build().toByteString());
			num++;
			if (num == 300) {
				break;
			}
		}
		if (num == 0) {
			state = NodeState.STOPED;
		}
	}

	@Override
	public void restart() throws Exception {
		state = NodeState.RUNNING;
	}

	@Override
	public String getName() {
		return BattleServerHandler.class.getName();
	}

	@Override
	public NodeState getState() {
		return state;
	}

	@Override
	public String getStatusDesc() {
		StringBuilder sb = new StringBuilder();
		sb.append("onlineSize:" + sessionManager.getCurrentOnlineCount());
		return sb.toString();
	}

}
