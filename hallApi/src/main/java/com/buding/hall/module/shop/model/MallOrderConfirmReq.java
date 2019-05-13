package com.buding.hall.module.shop.model;

public class MallOrderConfirmReq extends BaseOrderReq {
	public String order_number;
	public String trade_number;
	public String result;

	public String getOrder_number() {
		return order_number;
	}

	public void setOrder_number(String order_number) {
		this.order_number = order_number;
	}

	public String getTrade_number() {
		return trade_number;
	}

	public void setTrade_number(String trade_number) {
		this.trade_number = trade_number;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}
