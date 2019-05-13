package com.buding.hall.module.shop.service;

import com.buding.common.result.Result;

/**
 * 商店服务处理类,处理购买请求
 * @author Administrator
 *
 */
public interface ShopService {
	
	/**
	 * 购买道具
	 * 
	 * @param playerId
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Result buyItem(int playerId, String id) throws Exception;

	/**
	 * 购买成功回调
	 * 
	 * @param playerId
	 * @param id
	 * @throws Exception
	 */
	public Result finishOrder(String orderNo, boolean result,String money,String payType) throws Exception;
	
	/**
	 * 生成订单号
	 * @param prefix
	 * @return
	 */
	public String genOrderId(String prefix);
	
	public String processWxPayCallback(String data) throws Exception;

	String processWxPayCallbackTest(String strInfo) throws Exception;

	public String processAliPayCallback(String data) throws Exception;
}
