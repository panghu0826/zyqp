package com.buding.common.schedule;

public interface WorkerListener<T extends Job> {
	public void workerStop(Worker<T> worker);
	public void workerBusy(Worker<T> worker);
	public void workerFree(Worker<T> worker);
}
