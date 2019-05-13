package com.buding.common.schedule;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class DelayJob extends Job implements Delayed {
	private long submitTime;

	public DelayJob(int priority, long workTime, TimeUnit timeUnit) {
		this.submitTime = TimeUnit.NANOSECONDS.convert(workTime, timeUnit) + System.nanoTime();
	}

	@Override
	public int compareTo(Delayed o) {
		if (o == null || !(o instanceof DelayJob))
			return 1;
		if (o == this)
			return 0;
		DelayJob s = (DelayJob) o;
		
		if (this.submitTime > s.submitTime) {
			return 1;
		} else if (this.submitTime == s.submitTime) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(submitTime - System.nanoTime(),  TimeUnit.NANOSECONDS);
	}
}
