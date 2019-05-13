package com.buding.hall.network.cmd;

import com.buding.db.model.User;
import com.buding.hall.module.user.dao.UserDao;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.GenOrderRequest;
import packet.game.Hall.GenOrderResponse;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.result.TResult;
import com.buding.common.token.TokenClient;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.shop.channel.ChannelRepostory;
import com.buding.hall.module.shop.channel.PayChannel;
import com.buding.hall.module.user.helper.UserSecurityHelper;

@Component
public class GenOrderCmd extends HallCmd {
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
	ConfigManager configManager;

	@Autowired
	UserDao userDao;
	
	@Override
	public void execute(CmdData data) throws Exception {
		GenOrderRequest req = GenOrderRequest.parseFrom(data.packet.getData());
		
		int userId = req.getPlayerId();

		User user = userDao.getUser(userId);
		if(StringUtils.equals(req.getProductId(),"31014") && user.getShouchong2() == 1){
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "您已购买过此首冲礼包");
			return;
		}else if(StringUtils.equals(req.getProductId(),"31015") && user.getShouchong3() == 1){
			pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "您已购买过此首冲礼包");
			return;
		}
		ProductConfig conf = configManager.getItemConf(req.getProductId());
		if (conf == null) {
			logger.info("act=createOrderError;userId={};productId={}", userId, req.getProductId());			
			pushHelper.pushErrorMsg(data.session, PacketType.GenOrderResponse, "商品不存在");
			return;
		}
		PayChannel channel = channelRepostory.getChannel(req.getPlatformId());
		if(channel == null) {
			logger.error("PayChannel not found for platform {}", req.getPlatformId());
			pushHelper.pushErrorMsg(data.session, PacketType.GenOrderResponse, "支付平台不存在");
			return;
		}
		TResult<GenOrderResponse> rr = channel.createOrder(userId, req, conf);
		if(rr.isOk()) {
			pushHelper.pushPBMsg(data.session, PacketType.GenOrderResponse, rr.t.toByteString());
		} else {
			pushHelper.pushErrorMsg(data.session, PacketType.GenOrderResponse, rr.msg);
		}
	}

	@Override
	public PacketType getKey() {
		return PacketType.GenOrderRequest;
	}	
}
