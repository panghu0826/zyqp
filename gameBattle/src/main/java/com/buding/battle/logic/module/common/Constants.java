package com.buding.battle.logic.module.common;

public interface Constants {
	public static int PLAYER_HANGUP = 1;
	public static int PLAYER_UNHANGUP = 2;

	public static int CLUB_COMMON_DESK = 0;
	public static int CLUB_JI_FEN_DESK = 1;

	public static final int CLUB_CAN_FU_FEN = 2;//可负分
	public static final int CLUB_CAN_NOT_FU_FEN = 1;//不可负分


	public static final int CLUB_CREATE_CREATE_ROOM_MODE_MANAGE = 1;//管理员开房
	public static final int CLUB_CREATE_CREATE_ROOM_MODE_ALL = 2;//所有人开房

	public static final int CLUB_MEMEBER_TYPE_COMMON = 0;
	public static final int CLUB_MEMEBER_TYPE_OWNER = 1;
	public static final int CLUB_MEMEBER_TYPE_MANAGER = 2;

	public static final int SCORE_LOG_INIT = 1;
	public static final int SCORE_LOG_MANAGER_MODIFY = 2;
	public static final int SCORE_LOG_GAME = 3;
	public static final int SCORE_LOG_BIAO_QING = 4;

}