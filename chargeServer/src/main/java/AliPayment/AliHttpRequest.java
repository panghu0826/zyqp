package AliPayment;

import org.apache.commons.lang.StringUtils;

import Net.HttpRequest;


public class AliHttpRequest extends HttpRequest {

	public static String ALI_REQUEST_URL = "RequestAliOrder";
	public String productId;
	
	public AliHttpRequest(String s) {
		super(s);
		
		String lines[] = s.split("\n");
		
		String strTemp = lines[0];
		System.out.println(strTemp);
		
		String line[] = strTemp.split(" ");
		if(line.length == 3)
		{
			String v = line[1];
			productId = StringUtils.substringBetween(v, "productId=", "&");
		}
	}

	
}
