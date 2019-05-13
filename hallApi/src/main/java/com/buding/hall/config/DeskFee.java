package com.buding.hall.config;

import com.buding.hall.module.common.constants.CurrencyType;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class DeskFee {
	public String itemId; //消耗道具id
	public int quanCount; //前端的圈数选择
	public PropsConfig props;
	
	/**
	 * @see CurrencyType
	 */
	public int currenceType; //销毁玩家货币类型
	public int currenceCount; //销毁玩家货币类型
	
	//扣的钻石数
	public int diamondCount;
}
