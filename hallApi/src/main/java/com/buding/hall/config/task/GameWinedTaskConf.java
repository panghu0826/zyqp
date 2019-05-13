package com.buding.hall.config.task;

import com.buding.common.conf.ValRequired;


/**
 * 累计x局任务配置
 * @author Administrator
 *
 */
public class GameWinedTaskConf extends TaskConf {
	@ValRequired
	public Integer initWinedCount;
	
	@ValRequired
	public Integer maxWinedCount;
	
	@ValRequired
	public Integer stepWinedCount;
	
	@ValRequired
	public String gameId;//赛场id
}
