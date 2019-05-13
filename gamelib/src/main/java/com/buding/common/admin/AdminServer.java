package com.buding.common.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;

import com.buding.common.admin.cmd.GroovyCmd;


public class AdminServer implements InitializingBean, Runnable {
	private Logger logger = LogManager.getLogger(getClass());
	
	private Map<String, Object> globalVars = new HashMap<String, Object>();
	private int port;

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("AdminServer Start " + this);
		Thread t = new Thread(this, "后台管理线程");
		t.setDaemon(true);
		t.start();
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket();
			ss.bind(new InetSocketAddress("192.168.0.14", port));
			logger.info("Admin Server begin listen " + port);
			while (true) {
				listen(ss);
			}
		} catch (Throwable e) {
			logger.error("", e);
		}
	}

	private void listen(ServerSocket ss) throws IOException, Exception {
		Socket socket = ss.accept();
		logger.info("accept a new connection");
		new Thread(new CmdRunner(socket)).start();
	}

	public void propmt(InputStream input, OutputStream output) throws Exception {

	}

	public void sayBye(InputStream input, OutputStream output) throws Exception {

	}
	
	class CmdRunner implements Runnable {
		Socket socket;
		InputStream input;
		OutputStream output;

		public CmdRunner(Socket socket) throws Exception {
			this.socket = socket;
			input = socket.getInputStream();
			output = socket.getOutputStream();
		}

		@Override
		public void run() {
			try {
				GroovyCmd cmd = new GroovyCmd(globalVars);
				cmd.execute(input, output, socket);
				input.close();
				output.close();
				socket.close();
				logger.info("========close socket===============");
			} catch (Throwable e) {
				logger.error("", e);
			}
			
		}
	}

	public Map<String, Object> getGlobalVars() {
		return globalVars;
	}

	public void setGlobalVars(Map<String, Object> globalVars) {
		this.globalVars = globalVars;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
