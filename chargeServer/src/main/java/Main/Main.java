package Main;

import java.net.ServerSocket;
import java.net.Socket;

import Net.ConnectionHandler;


public class Main {

	ServerSocket serverSocket;
	
	public static void main(String[] args) throws Exception {
		new Main().runServer();
	}
	
	public void runServer() throws Exception
	{
		System.out.println("Server is started");
		
		serverSocket = new ServerSocket(6543);//
		
		acceptRequests();
	}

	private void acceptRequests() throws Exception
	{
		while(true)
		{
			Socket s = serverSocket.accept();
			ConnectionHandler ch = new ConnectionHandler(s);
			ch.start();
		}
	}
}
