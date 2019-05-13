package com.buding.hall.network;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.common.network.session.SessionManager;
import com.buding.hall.module.user.service.UserService;

@Component
public class HallSessionManager extends SessionManager<HallSession> {
	
	@Autowired
	UserService userService;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
//		System.out.println("aaaaa");
	}
	
	@Override
	public boolean cleanSession(HallSession session) {
		userService.onUserLogout(session.userId);
		return super.cleanSession(session);
	}
}
