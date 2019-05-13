package com.buding.common.schedule;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.buding.common.thread.NamedThreadFactory;

public class WorkerPool implements WorkerListener<DelayJob>, InitializingBean {
	public static WorkerPool instances;

	ExecutorService threadPool = Executors.newCachedThreadPool(new NamedThreadFactory(new ThreadGroup("桌子线程组"), "工作者线程"));

	public ConcurrentMap<Long, Worker<DelayJob>> workerMap = new ConcurrentHashMap<Long, Worker<DelayJob>>();

	public AtomicLong id = new AtomicLong(1);

	public int workLoad = 1;

	public void submitJob(Job job) {
		threadPool.submit(job);
	}

	public synchronized void borrowWorker(Boss boss) {
		for (Worker<DelayJob> worker : workerMap.values()) {
			if (worker.isBusy()) {
				continue;
			}
			worker.followBoss(boss);
			boss.setWorker(worker);
			return;
		}

		Worker<DelayJob> worker = new BaseWorker(workLoad, id.getAndIncrement());
		workerMap.put(worker.getId(), worker);
		threadPool.submit(worker);
		worker.followBoss(boss);
		boss.setWorker(worker);
	}
	
	public synchronized void returnWorker(Boss boss) {
		Worker<?> worker = boss.getWorker();
		long id = worker.getId();
		if(workerMap.containsKey(id)) {
			workerMap.remove(id);
			worker.freeFrom(boss);
			boss.setWorker(null);
		}
	}

	@Override
	public void workerStop(Worker<DelayJob> worker) {
		workerMap.remove(worker.getId());
	}

	@Override
	public void workerBusy(Worker<DelayJob> worker) {

	}

	@Override
	public void workerFree(Worker<DelayJob> worker) {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instances = this;
	}
}
