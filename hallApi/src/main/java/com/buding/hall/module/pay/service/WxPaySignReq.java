package com.buding.hall.module.pay.service;

import java.io.Serializable;

public class WxPaySignReq implements Serializable {
	private static final long serialVersionUID = -8011061374263995942L;
	
	public String orderId;
	public String itemName;
	public String attachData;
	public int totalFee;
	public String openid;
	public String orderIp;
}
