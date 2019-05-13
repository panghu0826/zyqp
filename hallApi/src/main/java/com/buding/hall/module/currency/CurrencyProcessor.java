package com.buding.hall.module.currency;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface CurrencyProcessor {
	//检查货币是否充足
	public Result checkEnough(int userId, int amount);
	//添加货币
	public Result add(int userId, int amount, String changeReason);
	//扣除货币
	public Result sub(int userId, int amount, String changeReason);
	//改变货币
	public Result change(int userId, int amount, String changeReason);
	//获取货币
	public TResult<Integer> get(int userId);
	//获取货币类型
	public int getCurrentType();
}
