package com.buding.hall.config;

import java.io.Serializable;

import com.buding.common.conf.ValRequired;

public class PropsConfig implements Serializable {
	private static final long serialVersionUID = -8011061374263995942L;
	
	/**
	 * 道具id
	 */
	@ValRequired
	public String itemId;
	
	/**
	 * 道具类型
	 * @see ${ItemType}
	 */
	@ValRequired
	public int itemType;
	
	/**
	 * 道具名称
	 */
	@ValRequired
	public String itemName;
	
	/**
	 * 道具描述
	 */
	@ValRequired
	public String description;
	
	/**
	 * 参数,使用生效时间：秒，兑换属性格式：个
	 */
	@ValRequired
	public int argument;
	
	/**
	 * 有效期是否可以累加
	 */
	@ValRequired
	public int accumulate;
	
	/**
	 * 自动使用
	 */
	@ValRequired
	public int autoUse;
	
	
	/**
	 * 生效延迟时间,精确到秒
	 */
	@ValRequired
	public int effectiveTime;
	

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public int getItemType() {
		return itemType;
	}

	public void setItemType(int itemType) {
		this.itemType = itemType;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getAccumulate() {
		return accumulate;
	}

	public void setAccumulate(int accumulate) {
		this.accumulate = accumulate;
	}

	public int getAutoUse() {
		return autoUse;
	}

	public void setAutoUse(int autoUse) {
		this.autoUse = autoUse;
	}
	
	public int getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(int effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public int getArgument() {
		return argument;
	}

	public void setArgument(int argument) {
		this.argument = argument;
	}
	
}
