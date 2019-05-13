package AliPayment;

import java.util.Map;

import Config.Constants;
import Net.HttpRequest;
import Net.HttpResponse;

public class AliHttpResponse extends HttpResponse {

	public AliHttpResponse(HttpRequest request) {
		super(request);
		//Auto-generated constructor stub
		try
		{
			AliHttpRequest kRequest = (AliHttpRequest)request;
			Map<String, String> params = OrderInfoUtil2.buildOrderParamMap(kRequest.productId, Constants.ALI_APP_ID, true);
			String orderParam = OrderInfoUtil2.buildOrderParam(params);

			String privateKey = Constants.ALI_PRIVATE_KEY;
			String sign = OrderInfoUtil2.getSign(params, privateKey, true);
			final String orderInfo = orderParam + "&" + sign;
			System.out.println("alipay:"+orderInfo);
			
			response = "HTTP/1.1 200 \r\n";
			response += "Server: Our Java Server/1.0 \r\n";
			response += "Content-Type: text/html \r\n";
			response += "Content-Length: " + orderInfo.length() + " \r\n";
			response += "\r\n";
			response += orderInfo;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

}
