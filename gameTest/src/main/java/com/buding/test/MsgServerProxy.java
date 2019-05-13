package com.buding.test;

import packet.game.Msg.ActAndNoticeMsgSyn;
import packet.game.Msg.MarqueeMsgSyn;
import packet.game.Msg.NewMailMsgSyn;
import packet.game.MsgGame.GameChatMsgSyn;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.AuthResponse;

import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class MsgServerProxy extends BaseListener {

	public MsgServerProxy(Player player, PlayerData data) {
		super(player, data);
	}

	public void msgRead(byte[] msg) {
		try {
			PacketBase p = PacketBase.parseFrom(msg);
			int code = p.getCode();
			PacketType packetType = p.getPacketType();
			if (p.getPacketType() == PacketType.HEARTBEAT) {
				return;
			}
			if (code != 0) {				
				logger.error("packet error! type=" + packetType.toString() + ";msg=" + p.getMsg());
				if(packetType == PacketType.AuthRequest) {
//					data.loginData = null; //重置登录
				}
				return;
			}
			switch (packetType) {
			case AuthRequest:
//				data.msgAuthData = AuthResponse.parseFrom(p.getData());
				data.authMsg = true;
				break;
			case GameChatMsgSyn:{
				GameChatMsgSyn syn = GameChatMsgSyn.parseFrom(p.getData());
				logger.info("聊天数据:");
				logger.info(JsonFormat.printToString(syn));
			}
				break;
			case MarqueeMsgSyn:{
				MarqueeMsgSyn syn = MarqueeMsgSyn.parseFrom(p.getData());
				logger.info("跑马灯数据:");
				logger.info(JsonFormat.printToString(syn));
			}
			break;
			case NewMailMsgSyn:
			{
				NewMailMsgSyn syn = NewMailMsgSyn.parseFrom(p.getData());
				logger.info("新邮件数据:");
				logger.info(JsonFormat.printToString(syn));
			}
			break;
			case ActAndNoticeMsgSyn:
			{
				ActAndNoticeMsgSyn syn = ActAndNoticeMsgSyn.parseFrom(p.getData());
				logger.info("活动与公告数据:");
				logger.info(JsonFormat.printToString(syn));
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

}
