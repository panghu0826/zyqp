package com.buding.common.admin.cmd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class BaseCmd implements Cmd {
	protected Logger logger = LogManager.getLogger(getClass());
	
	public Cmd parentCmd;

	@Override
	public CmdResultModel execute(InputStream input, OutputStream output, Socket socket) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		PrintWriter writer = new PrintWriter(output);
		
		CmdResultModel model = new CmdResultModel();
		
		String line = null;
		int err = 0;
		try {
			while(true) {
				try {
					boolean b = socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown() || socket.isConnected();
					if(socket.isClosed()) {
						model.result = ExecuteCmdResult.CONTINUE;
						logger.info("socket close");
						return model;
					}
					logger.info("show tips");
					showTips(writer);
					line = reader.readLine();
					logger.info("read:" + line);
					if(StringUtils.isBlank(line)) {
						err++;
						if(err > 3) {
							model.result = ExecuteCmdResult.EXIT;
							return model;
						}
						continue;
					}
					err = 0;
					for(String cmd : line.split("&&")) {
						if(StringUtils.isBlank(cmd)) {
							break;
						}
						cmd = cmd.trim();
						if("back".equals(cmd)) {
							model.result = ExecuteCmdResult.BACK;
							return model;
						}
						
						if("exit".equals(cmd)) {
							model.result = ExecuteCmdResult.EXIT;
							return model;
						}
						
						ExecuteCmdResult executeCmdOnSelect = executeCmdOnSelect(cmd, writer);
						if(executeCmdOnSelect != ExecuteCmdResult.CONTINUE) {
							model.result = executeCmdOnSelect;
							return model;
						}
					}				
				} catch (Exception e) {	
					logger.info("Error");
					logger.error("", e);
					e.printStackTrace(writer);
					writer.flush();
					if(e instanceof SocketException) {
						model.result = ExecuteCmdResult.CONTINUE;
						return model;
					}
				}
			}
		} finally {
			clean();
		}
	}

	public void clean() {
		
	}
	
	public abstract void showTips(PrintWriter writer) throws Exception;
	public abstract ExecuteCmdResult executeCmdOnSelect(String line, PrintWriter writer) throws Exception;

	public void setParentCmd(Cmd parentCmd) {
		this.parentCmd = parentCmd;
	}	
}
