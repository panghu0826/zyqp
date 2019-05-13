package com.buding.hall.module.shop.channel.wx;

import java.util.*;

import com.buding.db.model.MallConf;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.conf.ConfDao;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.ConfirmOrderRequest;
import packet.game.Hall.GenOrderRequest;
import packet.game.Hall.GenOrderResponse;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.UserOrder;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.HttpUtils;
import com.buding.hall.helper.WXUtil;
import com.buding.hall.module.constants.Constants;
import com.buding.hall.module.order.dao.UserOrderDao;
import com.buding.hall.module.shop.OrderStatus;
import com.buding.hall.module.shop.channel.BasePayChannel;
import com.buding.hall.module.shop.channel.Channels;
import com.buding.hall.module.shop.service.ShopService;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;

import javax.annotation.PostConstruct;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class WxPayChannel extends BasePayChannel {

	@Autowired
	UserOrderDao userOrderDao;

	@Autowired
	ShopService shopService;

	@Autowired
	ConfigManager configManager;

	@Override
	public TResult<GenOrderResponse> createOrder(int userId, GenOrderRequest req, ProductConfig conf) throws Exception {
		String orderId = shopService.genOrderId(userId + "-wx-");

		if (orderId == null) {
			return TResult.fail1("生成订单失败");
		}

		// 把相关参数传给微信支付
		Map<String, String> map = new HashMap<String, String>();
		map.put("appid", Constants.WX_APP_ID);
		map.put("mch_id", Constants.WX_MCH_ID);
		map.put("nonce_str", getRandomStr());
		map.put("body", conf.desc);
		map.put("out_trade_no", orderId);
		map.put("total_fee", conf.price.currenceCount*100+""); //单位分
//		map.put("total_fee", 1+""); //单位分 TODO
		map.put("spbill_create_ip", Constants.PAY_IP);
		map.put("notify_url", "http://"+Constants.PAY_RUL+"/a/wx/order_notify");
//		map.put("notify_url", "http://l1718f1374.51mypc.cn:16769/a/wx/order_notify");
		map.put("trade_type", "APP");
		map.put("sign", WXUtil.getSign(map, Constants.WX_MCH_SECRET));

		// 把map转换成xml，并发送到微信支付接口
		String info = WXUtil.map2xml(map);
		logger.info("unitOrder:" + info);
		String restxml = HttpUtils.post(Constants.ORDER_PAY, info);
		logger.info(restxml);
		Map<String, String> returnMap = WXUtil.xml2Map(restxml);

		Map<String, String> resultmap = new HashMap<String, String>();
		if (WXUtil.CheckSign(returnMap, Constants.WX_MCH_SECRET)) {
			// 返回的键要相对应，所以要改过来
			resultmap.put("appid", Constants.WX_APP_ID);
			resultmap.put("partnerid", Constants.WX_MCH_ID);
			resultmap.put("prepayid", returnMap.get("prepay_id"));
			resultmap.put("noncestr", returnMap.get("nonce_str"));
			resultmap.put("timestamp", System.currentTimeMillis() / 1000 + "");
			resultmap.put("package", "Sign=WXPay");
			resultmap.put("sign", WXUtil.getSign(resultmap, Constants.WX_MCH_SECRET));
			resultmap.put("return_code", "SUCCESS");
			resultmap.put("return_msg", "OK");
		} else {
			resultmap.put("return_code", "FAIL");
			resultmap.put("return_msg", "Wrong Sign");
		}
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();

		String inf = gson.toJson(resultmap);

		GenOrderResponse.Builder rsp = GenOrderResponse.newBuilder();
		rsp.setOrderId(orderId);
		rsp.setData(ByteString.copyFrom(inf.getBytes("UTF-8")));

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

		rsp.setPlatformId(req.getPlatformId());
		return TResult.sucess1(rsp.build());
	}

	@Override
	public Result confirmOrder(int userId, ConfirmOrderRequest req) throws Exception {
		return Result.success();
	}

	public static String getRandomStr() {
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		String str = "QWERTYUIOPASDFGHJKLZXCVBNM0123456789";
		for (int i = 0; i < 32; i++) {
			sb.append(str.charAt(r.nextInt(str.length())));
		}
		return sb.toString();
	}

	@Override
	public int getName() {
		return Channels.wx;
	}

}
