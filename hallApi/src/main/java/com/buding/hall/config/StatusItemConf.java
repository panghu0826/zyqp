package com.buding.hall.config;

import com.buding.common.conf.ValRequired;

public class StatusItemConf {
	@ValRequired
	public String startTime;// ": "01:00:00",

	@ValRequired
	public String endTime;// ": "07:00:00",

	@ValRequired
	public int randomNumStart;// ": "12",

	@ValRequired
	public int randomNumEnd;// ": "21"
	
	public int startIntTime;
	public int endIntTime;
	
	public void init() {
		this.startIntTime = Integer.valueOf(startTime.replaceAll(":", ""));
		this.endIntTime = Integer.valueOf(endTime.replaceAll(":", ""));
	}
}
