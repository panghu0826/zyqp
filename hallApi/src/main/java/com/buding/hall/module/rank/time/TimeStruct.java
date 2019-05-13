package com.buding.hall.module.rank.time;

import java.util.Date;

import com.buding.common.util.DateUtil;

public class TimeStruct {
	public Long startIntDateTime;
	public Long endIntDateTime;
	public Long settleIntDateTime;
	public Long marqueeIntDateTime;
	public Date startDateTime;
	public Date endDateTime;
	public Date settleDateTime;
	public Date marqueeDateTime;
	
	public boolean isTimeHit(Date date) {
		long datetime = DateUtil.getYYYYMMddHHmmss(date);
		return datetime >= startIntDateTime && datetime < endIntDateTime;
	}
	
	public String toString() {
		return startIntDateTime +"->" + endIntDateTime +"->" + settleIntDateTime;
	}
}
