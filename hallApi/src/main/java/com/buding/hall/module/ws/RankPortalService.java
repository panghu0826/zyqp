package com.buding.hall.module.ws;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface RankPortalService {
	// 停止服务
	public void stopService(String instanceId);

	// 恢复服务
	public void startService(String instanceId);
	
	public void closeSocket(int playerId);
}
