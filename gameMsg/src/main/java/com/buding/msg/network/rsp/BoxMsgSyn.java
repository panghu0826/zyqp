package com.buding.msg.network.rsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoxMsgSyn {
	public long msgId;
	public String msgType;
	public boolean read;
	public boolean popup;
	public int priority;
	public List<Integer> pos;
	public long startTime;
	public long stopTime;
	public List<Integer> clientType = new ArrayList<Integer>();
	public String title;
	public String msg;
	public String img;
	public Map<String, String> params = new HashMap<String, String>();
}
