package com.buding.rank.network;

import com.buding.common.network.session.SessionManager;
import com.buding.hall.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RankSessionManager extends SessionManager<RankSession> {
	
	@Autowired
	UserService userService;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
//		System.out.println("aaaaa");
	}
	
	@Override
	public boolean cleanSession(RankSession session) {
		userService.onUserLogout(session.userId);
		return super.cleanSession(session);
	}
}
