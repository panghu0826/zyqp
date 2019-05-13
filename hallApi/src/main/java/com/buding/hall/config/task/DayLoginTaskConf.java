package com.buding.hall.config.task;

import java.util.List;

import com.buding.common.conf.ValRequired;
import com.buding.hall.config.ItemPkg;

/**
 * 每日登录任务配置
 * @author Administrator
 *
 */
public class DayLoginTaskConf extends TaskConf {
	@ValRequired
	public List<ItemPkg> redVipAwardItems; //奖励的道具
}
