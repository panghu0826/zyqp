package com.buding.hall.config;

import java.util.List;
import java.util.Map;

import com.buding.common.conf.ValRequired;

public class BoxMsgConfig {
	//消息类型
	@ValRequired
	public String msgType;
	
	//强制弹出时场景要求
	@ValRequired
	public List<Integer> pos;
	
	//是否强制弹出
	@ValRequired
	public boolean popup;
	
	//强制弹出时优先级
	@ValRequired
	public int priority;
	
	//有效期. 未读或未领取时的有效期, 已读已领取的第二天凌晨删除
	@ValRequired
	public int lifeTime;
	
	//客户端平台要求
	@ValRequired
	public List<Integer> clientType;
	
	//消息标题
	@ValRequired
	public String title;
	
	//图片
	@ValRequired
	public String img;
	
	//消息内容
	@ValRequired
	public String contentTpl;
	
	public Map<String, String> params;
}
