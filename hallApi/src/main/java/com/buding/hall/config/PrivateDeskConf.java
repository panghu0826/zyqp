package com.buding.hall.config;

public class PrivateDeskConf {
	public static int TYPE_DESKOWNER = 1;
	public static int TYPE_COMMON = 2;
	
	public int type; // 1, //桌主包场 2 普通场
	public int deskOwnerExpand;
	public int otherPlayerExpand;
}
