package com.buding.hall.module.item.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.common.result.Result;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ItemPkg;
import com.google.gson.Gson;

@Component
public class ItemProcessorManager {
	private Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	private ConfigManager configManager;
	
	public Map<Integer, ItemProcessor> itemProcessMap = new HashMap<Integer, ItemProcessor>();
	
	public void register(Integer itemId, ItemProcessor processor) {
		this.itemProcessMap.put(itemId, processor);
	}
	
	public Result addItem(ItemContext ctx) {
		try {
			ItemPkg pkg = ctx.item;
			if(pkg.baseConf == null) {
				pkg.baseConf = configManager.getPropsConfigById(pkg.itemId);
			}
			
			if(pkg.baseConf == null) {
				logger.error("props not found by itemid:" + pkg.itemId);
			}
			
			ItemProcessor itemProcessor = itemProcessMap.get(ctx.item.baseConf.itemType);
			if(itemProcessor == null) {
				logger.error("itemProcessor not found by type:" + ctx.item.baseConf.itemType);
				return Result.fail();
			}
			return itemProcessor.add(ctx);
		} catch (Exception e) {
			logger.error("addItemError:" + new Gson().toJson(ctx), e);
			return Result.fail();
		}
	}
	
	public Result use(ItemContext ctx) {
		return itemProcessMap.get(ctx.useItem.getItemType()).use(ctx);
	}
}
