package com.buding.hall.module.msg.vo;

import java.util.List;

import com.buding.db.model.ActNotice;

public class ActNoticeMsg extends BaseMsg implements Cloneable {	
	 private static final long serialVersionUID = 1L;
	 
	public List<ActNotice> actList = null;
	public ActNotice notice = null;
	
	public ActNoticeMsg copy() throws Exception {
		return (ActNoticeMsg)this.clone();
	}
}
