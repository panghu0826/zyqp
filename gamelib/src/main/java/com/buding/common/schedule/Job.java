package com.buding.common.schedule;

public abstract class Job implements Runnable {
	public transient boolean stop = false;

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
}
