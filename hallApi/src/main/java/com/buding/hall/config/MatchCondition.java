package com.buding.hall.config;

import java.util.HashMap;
import java.util.Map;

public class MatchCondition {
	public String comment;
	public int version;
	public EnterCondition enterCondition;//入场条件
	public RoomConfig[] roomArray;
	public DeskConfig deskConf;
	
	public Map<String, RoomConfig> roomConf = new HashMap<String, RoomConfig>();
}
