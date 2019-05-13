package com.buding.hall.module.msg.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaseMsg implements Serializable {
	 private static final long serialVersionUID = 1L;
	 
	public int senderId;
	public String senderName;
	public int receiver;
	
	public long msgId;
	public String msgType;
	public int priority;
	public List<Integer> pos = new ArrayList<Integer>();
	public List<Integer> clientType = new ArrayList<Integer>();
	public String msg;	
	public long startTime;
	public long stopTime;
}
