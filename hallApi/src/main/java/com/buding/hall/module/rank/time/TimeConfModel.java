package com.buding.hall.module.rank.time;

import com.buding.hall.config.RankConfig;

public class TimeConfModel {
	public RankConfig conf;
	public TimeStruct time;
	
	public TimeConfModel(RankConfig conf, TimeStruct time) {
		this.conf = conf;
		this.time = time;
	}
}
