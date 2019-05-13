package com.buding.battle.logic.module.common;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.VipDesk;
import com.buding.common.model.Message;
import com.buding.db.model.User;
import com.buding.db.model.UserGameOutline;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.packet.player.PlayerInfoSyn;
import com.buding.poker.constants.PokerConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall.LogoutSyn;
import packet.game.MsgGame;
import packet.game.MsgGame.*;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.AuthResponse;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PushService implements InitializingBean {
	private Logger logger = LogManager.getLogger(getClass());

	public static PushService instance;

	@Autowired
	BattleSessionManager sessionManager;

	@Autowired
	UserDao userDao;
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

	/**
	 * 推送房间信息
	 * 
	 * @param playerId
	 * @param roomId
	 * @param errMsg
	 */
	public void pushRoomInfo(int playerId, String roomId, String errMsg) {
		// RoomInfo room = new RoomInfo();
		// room.roomId = roomId;
		// room.msg = errMsg;
		//
		// sessionManager.writeTextWebSocketFrame(playerId, "PB_Room_Info_Syn",
		// room);
	}

	/**
	 * 推送赛场信息
	 * 
	 * @param playerId
	 * @param matchId
	 * @param errMsg
	 */
	public void pushMatchInfo(int playerId, String matchId, String errMsg) {
		// MatchInfo match = new MatchInfo();
		// match.matchId = matchId;
		// match.msg = errMsg;
		//
		// sessionManager.writeTextWebSocketFrame(playerId, "PB_Match_Info_Syn",
		// match);
	}

	/**
	 * 推送桌子信息
	 * 
	 * @param playerId
	 * @param deskId
	 * @param errMsg
	 */
	public void pushDeskInfo(int playerId, String deskId, int playerCount, String errMsg) {
		// DeskInfo desk = new DeskInfo();
		// desk.deskId = deskId+"";
		// desk.msg = errMsg;
		// desk.playerCount = playerCount;
		//
		// sessionManager.writeTextWebSocketFrame(playerId, "PB_Desk_Info_Syn",
		// desk);
	}
	
	public void pushKickoutSyn(BattleSession session, Integer playerId, String msg) {
		pushMsg(session,null,playerId, PacketType.KickOutSyn);
	}

	public void pushKickoutSyn(int playerId, String msg) {
		pushPBMsg(playerId, PacketType.KickOutSyn, null);
	}

	public void pushQuickStartRsp(int playerId, int code, String msg) {
		// BaseRsp rsp = new BaseRsp();
		// rsp.result = code;
		// rsp.msg = msg;
		// sessionManager.writeTextWebSocketFrame(playerId,
		// "PB_QuickStart_Response", rsp);
	}

	/**
	 * 推送玩家进入信息
	 */
	public void pushPlayerSitSyn(int playerId, int toPushPlayerId,String deskId ) {
		logger.info("act=pushPlayerSitSyn;playerId=" + playerId + ";receiverId=" + toPushPlayerId);
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session != null) {
			User user = ServiceRepo.hallPortalService.getUser(playerId);
			PlayerInfo p = session.player;
			PlayerSitSyn.Builder pb = PlayerSitSyn.newBuilder();
			pb.setDeskId(deskId);
			pb.setCoin(p.coin);
			pb.setScore(p.score);
			pb.setSex(user.getGender());
			pb.setNickName(user.getNickname() == null ? "" : user.getNickname());
			pb.setPlayerId(p.playerId);
			pb.setPosition(p.position);
			boolean ready = session.getStatus() == PlayerStatus.READY || session.getStatus() == PlayerStatus.GAMING;
			pb.setState(ready ? 1 : 0); // 未准备
			pb.setOnline(session.onlineStatus == OnlineStatus.ONLINE ? 1 : 0);
			pb.setAway(session.awayStatus == AwayStatus.AWAY? 1 : 0);
			pb.setHeadImg(user.getHeadImg());

			UserGameOutline outline = userDao.getUserGameOutline(playerId);
			pb.setFanka(user.getFanka());
			pb.setDiamond(user.getDiamond()==null?0:user.getDiamond());
			pb.setContinueWinCount(outline == null ? 0 : outline.getContinueWinCount());
			pb.setTotalGameCount(outline == null ? 0 : outline.getTotalCount());
			if(user.getUserType()!=0 && session.getChannel()!=null){
				SocketAddress remoteAddress = session.getChannel().remoteAddress();
				if(remoteAddress instanceof InetSocketAddress) {
					pb.setIp(((InetSocketAddress)remoteAddress).getAddress().getHostAddress());
				}
			}else {
				pb.setIp("未知");
			}
			double winRate = 0;
			if(outline != null && outline.getTotalCount() > 0) {
				winRate = (outline.getWinCount() * 1.0) / outline.getTotalCount();
			}
			pb.setWinRate(winRate);

			List<Integer> downcards = new ArrayList<Integer>();
			if(outline != null && outline.getMaxFanDowncards() != null) {
				downcards = new Gson().fromJson(outline.getMaxFanDowncards(), new TypeToken<List<Integer>>(){}.getType());
			}
			pb.addAllDowncard(downcards);


			List<Integer> handcards = new ArrayList<Integer>();
			if(outline != null && outline.getMaxFanHandcards() != null) {
				handcards = new Gson().fromJson(outline.getMaxFanHandcards(), new TypeToken<List<Integer>>(){}.getType());
			}
			pb.addAllHandcard(handcards);

			if(outline != null && outline.getMaxFanDesc() != null) {
				pb.setMaxFanType(outline.getMaxFanDesc());
			}
			logger.info("向玩家"+toPushPlayerId+"推送玩家"+playerId+"坐下消息"+JsonFormat.printToString(pb.build()));
			pushPBMsg(toPushPlayerId, PacketType.PlayerSitSyn, pb.build().toByteString());
		}
	}

	/**
	 * 推送玩家进入信息
	 */
	public void pushPlayerEnterSyn(int playerId, int toPushPlayerId,String deskId ) {
		logger.info("act=pushPlayerSitSyn;playerId=" + playerId + ";receiverId=" + toPushPlayerId);
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session != null) {
			User user = ServiceRepo.hallPortalService.getUser(playerId);
			PlayerInfo p = session.player;
			PlayerSitSyn.Builder pb = PlayerSitSyn.newBuilder();
			pb.setDeskId(deskId);
			pb.setCoin(p.coin);
			pb.setScore(p.score);
			pb.setSex(user.getGender());
			pb.setNickName(user.getNickname() == null ? "" : user.getNickname());
			pb.setPlayerId(p.playerId);
			pb.setPosition(p.position);
			boolean ready = session.getStatus() == PlayerStatus.READY || session.getStatus() == PlayerStatus.GAMING;
			pb.setState(ready ? 1 : 0); // 未准备
			pb.setOnline(session.onlineStatus == OnlineStatus.ONLINE ? 1 : 0);
			pb.setAway(session.awayStatus == AwayStatus.AWAY? 1 : 0);
			pb.setHeadImg(user.getHeadImg());

			UserGameOutline outline = userDao.getUserGameOutline(playerId);
			pb.setFanka(user.getFanka());
			pb.setDiamond(user.getDiamond()==null?0:user.getDiamond());
			pb.setContinueWinCount(outline == null ? 0 : outline.getContinueWinCount());
			pb.setTotalGameCount(outline == null ? 0 : outline.getTotalCount());
			if(user.getUserType()!=0 && session.getChannel()!=null){
				SocketAddress remoteAddress = session.getChannel().remoteAddress();
				if(remoteAddress instanceof InetSocketAddress) {
					pb.setIp(((InetSocketAddress)remoteAddress).getAddress().getHostAddress());
				}
			}else {
				pb.setIp("未知");
			}
			double winRate = 0;
			if(outline != null && outline.getTotalCount() > 0) {
				winRate = (outline.getWinCount() * 1.0) / outline.getTotalCount();
			}
			pb.setWinRate(winRate);

			List<Integer> downcards = new ArrayList<Integer>();
			if(outline != null && outline.getMaxFanDowncards() != null) {
				downcards = new Gson().fromJson(outline.getMaxFanDowncards(), new TypeToken<List<Integer>>(){}.getType());
			}
			pb.addAllDowncard(downcards);


			List<Integer> handcards = new ArrayList<Integer>();
			if(outline != null && outline.getMaxFanHandcards() != null) {
				handcards = new Gson().fromJson(outline.getMaxFanHandcards(), new TypeToken<List<Integer>>(){}.getType());
			}
			pb.addAllHandcard(handcards);

			if(outline != null && outline.getMaxFanDesc() != null) {
				pb.setMaxFanType(outline.getMaxFanDesc());
			}
			logger.info("向玩家"+toPushPlayerId+"推送玩家"+playerId+"进入消息"+JsonFormat.printToString(pb.build()));
			pushPBMsg(toPushPlayerId, PacketType.PlayerEnterSyn, pb.build().toByteString());
		}
	}

	public void pushPlayerExitSyn(int position, int leavePlayerId, int toPushPlayerId) {
		logger.error("act=pushPlayerExitSyn;position=" + position + ";receiverId=" + toPushPlayerId);
		BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
		if (session != null) {
			PlayerExitSyn.Builder pb = PlayerExitSyn.newBuilder();
			// pb.setPosition(position);
			pb.setPlayerId(leavePlayerId);
			pushPBMsg(session, PacketType.PlayerExitSyn, pb.build().toByteString());
		}
		pushVipRoomList(leavePlayerId);
		pushVipRoomList(toPushPlayerId);
	}
	
	public void pushPlayerAwaySyn(int position, int leavePlayerId, int toPushPlayerId) {
		logger.info("act=pushPlayerAwaySyn;position=" + position + ";receiverId=" + toPushPlayerId);
		BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
		if (session != null) {
			PlayerExitSyn.Builder pb = PlayerExitSyn.newBuilder();
			// pb.setPosition(position);
			pb.setPlayerId(leavePlayerId);
			pushPBMsg(session, PacketType.PlayerAwaySyn, pb.build().toByteString());
		}
		pushVipRoomList(leavePlayerId);
		pushVipRoomList(toPushPlayerId);
	}
	
	public void pushDeskDestory(int playerId, String deskId) {
		logger.info("act=pushDeskDestory;playerId={};deskId={}", playerId, deskId);
		logger.info("向玩家"+playerId+"推送房间"+deskId+"解散消息");
		DeskDestorySyn.Builder syn = DeskDestorySyn.newBuilder();
		syn.setDeskId(deskId);
		pushPBMsg(playerId, PacketType.DeskDestorySyn, syn.build().toByteString());
	}


	public void pushVipRoomList(int playerId) {
		BattleSession session = sessionManager.getIoSession(playerId);

		if(session == null) return;

		if (!session.getDeskMap().isEmpty()) {
			VipRoomListSyn.Builder vb = VipRoomListSyn.newBuilder();
			for (Map.Entry<String,CommonDesk> entry : session.getDeskMap().entrySet()) {
				MsgGame.VipRoomModel.Builder model = MsgGame.VipRoomModel.newBuilder();
				CommonDesk desk = entry.getValue();
				if(!(desk instanceof VipDesk)) continue;
				VipDesk vipDesk = (VipDesk)desk;
				if(desk.getStatus() == DeskStatus.DESTROYED) continue;
				model.setDeskId(vipDesk.getDeskID());
				model.setRoomType(vipDesk.getClubId() > 0 ? vipDesk.getClubRoomType(): -1);
				model.setJuNum(vipDesk.getTotalQuan());
				model.setCurJuNum(vipDesk.getGameCount());
				model.setPlayerCount(vipDesk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG ? vipDesk.getAllPlayers().size() : vipDesk.getPlayerCount());
				model.setWanfa(vipDesk.getWanfa());
				model.setLimitMax(vipDesk.getLimitMax());
				model.setGameId(vipDesk.getGameId());
				model.setClubId(vipDesk.getClubId());
				model.setMatchId(vipDesk.getMatchId());
				vb.addRoomList(model);
			}
			logger.info("房间列表-----"+ JsonFormat.printToString(vb.build()));
			pushVipRoomListSyn(playerId, vb);
		} else {
			logger.info("房间列表-----空");
			pushVipRoomListSyn(playerId, VipRoomListSyn.newBuilder());
		}
	}

	public void pushPlayerOfflineSyn(int position, int offerlinePlayerId, int toPushPlayerId) {
		logger.info("act=pushPlayerOfflineSyn;position=" + position + ";receiverId=" + toPushPlayerId);
		BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
		if (session != null) {
			PlayerOfflineSyn.Builder pb = PlayerOfflineSyn.newBuilder();
			// pb.setPosition(position);
			pb.setPlayerId(offerlinePlayerId);
			pushPBMsg(session, PacketType.PlayerOfflineSyn, pb.build().toByteString());
		}
	}
	
	public void pushGamePauseSyn(String deskId, Integer receiver) {
//		logger.info("act=pushGamePauseSyn;deskId={};receiverId={};", deskId, receiver);
//		BattleSession session = ServiceRepo.sessionManager.getIoSession(receiver);
//		if (session != null) {
//			pushPBMsg(session, PacketType.GamePauseSyn, null);
//		}
	}
	
	public void pushGameResumeSyn(String deskId, Integer receiver) {
//		logger.info("act=pushGameResumeSyn;deskId={};receiverId={};", deskId, receiver);
//		BattleSession session = ServiceRepo.sessionManager.getIoSession(receiver);
//		if (session != null) {
//			pushPBMsg(session, PacketType.GameResumeSyn, null);
//		}
	}

	public void pushGlobalErrorSyn(int playerId, String msg) {
		logger.info("act=pushGlobalErrorSyn;playerId=" + playerId + ";msg=" + msg);
//		GlobalErrorSyn.Builder gb = GlobalErrorSyn.newBuilder();
//		gb.setMsg(msg);
//		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
//		pushPBMsg(session, PacketType.GlobalErrorSyn, gb.build().toByteString());
		pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
	}

	public void pushPlayerComebackSyn(int position, int combackPlayerId, int toPushPlayerId) {
		logger.info("act=pushPlayerComebackSyn;position=" + position + ";receiverId=" + toPushPlayerId);
		BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
		if (session != null) {
			PlayerComebackSyn.Builder pb = PlayerComebackSyn.newBuilder();
			// pb.setPosition(position);
			pb.setPlayerId(combackPlayerId);
			pushPBMsg(session, PacketType.PlayerComebackSyn, pb.build().toByteString());
		}
	}
	
	public void pushPlayerReconnectSyn(int position, int reconnectPlayerId, int toPushPlayerId) {
		logger.info("act=pushPlayerReconnectSyn;position=" + position + ";receiverId=" + toPushPlayerId);
		BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
		if (session != null) {
			PlayerReconnectSyn.Builder pb = PlayerReconnectSyn.newBuilder();
			// pb.setPosition(position);
			pb.setPlayerId(reconnectPlayerId);
			pushPBMsg(session, PacketType.PlayerReconnectSyn, pb.build().toByteString());
		}
	}

	/**
	 * 推送玩家进入信息
	 * 
	 * @param playerId
	 * @param player
	 */
	// public void pushPlayerEnterInfo(int playerId, PlayerVO player) {
	// // sessionManager.writeTextWebSocketFrame(playerId,
	// "PB_Desk_Player_Enter_Syn", player);
	// }

	/**
	 * 推送玩家退出信息
	 * 
	 * @param playerId
	 * @param exitPlayerId
	 */
	// public void pushPlayerExitMsg(int playerId, int exitPlayerId) {
	// // PlayerId player = new PlayerId();
	// // player.playerId = exitPlayerId;
	// //
	// // sessionManager.writeTextWebSocketFrame(playerId,
	// "PB_Desk_Player_Exit_Syn", player);
	// }

	/**
	 * 推送游戏开始消息
	 * 
	 * @param playerId
	 * @param deskId
	 */
	public void pushGameStartMsg(int playerId, String deskId) {
		GameStartSyn.Builder pb = GameStartSyn.newBuilder();
		pb.setDeskId(deskId);
		pushPBMsg(playerId, PacketType.GameStartSyn, pb.build().toByteString());
	}

	public void pushGameMsgSyn(int playerId,ByteString data) {
		pushPBMsg(playerId, PacketType.GameMsgSyn, data);
	}

	/**
	 * 推送游戏结束消息
	 * 
	 * @param playerId
	 * @param deskId
	 */
	public void pushGameStopMsg(int playerId, String deskId) {
		// GameStartStopSyn startMsg = new GameStartStopSyn();
		// startMsg.deskID = deskId+"";
		// sessionManager.writeTextWebSocketFrame(playerId,
		// "PB_Desk_Game_Stop_Syn", startMsg);
	}

	/**
	 * 推送报名响应信息
	 * 
	 * @param playerId
	 * @param result
	 * @param msg
	 */
	public void pushEnrollRsp(int playerId, boolean result, String msg,String gameId) {
		logger.info("act=pushEnrollRsp;playerId=" + playerId + ";result=" + result+";msg="+msg);
		if (!result) {
			if(!StringUtils.equals("桌子已满",msg)) {
				pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
				return;
			}
		}
		EnrollResponse.Builder pb = EnrollResponse.newBuilder();
		pb.setGameId(gameId);
		pushPBMsg(playerId, PacketType.EnrollRequest, pb.build().toByteString());
	}

	/**
	 * 推送比赛场报名响应信息
	 *
	 * @param playerId
	 * @param result
	 * @param msg
	 */
	public void pushMultiMatchEnrollRsp(int playerId, boolean result, String msg,String gameId) {
		logger.info("act=pushMultiMatchEnrollRsp;playerId=" + playerId + ";result=" + result+";msg="+msg);
		if (!result) {
			pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
			return;
		}
		EnrollResponse.Builder pb = EnrollResponse.newBuilder();
		pb.setGameId(gameId);
		pushPBMsg(playerId, PacketType.MultiMatchEnrollRequest, pb.build().toByteString());
	}
	/**
	 * 推送退赛响应信息
	 */
	public void pushUnEnrollMsg(int playerId, boolean result, String msg) {
		logger.info("act=pushUnEnrollMsg;playerId=" + playerId + ";result=" + result+";msg="+msg);
		if (!result) {
			pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
			return;
		}
		MsgGame.ExitMultiMatchResponse.Builder pb = MsgGame.ExitMultiMatchResponse.newBuilder();
		pushPBMsg(playerId, PacketType.ExitMultiMatchRequest, pb.build().toByteString());
	}

	public void pushPlayerGamingInfo(int playerId, String gameId, String matchId,
									 String roomId, String deskId, int wanfa,
									 int roomType, int totalQuan ,int limitMax,
									 int yaZhu,int menNum,int ownerId,long clubId,
									 int clubRoomType, int clubRoomEnterScore,
									 int clubRoomChouShuiScore,int clubRoomCanFuFen,
									 int clubRoomChouShuiNum,int clubRoomZengSongNum,
									 int qiangZhuangNum,String niuFanStr,int erBaGameType) {
		logger.info("act=pushPlayerGamingInfo;playerId={};gameId={};matchId={};roomId={};deskId={};", playerId, gameId, matchId, roomId, deskId);
		PlayerGamingSyn.Builder pb = PlayerGamingSyn.newBuilder();
		pb.setDeskId(deskId);
		pb.setRoomId(roomId);
		pb.setMatchId(matchId);
		pb.setGameId(gameId);
		pb.setWanfa(wanfa);
		pb.setRoomType(roomType);
		pb.setTotalQuan(totalQuan);
		pb.setLimitMax(limitMax);
		pb.setYaZhu(yaZhu);
		pb.setMenNum(menNum);
		pb.setOwnerId(ownerId);
		pb.setClubId(clubId);
		pb.setClubRoomType(clubRoomType);
		pb.setClubRoomEnterScore(clubRoomEnterScore);
		pb.setClubRoomChouShuiScore(clubRoomChouShuiScore);
		pb.setClubRoomCanFuFen(clubRoomCanFuFen);
		pb.setClubRoomChouShuiNum(clubRoomChouShuiNum);
		pb.setClubRoomZengSongNum(clubRoomZengSongNum);
		pb.setQiangZhuangNum(qiangZhuangNum);
		pb.setNiuFanStr(niuFanStr);
		pb.setErBaGameType(erBaGameType);
		pushPBMsg(playerId, PacketType.PlayerGamingSyn, pb.build().toByteString());
	}

	public void pushErrorMsg(BattleSession session, PacketType type, String msg) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(-1);
		pb.setPacketType(type);
		pb.setMsg(msg);
		sessionManager.write(session, pb.build().toByteArray());
	}

	public void pushErrorMsg(int playerId, PacketType type, String msg) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(-1);
		pb.setPacketType(type);
		pb.setMsg(msg);
		sessionManager.write(playerId, pb.build());
	}

	public void pushPBMsg(BattleSession session, PacketType type, ByteString data) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(0);
		pb.setPacketType(type);
		if (data != null) {
			pb.setData(data);
		}
		if(type!=PacketType.HEARTBEAT){
//			logger.info("stype={}",type);
		}
		sessionManager.write(session, pb.build().toByteArray());
	}

	public void pushPBMsg(int playerId, PacketType type, ByteString data) {
		BattleSession session = sessionManager.getIoSession(playerId);
		if (session != null) {
			pushPBMsg(session, type, data);
		}
	}

	public void pushLogoutSyn(int playerId, String msg) {
		LogoutSyn.Builder syn = LogoutSyn.newBuilder();
		syn.setReason(msg);
		pushPBMsg(playerId, PacketType.LogoutSyn, syn.build().toByteString());
	}

	/**
	 * 推送登录响应消息
	 * 
	 * @param session
	 * @param result
	 * @param msg
	 */
	public void pushLoginRsp(BattleSession session, boolean result, PlayerInfo player, String msg) {
		logger.info(String.format("act=pushLoginRsp;playerId={};result={}", session.userId, result));
		if (result) {
			AuthResponse.Builder lb = AuthResponse.newBuilder();
			pushMsg(session, lb.build().toByteString(), player.playerId,PacketType.AuthRequest);
			return;
		}
		pushMsgError(session,msg, player.playerId,PacketType.AuthRequest);
		return;
	}

	/**
	 * 推送准备响应消息
	 * 
	 * @param playerId
	 * @param result
	 * @param msg
	 */
	public void pushReadyRsp(int playerId, boolean result, String msg) {
		// if(result) {
		// pushPBMsg(playerId, PacketType.READY, null);
		// return;
		// }
		// pushErrorMsg(playerId, PacketType.READY, msg);
	}

	/**
	 * 推送玩家已准消息
	 * 
	 * @param playerId
	 * @param readyPosition
	 * @param readyPlayerId
	 */
	public void pushReadySyn(int playerId, int readyPosition, int readyPlayerId) {
		logger.info("act=pushReadySyn;playerId=" + playerId + ";readyPosition=" + readyPosition);
		ReadySyn.Builder pb = ReadySyn.newBuilder();
		// pb.setPosition(readyPosition);
		pb.setPlayerId(readyPlayerId);
		pb.setState(1); // 已准备
		pushPBMsg(playerId, PacketType.ReadySyn, pb.build().toByteString());
	}

	/**
	 * 推送换桌响应信息
	 * 
	 * @param playerId
	 * @param result
	 * @param msg
	 */
	public void pushChangeDeskRsp(int playerId, boolean result, String msg) {
		if (result) {
			pushPBMsg(playerId, PacketType.ChangeDeskRequest, null);
			return;
		}
		pushErrorMsg(playerId, PacketType.ChangeDeskRequest, msg);
	}

	public void pushCreateVipRoomRsp(int playerId, boolean ok, String msg) {
		if (ok) {
//			pushPBMsg(playerId, PacketType.GlobalErrorSyn, null);
			return;
		}
		pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
	}

	public void pushBack2HallRsp(int playerId) {
		// sessionManager.writeTextWebSocketFrame(playerId,
		// "PB_Back_To_Hall_Response", rsp);
	}

	public void pushUserAttrChange(int playerId, PlayerInfoSyn syn) {
		// sessionManager.writeTextWebSocketFrame(playerId,
		// "PB_Player_Info_Update", syn);
	}

	public void pushDeskPlayerKickoutRsp(int playerId, boolean result, String msg) {
		if (result) {
//			pushPBMsg(playerId, PacketType.KickPlayerRequest, null);
			return;
		}
		pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
	}
	
	public void pushDismissVipRoomResponse(int playerId, boolean result, String msg) {
		if (result) {
			pushPBMsg(playerId, PacketType.DismissVipRoomRequest, null);
			return;
		}
		pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
	}
	
	public void pushHangupSyn(int playerId, int position, int status) {
		logger.info("act=pushHangupSyn;playerId={};position={};status={};", playerId, position, status);
		HangupSyn.Builder gb = HangupSyn.newBuilder();
		gb.setPosition(position);
		gb.setStatus(status);
		pushPBMsg(playerId, PacketType.HangupSyn, gb.build().toByteString());
	}
	
	public void pushVipRoomListSyn(int playerId, VipRoomListSyn.Builder vb) {
		pushPBMsg(playerId, PacketType.VipRoomListSyn, vb.build().toByteString());
	}
		
	public void pushChatMsg(PlayerInfo p, String deskId, List<Integer> receiverIds, int contentType, byte[] conetnt) {
		GameChatMsgSyn.Builder gb = GameChatMsgSyn.newBuilder();
		gb.setData(ByteString.copyFrom(conetnt));
		gb.setContentType(contentType);
		gb.setDeskId(deskId);
		gb.setPosition(p.position);
		for(int receiver : receiverIds) {
			if(receiver == p.playerId && contentType == 3) continue; //语音不发自己
			BattleSession session = sessionManager.getIoSession(receiver);
			if(session != null) {
				pushPBMsg(session, PacketType.GameChatMsgSyn, gb.build().toByteString());
			}
		}
	}

	public void sendMultiMatchPlayerNum(int type, int size) {
		for(Object userId : sessionManager.getOnlinePlayerIdList()) {
			Integer uid = (Integer)userId;
			BattleSession session = sessionManager.getIoSession(uid);
			if(session.getStatus()==PlayerStatus.GAMING || StringUtils.isNotBlank(session.user.getBindedMatch())) continue;
			MsgGame.MultiMatchPlayerNum.Builder syn = MsgGame.MultiMatchPlayerNum.newBuilder();
			syn.setMatchid(type);
			syn.setPlayerNum(size);
//			logger.info("MultiMatchPlayerNum {}, data: {} " , userId, JsonFormat.printToString(syn.build()));
			PacketBase.Builder pb = PacketBase.newBuilder();
			pb.setCode(0);
			pb.setPacketType(PacketType.MultiMatchPlayerNum);
			pb.setData(syn.build().toByteString());
			sessionManager.write(uid, pb.build());
		}
	}

	public void multiMatchEnrollSynWithLogin(BattleSession session,String gameId,String matchId,int type) {
		MsgGame.MultiMatchEnrollSynWithLogin.Builder pb = MsgGame.MultiMatchEnrollSynWithLogin.newBuilder();
		pb.setGameId(gameId);
		pb.setMatchId(matchId);
		pb.setType(type);
		logger.info("MultiMatchEnrollSynWithLogin {}, data: {} " , session.userId, JsonFormat.printToString(pb.build()));
		PacketBase.Builder sb = PacketBase.newBuilder();
		sb.setCode(0);
		sb.setPacketType(PacketType.MultiMatchEnrollSynWithLogin);
		sb.setData(pb.build().toByteString());
		sessionManager.write(session,sb.build().toByteArray());
	}

	//	public void pushDismissVote(int playerId, int position, List<MsgGame.DissmissStatus.Builder> agree, int applyOpsition) {
//		DissmissVoteSyn.Builder sb = DissmissVoteSyn.newBuilder();
//		sb.setApplyOpsition(applyOpsition);
//		pushPBMsg(playerId, PacketType.DissmissVoteSyn, sb.build().toByteString());
//	}

	public void pushDismissVote(int timeLeft, int playerId, List<MsgGame.DissmissStatus> pbList, int applyDissMissPosition) {
		MsgGame.DissmissVoteResponse.Builder sb = MsgGame.DissmissVoteResponse.newBuilder();
		sb.setApplyOpsition(applyDissMissPosition);
		sb.setLeftTimeMillis(timeLeft);
		sb.addAllDissmissStatus(pbList);
//		logger.info("----------------解散消息"+JsonFormat.printToString(sb.build()));
		pushPBMsg(playerId, PacketType.DissmissVoteResponse, sb.build().toByteString());
	}
	
		public void pushMsg(BattleSession session,ByteString data, int playerId,PacketType type) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(0);
		pb.setPacketType(type);
		if(data != null) {
			pb.setData(data);
		}

		Message msg = new Message(pb.build().toByteArray());
		sessionManager.write(session, msg);
	}

	public void pushMsgError(BattleSession session,String s, int playerId,PacketType type) {
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(-1);
		pb.setPacketType(type);
		pb.setMsg(s);

		Message msg = new Message(pb.build().toByteArray());
		sessionManager.write(session, msg);
	}

	public void pushPlayerExitPosNotExitRoomMsg(String deskid, int deskPos, int playerId, int toPushPlayerId) {
		MsgGame.PlayerExitPosNotExitRoomResponse.Builder builder = MsgGame.PlayerExitPosNotExitRoomResponse.newBuilder();
		builder.setDeskId(deskid);
		builder.setDeskPos(deskPos);
		builder.setPlayerId(playerId);
		logger.info("向玩家(id)"+toPushPlayerId+"推送玩家(ID:位置)--("+playerId+":"+deskPos+")--离座消息");
		pushPBMsg(toPushPlayerId, PacketType.PlayerExitPosNotExitRoomResponse, builder.build().toByteString());
	}

    public void pushVipRoomPlayerMsg(CommonDesk desk,BattleSession toPushPlayeSession) {
        MsgGame.VipRoomPlayerResponse.Builder builder = MsgGame.VipRoomPlayerResponse.newBuilder();
        builder.setDeskId(desk.getDeskID());
        builder.setCurJuNum(desk.getGameCount()+"");
        builder.setJuNum(desk.getTotalQuan());
        builder.setWanfa(desk.getWanfa());
        builder.setGameId(desk.getGameId());
        List<PlayerInfo> plist = desk.getPlayers();
        for(PlayerInfo p : plist){
            MsgGame.DeskPalyer.Builder b = MsgGame.DeskPalyer.newBuilder();
            b.setPlayerId(p.playerId);
            b.setNickName(p.name);
            b.setPlayerScore(p.score);
            b.setImgUrl(p.headImg);
            builder.addPlayerList(b);
        }
        pushPBMsg(toPushPlayeSession,PacketType.VipRoomPlayerResponse,builder.build().toByteString());
    }

	public void pushGuanZhanPlayers(int playerId, String deskID, List<PlayerInfo> list) {
		MsgGame.ViewGuanZhanResp.Builder bu = MsgGame.ViewGuanZhanResp.newBuilder();
		bu.setDeskId(deskID);
		if(list != null && !list.isEmpty()) {
			for(PlayerInfo p : list){
				MsgGame.GuanZhanPlayer.Builder de = MsgGame.GuanZhanPlayer.newBuilder();
				de.setPlayerId(p.playerId);
				de.setPlayerImg(p.headImg);
				de.setPlayerName(p.name);
				bu.addPlayerList(de);
			}
		}
		logger.info(JsonFormat.printToString(bu.build()));
		pushPBMsg(playerId,PacketType.ViewGuanZhanResp,bu.build().toByteString());
	}
}
