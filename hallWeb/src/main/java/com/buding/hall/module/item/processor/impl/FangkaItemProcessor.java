package com.buding.hall.module.item.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.common.result.Result;
import com.buding.db.model.User;
import com.buding.db.model.UserItemLog;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.item.processor.ItemContext;
import com.buding.hall.module.item.type.ItemType;
import com.buding.hall.module.user.service.UserService;

/**
 * 
 * 房卡
 *
 */
@Component
public class FangkaItemProcessor extends BaseItemProcessor {
	@Autowired
	UserService userService;

	@Override
	public Result use(ItemContext ctx) {
		return Result.fail("房卡道具在购买时会自动使用");
	}

	@Override
	public Result add(ItemContext ctx) {
		User user = userService.getUser(ctx.userId);
		if(user == null) {
			return Result.fail("UserNotFound:" + ctx.userId);
		}
		ItemPkg item = ctx.item;
		int change = item.count*item.baseConf.getArgument();
		
		Result ret = userService.changeFangka(ctx.userId, change, false, ctx.reason);
		if(ret.isFail()) {
			return ret;
		}	
		
		UserItemLog log = logItemAdd(ctx);
		logItemAutoUse(log, null, change);
		
		return Result.success();
	}
	
	@Override
	public int getItemId() {
		return ItemType.FANGKA;
	}
}
