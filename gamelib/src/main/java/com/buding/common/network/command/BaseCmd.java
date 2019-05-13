package com.buding.common.network.command;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public abstract class BaseCmd<KEY, DATA> implements Cmd<KEY, DATA>, InitializingBean {	
	@Override
	public void afterPropertiesSet() throws Exception {
		getCmdMapper().register(this);
	}
	
	public abstract CmdMapper<KEY,DATA> getCmdMapper();
	
}
