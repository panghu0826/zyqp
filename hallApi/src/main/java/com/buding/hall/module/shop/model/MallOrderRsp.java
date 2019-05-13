package com.buding.hall.module.shop.model;

import com.buding.common.network.model.BaseRsp;

public class MallOrderRsp extends BaseRsp {
	public String order_number;
	public int platform; // 1pingpp 2weixin
	public String order_string;

	public String getOrder_number() {
		return order_number;
	}

	public void setOrder_number(String order_number) {
		this.order_number = order_number;
	}

	public int getPlatform() {
		return platform;
	}

	public void setPlatform(int platform) {
		this.platform = platform;
	}

	public String getOrder_string() {
		return order_string;
	}

	public void setOrder_string(String order_string) {
		this.order_string = order_string;
	}

}
