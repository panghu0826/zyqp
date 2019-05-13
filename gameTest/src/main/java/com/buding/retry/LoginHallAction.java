package com.buding.retry;

import com.buding.test.Player;

public class LoginHallAction extends RetryAction {
	Player player = null;

	public LoginHallAction(Player player) {
		super("登录");
		this.player = player;
	}

	@Override
	public void doAct() {
		player.login();
	}

	@Override
	public boolean isDone() {
		return player.isLogin();
	}

	@Override
	public void reset() {
		
	}

}
