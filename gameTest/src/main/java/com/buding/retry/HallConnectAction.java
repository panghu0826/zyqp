package com.buding.retry;

import com.buding.test.Player;

public class HallConnectAction extends RetryAction {
	Player player = null;
	String serverIp;
	int serverPort;

	public HallConnectAction(Player player, String serverIp, int serverPort) {
		super("大厅初始化 ");
		this.player = player;
		this.serverIp = serverIp;
		this.serverPort = serverPort;
	}

	@Override
	public void doAct() {
		player.init(serverIp, serverPort);
	}

	@Override
	public boolean isDone() {
		return player.isInit();
	}

	@Override
	public void reset() {
		
	}

}
