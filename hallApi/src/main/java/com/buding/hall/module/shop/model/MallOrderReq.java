package com.buding.hall.module.shop.model;

public class MallOrderReq extends BaseOrderReq {
	public String productId;
	public String remoteIp = "";

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}
}
