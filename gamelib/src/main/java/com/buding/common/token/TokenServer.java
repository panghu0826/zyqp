package com.buding.common.token;

import com.buding.common.result.Result;


public interface TokenServer {
	/**
	 * 更新令牌
	 * @param userId
	 * @param tmp 是否是临时令牌. 临时令牌只能校验一次，并且有过期时间
	 * @return
	 */
	public String updateToken(int userId, boolean tmp);
	
	/**
	 * 验证令牌
	 * @param userId
	 * @param token
	 * @return
	 */
	public boolean verifyToken(int userId, String token);
	
	/**
	 * 验证临时令牌
	 * @param userId
	 * @param token
	 * @return
	 */
	public Result verifyTmpToken(int userId, String token);
}
