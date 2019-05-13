package com.buding.hall.config;

import com.buding.hall.module.common.constants.CurrencyType;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class PriceConf {
	public String itemId; //消耗道具id
	public int itemCount; //销毁道具数量
	public PropsConfig props;
	
	/**
	 * @see CurrencyType
	 */
	public int currenceType; //销毁玩家货币类型
	public int currenceCount; //销毁玩家货币类型
	
	//可玩游戏次数. gameCount只是一个抽象说法，具体可玩多少局有不同游戏确定
	public int gameCount;
}
