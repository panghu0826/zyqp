package com.buding.hall.config.task;

import java.util.List;

import com.buding.common.conf.ValRequired;
import com.buding.hall.config.ItemPkg;


/**
 * 破产救济任务配置
 * @author Administrator
 *
 */
public class BankruptTaskConf extends TaskConf {
	//普通玩家每日可领取次数
	@ValRequired
	public Integer repeatCount = 3;
	
	//蓝钻用户每日可领取的次数
	@ValRequired
	public Integer blueVipRepeatCount = 10;
	
	//奖励的道具
	@ValRequired
	public List<ItemPkg> bludVipAwardItems;
}
