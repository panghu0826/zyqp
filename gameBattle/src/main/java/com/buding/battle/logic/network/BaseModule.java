package com.buding.battle.logic.network;

import com.buding.api.game.PokerWanfa;
import com.buding.battle.common.network.Invoker;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.common.*;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import com.buding.battle.logic.module.game.service.GameService;
import com.buding.battle.logic.module.game.service.VipService;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.match.MultiMatchImpl;
import com.buding.battle.logic.module.user.service.LoginService;
import com.buding.battle.logic.network.module.Module;
import com.buding.common.result.Result;
import com.buding.db.model.UserRoom;
import com.buding.hall.module.common.constants.CurrencyType;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import packet.game.MsgGame;
import packet.game.MsgGame.*;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.AuthRequest;

import java.util.HashMap;
import java.util.Map;

public class
BaseModule implements Module<PacketType, PacketBase>, InitializingBean {
	protected Logger LOG = LogManager.getLogger(getClass());

	@Autowired
	protected BattleSessionManager sessionManager;

	@Autowired
	GameService matchService;

	@Autowired
	GameService gameService;

	@Autowired
	PushService pushService;

	@Autowired
	LoginService loginService;

	@Autowired
	VipService vipService;

	protected Map<PacketType, Invoker<PacketBase>> cmdInvokerMap = new HashMap<PacketType, Invoker<PacketBase>>();

	public void handleMsg(ChannelHandlerContext ctx, BattleSession session, PacketType key, PacketBase packet) throws Exception {
		long startTime = System.currentTimeMillis();
		Invoker<PacketBase> invoker = cmdInvokerMap.get(key);
		if (invoker == null) {
			onUnRecognizeMsgReceived(session, key.toString(), packet);
		} else {
			invoker.invoke(session, packet);
		}
		if(key != PacketType.HEARTBEAT) {
			LOG.info("type={};wait={};executetime={}", key, -1, System.currentTimeMillis() - startTime);
		}
		if(key == PacketType.HEARTBEAT) {
//			LOG.info("111111111111111111111");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.cmdInvokerMap.put(PacketType.AuthRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onAuthMsgReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.EnrollRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onEnrollMsgReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.MultiMatchEnrollRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onMultiMatchEnrollMsgReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.ExitMultiMatchRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onExitMultiMatchRequestMsgReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.PlayerSitRequst, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerSitMsgReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.PlayerExitPosNotExitRoomRequst, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerExitPosNotExitRoomRequstReceived(session, msg);
			}
		});

		// this.cmdInvokerMap.put("PB_Room_Enter_Room_Request", new
		// Invoker<PacketBase>() {
		// @Override
		// public void invoke(BattleSession session, PacketBase msg) throws
		// Exception {
		// onEnterRoomMsgReceived(session, msg);
		// }
		// });

		this.cmdInvokerMap.put(PacketType.ReadyRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onDeskReadyMsgReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.GameOperation, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onGamePacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.ChangeDeskRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onChangeDeskMsgReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.StartGameRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onStartGameMsgReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.Back2HallRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onBack2HallReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.KickPlayerRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onKickoutPlayerReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.HEARTBEAT, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onHeatbeatReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.Dump, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onDumpGamePacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.CreateVipRoomRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onCreateVipPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.EnterVipRoomRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onEnterVipPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.VipRoomListReuqest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onVipRoomListPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.DismissVipRoomRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onDismissVipRoomPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.ExitGameRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerExitPacketReceived(session, msg);
			}
		});
		
		this.cmdInvokerMap.put(PacketType.AwayGameRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerAwayPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.GameChatMsgRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onGameChatMsgPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.HangupRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onHangupRequestPacketReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.CancelHangupRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onCancelHangupRequestPacketReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.DissmissVoteRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onDismissVoteRequestPacketReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.PlayerGamingSynInquire, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerGamingSynReqPacketReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.BackGameRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerBackGamePacketReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.ViewGuanZhanReq, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onViewGuanZhanReqPacketReceived(session, msg);
			}
		});
	}

	private void onViewGuanZhanReqPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		MsgGame.ViewGuanZhanReq req = MsgGame.ViewGuanZhanReq.parseFrom(packet.getData());
		CommonDesk desk = session.getDeskMap().get(req.getDeskId());
		if(desk!= null){
			desk.onViewGuanZhanReqPacketReceived(session.userId);
		}
	}

	private void onStartGameMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		MsgGame.StartGameRequest req = MsgGame.StartGameRequest.parseFrom(packet.getData());
		CommonDesk desk = session.getDeskMap().get(req.getRoomCode());
		if (desk != null) {
			desk.onStartGameMsgReceived(session.userId);
		}
	}

   public void onPlayerSitMsgReceived(BattleSession session, PacketBase packet) throws Exception {
        MsgGame.PlayerSitRequst req = MsgGame.PlayerSitRequst.parseFrom(packet.getData());
        CommonDesk desk = session.getDeskMap().get(req.getDeskId());
        LOG.info("玩家--"+session.userId+"--请求坐下,桌子ID--"+req.getDeskId()+"玩家请求位置--"+req.getDeskPos());
        if(req.getDeskPos()<0) {
        	LOG.error("居然传位置-1");
		}
        if(desk != null){
            desk.onPlayerSitMsgReceived(session.userId,req.getDeskPos());
        }
    }

    private void onPlayerExitPosNotExitRoomRequstReceived(BattleSession session, PacketBase packet) throws Exception {
        MsgGame.PlayerExitPosNotExitRoomRequst req = MsgGame.PlayerExitPosNotExitRoomRequst.parseFrom(packet.getData());
	    CommonDesk desk = session.getDeskMap().get(req.getDeskId());
	    if(desk != null){
	        desk.onPlayerExitPosNotExitRoomRequstReceived(session.userId,req.getDeskPos());
        }
    }

    public void onHangupRequestPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		MsgGame.HangupRequest req = MsgGame.HangupRequest.parseFrom(packet.getData());
		CommonDesk desk = session.getDeskMap().get(req.getDeskId());
		if (desk != null) {
			desk.onPlayerHangupPacketReceived(session.userId);
		}
	}

	//玩家点加入房间判断有没有在玩的桌子,如果有直接进入
	public void onPlayerGamingSynReqPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		PlayerGamingSynInquire.Builder pb =  PlayerGamingSynInquire.newBuilder();
		if(session.getMatch()!=null && session.getMatch() instanceof MultiMatchImpl && session.getStatus() != PlayerStatus.GAMING){
			pushService.pushErrorMsg(session.userId, PacketType.GlobalMsgSyn, "您报名的比赛即将开始,请先退赛后进入房间");
			return;
		}

		CommonDesk desk = session.getPlayingOrReadyDesk(session.userId);
		if (session != null && desk != null) {
			if(session.awayStatus == AwayStatus.AWAY) {
				desk.onPlayerComeBackPacketReceived(session.userId);
			} else {
				desk.onPlayerReconnectPacketReceived(session.userId);
			}
			pb.setIsGaming(true);
		} else {
			pb.setIsGaming(false);
		}
		pushService.pushPBMsg(session, PacketType.PlayerGamingSynInquire, pb.build().toByteString());
	}

	//从别的应用切换到我们游戏,比如按home键离开,在回来
	public void onPlayerBackGamePacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CommonDesk desk = session.getPlayingOrReadyDesk(session.userId);
		if (session != null && desk != null) {
			desk.onPlayerComeBackPacketReceived(session.userId);
		}
	}

	public void onDismissVoteRequestPacketReceived(BattleSession session, PacketBase packet) throws Exception {
        MsgGame.DissmissVoteRequest req = MsgGame.DissmissVoteRequest.parseFrom(packet.getData());
		CommonDesk desk = session.getDeskMap().get(req.getDeskId());
		if (desk != null) {
//		    if((desk.getplayerIdList().contains(session.userId) || session.userId == desk.getDeskOwner()))
			desk.onPlayerDissVotePacketReceived(session.userId, req.getAgree());
		}
	}

	public void onCancelHangupRequestPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		MsgGame.HangupRequest req = MsgGame.HangupRequest.parseFrom(packet.getData());
		CommonDesk desk = session.getDeskMap().get(req.getDeskId());
		if (desk != null) {
			desk.onPlayerCancelHangupPacketReceived(session.userId);
		}
	}

	public void onGameChatMsgPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CommonDesk desk = session.getPlayingOrReadyDesk(session.userId);
		if (desk != null) {
			GameChatMsgRequest req = GameChatMsgRequest.parseFrom(packet.getData());
			desk.onChatMsgPacketReceived(session.getPlayerId(), req.getContentType(), req.getContent().toByteArray());
		}
	}

	public void onVipRoomListPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		vipService.pushVipRoomList(session.userId);
	}

	public void onDismissVipRoomPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		DismissVipRoomRequest req = DismissVipRoomRequest.parseFrom(packet.getData());
		vipService.dissmissVipRoom(session.userId, req.getCode());
	}

	public void onPlayerExitPacketReceived(BattleSession session, PacketBase packet) throws Exception {
        MsgGame.ExitGameRequest req = MsgGame.ExitGameRequest.parseFrom(packet.getData());
		CommonDesk desk = session.getDeskMap().get(req.getDeskId());
		if (desk != null) {
			desk.onPlayerExitPacketReceived(session.userId);
		}
	}
	
	public void onPlayerAwayPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CommonDesk desk = session.getPlayingOrReadyDesk(session.userId);
		if (desk != null) {
			desk.onPlayerAwayPacketReceived(session.userId);
		}
	}

	public void onKickoutPlayerReceived(BattleSession session, PacketBase packet) throws Exception {
		KickPlayerRequest req = KickPlayerRequest.parseFrom(packet.getData());
		String deskCode = req.getCode();
		int playerId = req.getPlayerId();

		vipService.kick(session.userId, playerId, deskCode);
	}

	public void onHeatbeatReceived(BattleSession session, PacketBase packet) throws Exception {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(0);
		pb.setPacketType(packet.getPacketType());
	 	sessionManager.write(session, pb.build().toByteArray());
	}

	public void onBack2HallReceived(BattleSession session, PacketBase packet) {
		if (session.getStatus() == PlayerStatus.GAMING) {
			pushService.pushErrorMsg(session, PacketType.Back2HallRequest, "游戏已开始,不能退出");
			return;
		}
		//
		int playerId = session.userId;
		Match match = session.getMatch();

		if (match != null) {
			match.playerExit(playerId, PlayerExitType.REQUEST_EXIT);
		}
		//
		// pushService.pushBack2HallRsp(session.userId, BaseRsp.success());
	}

	public void onDumpGamePacketReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onCreateVipPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CreateVipRoomRequest request = CreateVipRoomRequest.parseFrom(packet.getData());
		LOG.info("桌子玩法---"+JsonFormat.printToString(request)+"--"+ PokerWanfa.getWanFaString(request.getWangfa()));
		Result r = vipService.createVipRoom(session.userId, request);

		if(r.isOk()){
			vipService.enroll(session, r.msg);
        }else{
			PushService.instance.pushErrorMsg(session.userId, PacketType.GlobalMsgSyn, r.msg);
		}
	}

	public void onEnterVipPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onGamePacketReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onDeskReadyMsgReceived(BattleSession session, PacketBase packet) {
		gameService.requestReady(session);
	}

	public void onChangeDeskMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onQuickStartMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onEnterRoomMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onEnrollMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		EnrollRequest enrollReq = EnrollRequest.parseFrom(packet.getData());
		String gameId = enrollReq.getGameId();
		String matchId = enrollReq.getMatchId();

		session.debugData = enrollReq.getCardsList();

		String roomCode = enrollReq.getRoomCode();

		Result ret;

		if(session.getMatch()!=null && session.getMatch() instanceof MultiMatchImpl && session.getStatus() != PlayerStatus.GAMING){
			pushService.pushErrorMsg(session.userId, PacketType.GlobalMsgSyn, "您报名的比赛即将开始,请先退赛后进入房间");
			return;
		}

		if (StringUtils.isNotBlank(roomCode)) {
			UserRoom ur = ServiceRepo.userRoomDao.getByCode(roomCode);
			if(ur == null) {
				pushService.pushErrorMsg(session.userId, PacketType.GlobalMsgSyn, "房间不存在");
				return;
			}
            JSONObject obj = JSONObject.fromObject(ur.getParams());
            int fee = obj.optInt("fee");
//            int clubRoomType = obj.getInt("clubRoomType");
            String deskId = session.getPlayingDesk(session.userId)== null ? "":session.getPlayingDesk(session.userId).getDeskID();
            if(!StringUtils.equals(deskId,roomCode)) {
//                if (session.userId != ur.getOwnerId()) {
                    Result r = ServiceRepo.hallPortalService.hasEnoughCurrency(session.userId, CurrencyType.diamond, fee);
                    if (r.isFail()) {
                        pushService.pushErrorMsg(session.userId, PacketType.GlobalMsgSyn, "钻石不足");
                        return;
                    }
//                }
            }
			ret = vipService. enroll(session, roomCode);
			// 2 代表已经在游戏中
            pushService.pushEnrollRsp(session.userId, ret.isOk() || ret.code == Result.RESULT_BACK, ret.msg,gameId);
		} else {
		    BattleContext ctx = BattleContext.create(session).setGameId(gameId).setMatchId(matchId);
			ret = gameService.enroll(session, ctx);
            pushService.pushEnrollRsp(session.userId, ret.isOk() || ret.code == Result.RESULT_BACK, ret.msg,gameId);

            if (ret.code >= 1) return;
            CommonDesk desk =  session.getDeskMap().get(ctx.getDeskId());
            if(ret.isOk() && desk.isAutoReady()) desk.onPlayerReadyPacketReceived(session.userId);
		}
	}


	//比赛场退赛处理
	private void onExitMultiMatchRequestMsgReceived(BattleSession session, PacketBase packet) throws InvalidProtocolBufferException {
		MsgGame.ExitMultiMatchRequest exitMultiMatchRequest = MsgGame.ExitMultiMatchRequest.parseFrom(packet.getData());
		int playerId = exitMultiMatchRequest.getPlayerId();
		Match match = gameService.getMultiMatch(exitMultiMatchRequest.getGameId(),exitMultiMatchRequest.getMatchId(),playerId);
		if(match==null){
			pushService.pushErrorMsg(playerId, PacketType.GlobalMsgSyn, "未找到该比赛");
			return;
		}
		if(session.getStatus()==PlayerStatus.LUNKONG || session.getStatus()==PlayerStatus.GAMING){
			pushService.pushErrorMsg(playerId, PacketType.GlobalMsgSyn, "您已参与比赛,请耐心等待下一轮开始");
			return;
		}

		Result ret = match.unEnroll(playerId);
		//返还钻石
		ServiceRepo.hallPortalService.changeDiamond(playerId, match.getMatchConfig().conditionInfo.enterCondition.minCoinLimit, false, ItemChangeReason.EXIT_MULTI_MATCH);
		pushService.pushUnEnrollMsg(playerId,ret.isOk(),ret.msg);
	}
	//比赛场报名消息处理
	public void onMultiMatchEnrollMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		MsgGame.MultiMatchEnrollRequest multiMatchEnrollRequest = MsgGame.MultiMatchEnrollRequest.parseFrom(packet.getData());
		String gameId = multiMatchEnrollRequest.getGameId();
		String matchId = multiMatchEnrollRequest.getMatchId();
		String roomId = multiMatchEnrollRequest.getRoomCode();
		MultiMatchImpl match = gameService.getMultiMatch(gameId,matchId,session.userId);
		StringBuilder result = new StringBuilder();
		if(match!=null){
			result.append("您已报名");
			if(match.biSaiNum>0){
				result.append(",请重新进入房间进行比赛");
			}
			pushService.pushMultiMatchEnrollRsp(session.userId, false, result.toString(),gameId);
			return;
		}
		Result ret = gameService.enroll(session, BattleContext.create(session).setGameId(gameId).setMatchId(matchId).setRoomId(roomId));

		// 2 代表已经在游戏中
		pushService.pushMultiMatchEnrollRsp(session.userId, ret.isOk() || ret.code == 2, ret.msg,gameId);

		if (ret.code == 2) {
			return;
		}
	}

	public void onAuthMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		AuthRequest authReq = AuthRequest.parseFrom(packet.getData());
		loginService.auth(session, authReq.getUserId(), authReq.getToken());
	}

	protected void writeResponse(BattleSession session, String key, Object obj) {
		JSONObject json = new JSONObject();
		json.put(key, new Gson().toJson(obj));
		String txt = new Gson().toJson(json);
		session.channel.writeAndFlush(new TextWebSocketFrame(txt));
	}

	/**
	 * 处理无法识别的命令
	 *
	 * @param packet
	 */
	public void onUnRecognizeMsgReceived(BattleSession session, String key, PacketBase packet) {
		// TODO
//		LOG.error("ErrCmd:{}, currentModule:{},userId:{}", key, session.currentModule == null ? "":session.currentModule.getClass(), session.user!=null? session.user.getId():"");
//		PacketBase.Builder pb = PacketBase.newBuilder();
//		pb.setCode(-1);
//		pb.setPacketType(packet.getPacketType());
//		pb.setMsg("无法处理该命令:" + packet.getPacketType());
//		sessionManager.write(session, pb.build().toByteArray());
	}
}
