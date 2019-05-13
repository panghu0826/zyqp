package Net;

public class HttpResponse {

	protected HttpRequest req;
	
	public String response;
	
	public HttpResponse(HttpRequest request)
	{
		req = request;
	}
}
