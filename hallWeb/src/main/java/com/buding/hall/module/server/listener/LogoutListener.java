package com.buding.hall.module.server.listener;

import com.buding.db.model.User;

/**
 * 角色登出保存事件
 * 
 * @author vince
 */
public interface LogoutListener extends Listener {
	
	/**
	 * 角色登出保存数据接口
	 * 
	 * @param user	角色ID
	 */
	void onLogoutEvent(User user);
	
}
