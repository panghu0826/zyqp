package com.buding.hall.config.task;

import com.buding.common.conf.ValRequired;


/**
 * 打满x局任务配置
 * @author Administrator
 *
 */
public class GamePlayedTaskConf extends TaskConf {
	@ValRequired
	public Integer initPlayedCount;
	
	@ValRequired
	public Integer maxPlayedCount;
	
	@ValRequired
	public Integer stepPlayedCount;
	
	public String gameId;
}
