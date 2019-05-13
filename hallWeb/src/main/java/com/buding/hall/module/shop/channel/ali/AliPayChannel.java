package com.buding.hall.module.shop.channel.ali;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.buding.hall.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.ConfirmOrderRequest;
import packet.game.Hall.GenOrderRequest;
import packet.game.Hall.GenOrderResponse;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.UserOrder;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.SignUtils;
import com.buding.hall.module.constants.Constants;
import com.buding.hall.module.order.dao.UserOrderDao;
import com.buding.hall.module.shop.OrderStatus;
import com.buding.hall.module.shop.channel.BasePayChannel;
import com.buding.hall.module.shop.channel.Channels;
import com.buding.hall.module.shop.service.ShopService;
import com.google.protobuf.ByteString;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class AliPayChannel extends BasePayChannel {

	@Autowired
	UserOrderDao userOrderDao;

	@Autowired
	ShopService shopService;

	@Autowired
	ConfigManager configManager;

	@Override
	public TResult<GenOrderResponse> createOrder(int userId, GenOrderRequest req, ProductConfig conf) throws Exception {
		String orderId = shopService.genOrderId(userId + "-ali-");

		if (orderId == null) {
			return TResult.fail1("生成订单失败");
		}

		Map<String, String> params = buildOrderParamMap(orderId, conf, Constants.ALI_APP_ID, true);
		String orderParam = buildOrderParam(params);

		String privateKey = Constants.ALI_PRIVATE_KEY;
		String sign = getSign(params, privateKey, true);
		final String orderInfo = orderParam + "&" + sign;
		logger.info("alipay:"+orderInfo);

		GenOrderResponse.Builder rsp = GenOrderResponse.newBuilder();
		rsp.setOrderId(orderId);
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

		rsp.setPlatformId(req.getPlatformId());
		return TResult.sucess1(rsp.build());
	}
	
	public static String getSign(Map<String, String> map, String rsaKey, boolean rsa2) {
		List<String> keys = new ArrayList<String>(map.keySet());
		// key排序
		Collections.sort(keys);

		StringBuilder authInfo = new StringBuilder();
		for (int i = 0; i < keys.size() - 1; i++) {
			String key = keys.get(i);
			String value = map.get(key);
			authInfo.append(buildKeyValue(key, value, false));
			authInfo.append("&");
		}

		String tailKey = keys.get(keys.size() - 1);
		String tailValue = map.get(tailKey);
		authInfo.append(buildKeyValue(tailKey, tailValue, false));

		String oriSign = SignUtils.sign(authInfo.toString(), rsaKey, rsa2);
		String encodedSign = "";

		try {
			encodedSign = URLEncoder.encode(oriSign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "sign=" + encodedSign;
	}
	
	public static String buildOrderParam(Map<String, String> map) {
		List<String> keys = new ArrayList<String>(map.keySet());

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.size() - 1; i++) {
			String key = keys.get(i);
			String value = map.get(key);
			sb.append(buildKeyValue(key, value, true));
			sb.append("&");
		}

		String tailKey = keys.get(keys.size() - 1);
		String tailValue = map.get(tailKey);
		sb.append(buildKeyValue(tailKey, tailValue, true));

		return sb.toString();
	}
	
	private static String buildKeyValue(String key, String value, boolean isEncode) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append("=");
		if (isEncode) {
			try {
				sb.append(URLEncoder.encode(value, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				sb.append(value);
			}
		} else {
			sb.append(value);
		}
		return sb.toString();
	}
	
	public static Map<String, String> buildOrderParamMap(String orderId, ProductConfig conf, String app_id, boolean rsa2) {
		Map<String, String> keyValues = new HashMap<String, String>();

		keyValues.put("app_id", app_id);

		double price = conf.price.currenceCount;
//		price = 0.01;
		
		String prdName = conf.desc;
		prdName = "diamond";
		keyValues.put("biz_content", "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\"" + price + "\",\"subject\":\"" + prdName + "\",\"body\":\"" + prdName + "\",\"out_trade_no\":\"" + orderId +  "\"}");
		
		keyValues.put("charset", "utf-8");

		keyValues.put("method", "alipay.trade.app.pay");

		keyValues.put("sign_type", rsa2 ? "RSA2" : "RSA");

		keyValues.put("timestamp", "2016-07-29 16:55:53");

		keyValues.put("version", "1.0");
		
		keyValues.put("notify_url", "http://"+Constants.PAY_RUL+"/a/ali/order_notify");
//		keyValues.put("notify_url", "http://l1718f1374.51mypc.cn:16769/a/ali/order_notify");

		return keyValues;
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
		return Channels.ali;
	}

}
