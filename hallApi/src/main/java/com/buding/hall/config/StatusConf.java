package com.buding.hall.config;

import java.util.List;

import com.buding.common.conf.ValRequired;

public class StatusConf {
	@ValRequired
	public String gameId;
	
	@ValRequired
	public List<StatusItemConf> rankNum;
	
	public void init() {
		for(StatusItemConf item : rankNum) {
			item.init();
		}
	}
}
