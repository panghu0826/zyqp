package com.buding.battle.logic.network.module;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class Modules implements InitializingBean {
	public static Modules instance;
	
//	@Autowired
//	public HallModule hallModule;
//	
//	@Autowired
//	public LoginModule loginModule;
//	
//	@Autowired
//	public MatchModule matchModule;
//	
//	@Autowired
//	public GameModule gameModule;

	
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

}
