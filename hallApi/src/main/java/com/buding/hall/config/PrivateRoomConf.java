package com.buding.hall.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateRoomConf {
	public String id;
	public String gameTypeName;	
	public String roomName;
	public int basePoint;
	public int startType;
	public int currencyType; //1 金币 2 元宝 3粽子
	
	public List<PrivateDeskConf> deskConf;
	
	public Map<Integer, PrivateDeskConf> deskConfMap = new HashMap<Integer, PrivateDeskConf>();
}
