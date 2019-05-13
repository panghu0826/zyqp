package com.buding.hall.module.item.service.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.result.Result;
import com.buding.db.model.User;
import com.buding.db.model.UserItem;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.item.dao.ItemDao;
import com.buding.hall.module.item.processor.ItemContext;
import com.buding.hall.module.item.processor.ItemProcessorManager;
import com.buding.hall.module.item.service.ItemService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.item.type.ItemType;
import com.buding.hall.module.user.service.UserService;

/**
 * 道具服务处理类,处理道具的新增和使用
 * @author Administrator
 *
 */
public class ItemServiceImpl implements ItemService {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	ItemDao itemDao;

	@Autowired
	ItemProcessorManager itemProcessorManager;

	@Autowired
	UserService userService;
	
	AtomicLong addItemVersion = new AtomicLong(System.currentTimeMillis());
	AtomicLong useItemVersion = new AtomicLong(System.currentTimeMillis());
	
	/**
	 * 增加道具
	 * @param item
	 * @param reason
	 * @return
	 */
	public Result addItem(int userId, ItemChangeReason reason, String msg, List<ItemPkg> items) {
		String batchNum = System.currentTimeMillis()+"";
		for(ItemPkg item : items) {
			Result res = itemProcessorManager.addItem(ItemContext.valueOf(userId, item, reason, msg, batchNum));
			
			if(res.isFail()) {
				logger.error("AddItemError, Msg:{}, userId:{}, itemId:{}, reason:{}, result:{}", msg, userId, item.baseConf.getItemId(), reason, res.msg);
				return res;
			}
		}
				
		return Result.success();
	}
	
	/**
	 * 手动使用道具
	 * @param userId
	 * @param itemType
	 * @param count
	 * @param reason 在xxx场使用道具
	 * @return
	 */
	public Result useItem(int userId, int itemType, int count, ItemChangeReason reason, String msg) {
		if(count <= 0) {
			return Result.fail("使用数量不正确");
		}
		
		switch (itemType) {
		case ItemType.COIN:
		case ItemType.FANGKA:
			return usePropsItem(userId, itemType, count, reason, msg);

		default:
			break;
		}
		
		UserItem item = itemDao.getUserItem(userId, itemType);
		if(item == null) {
			return Result.fail("道具不足,请先购买");
		}
				
		if(item.getItemCount()== null || item.getItemCount() < count) {
			return Result.fail("道具不足,请先购买");
		}
		
		logItemUse(userId, itemType, count, reason, msg);
		
		item.setItemCount(item.getItemCount() - count);
		
		itemDao.updateUserItem(item);
				
		return Result.success();
	}
	
	private Result usePropsItem(int userId, int itemType, int count, ItemChangeReason reason, String msg) {
		User user = userService.getUser(userId);
		if(user == null) {
			return Result.fail("用户不存在");
		}
		
		switch (itemType) {
		case ItemType.FANGKA:
		{
			if(user.getFanka() == null || count > user.getFanka()) {
				return Result.fail("房卡数量不足");
			}
			user.setFanka(user.getFanka() - count);
		}
			break;
		case ItemType.COIN:
		{
			if(user.getCoin() == null || count > user.getCoin()) {
				return Result.fail("金币数量不足");
			}
			user.setCoin(user.getCoin() - count);
		}
		break;
		default:
		{
			return Result.fail("无法使用道具");
		}
		}
				
		logItemUse(userId, itemType, count, reason, msg);
		
		userService.updateUser(user);
				
		return Result.success();
	}
	
	/**
	 * 记录道具变动情况到日志,方便问题定位
	 * @param userId
	 * @param itemType
	 * @param count
	 * @param msg
	 */
	private void logItemUse(int userId, int itemType, int count, ItemChangeReason reason, String msg) {
		logger.info("userId={};itemType={};count={};type=use;reason={};msg={}", userId, itemType, count, reason, msg);
	}
	
	/**
	 * 记录道具变动情况到日志,方便问题定位
	 * @param userId
	 * @param itemType
	 * @param count
	 * @param msg
	 */
	private void logItemAdd(int userId, int itemType, int count, ItemChangeReason reason, String msg) {
		logger.info("userId={};itemType={};count={};type=add;reason={};msg={}", userId, itemType, count, reason, msg);
	}
	
	public UserItem getUserItem(int userId, int itemType) {
		return itemDao.getUserItem(userId, itemType);
	}
}
