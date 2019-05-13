package com.buding.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import packet.game.Hall.VistorRegisterRequest;
import packet.game.MsgGame.EnrollRequest;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.AuthRequest;
import packet.user.User.LoginRequest;

import com.buding.common.network.client.NettyClient;
import com.buding.common.network.codec.CommHanler;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class PlayerImpl implements Player {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	GameOperInput input;
	ScheduledExecutorService pool = Executors.newScheduledThreadPool(100);
	private PlayerData data;

	private GameServerProxy gameServerProxy;
	private HallServerProxy hallServerProxy;
	private MsgServerProxy msgServerProxy;
	
	public String username;
	public String password;

	public boolean init(final String serverIp, final int serverPort) {
		new Thread(new Runnable() {

			public void run() {
				try {
					PlayerImpl.this.data = new PlayerData();
					hallServerProxy = new HallServerProxy(PlayerImpl.this, PlayerImpl.this.data);
					new NettyClient().connect(serverIp, serverPort, new CommHanler(hallServerProxy));
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}).start();
		return false;
	}

	public boolean initMsg() {
		new Thread(new Runnable() {

			public void run() {
				try {					
					String addr = data.msgServerAddr;
					String serverIp = addr.split(":")[0];
					int serverPort = Integer.valueOf(addr.split(":")[1]);
					logger.info("初始化消息服务器 {}", addr);
					msgServerProxy = new MsgServerProxy(PlayerImpl.this, PlayerImpl.this.data);
					new NettyClient().connect(serverIp, serverPort, new CommHanler(msgServerProxy));
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}).start();
		return false;
	}

	public boolean initGame() {
		new Thread(new Runnable() {

			public void run() {
				try {
					String addr = data.gameServerAddr;
					String serverIp = addr.split(":")[0];
					int serverPort = Integer.valueOf(addr.split(":")[1]);
					logger.info("初始化游戏服务器 {}", addr);
					gameServerProxy = new GameServerProxy(PlayerImpl.this, PlayerImpl.this.data);
					new NettyClient().connect(serverIp, serverPort, new CommHanler(gameServerProxy));
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}).start();
		return false;
	}

	
	@Override
	public boolean isInitInteractCmd() {
		return input != null;
	}

	@Override
	public void initInteractCmd() {
		input = new GameOperInput(this.gameServerProxy, this.msgServerProxy, this.hallServerProxy);
		pool.scheduleAtFixedRate(input, 1, 1, TimeUnit.SECONDS);
	}

	public boolean isInit() {
		if(hallServerProxy == null) {
			return false;
		}
		boolean init = hallServerProxy.isInit();
		// logger.info("this.channel=" + this.channel.isActive());
		return init;
	}

	public boolean isMsgInit() {
		return msgServerProxy != null && msgServerProxy.isInit();
	}

	public boolean isGameInit() {
		return gameServerProxy != null && gameServerProxy.isInit();
	}

	public boolean isLogin() {
		return data.loginData != null;
	}

	public boolean isAuth() {
		return isGameAuth() && isMsgAuth();
	}

	public boolean isGameAuth() {
		return data.authGame;
	}

	public boolean isMsgAuth() {
		return data.authMsg;
	}

	public boolean isEnroll() {
		return data.gamingData != null && data.gamingData.enroll;
	}

	public boolean isReady() {
		return data.gamingData != null && data.gamingData.ready;
	}

	public void login() {
		LoginRequest.Builder lb = LoginRequest.newBuilder();
		lb.setUsername(this.username);
		lb.setPassward(this.password);
		lb.setType(2);
		hallServerProxy.sendPacket(PacketType.LoginRequest, lb.build().toByteString());
	}

	public void auth() {
		AuthRequest.Builder lb = AuthRequest.newBuilder();
		lb.setToken(data.loginData.getToken());
		lb.setUserId(data.loginData.getUserId());
		hallServerProxy.sendPacket(PacketType.AuthRequest, lb.build().toByteString());
	}

	public void authMsg() {
		AuthRequest.Builder lb = AuthRequest.newBuilder();
		lb.setToken(data.loginData.getToken());
		lb.setUserId(data.loginData.getUserId());
		msgServerProxy.sendPacket(PacketType.AuthRequest, lb.build().toByteString());
	}

	public void authGame() {
		AuthRequest.Builder lb = AuthRequest.newBuilder();
		lb.setToken(data.loginData.getToken());
		lb.setUserId(data.loginData.getUserId());
		gameServerProxy.sendPacket(PacketType.AuthRequest, lb.build().toByteString());
	}

	@Override
	public void gameHeatbeat() {
		gameServerProxy.sendPacket(PacketType.HEARTBEAT, null);
	}

	@Override
	public void hallHeatbeat() {
		hallServerProxy.sendPacket(PacketType.HEARTBEAT, null);
	}

	@Override
	public void msgHeatbeat() {
		msgServerProxy.sendPacket(PacketType.HEARTBEAT, null);
	}

	public void enroll(String match, String code) {
		EnrollRequest.Builder lb = EnrollRequest.newBuilder();
		lb.setGameId("G_DQMJ");
		lb.setMatchId(match == null ? "G_DQMJ_MATCH_SINGLE" : match);
		if(code != null) {
			lb.setRoomCode(code);
		}
//		lb.addCards(1);
//		lb.addCards(1);
//		lb.addCards(2);
//		lb.addCards(2);
//		lb.addCards(3);
//		lb.addCards(3);
//		lb.addCards(24);
//		lb.addCards(24);
//		lb.addCards(25);
//		lb.addCards(25);
//		lb.addCards(26);
//		lb.addCards(26);
//		lb.addCards(69);
//		lb.addCards(69);
		gameServerProxy.sendPacket(PacketType.EnrollRequest, lb.build().toByteString());
	}

	public void ready() {
		gameServerProxy.sendPacket(PacketType.ReadyRequest, null);
	}

	@Override
	public boolean hasAccount() {
		return username != null;
	}

	@Override
	public void loadAccount() {
		//游客注册
		VistorRegisterRequest.Builder vb = VistorRegisterRequest.newBuilder();
		vb.setDeviceFlag(1);
		vb.setDeviceId(System.currentTimeMillis()+"");
		hallServerProxy.sendPacket(PacketType.VistorRegisterRequest, vb.build().toByteString());
	}

	public GameServerProxy getGameServerProxy() {
		return gameServerProxy;
	}

	public HallServerProxy getHallServerProxy() {
		return hallServerProxy;
	}

	public MsgServerProxy getMsgServerProxy() {
		return msgServerProxy;
	}

	@Override
	public void setAccount(String username, String passwd) {
		this.username = username;
		this.password = passwd;
	}	
}
