package com.buding.hall.module.item.processor.impl;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.db.model.UserItemLog;
import com.buding.hall.module.item.processor.ItemContext;
import com.buding.hall.module.item.processor.ItemProcessor;
import com.buding.hall.module.item.processor.ItemProcessorManager;
import com.google.gson.Gson;


public abstract class BaseItemProcessor implements ItemProcessor, InitializingBean {
	protected Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	ItemProcessorManager itemManager;

	@Override
	public void afterPropertiesSet() throws Exception {
		itemManager.register(getItemId(), this);
	}
	
	public abstract int getItemId();
	
	protected UserItemLog logItemAdd(ItemContext ctx) {
		UserItemLog log = genUserItemAddLog(ctx);
		logItemChanged(log);
		return log;
	}
	
	protected void logItemAutoUse(UserItemLog log, Date effectEndTime, int propsCount) {
		log.setEffectEndTime(effectEndTime);
		log.setPropsCount(propsCount);
		log.setIsAdd(false);
		log.setChangeReason("自动使用");
		logItemChanged(log);
	}
	
	protected UserItemLog genUserItemAddLog(ItemContext ctx) {
		UserItemLog ui = new UserItemLog();
		ui.setBatch(ctx.batchNum);
		ui.setChangeReason(ctx.reason.toString() + ":" + ctx.msg);
		ui.setIsAdd(true);
		ui.setItemCount(ctx.item.count);
		ui.setItemId(ctx.item.baseConf.getItemId());
		ui.setItemInsId(-1L);
		ui.setItemName(ctx.item.baseConf.getItemName());
		ui.setLogTime(new Date());
		ui.setUserId(ctx.userId);
		return ui;
	}
	
	protected void logItemChanged(UserItemLog log) {
		logger.info(new Gson().toJson(log));
	}
}
