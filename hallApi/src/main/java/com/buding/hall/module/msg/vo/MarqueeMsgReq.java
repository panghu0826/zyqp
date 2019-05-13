package com.buding.hall.module.msg.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MarqueeMsgReq implements Serializable {
	private static final long serialVersionUID = 1L;

	public int senderId;
	public String senderName;
	public int receiver;
	public String msgType;
	public Map<String, String> context = new HashMap<String, String>();
}
