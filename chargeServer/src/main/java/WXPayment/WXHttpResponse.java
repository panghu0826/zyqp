package WXPayment;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.sf.json.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import Config.Constants;
import Config.ProductConfig;
import Net.HttpRequest;
import Net.HttpResponse;

public class WXHttpResponse extends HttpResponse {

	public WXHttpResponse(HttpRequest request) {
		super(request);
		//Auto-generated constructor stub
		String productArgs = genProductArgs(((WXHttpRequest)request).RemoteAddr, ((WXHttpRequest)request).productId , ((WXHttpRequest)request).wx_app_id);
		System.out.println(productArgs);
		
		response = "HTTP/1.1 200 \r\n";
		response += "Server: Our Java Server/1.0 \r\n";
		response += "Content-Type: text/html \r\n";
		response += "Content-Length: " + productArgs.length() + " \r\n";
		response += "\r\n";
		response += productArgs;
	}

	private String nonceStr, packageValue; 
	private long timeStamp;
	private String genProductArgs(String RemoteAddr, String strProductId, String appId) {
		StringBuffer xml = new StringBuffer();

		try {
			JSONObject json = new JSONObject();
			
			try {
				json.put("appid", appId);
				String traceId = getTraceId();
				json.put("traceid", traceId);
				nonceStr = getRandomStr();
				json.put("noncestr", nonceStr);
				
				List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
				packageParams.add(new BasicNameValuePair("bank_type", "WX"));
				packageParams.add(new BasicNameValuePair("body", ProductConfig.GetBodyByProductId(strProductId)));
				packageParams.add(new BasicNameValuePair("fee_type", "1"));
				packageParams.add(new BasicNameValuePair("input_charset", "UTF-8"));
				packageParams.add(new BasicNameValuePair("notify_url", "http://weixin.qq.com"));
				packageParams.add(new BasicNameValuePair("out_trade_no", genOutTradNo()));
				packageParams.add(new BasicNameValuePair("partner", Constants.WX_PARTERN_ID));
				packageParams.add(new BasicNameValuePair("spbill_create_ip", RemoteAddr));
				packageParams.add(new BasicNameValuePair("total_fee", ProductConfig.GetWXPriceByProductId(strProductId)));
				packageValue = genPackage(packageParams);
				
				json.put("package", packageValue);
				timeStamp = genTimeStamp();
				json.put("timestamp", timeStamp);
				
				List<NameValuePair> signParams = new LinkedList<NameValuePair>();
				signParams.add(new BasicNameValuePair("appid", appId));
				signParams.add(new BasicNameValuePair("appkey", Constants.WX_PARTNER_KEY));
				signParams.add(new BasicNameValuePair("noncestr", nonceStr));
				signParams.add(new BasicNameValuePair("package", packageValue));
				signParams.add(new BasicNameValuePair("timestamp", String.valueOf(timeStamp)));
				signParams.add(new BasicNameValuePair("traceid", traceId));
				json.put("app_signature", genSign(signParams));
				
				json.put("sign_method", "sha1");
			} catch (Exception e) {
				System.out.println("genProductArgs fail, ex = " + e.getMessage());
				return null;
			}
			
			return json.toString();

		} catch (Exception e) {
			System.out.println("genProductArgs fail, ex = " + e.getMessage());
			return null;
		}
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
	
	private String genSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		for (; i < params.size() - 1; i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append(params.get(i).getName());
		sb.append('=');
		sb.append(params.get(i).getValue());
		
		String sha1 = MD5Util.sha1(sb.toString());
		System.out.println( "genSign, sha1 = " + sha1);
		return sha1;
	}
	
	private String genPackage(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.WX_PARTNER_KEY);
		
		String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
		
		return URLEncodedUtils.format(params, "utf-8") + "&sign=" + packageSign;
	}
	
	private String getTraceId() {
		return "crestxu_" + genTimeStamp(); 
	}
	
	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}
	
	private String genOutTradNo() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}
}
