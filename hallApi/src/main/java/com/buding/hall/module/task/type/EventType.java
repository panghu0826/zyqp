package com.buding.hall.module.task.type;

public interface EventType {
	public static final String SHARE = "SHARE"; //分享
	public static final String LOGIN = "LOGIN"; //登录
	public static final String RATING = "RATING"; //好评
	public static final String COIN_CHANGE = "COIN_CHANGE"; //金币变动
	public static final String DIAMOND_CHANGE = "DIAMOND_CHANGE"; //金币变动
	public static final String BIND_MOBILE = "BIND_MOBILE"; //绑定手机
	public static final String PLAYED_GAME_WEEK = "PLAYED_GAME_WEEK"; //已进行一场游戏(周统计)
	public static final String PLAYED_GAME_MONTH = "PLAYED_GAME_MONTH"; //已进行一场游戏(月统计)
	public static final String FANGKA_GAME = "FANGKA_GAME"; //进行游戏打完4圈(2人是24局,3人是20局)
}
