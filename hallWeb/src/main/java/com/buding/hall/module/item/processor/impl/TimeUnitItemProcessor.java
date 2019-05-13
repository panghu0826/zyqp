package com.buding.hall.module.item.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.result.Result;
import com.buding.hall.module.item.dao.ItemDao;
import com.buding.hall.module.item.processor.ItemContext;
import com.buding.hall.module.item.service.ItemService;
import com.buding.hall.module.user.dao.UserDao;

/**
 * 以时间为单位的道具
 * @author Administrator
 *
 */
public abstract class TimeUnitItemProcessor extends BaseItemProcessor {
	@Autowired
	UserDao userDao;
	
	@Autowired
	ItemService itemService;
	
	@Autowired
	ItemDao itemDao;


	@Override
	public Result use(ItemContext ctx) {
		return Result.fail("以时间为单位的道具在购买时会自动使用");
	}
}
