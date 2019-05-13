package com.buding.retry;

import com.buding.test.Player;

public class MsgConnectAction extends RetryAction {
	Player player = null;

	public MsgConnectAction(Player player) {
		super("消息服认证");
		this.player = player;
	}

	@Override
	public void doAct() {
		player.initMsg();
	}

	@Override
	public boolean isDone() {
		return player.isMsgInit();
	}

	@Override
	public void reset() {
		
	}

}
