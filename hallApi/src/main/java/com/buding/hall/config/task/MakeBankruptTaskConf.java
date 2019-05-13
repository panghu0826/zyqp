package com.buding.hall.config.task;

import com.buding.common.conf.ValRequired;


/**
 * 使对手破产x次任务
 * @author Administrator
 *
 */
public class MakeBankruptTaskConf extends TaskConf {
	@ValRequired
	public Integer initBankruptCount;
	
	@ValRequired
	public Integer maxBankruptCount;
	
	@ValRequired
	public Integer stepBankruptCount;
	
	@ValRequired
	public String gameId;
}
