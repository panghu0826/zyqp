package com.buding.common.event;

public interface Receiver<T extends Event<?>> {
	public void onEvent(T paramEvent) throws Exception;
	public String getEventName();
}