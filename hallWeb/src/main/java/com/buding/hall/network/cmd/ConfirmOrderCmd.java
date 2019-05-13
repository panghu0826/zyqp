package com.buding.hall.network.cmd;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.ConfirmOrderRequest;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.result.Result;
import com.buding.common.token.TokenClient;
import com.buding.db.model.UserOrder;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.order.dao.UserOrderDao;
import com.buding.hall.module.shop.OrderStatus;
import com.buding.hall.module.shop.channel.ChannelRepostory;
import com.buding.hall.module.shop.channel.PayChannel;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.hall.network.HallSession;

@Component
public class ConfirmOrderCmd extends HallCmd {

	protected Logger logger = LogManager.getLogger(getClass());

	@Autowired
	protected ChannelRepostory channelRepostory;
	
	@Autowired
	protected TokenClient tokenClient;
	
	@Autowired
	protected UserSecurityHelper userSecurityHelper;
	
	@Autowired
	protected HallPushHelper pushHelper;
	
	@Autowired
	UserOrderDao userOrderDao;
	
	@Autowired
	ConfigManager configManager;
	
	@Override
	public void execute(CmdData data) throws Exception {
		ConfirmOrderRequest req = ConfirmOrderRequest.parseFrom(data.packet.getData());
		
		int userId = data.session.userId;
		HallSession session = data.session;
		if(userId == 0) {
			pushHelper.pushErrorMsg(session, PacketType.ConfirmOrderReqsponse, "用户未登录");
			return;
		}
		
		UserOrder order = userOrderDao.getByOrderId(req.getOrderId());
		if(order == null) {
			pushHelper.pushErrorMsg(session, PacketType.ConfirmOrderReqsponse, "订单不存在");
			return;
		}
		
		if(order.getOrderStatus() != OrderStatus.WAITING) {
			pushHelper.pushErrorMsg(session, PacketType.ConfirmOrderReqsponse, "订单状态不正确");
			return;
		}
		
		ProductConfig conf = configManager.getItemConf(order.getProductId());
		if (conf == null) {
			logger.info("act=createOrderError;userId={};productId={}", userId, order.getProductId());			
			pushHelper.pushErrorMsg(session, PacketType.ConfirmOrderReqsponse, "商品不存在");
			return;
		}
		
		PayChannel channel = channelRepostory.getChannel(req.getPlatformId());
		if(channel == null) {
			logger.error("PayChannel not found for platform {}", req.getPlatformId());
			pushHelper.pushErrorMsg(session, PacketType.ConfirmOrderReqsponse, "支付平台不存在");
			return;
		}
		Result rr = channel.confirmOrder(userId, req);
		if(rr.isOk()) {
			pushHelper.pushPBMsg(session, PacketType.ConfirmOrderReqsponse, null);
		} else {
			pushHelper.pushErrorMsg(session, PacketType.ConfirmOrderReqsponse, rr.msg);
		}
	}

	@Override
	public PacketType getKey() {
		return PacketType.ConfirmOrderRequest;
	}	
}
