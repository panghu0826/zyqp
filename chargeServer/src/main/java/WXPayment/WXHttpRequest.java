package WXPayment;

import org.apache.commons.lang.StringUtils;

import Net.HttpRequest;

public class WXHttpRequest extends HttpRequest {

	public static String WX_REQUEST_URL = "RequestWXOrder";
	
	public String productId;
	public String wx_app_id;
	public String RemoteAddr;
	
	public WXHttpRequest(String s) {
		super(s);
		// Auto-generated constructor stub
		String lines[] = s.split("\n");
		
		String strTemp = lines[0];
		System.out.println(strTemp);
		
		String line[] = strTemp.split(" ");
		if(line.length == 3)
		{
			String v = line[1];
			wx_app_id = StringUtils.substringBetween(v, "appId=", "&");
			productId = StringUtils.substringBetween(v, "productId=", "&");
			RemoteAddr = StringUtils.substringBetween(v, "RemoteAddr=", "&");
		}
	}

}
