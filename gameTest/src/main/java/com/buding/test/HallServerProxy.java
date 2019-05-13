package com.buding.test;

import net.sf.json.JSONObject;
import packet.game.Hall.ConfirmOrderRequest;
import packet.game.Hall.GenOrderRequest;
import packet.game.Hall.GenOrderResponse;
import packet.game.Hall.MallProductResponse;
import packet.game.Hall.RegisterResponse;
import packet.game.Hall.RoomConfigResponse;
import packet.game.Hall.RoomResultRequest;
import packet.game.Hall.RoomResultResponse;
import packet.game.Hall.VistorRegisterResponse;
import packet.game.Msg.ReceiveMailAttachRequest;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.rank.Rank.RankSyn;
import packet.user.User.LoginResponse;
import packet.user.User.ModifyUserInfoRequest;
import packet.user.User.UserInfoSyn;

import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class HallServerProxy extends BaseListener {
	
	public HallServerProxy(Player player, PlayerData data) {
		super(player, data);
	}
	
	public void roomResultRequest(long id) {
		RoomResultRequest.Builder rb = RoomResultRequest.newBuilder();
		rb.setRoomId(id);
		sendPacket(PacketType.RoomResultRequest, rb.build().toByteString());
	}
	
	public void genOrderRequest(String productId, int channel) {
		GenOrderRequest.Builder rb = GenOrderRequest.newBuilder();
		rb.setPlatformId(channel);
		rb.setProductId(productId);
		sendPacket(PacketType.GenOrderRequest, rb.build().toByteString());
	}
	
	public void loadRoomConf() {
		sendPacket(PacketType.RoomConfigRequest, null);
	}
	
	public void getMailAttch(long mailId) {
		ReceiveMailAttachRequest.Builder rb = ReceiveMailAttachRequest.newBuilder();
		rb.setMailId(mailId);
		sendPacket(PacketType.ReceiveMailAttachRequest, rb.build().toByteString());
	}
	
	public void loadRank() {
		sendPacket(PacketType.RankRequest, null);
	}
	
	public void modifyUserInfo(String head, String nickname) {
		ModifyUserInfoRequest.Builder req = ModifyUserInfoRequest.newBuilder();
		if(head != null) {
			req.setHeadImg(head);
		}
		if(nickname != null) {
			req.setNickName(nickname);
		}
		sendPacket(PacketType.ModifyUserInfoRequest, req.build().toByteString());
	}
	
	public void mallProductRequest() {
		sendPacket(PacketType.MallProductRequest, null);
	}
	
	public void confirmOrderRequest(String orderId) {
		ConfirmOrderRequest.Builder rb = ConfirmOrderRequest.newBuilder();
		rb.setPlatformId(3);
		rb.setOrderId(orderId);
		sendPacket(PacketType.ConfirmOrderRequest, rb.build().toByteString());
	}

	public void msgRead(byte[] msg) {
		try {
			PacketBase p = PacketBase.parseFrom(msg);
			int code = p.getCode();
			PacketType packetType = p.getPacketType();
			if (code != 0) {				
				logger.error("packet error! type=" + packetType.toString() + ";msg=" + p.getMsg());
				if(packetType == PacketType.AuthRequest) {
//					data.loginData = null; //重置登录
				}
				return;
			}

			switch (packetType) {
			case LoginRequest:
			{
				data.loginData = LoginResponse.parseFrom(p.getData());
				data.msgServerAddr = data.loginData.getMsgServerAddr();
				data.hallServerAddr = data.loginData.getHallServerAddr();
				data.gameServerAddr = data.loginData.getGameServerAddr();
			}
				break;
			case VistorRegisterRequest:
			{
				VistorRegisterResponse syn = VistorRegisterResponse.parseFrom(p.getData());
				player.setAccount(syn.getAccount(), syn.getPassword());
			}
				break;
			case MallProductResponse:
			{
				MallProductResponse syn = MallProductResponse.parseFrom(p.getData());
				logger.info(JsonFormat.printToString(syn));
			}
				break;
			case RegisterResponse:
			{
				RegisterResponse syn = RegisterResponse.parseFrom(p.getData());
				logger.info("注册返回:");
				logger.info(JsonFormat.printToString(syn));
			}
				break;
			case RoomResultResponse:
			{
				RoomResultResponse syn = RoomResultResponse.parseFrom(p.getData());
				logger.info("房间战绩列表:");
				logger.info(JsonFormat.printToString(syn));
			}
			case GenOrderResponse:
			{
				GenOrderResponse syn = GenOrderResponse.parseFrom(p.getData());
				logger.info("生成订单:");
				logger.info(JsonFormat.printToString(syn));
			}
			break;
			case VistorRegisterResponse:
			{
				VistorRegisterResponse syn = VistorRegisterResponse.parseFrom(p.getData());
				logger.info("注册成功:");
				String account = syn.getAccount();
				String passwd = syn.getPassword();
				player.setAccount(account, passwd);
			}
			break;

			case UserInfoSyn:
			{
				UserInfoSyn syn = UserInfoSyn.parseFrom(p.getData());
				logger.debug("推送用户信息:");
				logger.debug(JsonFormat.printToString(syn));
			}
			break;
			case RoomConfigResponse:
			{
				RoomConfigResponse syn = RoomConfigResponse.parseFrom(p.getData());
				logger.info("推送房间配置信息:");
				logger.info(JsonFormat.printToString(syn));
			}
			break;
			case RankSyn:{
				RankSyn syn = RankSyn.parseFrom(p.getData());
				logger.debug("推送排行榜信息:");
				String json = JsonFormat.printToString(syn);
				JSONObject obj = JSONObject.fromObject(json);
				logger.info(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
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
