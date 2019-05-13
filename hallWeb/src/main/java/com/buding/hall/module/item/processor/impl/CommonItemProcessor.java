package com.buding.hall.module.item.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.result.Result;
import com.buding.db.model.UserItem;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.item.dao.ItemDao;
import com.buding.hall.module.item.processor.ItemContext;
import com.buding.hall.module.item.service.impl.ItemServiceImpl;
import com.buding.hall.module.user.dao.UserDao;

/**
 * 
 * 普通的可累积非自动使用的道具:根据itemType进行累积.
 *
 */
public abstract class CommonItemProcessor extends BaseItemProcessor {
	@Autowired
	UserDao userDao;
	
	@Autowired
	ItemServiceImpl itemService;
	
	@Autowired
	ItemDao itemDao;

	@Override
	public Result use(ItemContext ctx) {
		UserItem u = itemService.getUserItem(ctx.useItem.getUserId(), ctx.useItem.getItemType());
		if(u == null) {
			return Result.fail("道具不存在");
		}
		if(u.getItemCount() < ctx.useItem.getItemCount()) {
			return Result.fail("道具数量不足");
		}
		u.setItemCount(u.getItemCount() - ctx.useItem.getItemCount());
		itemDao.updateUserItem(u);
		return Result.success();
	}
	
	@Override
	public Result add(ItemContext ctx) {
		ItemPkg item = ctx.item;
		UserItem u = itemService.getUserItem(ctx.userId, item.baseConf.getItemType());
		int change = item.count*item.baseConf.getArgument();
		if(u == null) {
			u = new UserItem();
			u.setUserId(ctx.userId);
			u.setItemCount(change);
			u.setItemType(item.baseConf.getItemType());
			itemDao.insert(u);
		} else {
			u.setItemCount(change + u.getItemCount());
			itemDao.updateUserItem(u);
		}
		
		logItemAdd(ctx);
		
		return Result.success();
	}
}
