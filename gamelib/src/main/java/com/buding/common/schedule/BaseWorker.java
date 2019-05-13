package com.buding.common.schedule;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class BaseWorker implements Worker<DelayJob> {
	private Logger logger = LogManager.getLogger(getClass());

	DelayQueue<DelayJob> delayQueue = new DelayQueue<DelayJob>();
	ConcurrentMap<String, Boss> bossMap = new ConcurrentHashMap<String, Boss>();

	WorkerListener<DelayJob> listener;
	volatile boolean stop = false;

	long id;
	int capacity = 1;

	public BaseWorker(int capacity, long id) {
		this.capacity = capacity;
		this.id = id;
	}

	@Override
	public void freeFrom(Boss boss) {
		bossMap.remove(boss.getBossId());
	}

	@Override
	public void followBoss(Boss boss) {
		bossMap.put(boss.getBossId(), boss);
	}

	@Override
	public void run() {
		while(!stop) {
			try {
				Job job = delayQueue.take();

				if(job.isStop()) {
					continue;
				}
				job.run();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		this.stop();
	}

	@Override
	public boolean isBusy() {
		return bossMap.size() >= capacity;
	}

	@Override
	public void submitJob(DelayJob job) {
		try {
			if(stop || isBusy()) {
				throw new IllegalStateException(String.format("Worker is stop or isBusy, stop:%b, isBusy:%b", stop, isBusy()));
			}
			delayQueue.put(job);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		this.stop = true;
		if(listener != null) {
			listener.workerStop(this);
		}
	}

	@Override
	public void setWorkerListener(WorkerListener<DelayJob> listener) {
		this.listener = listener;
	}

	@Override
	public long getId() {
		return id;
	}
}
