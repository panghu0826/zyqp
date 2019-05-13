package com.buding.retry;

import com.buding.test.Player;

public class MsgAuthAction extends RetryAction {
	Player player = null;

	public MsgAuthAction(Player player) {
		super("消息服认证");
		this.player = player;
	}

	@Override
	public void doAct() {
		player.authMsg();
	}

	@Override
	public boolean isDone() {
		return player.isMsgAuth();
	}

	@Override
	public void reset() {
		
	}

}
