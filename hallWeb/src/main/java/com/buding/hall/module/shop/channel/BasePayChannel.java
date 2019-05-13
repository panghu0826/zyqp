package com.buding.hall.module.shop.channel;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BasePayChannel implements PayChannel, InitializingBean {
	protected Logger logger = LogManager.getLogger(getClass());

	@Autowired
	ChannelRepostory channelRepostory;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		channelRepostory.register(this);
	}
}
