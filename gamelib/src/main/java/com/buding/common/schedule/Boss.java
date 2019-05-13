package com.buding.common.schedule;

public interface Boss {
	public String getBossId();
	public Worker<? extends Job> getWorker();
	public Worker<? extends Job> setWorker(Worker<? extends Job> worker);
}
