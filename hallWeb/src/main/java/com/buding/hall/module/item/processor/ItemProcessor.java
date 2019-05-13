package com.buding.hall.module.item.processor;

import com.buding.common.result.Result;


/**
 * 道具处理器,每种道具的销毁方式不一样,比如:
 * 1.娱乐道具，简单减少数量.
 * 2.金币包，转换成等额的金币:改变用户货币
 * 3.幸运道具、记牌器:增加道具作用时间,即修改用户属性
 * 4.话费券:增加兑换记录然后活动人员跟进充话费
 * 5.蓝钻/红钻资格:修改用户属性变为蓝钻红钻用户
 * @author Administrator
 *
 */
public interface ItemProcessor {
	/**
	 * 使用
	 * @param ctx
	 */
	public Result use(ItemContext ctx);
	
	/**
	 * 增加
	 * @param item
	 */
	public Result add(ItemContext ctx);
}
