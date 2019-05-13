package com.buding.hall.module.shop;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface OrderStatus {
	public int WAITING = 1; //等待支付
	public int PAY = 2; //已支付
	public int FAIL = 3; //失败 
	public int END = 4; //已发货
}
