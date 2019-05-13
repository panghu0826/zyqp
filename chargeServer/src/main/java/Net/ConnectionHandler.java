package Net;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import AliPayment.AliHttpRequest;
import AliPayment.AliHttpResponse;
import NewWXPayment.NewWXHttpRequest;
import NewWXPayment.NewWXHttpResponse;
import WXNotifyURL.WXNotifyHttpRequest;
import WXNotifyURL.WXNotifyHttpResponse;


public class ConnectionHandler extends Thread
{
	Socket s;
	
	PrintWriter pw;
	
	BufferedReader br;
	
	public ConnectionHandler(Socket s) throws Exception
	{
		this.s = s;
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		pw = new PrintWriter(s.getOutputStream());
	}
	
	public void run() {
		try {
			String reqS = "";
			while(br.ready() || reqS.length() == 0) {
				reqS += (char) br.read();
			}
			
			if(reqS.contains(AliHttpRequest.ALI_REQUEST_URL)) {
				System.out.println(reqS);
				AliHttpRequest req = new AliHttpRequest(reqS);
				AliHttpResponse res  = new AliHttpResponse(req);
				pw.write(res.response.toCharArray());
				pw.close();
				br.close();
				s.close();
			}
			else if(reqS.contains(NewWXHttpRequest.WX_REQUEST_URL)) {
				System.out.println(reqS);
				NewWXHttpRequest req = new NewWXHttpRequest(reqS);
				NewWXHttpResponse res = new NewWXHttpResponse(req);
				System.out.println(res.response);
				pw.write(res.response.toCharArray());
				pw.close();
				br.close();
				s.close();
			}
			else if(reqS.contains(WXNotifyHttpRequest.WX_NOTIFY_URL)) {
				System.out.println(reqS);
				WXNotifyHttpRequest req = new WXNotifyHttpRequest(reqS);
				WXNotifyHttpResponse res = new WXNotifyHttpResponse(req);
				System.out.println(res.response);
				pw.write(res.response.toCharArray());
				pw.close();
				br.close();
				s.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
