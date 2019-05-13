package com.buding.common.network.session;

public interface SessionListener<T extends BaseSession> {
	public void sessionInvalided(T session);
}
