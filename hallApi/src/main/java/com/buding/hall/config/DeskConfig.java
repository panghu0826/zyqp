package com.buding.hall.config;

public class DeskConfig {
	// 最小开赛人数
	public int seatSizeLower = 4;
	// 最大开赛人数
	public int seatSizeUpper = 4;
	//最大活跃玩家数,比如如果是1，则只允许1个真实玩家，其余全是机器人
	public int maxRealPlayer = 4;
	
	public boolean autoStartGame = true;

	public boolean autoChangeDesk = true;

	// public String gameClassFullName = "com.buding.hall.logic.game.DDZGame";

	public String gameClassFullName = "com.buding.ddz.DDZServer";

	public String deskClassFullName = "com.buding.battle.logic.module.desk.bo.RobotSupportDeskImpl";

	// 自动准备
	public boolean autoReady;

	// 15秒不准备踢出桌子
	public int secondsBeforKickout = 15;

	// 所有玩家都掉线时，等待多少秒时间，如果没有玩家重连，直接kill掉桌子(已废弃)
	public int secondsWaitActivePlayer = 40*60 ;

	// 出错多少次kill掉桌子
	public int errCount4KillDesk = 50;

	// 游戏开始后多少秒没有结束，直接kill掉桌子(2小时)
	public int secondsWaitingGameStop = 2 * 60 * 60;

	// 等待多少秒时间没有开始游戏，直接kill掉桌子
	public int secondsWaitingGameStart = 70*60;
	
	//游戏发起解散多少秒没结束解散
	public int voteTimeLimit = 5 * 60;
	//游戏暂停等待多少秒没开始即解散
	public static int gamePauseTimeout = 15*60 ;
	
	//空桌生存时间
	public int emptyDeskTTL = 5;

	// 是否支持机器人,默认是
	public boolean supportRobot = true;

	// 3秒后加入第一个机器人
	public int secondsAddFirstRobot = 3;

	// 以后加入机器人的间隔减半,比如3秒->1.5秒->0.75秒
	public double addRobotRate = 0.3;

	public int timerDelayThreadShold = 10000; // 定时器延迟报警阀值,单位毫秒

	public int gameOperTimeOut = 12; // 游戏开始时,超时操作时间,单位:秒
	
	public boolean allowExitWhenGaming = false; //游戏中是否允许退出(只有单机场才允许), 如果不允许，则exit当作away处理
	
	public boolean awayIsExit = false; //away是否等同于exit，即退出桌子(单机场为true)
	
	public boolean synPlayerOfflineAndReconnect = true; // 玩家掉线和重连，是否告知其它玩家(斗地主一般不会主动告知，而是等操作超时后自动进入托管模式)
	
	public String gameParam;
}
