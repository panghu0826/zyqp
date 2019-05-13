package com.buding.hall.config;

import java.util.List;

import com.buding.common.conf.ValRequired;

public class MarqueeMsgConfig {
	//消息类型
	@ValRequired
	public String msgType;
	
	//播放场景
	@ValRequired
	public List<Integer> pos;
	
	//优先级
	@ValRequired
	public int priority;
	
	//播放速度和次数
	@ValRequired
	public String playSetting;
	
	//有效期(在有效期内的登录用户都会收到该跑马灯消息)
	@ValRequired
	public int lifeTime;
	
	//平台要求
	@ValRequired
	public List<Integer> clientType;
	
	//内容
	@ValRequired
   	public String contentTpl;
}
