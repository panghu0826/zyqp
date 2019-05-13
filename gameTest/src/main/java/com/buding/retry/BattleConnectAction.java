package com.buding.retry;

import com.buding.test.Player;

public class BattleConnectAction extends RetryAction {
	Player player = null;

	public BattleConnectAction(Player player) {
		super("游戏服初始化");
		this.player = player;
	}

	@Override
	public void doAct() {
		player.initGame();
	}

	@Override
	public boolean isDone() {
		return player.isGameInit();
	}

	@Override
	public void reset() {
		
	}

}
