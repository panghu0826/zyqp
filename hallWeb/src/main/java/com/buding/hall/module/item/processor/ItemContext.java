package com.buding.hall.module.item.processor;

import com.buding.db.model.UserItem;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.item.type.ItemChangeReason;

public class ItemContext {
	public int userId;
	public ItemPkg item;
	public ItemChangeReason reason;
	public String msg;
	public String batchNum;
	
	public UserItem useItem;
	
	public static ItemContext valueOf(int userId, ItemPkg item, ItemChangeReason reason, String msg, String batchNum) {
		ItemContext ctx = new ItemContext();
		ctx.userId = userId;
		ctx.item = item;
		ctx.reason = reason;
		ctx.msg = msg;
		ctx.batchNum = batchNum;
		return ctx;
	}
}
