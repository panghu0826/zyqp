package com.buding.hall.module.server.facade.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.stereotype.Component;

import com.buding.common.server.facade.ContainerFacade;
import com.buding.db.model.User;
import com.buding.hall.module.server.listener.Listener;
import com.buding.hall.module.server.listener.LoginListener;
import com.buding.hall.module.server.listener.LogoutListener;
import com.buding.hall.module.user.service.UserService;

/**
 * 保存者接口 
 * 
 * @author vince
 */
@Component
public class HallContainerFacade implements ApplicationListener<ApplicationContextEvent>, InitializingBean, ContainerFacade<User> {
	@Autowired
	UserService userService;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	private final Logger LOGGER = LogManager.getLogger(getClass());
	
	/** 监听器列表 */
	private static final Map<Class<?>, List<?>> LISTENER_MAP = new HashMap<Class<?>, List<?>>(1);
	@SuppressWarnings("unchecked")
	private static final Class<? extends Listener>[] LISTENERS = new Class[]{ LoginListener.class, LogoutListener.class };
	
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		for (Class<? extends Listener> clazz : LISTENERS) {
			Map<String, ? extends Listener> listenerMap = applicationContext.getBeansOfType(clazz);
			if(listenerMap != null && !listenerMap.isEmpty()) {
				LISTENER_MAP.put(clazz, new ArrayList<Listener>(listenerMap.values()));
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (Class<? extends Listener> clazz : LISTENERS) {
			Map<String, ? extends Listener> listenerMap = applicationContext.getBeansOfType(clazz);
			if(listenerMap != null && !listenerMap.isEmpty()) {
				LISTENER_MAP.put(clazz, new ArrayList<Listener>(listenerMap.values()));
			}
		}
	}

	/**
	 * 角色登出更新事件接口
	 * 
	 * @param playerId		角色ID
	 * @param remoteIp		远程IP
	 */
	@Override
	public void onLogoutUpdateListener(User user, String remoteIp) {
		
	}
	
	/**
	 * 登录事件监听器
	 * 
	 * @param userDomain	用户域模型
	 * @param branching		分线号
	 */
	@Override
	public void onLoginListener(User user) {
		List<LoginListener> loginListeners = getListener(LoginListener.class); 
		if(loginListeners == null || loginListeners.isEmpty()) {
			return;
		}
		
		for (LoginListener listener : loginListeners) {
			try {
				listener.onLoginEvent(user);
			} catch (Exception e) {
				LOGGER.error("角色:[{}] 处理登录信息异常:", user.getId());
				LOGGER.error("{}", e);
			}
		}
	}

	/**
	 * 获得事件监听器
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> getListener(Class<T> clazz) {
		return (List<T>)LISTENER_MAP.get(clazz);
	}	
}
