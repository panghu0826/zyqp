package com.buding.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RetryAction {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private int retryCount = 10;
	private int detectedCount = 0;
	protected boolean doing = false;
	protected long startTime = 0;
	protected long endTime = 0;
	private String name;

	public RetryAction(String name) {
		this.name = name;
	}

	public RetryResult act() {
		if (isDone()) {
//			logger.info(name + "   完成  ");
			if(endTime == 0) {
				endTime = System.currentTimeMillis();
				logger.info(name + " 耗时  " + (endTime - startTime));
			}
			return RetryResult.DONE;
		}
		if (isDoing()) {			
			detectedCount++;
			if (detectedCount >= retryCount) {
				logger.info(name + "   失败，放弃!!!!!  ");
				return RetryResult.ABORT;
			}
			logger.info(name + "   进行中  ");
			return RetryResult.DOING;
		}

		logger.info(name + "   开始执行  ");
		startTime = System.currentTimeMillis();
		doAct();
		doing = true;
		return RetryResult.DOING;
	}

	public boolean isDoing() {
		return doing;
	}

	public abstract void doAct();
	
	public abstract boolean isDone();

	public abstract void reset();
}
