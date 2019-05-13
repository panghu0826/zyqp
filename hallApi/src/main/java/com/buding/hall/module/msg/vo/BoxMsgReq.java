package com.buding.hall.module.msg.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BoxMsgReq implements Serializable {
	private static final long serialVersionUID = 1L;

	public int senderId;
	public String senderName;
	public int received;
	public String msgType;
	public long awardId;
	public Map<String, String> context = new HashMap<String, String>();
	public Map<String, String> params = new HashMap<String, String>();
}
