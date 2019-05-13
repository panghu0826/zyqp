package com.buding.hall.module.msg.vo;

import com.buding.hall.module.msg.constant.MarqueeType;

public class MarqueeMsg extends BaseMsg implements Cloneable {	
	 private static final long serialVersionUID = 1L;
	 
	public String playSetting;
	
	public int marqueeType = MarqueeType.MARQUEE_TYPE_PUSH_ONCE;
	public int loopPushCount = 1;
	public int loopPushInterval = -1;
	public int loopPlayCount = 1;
	public int pushedCount = 0;
	public long lastPushTime = 0;
	public int userGroup = 0;
	public boolean pushOnLogin = true;
	public int level = 0;

	public MarqueeMsg copy() throws Exception {
		return (MarqueeMsg)this.clone();
	}
}
