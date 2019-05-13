package WXNotifyURL;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import Config.Constants;
import Net.HttpRequest;
import Net.HttpResponse;
import NewWXPayment.HttpUtils;
import NewWXPayment.NewWXHttpResponse;
import NewWXPayment.WXUtil;

public class WXNotifyHttpResponse extends HttpResponse {

	public WXNotifyHttpResponse(HttpRequest request) throws UnsupportedEncodingException {
		super(request);
		//Auto-generated constructor stub
		
		WXNotifyHttpRequest kRequest = (WXNotifyHttpRequest)request;
		
		Map<String , String> returnMap = new HashMap<String, String>();
		
		if(kRequest != null)
		{
			String strInfo = kRequest.strInfo;
			//需要检验签名
			Map<String, String> requestMap = WXUtil.xml2Map(strInfo);
			
			if(WXUtil.CheckSign(requestMap, Constants.WX_MCH_SECRET))
			{
				if(requestMap.get("return_code").equals("SUCCESS"))
				{
					//签名校验成功.
					//收到支付回调，并且校验签名成功，需要根据订单id，去微信服务器查询订单.
					//我这边因为是模拟出来的数据，所以查询订单肯定是失败的。
					Map<String , String> searchOrderMap = new HashMap<String, String>();
					searchOrderMap.put("appid", requestMap.get("appid"));
					searchOrderMap.put("mch_id", Constants.WX_MCH_ID);
					searchOrderMap.put("transaction_id", requestMap.get("transaction_id"));
					searchOrderMap.put("out_trade_no",requestMap.get("out_trade_no"));
					searchOrderMap.put("nonce_str",requestMap.get("nonce_str"));
					searchOrderMap.put("sign", WXUtil.getSign(searchOrderMap, Constants.WX_MCH_SECRET));
					
					String restxml = HttpUtils.post(Constants.SEARCH_ORDER_PAY, WXUtil.map2xml(searchOrderMap));
					//校验签名
					System.out.println(restxml);
					Map<String , String> retMap = WXUtil.xml2Map(restxml);
					if(WXUtil.CheckSign(retMap, Constants.WX_MCH_SECRET))
					{
						//校验签名成功
						if(retMap.containsKey("return_code") && retMap.get("return_code").equals("SUCCESS"))
						{
							//取出这边的订单内的信息，然后处理添加道具之类的逻辑 
						}
						else
						{
							returnMap.put("return_code", retMap.get("return_code"));
							returnMap.put("return_msg", retMap.get("return_msg"));
						}
					}
					else
					{
						returnMap.put("return_code", "FAIL");
						returnMap.put("return_msg", "REQUEST ORDER WITH WRONG SIGN");
					}
				}
				else
				{
					//微信支付失败,通知微信服务器，接收到了.
					returnMap.put("return_code", "SUCCESS");
					returnMap.put("return_msg", "OK");
				}
			}
			else
			{
				//签名不通过
				returnMap.put("return_code", "FAIL");
				returnMap.put("return_msg", "WRONG SIGN");
			}
		}
		else
		{
			returnMap.put("return_code", "FAIL");
			returnMap.put("return_msg", "EXCEPTION");
		}
		String inf = WXUtil.map2xml(returnMap);
		
		response = "HTTP/1.1 200 \r\n";
		response += "Server: Our Java Server/1.0 \r\n";
		response += "Content-Type: text/html \r\n";
		response += "Content-Length: " + inf.length() + " \r\n";
		response += "\r\n";
		response += inf;
	}

}
