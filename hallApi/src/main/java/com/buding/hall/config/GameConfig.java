package com.buding.hall.config;

import java.util.ArrayList;
import java.util.List;

import com.buding.common.conf.ValRequired;

public class GameConfig {
	@ValRequired
	public String gameId;
	
	@ValRequired
	public String gameName;
	
	public boolean isRank = false; //是否是排位赛
	
	public List<MatchConfig> matchs = new ArrayList<MatchConfig>();
}
