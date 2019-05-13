package com.buding.common.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AdminMultiClient {
	static List<Server> serverList = new ArrayList<Server>();

	static BufferedReader sysin;
	static PrintWriter sysout;

	public static void main(String[] args) throws Exception {
		init();
		run();
	}

	public static void init() throws Exception {
		sysin = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
		sysout = new PrintWriter(System.out);
				
		registerServer("192.168.0.14", 9981, "BattleServer");
		registerServer("192.168.0.14", 9982, "DataServer");
		registerServer("192.168.0.14", 9983, "WebServer");
		registerServer("192.168.0.14", 9984, "RankServer");
		registerServer("192.168.0.14", 9985, "MsgServer");
		registerServer("192.168.0.14", 9986, "ClusterServer");
		registerServer("192.168.0.14", 9987, "WxServer");
		registerServer("192.168.0.14", 9988, "TokenServer");
		registerServer("192.168.0.14", 9989, "BaseServer");
		registerServer("192.168.0.14", 9999, "MockClient");
	}

	private static void registerServer(String ip, int port, String serverName) {
		Server server = new Server();
		server.ip = ip;
		server.port = port;
		server.name = serverName;
		serverList.add(server);
	}

	public static void run() throws Exception {
		while (true) {
			sysout.println("Pls Select Index To Login or enter exit to leave:");			
			for (int i = 1; i <= serverList.size(); i++) {
				Server server = serverList.get(i - 1);
				sysout.println(i + ":" + server.name + " " + server.ip + " " + server.port);				
			}
			sysout.flush();

			
			
			String line = sysin.readLine();
			if ("exit".equals(line)) {
				System.exit(0);
			}
			try {
				int ind = Integer.valueOf(line.trim());
				Server server = serverList.get(ind - 1);

				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(server.ip, server.port));
				final InputStream sockInp = socket.getInputStream();
				final BufferedReader serverReader = new BufferedReader(new InputStreamReader(sockInp, "utf8"));
				final PrintWriter serverWriter = new PrintWriter(socket.getOutputStream());

				final BufferedReader clientReader = new BufferedReader(new InputStreamReader(System.in, "utf8"));
				final PrintWriter clientWriter = new PrintWriter(System.out);

				Thread cthread = new Thread(new ClientProxy(serverReader, clientWriter));
				Thread sthread = new Thread(new ServerProxy(clientReader, serverWriter));
				cthread.start();
				sthread.start();

				cthread.join();
				sthread.join();
				
				socket.close();
			} catch (NumberFormatException e) {
				sysout.println("无效的输入");
			}
		}
	}
}

class Server {
	public String ip;
	public int port;
	public String name;
}

class Proxy implements Runnable {
	public BufferedReader in;
	public PrintWriter out;

	public Proxy(BufferedReader in, PrintWriter out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public void run() {
		try {
			String line = null;
			while ((line = in.readLine()) != null) {
				boolean ret = processLine(line);
				if (!ret) {
					break;
				}
			}
			System.out.println(getClass()+"break");
			//close();
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	protected void close() throws IOException {
		in.close();
		out.close();
	}

	protected boolean processLine(String line) {
		out.println(line);
		out.flush();
		return true;
	}
}

class ClientProxy extends Proxy {
	public ClientProxy(BufferedReader in, PrintWriter out) {
		super(in, out);
	}

	@Override
	protected boolean processLine(String line) {
		return super.processLine(line);
	}
	
}

class ServerProxy extends Proxy {
	public ServerProxy(BufferedReader in, PrintWriter out) {
		super(in, out);
	}

	@Override
	protected boolean processLine(String line) {
		super.processLine(line);
		if ("exit".equals(line.trim())) {
			return false;
		}
		return true;
	}
	
}