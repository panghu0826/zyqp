package com.buding.hall.module.server.listener;

import com.buding.db.model.User;

/**
 * 登录事件监听器
 * 
 * @author vince
 */
public interface LoginListener extends Listener {

	/**
	 * 登录事件接口. 所有使用者都调用同一个接口
	 * 
	 * @param user		用户域模型对象
	 */
	void onLoginEvent(User user);
}
