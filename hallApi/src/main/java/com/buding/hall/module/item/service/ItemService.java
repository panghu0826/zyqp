package com.buding.hall.module.item.service;

import java.util.List;

import com.buding.common.result.Result;
import com.buding.db.model.UserItem;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.item.type.ItemChangeReason;

/**
 * 道具服务处理类,处理道具的新增和使用
 * @author Administrator
 *
 */
public interface ItemService {
	
	
	/**
	 * 增加道具
	 * @param item
	 * @param reason
	 * @return
	 */
	public Result addItem(int userId, ItemChangeReason reason, String msg, List<ItemPkg> item);
	
	/**
	 * 手动使用道具
	 * @param userId
	 * @param itemType
	 * @param count
	 * @param reason 在xxx场使用道具
	 * @return
	 */
	public Result useItem(int userId, int itemType, int count, ItemChangeReason reason, String msg);
	
	/**
	 * 获取用户道具
	 * @param userId
	 * @param itemType
	 * @return
	 */
	public UserItem getUserItem(int userId, int itemType);
}
