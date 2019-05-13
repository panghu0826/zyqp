package com.buding.hall.module.rank.time;

import java.util.Date;

import com.buding.hall.config.RankConfig;

/**
 * 时间解释器
 * @author Administrator
 *
 */
public interface TimeResolver {
	TimeStruct resolve(RankConfig conf, Date refer);
	boolean check(TimeStruct ts);
}
