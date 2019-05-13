package com.buding.battle.logic.module.common;

public enum PlayerStatus {
	IN_HALL, //在大厅,默认状态
	ENROLL_OK, //报名成功
	ENTER_ROOM, //进入房间
	UNREADY, //坐下，处于未准备状态
	READY, //准备完成.
	GAMING, //游戏中
	LUNKONG, //比赛场轮空
}