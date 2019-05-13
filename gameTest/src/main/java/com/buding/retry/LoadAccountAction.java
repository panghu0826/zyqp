package com.buding.retry;

import com.buding.test.Player;

public class LoadAccountAction extends RetryAction {
	Player player = null;

	public LoadAccountAction(Player player) {
		super("注册");
		this.player = player;
	}

	@Override
	public void doAct() {
		player.loadAccount();
	}

	@Override
	public boolean isDone() {
		return player.hasAccount();
	}

	@Override
	public void reset() {
		
	}

}
