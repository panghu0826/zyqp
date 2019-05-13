package com.buding.hall.module.shop.channel.ipa;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.UserOrder;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.SignUtils;
import com.buding.hall.module.constants.Constants;
import com.buding.hall.module.order.dao.UserOrderDao;
import com.buding.hall.module.shop.OrderStatus;
import com.buding.hall.module.shop.channel.BasePayChannel;
import com.buding.hall.module.shop.channel.Channels;
import com.buding.hall.module.shop.service.ShopService;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall.ConfirmOrderRequest;
import packet.game.Hall.GenOrderRequest;
import packet.game.Hall.GenOrderResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class IpaPayChannel extends BasePayChannel {

	@Autowired
	UserOrderDao userOrderDao;

	@Autowired
	ShopService shopService;

	@Autowired
	ConfigManager configManager;

	@Override
	public TResult<GenOrderResponse> createOrder(int userId, GenOrderRequest req, ProductConfig conf) throws Exception {
		String orderId = shopService.genOrderId(userId + "-ipa-");

		if (orderId == null) {
			return TResult.fail1("生成订单失败");
		}

		String orderInfo = "com.xsk.game_"+req.getProductId();
		GenOrderResponse.Builder rsp = GenOrderResponse.newBuilder();
		rsp.setOrderId(orderId);
		rsp.setPlatformId(req.getPlatformId());
		rsp.setData(ByteString.copyFrom(orderInfo.getBytes("UTF-8")));

		UserOrder order = new UserOrder();
		order.setCtime(new Date());
		order.setMtime(new Date());
		order.setOrderId(orderId);
		order.setOrderStatus(OrderStatus.WAITING);
		order.setProductId(req.getProductId());
		order.setUserId(userId);
		ProductConfig config = configManager.getItemConf(req.getProductId());
		order.setPrice(Double.valueOf(config.price.currenceCount));

		userOrderDao.insert(order);

		return TResult.sucess1(rsp.build());
	}

	@Override
	public Result confirmOrder(int userId, ConfirmOrderRequest req) throws Exception {
		return Result.success();
	}

	@Override
	public int getName() {
		return Channels.ipa;
	}

}
