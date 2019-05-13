package com.buding.hall.config;

import java.util.List;

import com.buding.common.conf.ValRequired;

public class ExchangeConfig {
	@ValRequired
	public String id;// DH001,
	
	@ValRequired
	public String name;// 5000金币,
	
	@ValRequired
	public String img;// red_diamond,
	
	@ValRequired
	public long startTime;//11111,
	
	@ValRequired
	public long stopTime; //1111,
	
	@ValRequired
	public int totalBalance; //123,
	
	@ValRequired
	public int dayBalance; //12,
	
	@ValRequired
	public List<Integer> clientType;// [1,2,3,4],
	
	@ValRequired
	public int price;// 12,
	
	@ValRequired
	public int status;// 1,
	
	@ValRequired
	public int currency;// 1, //1 Gold
	
	@ValRequired
	public int type;// 1, //1 虚拟物品 2：话费流量 3 其它实物
	
	@ValRequired
	public String msgId;
		
	public boolean autoAddVirtualItem = true; //虚拟物品自动发放
	
	@ValRequired
	public List<ItemPkg> items;
}
