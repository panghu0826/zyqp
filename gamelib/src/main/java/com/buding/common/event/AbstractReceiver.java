package com.buding.common.event;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractReceiver<T extends Event<?>> implements Receiver<T> {
	protected final Logger LOGGER = LogManager.getLogger(getClass());

	@Autowired
	protected EventBus eventBus;

	@PostConstruct
	private void init() {
		for (String name : getEventNames())
			this.eventBus.register(name, this);
	}

	public abstract String[] getEventNames();

	public void onEvent(T event) {
		Object content = event.getBody();
		doEvent((T)content);
	}

	public abstract void doEvent(T paramT);
}