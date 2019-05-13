package com.buding.hall.module.common.constants;

public enum SubSys {
	RANK_SYS("RANK_SYSTEM", "排行榜系统"),
	MSG_SYS("MSG_SYSTEM", "消息系统"),
	ITEM_SYS("ITEM_SYSTEM", "道具系统"),
	SHOP_SYS("SHOP_SYSTEM", "商城系统"),
	AWARD_SYS("AWARD_SYSTEM", "兑奖系统"),
	EXCHANGE_SYS("EXCHANGE_SYSTEM", "物品兑换系统"),
	TASK_SYS("TASK_SYSTEM", "任务系统"),
	GAME_SYS("GAME_SYSTEM", "游戏系统");
	
	private String ename;
	private String displayName;
	
	private SubSys(String ename, String displayName) {
		this.ename = ename;
		this.displayName = displayName;
	}

	public String getEname() {
		return ename;
	}

	public String getDisplayName() {
		return displayName;
	}	
}
