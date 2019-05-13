package com.buding.retry;

import com.buding.test.Player;

public class GameAuthAction extends RetryAction {
	Player player = null;

	public GameAuthAction(Player player) {
		super("游戏认证");
		this.player = player;
	}

	@Override
	public void doAct() {
		player.authGame();
	}

	@Override
	public boolean isDone() {
		return player.isGameAuth();
	}

	@Override
	public void reset() {
		
	}

}
