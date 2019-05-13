package com.yayo.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	public NamedThreadFactory(ThreadGroup group, String name) {
		this.group = group;
		this.namePrefix = (group.getName() + ":" + name);
	}

	public Thread newThread(Runnable r) {
		return new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
	}
}