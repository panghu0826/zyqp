package com.buding.hall.module.item.processor.impl;

import com.buding.common.result.Result;
import com.buding.db.model.UserItemLog;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.item.processor.ItemContext;
import com.buding.hall.module.item.type.ItemType;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiamondItemProcessor extends BaseItemProcessor {
	@Autowired
	UserDao userDao;
	
	@Autowired
	UserService userService;
	
	@Override
	public int getItemId() {
		return ItemType.DIAMOND;
	}

	@Override
	public Result use(ItemContext ctx) {
		return Result.fail("钻石道具在购买时会自动使用");
	}

	@Override
	public Result add(ItemContext ctx) {
		
		ItemPkg item = ctx.item;
		int change = item.count*item.baseConf.getArgument();
		
		Result ret = userService.changeDiamond(ctx.userId, change, false, ctx.reason);
		if(ret.isFail()) {
			return ret;
		}		
		
		UserItemLog log = logItemAdd(ctx);
		logItemAutoUse(log, null, change);
		
		return Result.success();
	}

}
