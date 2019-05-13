package com.buding.common.server.facade;



/**
 * 服务器容器Facade接口
 * 
 * @author Hyint
 */
public interface ContainerFacade<K> {

	/**
	 * 角色登出更新事件接口
	 * 
	 * @param  playerId		角色ID
	 * @param  remoteIp		远程IP
	 */
	void onLogoutUpdateListener(K playerId, String remoteIp);
	
	/**
	 * 登录事件监听器
	 * 
	 * @param user	用户域模型
	 */
	void onLoginListener(K playerId);
}
