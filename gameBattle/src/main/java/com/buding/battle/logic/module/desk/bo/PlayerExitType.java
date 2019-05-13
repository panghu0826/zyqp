package com.buding.battle.logic.module.desk.bo;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public enum PlayerExitType {
	UNREADY_KICK, //未准备被系统踢
	NOONLINE_USER_KICK, //掉线过长不重连被踢
	REQUEST_EXIT, //主动退出
	ROBOT_RECYCLE, // 机器人回收
	DULICATE_ENROLL, //重复报名，剔除旧的游戏
	DULICATE_LOGIN_KICK, //重复登录，剔除旧会话
	CHANGE_DESK, //换桌推出旧桌子
	SESSION_CLOSE_KICK //会话关闭被踢
}
