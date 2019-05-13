package WXNotifyURL;

import Net.HttpRequest;

public class WXNotifyHttpRequest extends HttpRequest {

	public static String WX_NOTIFY_URL = "NotifyUrl";
	
	public String strInfo;
	
	public WXNotifyHttpRequest(String s){
		super(s);
		//Auto-generated constructor stub
		String lines[] = s.split("\n");
		
		strInfo = lines[lines.length - 1];
		System.out.println(strInfo);
	}

}
