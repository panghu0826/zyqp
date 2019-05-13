package com.buding.test;

import java.io.File;

import com.buding.api.game.MJWanfa;
import net.sf.json.JSONObject;
import packet.game.Hall.ServerChangeSyn;
import packet.game.MsgGame.CreateVipRoomRequest;
import packet.game.MsgGame.DismissVipRoomRequest;
import packet.game.MsgGame.DissmissVoteSyn;
import packet.game.MsgGame.GameChatMsgRequest;
import packet.game.MsgGame.GameChatMsgSyn;
import packet.game.MsgGame.HangupSyn;
import packet.game.MsgGame.KickPlayerRequest;
import packet.game.MsgGame.PlayerExitSyn;
import packet.game.MsgGame.PlayerGamingSyn;
import packet.game.MsgGame.PlayerOfflineSyn;
import packet.game.MsgGame.PlayerSitSyn;
import packet.game.MsgGame.ReadySyn;
import packet.game.MsgGame.VipRoomListSyn;
import packet.mj.MJ.GameOperActorSyn;
import packet.mj.MJ.GameOperFinalSettleSyn;
import packet.mj.MJ.GameOperHandCardSyn;
import packet.mj.MJ.GameOperPlayerActionNotify;
import packet.mj.MJ.GameOperPlayerActionSyn;
import packet.mj.MJ.GameOperPlayerHuSyn;
import packet.mj.MJ.GameOperPlayerSettle;
import packet.mj.MJ.GameOperPublicInfoSyn;
import packet.mj.MJ.GameOperStartSyn;
import packet.mj.MJ.PlayerFinalResult;
import packet.mj.MJBase.GameOperType;
import packet.mj.MJBase.GameOperation;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.util.IOUtil;
import com.buding.game.Action;
import com.buding.game.GameRecorder;
import com.buding.mj.constants.MJConstants;
import com.buding.mj.helper.MJHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class GameServerProxy extends BaseListener {
	MJHelpler mj = new MJHelpler();
	public GameRecorder record;
	public int replay2 = 0;
	GameOperPlayerActionNotify curSyn = null;
	public boolean autoMock = false;
	boolean gameEnd = false;

	public GameServerProxy(Player player, PlayerData data) {
		super(player, data);
	}

	public void initGamingData() {
		if (data.gamingData == null) {
			data.gamingData = new GamingData();
			mj.data = data.gamingData;
		}
	}

	public void msgRead(byte[] msg) {
		synchronized (GameServerProxy.class) {
			processMsg(msg);
		}

	}

	private void processMsg(byte[] msg) {
		try {
			initGamingData();
			PacketBase p = PacketBase.parseFrom(msg);
			if (p.getPacketType() == PacketType.HEARTBEAT) {
				return;
			}
			logger.debug("=================================");
			logger.debug("玩家({})收到网络包:{}", data.gamingData.nickName, p.getPacketType().toString());

			int code = p.getCode();
			PacketType packetType = p.getPacketType();
			if (code != 0) {
				logger.error("packet error! type=" + packetType.toString() + ";msg=" + p.getMsg());
				if (packetType == PacketType.AuthRequest) {
//					data.loginData = null; // 重置登录
				}
				return;
			}

			switch (packetType) {
			case ServerChangeSyn: {
				logger.debug("服务器已变更");
				ServerChangeSyn syn = ServerChangeSyn.parseFrom(p.getData());
				if (syn.getGameServerAddr() != null) {
					data.gameServerAddr = syn.getGameServerAddr();
//					data.gameAuthData = null;
					data.authGame = false;
					player.getGameServerProxy().channel.close();
					player.getGameServerProxy().channel = null;
				}
				if (syn.getMsgServerAddr() != null) {
					data.msgServerAddr = syn.getMsgServerAddr();
				}
				if (syn.getHallServerAddr() != null) {
					data.hallServerAddr = syn.getHallServerAddr();
				}
			}
			case GameChatMsgSyn: {
				GameChatMsgSyn syn = GameChatMsgSyn.parseFrom(p.getData());
				logger.debug("聊天数据:");
				logger.debug(JsonFormat.printToString(syn));
			}
				break;
			case AuthRequest:
				logger.debug("认证成功");
//				data.gameAuthData = AuthResponse.parseFrom(p.getData());
				data.authGame = true;
				break;
			case VipRoomListSyn: {
				VipRoomListSyn syn = VipRoomListSyn.parseFrom(p.getData());
				logger.debug("房间列表:");
				logger.debug(JsonFormat.printToString(syn));
			}
				break;
			case EnrollRequest:
				initGamingData();
				data.gamingData.enroll = true;
				logger.debug("报名成功");
				break;
			case PlayerExitSyn: {
				PlayerExitSyn syn = PlayerExitSyn.parseFrom(p.getData());
				logger.debug("玩家{}收到玩家{}退出数据包", mj.getMyLable(), mj.getById(syn.getPlayerId()).getNickName());
			}
				break;
			case PlayerAwaySyn: {
				PlayerExitSyn syn = PlayerExitSyn.parseFrom(p.getData());
				logger.debug("玩家{}收到玩家{}离开数据包", mj.getMyLable(), mj.getById(syn.getPlayerId()).getNickName());
			}
				break;
			case PlayerOfflineSyn: {
				PlayerOfflineSyn syn = PlayerOfflineSyn.parseFrom(p.getData());
				logger.debug("玩家[{}]收到玩家{}掉线消息", mj.getMyLable(), mj.getById(syn.getPlayerId()).getNickName());
			}
				break;
			case PlayerComebackSyn: {
				PlayerOfflineSyn syn = PlayerOfflineSyn.parseFrom(p.getData());
				logger.debug("玩家[{}]收到玩家{}重连消息", mj.getMyLable(), mj.getById(syn.getPlayerId()).getNickName());
			}
				break;
			case HangupSyn: {
				HangupSyn syn = HangupSyn.parseFrom(p.getData());
				logger.debug("玩家切换托管状态:" + JsonFormat.printToString(syn));
				break;
			}
			case DissmissVoteSyn: {
				DissmissVoteSyn syn = DissmissVoteSyn.parseFrom(p.getData());
				logger.debug("申请解散:" + JsonFormat.printToString(syn));
				break;
			}
			case PlayerSitSyn: {
				initGamingData();
				PlayerSitSyn syn = PlayerSitSyn.parseFrom(p.getData());
				mj.onPlayerSit(syn);
				if (syn.getPlayerId() == data.loginData.getUserId()) {
					data.gamingData.position = syn.getPosition();
					data.gamingData.nickName = syn.getNickName();
//					data.gamingData.playerId = syn.getPlayerId();
					logger.debug("我已坐下: 玩家={};位置={};准备态={}", mj.getById(syn.getPlayerId()).getNickName(), data.gamingData.position, syn.getState());
				} else {
					logger.debug("玩家坐下: 玩家id={};位置={};准备态={}", mj.getById(syn.getPlayerId()).getNickName(), syn.getPosition(), syn.getState());
				}
			}
				break;
			case ReadySyn: {
				initGamingData();
				ReadySyn syn = ReadySyn.parseFrom(p.getData());
				logger.debug("玩家{}已准备:{}", mj.getById(syn.getPlayerId()).getNickName(), syn.getState());
			}
				break;
			case KickOutSyn: {
				gameEnd = true;
				logger.debug("你已被踢出桌子");
			}
				break;
			case PlayerGamingSyn: {
				PlayerGamingSyn syn = PlayerGamingSyn.parseFrom(p.getData());
				logger.debug(packetType.toString());
				logger.debug(JsonFormat.printToString(syn));
				break;
			}
			case GameStartSyn:
				initGamingData();
				logger.info("游戏开始");
				break;
			case GameOperation:
				initGamingData();
				logger.debug("收到游戏数据包");
				GameOperation oper = GameOperation.parseFrom(p.getData());

				GameOperType type = oper.getOperType();
				switch (type) {
				case GameOperActorSyn: {
					GameOperActorSyn syn = GameOperActorSyn.parseFrom(oper.getContent());
					boolean isMe = (syn.getPosition() == data.gamingData.position);
					logger.debug("同步当前正在操作的玩家:我叫{},当前操作玩家={};时间剩余={};我？{}", mj.getMyLable(), mj.getByPos(syn.getPosition()).getNickName(), syn.getTimeLeft(), isMe);
				}
					break;
				case GameOperFinalSettleSyn: {
					GameOperFinalSettleSyn syn = GameOperFinalSettleSyn.parseFrom(oper.getContent());
					logger.debug("最终结算");
					for (PlayerFinalResult res : syn.getDetailList()) {
						logger.debug(JsonFormat.printToString(res));
					}
					break;
				}
				case GameOperStartSyn: {
					GameOperStartSyn syn = GameOperStartSyn.parseFrom(oper.getContent());
					if (syn.getReconnect()) {
						logger.debug("断线重连");
						String json = JsonFormat.printToString(syn);
						JSONObject obj = JSONObject.fromObject(json);
						logger.debug(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
					} else {
						logger.debug("游戏开始:我叫{}, 庄家={};台费={};圈数={};连庄？={};", mj.getMyLable(), mj.getByPos(syn.getBankerPos()).getNickName(), syn.getServiceGold(), syn.getQuanNum(),
								syn.getBankerContinue());
						for (GameOperHandCardSyn playerHandCard : syn.getPlayerHandCardsList()) {
							boolean isMe = playerHandCard.getPosition() == data.gamingData.position;
							if (isMe) {
								logger.debug("我({})的手牌:", mj.getMyLable());
								logger.debug(MJHelper.getCompositeCardListName(playerHandCard.getHandCardsList()));
								mj.processHandcardSyn(playerHandCard);
							} else {
								logger.debug("玩家({})手牌:", mj.getByPos(playerHandCard.getPosition()).getNickName());
								logger.debug(MJHelper.getCompositeCardListName(playerHandCard.getHandCardsList()));
							}
						}
					}
					// loadActs("/home/game/data/replay.json", 66);
				}
					break;
				case GameOperPublicInfoSyn: {
					GameOperPublicInfoSyn syn = GameOperPublicInfoSyn.parseFrom(oper.getContent());
					// logger.debug("PublicInfo,剩余牌数:{};宝牌:{}",
					// syn.getCardLeft(),
					// MJHelper.getSingleCardName(syn.getBaoCard()));
					logger.debug("publicInfo:" + JsonFormat.printToString(syn));
				}
					break;
				case GameOperPlayerActionSyn: {
					GameOperPlayerActionSyn syn = GameOperPlayerActionSyn.parseFrom(oper.getContent());
					logger.debug("ActionSyn:我={}, 操作玩家={};操作码={};牌值={};", mj.getMyLable(), mj.getByPos(syn.getPosition()).getNickName(), MJHelper.getActionName(syn.getAction()),
							MJHelper.getCompositeCardCodeNames(syn.getCardValueList()));
					mj.porcessActionSyn(syn);
				}
					break;
				case GameOperPlayerActionNotify: {
					GameOperPlayerActionNotify syn = GameOperPlayerActionNotify.parseFrom(oper.getContent());
					logger.debug("ActionNotify:我={};提醒玩家={};可选操作={};刚摸到的牌={};待吃牌={};待碰牌={};出牌表={};支对表={};seq={}", mj.getMyLable(), mj.getByPos(syn.getPosition()).getNickName(),
							MJHelper.getActionName(syn.getActions()), syn.getNewCard(), MJHelper.getChiComboStr(syn.getChiArgList()), syn.getPengArg(),
							MJHelper.getCompositeCardCodeNames(syn.getTingListList()), MJHelper.getCompositeCardCodeNames(syn.getTingDzsList()), syn.getSeq());
					if (syn.getNewCard() > 0) {
						MJHelper.add2SortedList(syn.getNewCard(), data.gamingData.handCards);
					}
					logger.debug("请操作.........................");
					dumpCard();
					curSyn = syn;

					if (record != null && autoMock) {
						tryAutoReact(syn.getSeq());
					}
				}
					break;
				case GameOperHandCardSyn: {
					logger.debug("同步手牌");
					GameOperHandCardSyn syn = GameOperHandCardSyn.parseFrom(oper.getContent());
					mj.processHandcardSyn(syn);
					count();
					// logger.debug(mj.getCardCodeNames(data.gamingData.handCards));
				}
					break;
				case GameOperPlayerHuSyn: {
					GameOperPlayerHuSyn syn = GameOperPlayerHuSyn.parseFrom(oper.getContent());
					logger.debug("玩家胡牌,位置={},胡牌={};宝牌={}", syn.getPosition(), MJHelper.getSingleCardName(syn.getCard()), MJHelper.getSingleCardName(syn.getBao()));
					for (GameOperPlayerSettle detail : syn.getDetailList()) {
						logger.debug("玩家({}),手牌:{};番型:{};番数:{}", mj.getByPos(syn.getPosition()).getNickName(), MJHelper.getCompositeCardListName(detail.getHandcardList()),
								MJHelper.getFanDesc(detail.getFanType()), detail.getFanNum());
					}
					logger.debug(JsonFormat.printToString(syn));
				}
					break;
				default:
					logger.error("invalid packet, type=" + oper.getOperType().toString());
					break;
				}
				break;

			default:
				logger.error("invalid packet, type=" + packetType.toString());
				break;
			}

		} catch (InvalidProtocolBufferException e) {
			logger.error("", e);
		}
	}

	private void tryAutoReact(int seq) {
		if (seq >= replay2) {
			logger.debug("自动操作暂停;当前序号{}>设定序号{}", seq, replay2);
			return;
		}
		seq++;
		logger.debug("自动出牌中,position={};seq={};replay2={}", data.gamingData.position, seq, replay2);
		Action act = record.getReplyAct(seq, data.gamingData.position);
		if (act != null) {
			logger.debug("找到动作开始模拟:{}", new Gson().toJson(act));
			react(act);
			tryAutoReact(seq);
		} else {
			logger.debug("操作暂停,动作无法找到:position={};seq={}", data.gamingData.position, seq);
		}
	}

	private void react(Action act) {
		switch (act.code) {
		case MJConstants.MAHJONG_OPERTAION_CHI:
			logger.debug("自动吃:" + act.card1 + " " + act.card2);
			chi(act.card1, act.card2);
			break;
		case MJConstants.MAHJONG_OPERTAION_CHI_TING:
			logger.debug("自动吃听:" + act.card1 + " " + act.card2);
			chi_ting(act.card1, act.card2);
			break;
		case MJConstants.MAHJONG_OPERTAION_CHU:
			logger.debug("自动出:" + act.card1);
			chu(act.card1, (act.code & MJConstants.MAHJONG_OPERTAION_TING) == MJConstants.MAHJONG_OPERTAION_TING);
			break;
		case MJConstants.MAHJONG_OPERTAION_PENG:
			logger.debug("自动碰:" + act.card1);
			peng(act.card1);
			break;
		case MJConstants.MAHJONG_OPERTAION_PENG_TING:
			logger.debug("自动碰听:" + act.card1);
			peng_ting(act.card1);
			break;
		case MJConstants.MAHJONG_OPERTAION_CANCEL:
			logger.debug("自动取消:" + act.card1);
			cancel();
			break;
		case MJConstants.MAHJONG_OPERTAION_TING:
			logger.debug("自动听:");
			ting();
			break;
		case MJConstants.MAHJONG_OPERTAION_ZD_TING:
			logger.debug("自动支对:");
			zd(act.card1);
			break;
		default:
			logger.debug("无法识别的操作码:0X" + Integer.toHexString(act.code));
			break;

		}
	}

	public void viewHandCard(GameOperCmd cmd) {
		String str = mj.getHandCardStr(cmd.hcard);
		logger.debug("手牌:");
		logger.debug(str);
	}

	public void count() {
		logger.debug("手牌:" + data.gamingData.handCards.size());
		logger.debug("吃碰杠牌:" + data.gamingData.downCards.size());
	}

	public void dumpCard() {
		logger.debug("手牌" + MJHelper.getCompositeCardCodeNames(data.gamingData.handCards));
		logger.debug("吃碰杠牌:" + MJHelper.getCompositeCardCodeNames(data.gamingData.downCards));
		logger.debug("手牌数：" + data.gamingData.handCards.size());
		logger.debug("吃碰数：" + data.gamingData.downCards.size() * 3);
	}

	public void dumpgame() {
		sendPacket(PacketType.Dump, null);
	}

	public void viewDownCard(GameOperCmd cmd) {
		String str = mj.getDownCardStr(cmd.hcard);
		logger.debug("手牌:");
		logger.debug(str);
	}

	public void gang(GameOperCmd cmd) {
//		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
//		syn.setAction(MJConstants.MAHJONG_OPERTAION_MING_GANG);
//		syn.setPosition(data.gamingData.position);
//		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	public void sendGameOperation(GameOperType type, ByteString data) {
		GameOperation.Builder pb = GameOperation.newBuilder();
		pb.setContent(data);
		pb.setOperType(type);
		sendGamePacket(pb);
	}

	public void sendGamePacket(GameOperation.Builder gb) {
		sendPacket(PacketType.GameOperation, gb.build().toByteString());
	}

	public void back2hall(GameOperCmd cmd) {
		logger.debug("返回大厅");
		sendPacket(PacketType.Back2HallRequest, null);
	}

	public void cancel(GameOperCmd cmd) {
		logger.debug("客户端取消候选操作");
		cancel();
	}

	private void cancel() {
		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
		syn.setAction(MJConstants.MAHJONG_OPERTAION_CANCEL);
		syn.setPosition(data.gamingData.position);
		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	public void chi(GameOperCmd cmd) {
		logger.debug("客户端吃牌");
		int card = cmd.chi;
		int card2 = cmd.card2;
		if (cmd.ting) {
			chi_ting(card, card2);
		} else {
			chi(card, card2);
		}
	}

	private void chi(int card, int card2) {
		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
		syn.setAction(MJConstants.MAHJONG_OPERTAION_CHI);
		syn.setPosition(data.gamingData.position);
		syn.addCardValue(card);
		syn.addCardValue(card2);
		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	private void chi_ting(int card, int card2) {
		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
		syn.setAction(MJConstants.MAHJONG_OPERTAION_CHI_TING);
		syn.setPosition(data.gamingData.position);
		syn.addCardValue(card);
		syn.addCardValue(card2);
		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	public void peng(GameOperCmd cmd) {
		logger.debug("客户端碰");
		int card = cmd.peng;
		peng(card);
	}

	private void peng(int card) {
		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
		syn.setAction(MJConstants.MAHJONG_OPERTAION_PENG);
		syn.setPosition(data.gamingData.position);
		syn.addCardValue(card);
		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	private void peng_ting(int card) {
		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
		syn.setAction(MJConstants.MAHJONG_OPERTAION_PENG_TING);
		syn.setPosition(data.gamingData.position);
		syn.addCardValue(card);
		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	public void hu(GameOperCmd cmd) {
		logger.debug("客户端胡牌");
		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
		syn.setAction(MJConstants.MAHJONG_OPERTAION_HU);
		syn.setPosition(data.gamingData.position);
		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	public void ting(GameOperCmd cmd) {
		logger.debug("客户端听牌");
		ting();
	}

	public void enroll(String match, String roomCode) {
		player.enroll(match, roomCode);
	}

	public void createVipRoom(int quan, int roomType, int type) {
		CreateVipRoomRequest.Builder cb = CreateVipRoomRequest.newBuilder();
		cb.setQuanNum(quan);
		cb.setVipRoomType(roomType);
		cb.setWangfa(type == 0 ? MJWanfa.ALL - MJWanfa.WUJIA_BUHU : type);
		System.out.println(JsonFormat.printToString(cb.build()));
		sendPacket(PacketType.CreateVipRoomRequest, cb.build().toByteString());
	}

	public void kick(String deskId, int playerId) {
		KickPlayerRequest.Builder cb = KickPlayerRequest.newBuilder();
		cb.setCode(deskId);
		cb.setPlayerId(playerId);
		sendPacket(PacketType.KickPlayerRequest, cb.build().toByteString());
	}

	public void awayGame() {
		sendPacket(PacketType.AwayGameRequest, null);
	}
	
	public void exitGame() {
		sendPacket(PacketType.ExitGameRequest, null);
	}

	public void dismiss(String deskId) {
		DismissVipRoomRequest.Builder cb = DismissVipRoomRequest.newBuilder();
		cb.setCode(deskId);
		sendPacket(PacketType.DismissVipRoomRequest, cb.build().toByteString());
	}

	public void viplist() {
		sendPacket(PacketType.VipRoomListReuqest, null);
	}

	public void ready() {
		player.ready();
	}

	private void ting() {
		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
		syn.setAction(MJConstants.MAHJONG_OPERTAION_TING);
		syn.setPosition(data.gamingData.position);
		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	public void chu(GameOperCmd cmd) {
		int card = cmd.chu;
		logger.debug("客户端出牌{}", card);
		chu(card, cmd.ting);
	}

	public void zd(GameOperCmd cmd) {
		int card = cmd.zd;
		logger.debug("客户端支对{}", card);
		zd(card);
	}

	private void zd(int card) {
		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
		syn.setAction(MJConstants.MAHJONG_OPERTAION_ZD_TING);
		syn.setPosition(data.gamingData.position);
		syn.addCardValue(card);
		// syn.addCardValue(card);
		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	private void chu(int card, boolean ting) {
		GameOperPlayerActionSyn.Builder syn = GameOperPlayerActionSyn.newBuilder();
		syn.setAction(MJConstants.MAHJONG_OPERTAION_CHU);
		if (ting) {
			syn.setAction(syn.getAction() | MJConstants.MAHJONG_OPERTAION_TING);
		}
		syn.setPosition(data.gamingData.position);
		syn.addCardValue(card);
		sendGameOperation(GameOperType.GameOperPlayerActionSyn, syn.build().toByteString());
	}

	public void loadActs(String path, int r, boolean go) {
		try {
			if (record == null) {
				if (new File(path).exists() == false) {
					System.out.println(path + " 不存在");
					return;
				}

				byte data[] = IOUtil.getFileData(path);
				String detail = new String(data, "utf8");
				record = new Gson().fromJson(detail, GameRecorder.class);
			}

			this.replay2 = r;
			System.out.println("已设置自动出牌:" + replay2);
			if (curSyn != null && go) {
				tryAutoReact(curSyn.getSeq());
				curSyn = null;
				autoMock = true;
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void chat(String msg) throws Exception {
		GameChatMsgRequest.Builder gb = GameChatMsgRequest.newBuilder();
		gb.setContent(ByteString.copyFrom(msg.getBytes("utf8")));
		sendPacket(PacketType.GameChatMsgRequest, gb.build().toByteString());
	}

	@Override
	public void tick() {
		// if (record == null) {
		// try {
		// String path = "/home/game/data/replay.json";
		// if (new File(path).exists() == false) {
		// System.out.println(path + " 不存在");
		// return;
		// }
		// replay2 = 1000;
		// byte data[] = IOUtil.getFileData(path);
		// String detail = new String(data, "utf8");
		// record = new Gson().fromJson(detail, GameRecorder.class);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		//
		// if (record != null && curSyn != null) {
		// tryAutoReact(curSyn.getSeq());
		// curSyn = null;
		// }
		// if(gameEnd) {
		// player.enroll(null, null);
		// logger.debug("重新报名游戏");
		// gameEnd = false;
		// }
		if (curSyn != null) {
			int oper = curSyn.getActions();
			boolean pengTing = (oper & MJConstants.MAHJONG_OPERTAION_PENG_TING) == MJConstants.MAHJONG_OPERTAION_PENG_TING;
			boolean chiTing = (oper & MJConstants.MAHJONG_OPERTAION_CHI_TING) == MJConstants.MAHJONG_OPERTAION_CHI_TING;
			boolean ting = (oper & MJConstants.MAHJONG_OPERTAION_TING) == MJConstants.MAHJONG_OPERTAION_TING;
			boolean peng = (oper & MJConstants.MAHJONG_OPERTAION_PENG) == MJConstants.MAHJONG_OPERTAION_PENG;
			boolean chi = (oper & MJConstants.MAHJONG_OPERTAION_CHI) == MJConstants.MAHJONG_OPERTAION_CHI;
			boolean zhiDui = (oper & MJConstants.MAHJONG_OPERTAION_ZD_TING) == MJConstants.MAHJONG_OPERTAION_ZD_TING;
			boolean chu = (oper & MJConstants.MAHJONG_OPERTAION_CHU) == MJConstants.MAHJONG_OPERTAION_CHU;

			if (pengTing) {
				peng_ting(curSyn.getPengArg());
			} else if (chiTing) {
				chi_ting(curSyn.getChiArgList().get(0).getMyCard1(), curSyn.getChiArgList().get(0).getMyCard2());
			} else if (peng) {
				peng(curSyn.getPengArg());
			} else if (chi) {
				int card1 = curSyn.getChiArgList().get(0).getMyCard1();
				int card2 = curSyn.getChiArgList().get(0).getMyCard2();
				chi(card1, card2);
			} else if (ting) {
				ting();
			} else if (chu) {
				int card = curSyn.getTingListCount() > 0 ? curSyn.getTingList(0) : data.gamingData.handCards.get(0);
				chu(card, false);
			} else if (zhiDui) {
				zd(curSyn.getTingDzs(0));
			} else {
				throw new RuntimeException("无法识别的操作:" + oper);
			}
			curSyn = null;
		}
		if(gameEnd) {
			player.enroll(null, null);
			logger.debug("重新报名游戏");
			gameEnd = false;
		}
	}

	public void nohangup() {
		sendPacket(PacketType.CancelHangupRequest, null);
	}

	public void isGaming() {
		sendPacket(PacketType.PlayerGamingSynInquire, null);
	}

	public void voteDissmiss(boolean agree) {
		DissmissVoteSyn.Builder gb = DissmissVoteSyn.newBuilder();
		gb.setAgree(agree);
		sendPacket(PacketType.DissmissVoteSyn, gb.build().toByteString());
	}

	public void hangup() {
		sendPacket(PacketType.HangupRequest, null);
	}
}
