package com.buding.hall.module.msg.vo;

import java.util.Map;

import com.buding.db.model.Msg;

public class BoxMsg extends BaseMsg implements Cloneable {	
	 private static final long serialVersionUID = 1L;
	 
	public boolean read;
	public boolean popup;
	public String title;
	public String img;
	public Map<String, String> params;
	public long awardId;
	public int awardType;
	public int attachNum;
	public int itemCount;
	public boolean received;
	public Msg rawMsg;
	
	public BoxMsg copy() throws Exception {
		return (BoxMsg)this.clone();
	}
}