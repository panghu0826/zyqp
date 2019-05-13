package com.buding.hall.config;

public class ItemPkg {
	public String itemId;
	public int count;
	
	public transient PropsConfig baseConf;

	@Override
	public String toString() {
		return "ItemPkg{" +
				"itemId='" + itemId + '\'' +
				", count=" + count +
				", baseConf=" + baseConf +
				'}';
	}
}
