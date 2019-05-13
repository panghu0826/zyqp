package com.buding.hall.network.cmd;

import com.buding.common.token.TokenClient;
import com.buding.db.model.UserOrder;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.item.service.ItemService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.order.dao.UserOrderDao;
import com.buding.hall.module.shop.OrderStatus;
import com.buding.hall.module.shop.channel.ChannelRepostory;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall.IPABuyItemComplete;
import packet.msgbase.MsgBase.PacketType;
import org.json.JSONObject;
import sun.misc.BASE64Encoder;


import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component
public class IPABuyItemCompleteCmd extends HallCmd {

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

	@Autowired
	ItemService itemService;
	
	@Override
	public void execute(CmdData data) throws Exception {
		IPABuyItemComplete req = IPABuyItemComplete.parseFrom(data.packet.getData());
		boolean isOk = verifyReceipt(req.getResult(),req.getOrderId());
		UserOrder order = userOrderDao.getByOrderId(req.getOrderId());
		if(isOk){
			ProductConfig conf = configManager.getItemConf(order.getProductId());
			itemService.addItem(data.session.userId, ItemChangeReason.UserBuyItem, "UserBuy:" + req.getOrderId(), conf.items);
			for (ItemPkg d : conf.items) {
				logger.info("playerId={};id={};itemType={};args={};count={};act=addItemOk;", data.session.userId, req.getOrderId(), d.baseConf.itemType, d.baseConf.getArgument(), d.count);
			}
		}else{
			order.setOrderStatus(OrderStatus.FAIL);
			userOrderDao.update(order);
		}
	}
	public  boolean verifyReceipt(String result, String orderNo) {
		String _url ="https://buy.itunes.apple.com/verifyReceipt";//正式
		String _sandboxUrl = "https://sandbox.itunes.apple.com/verifyReceipt";//测试

		int status= postIpaUrl(_url,result,orderNo);
		if(status==21007){
			status = postIpaUrl(_sandboxUrl,result,orderNo);
		}
		return status==0;
	}
	private int postIpaUrl(String _url,String result,String orderNo) {
		int status ;
		if(StringUtils.isBlank(result)){
			return -1;
		}
		try {
			JsonNode node = new ObjectMapper().readTree(result);
			result = node.get("Payload").asText();
			URL url = new URL(_url);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setAllowUserInteraction(false);
			Map map = new HashMap();
			map.put("receipt-data", result);
			JSONObject jsonObject = new JSONObject(map);
			PrintStream ps = new PrintStream(connection.getOutputStream());
			ps.print(jsonObject.toString());
			ps.close();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String str;
			StringBuffer sb = new StringBuffer();
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
			String response = sb.toString();
			logger.info("ipa支付完成调用返回==========="+response);
			JSONObject jresult = new JSONObject(response);
			//验证结果
			status = jresult.getInt("status");
			if (0 == status) {
				String product_id;
				if(jresult.has("receipt")) {
					JSONObject receipt = jresult.getJSONObject("receipt");
					JSONObject in_app = (JSONObject) receipt.getJSONArray("in_app").get(0);
					product_id = in_app.getString("product_id");				//道具ID
				} else {
					return -1;
				}

				UserOrder order = userOrderDao.getByOrderId(orderNo);
				if (null == order) {
					logger.error("交易记录不存在 ");
					return -1;
				}
				String item_str = "com.xsk.game_" + order.getProductId();
				if (!item_str.equals(product_id)) {
					logger.error("道具ID不对 ");
					return -1;
				}
				//验证通过，将交易ID，写入支付记录
				order.setOrderStatus(OrderStatus.END);
				userOrderDao.update(order);

				logger.info("苹果支付交易成功");
			}
		} catch(Exception ex) {
			logger.info("IOS IAP postIpaUrl ERROR:" + ex);
			return -1;
		}
		return status;
	}

	@Override
	public PacketType getKey() {
		return PacketType.IPABuyItemComplete;
	}
	public static String getBase64(String str) {
		byte[] b = null;
		String s = null;
		try {
			b = str.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (b != null) {
			s = new BASE64Encoder().encode(b);
		}
		return s;
	}
}
