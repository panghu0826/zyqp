package com.buding.common.admin.component;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.admin.AdminServer;

public abstract class BaseComponent implements InitializingBean {

	@Override
	public void afterPropertiesSet() throws Exception {
		adminServer.getGlobalVars().put(getComponentName(), this);
	}
	
	public abstract String getComponentName();

	@Autowired
	public AdminServer adminServer;
}
