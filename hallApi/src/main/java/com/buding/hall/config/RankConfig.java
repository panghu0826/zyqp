package com.buding.hall.config;

import com.buding.common.conf.ValRequired;

public class RankConfig {
	@ValRequired
	public String note;
	
	@ValRequired
	public String id;
	
	@ValRequired
	public String name;
	
	@ValRequired
	public int pointType;
	
	@ValRequired
	public int circleType;

	@ValRequired
	public int rankLimit = 200; //只显示前多少名
}
