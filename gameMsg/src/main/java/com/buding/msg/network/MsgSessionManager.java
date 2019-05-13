package com.buding.msg.network;

import org.springframework.stereotype.Component;

import com.buding.common.network.session.SessionManager;

@Component
public class MsgSessionManager extends SessionManager<MsgSession> {

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
//		System.out.println("aaaaa");
	}
}
