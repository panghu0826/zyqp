package com.buding.msg.network.rsp;

import java.util.List;

public class MarqueeMsgSyn {
	public long msgId;
	public String msgType;
	public List<Integer> pos;
	public int priority;
	public String playSetting;
	public List<Integer> clientType;
	public String msg;
}
