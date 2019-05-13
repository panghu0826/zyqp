package com.buding.hall.config;

import java.util.List;

import com.buding.common.conf.ValRequired;

public class ProductConfig {
	@ValRequired
	public String id;
	
	@ValRequired
	public String name;
	
	@ValRequired
	public int category;
	
	@ValRequired
	public String img;
		
	@ValRequired
	public String desc;
	
	@ValRequired
	public int cItemCount; //客户端展示的道具数量
	
	@ValRequired
	public int status; //1 上架
	
	@ValRequired
	public PriceConf price;
	
	@ValRequired
	public List<ItemPkg> items;
}