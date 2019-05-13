package NewWXPayment;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import Config.Constants;
import Config.ProductConfig;
import Net.HttpRequest;
import Net.HttpResponse;
import WXPayment.MD5;

import com.google.gson.Gson;

public class NewWXHttpResponse extends HttpResponse {

	public NewWXHttpResponse(HttpRequest request) throws UnsupportedEncodingException {
		super(request);
		//把相关参数传给微信支付
		Map<String, String> map = new HashMap<String, String>();
        NewWXHttpRequest req = (NewWXHttpRequest)request;
        map.put("appid", req.wx_app_id);
        map.put("mch_id", Constants.WX_MCH_ID);
        map.put("nonce_str", getRandomStr());
        map.put("body", ProductConfig.GetBodyByProductId(req.productId));
        map.put("out_trade_no", genOutTradNo());
        map.put("total_fee", ProductConfig.GetWXPriceByProductId(req.productId));
        map.put("spbill_create_ip", req.RemoteAddr);
        map.put("notify_url",
                "http://www.xskdqmj3d.cn/a/wx/order_notify");
        map.put("trade_type", "APP");
        map.put("sign", WXUtil.getSign(map, Constants.WX_MCH_SECRET));
        
        //把map转换成xml，并发送到微信支付接口
        String info = WXUtil.map2xml(map);
        System.out.println("unitOrder:"+info);
        String restxml = HttpUtils.post(Constants.ORDER_PAY, info);
        System.out.println(restxml);
        Map<String, String> returnMap = WXUtil.xml2Map(restxml);
        
        Map<String, String> resultmap = new HashMap<String, String>();
        if(WXUtil.CheckSign(returnMap, Constants.WX_MCH_SECRET))
        {
        	 //返回的键要相对应，所以要改过来
            resultmap.put("appid", req.wx_app_id);
            resultmap.put("partnerid", Constants.WX_MCH_ID);
            resultmap.put("prepayid",returnMap.get("prepay_id"));
            resultmap.put("noncestr",returnMap.get("nonce_str"));
            resultmap.put("timestamp",payTimestamp());
            resultmap.put("package","WXPay");
            resultmap.put("sign",WXUtil.getSign(resultmap, Constants.WX_MCH_SECRET));
        	resultmap.put("return_code" , "SUCCESS");
        	resultmap.put("return_msg", "OK");
        }
        else
        {
        	resultmap.put("return_code" , "FAIL");
        	resultmap.put("return_msg", "Wrong Sign");
        }
        

        String inf = new Gson().toJson(resultmap);
        
        response = "HTTP/1.1 200 \r\n";
		response += "Server: Our Java Server/1.0 \r\n";
		response += "Content-Type: text/html \r\n";
		response += "Content-Length: " + inf.length() + " \r\n";
		response += "\r\n";
		response += inf;
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
	
	/**
	 * 注意：商户系统内部的订单号,32个字符内、可包含字母,确保在商户系统唯一
	 */
	private String genOutTradNo() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}
	
    /**
	 * @return
	 */
	public static String payTimestamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}

}
