package com.buding.common.schedule;

public interface Worker<T extends Job> extends Runnable {
	/**
	 * 是否有空
	 * @return
	 */
	public boolean isBusy();
	
	/**
	 * 提交任务
	 * @param job
	 */
	public void submitJob(T job);
	
	/**
	 * 停止干活
	 */
	public void stop();
	
	/**
	 * 设置监听器
	 * @param listener
	 */
	public void setWorkerListener(WorkerListener<T> listener);
	
	/**
	 * 获取工作者id
	 * @return
	 */
	public long getId();
	
	public void followBoss(Boss boss);
	
	public void freeFrom(Boss boss);
}
