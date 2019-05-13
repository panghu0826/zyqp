package com.buding.retry;

import com.buding.test.Player;

public class EnrollAction extends RetryAction {
	Player player = null;

	public EnrollAction(Player player) {
		super("报名");
		this.player = player;
	}

	@Override
	public void doAct() {
		player.enroll(null, null);
	}

	@Override
	public boolean isDone() {
		return player.isEnroll();
	}

	@Override
	public void reset() {
		
	}

}
