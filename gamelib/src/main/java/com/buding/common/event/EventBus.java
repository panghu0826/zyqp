package com.buding.common.event;


public abstract interface EventBus {
	
	/**
	 * 提交事件 
	 * @param paramEvent
	 */
	public abstract void post(Event<?> paramEvent);

	/**
	 * 提交事件
	 * @param paramEvent
	 * @param paramLong
	 * @return
	 */
//	public abstract ScheduledFuture<?> post(Event<?> paramEvent, long paramLong);

	/**
	 * 注册事件
	 * @param paramString
	 * @param paramReceiver
	 */
	public abstract void register(String paramString, Receiver<?> paramReceiver);

	/**
	 * 注销事件
	 * @param paramString
	 * @param paramReceiver
	 */
	public abstract void unregister(String paramString, Receiver<?> paramReceiver);
}