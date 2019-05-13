package com.buding.hall.module.item.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.common.result.Result;
import com.buding.db.model.UserItemLog;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.item.processor.ItemContext;
import com.buding.hall.module.item.type.ItemType;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.service.UserService;

@Component
public class CoinItemProcessor extends BaseItemProcessor {
	@Autowired
	UserDao userDao;
	
	@Autowired
	UserService userService;
	
	@Override
	public int getItemId() {
		return ItemType.COIN;
	}

	@Override
	public Result use(ItemContext ctx) {
		return Result.fail("金币道具在购买时会自动使用");
	}

	@Override
	public Result add(ItemContext ctx) {
		
		ItemPkg item = ctx.item;
		int change = item.count*item.baseConf.getArgument();
		
		Result ret = userService.changeCoin(ctx.userId, change, false, ctx.reason);
		if(ret.isFail()) {
			return ret;
		}		
		
		UserItemLog log = logItemAdd(ctx);
		logItemAutoUse(log, null, change);
		
		return Result.success();
	}

}
