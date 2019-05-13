package com.buding.hall.module.task.type;

/**
 * 任务类型
 * 
 * @author Hyint
 */
public interface TaskType {
	/** 首次登录 */
	int FIRST_LOGIN = 1;
	
	/** 每日登录 */
	int DAILY_LOGIN = 2;
	
	/** 破产救济 */
	int CORUPT_ASSIST = 3;
	
	/** 宝箱任务 */
	int TREASURE_BOX = 4;
	
	/** 分享有礼 */
	int SHARE = 5;
	
	/** 好评有礼 */
	int RATING = 6;
	
	/** 累计赢 */
	int TOTAL_WIN = 7;
	
	/** 累计游戏 */
	int TOTAL_GAMING = 8;
	
	/** 累计使他人破产 */
	int MAKE_CORRUPTION = 9;
	
	/** 首充 */
	int FIRST_CHARGE = 10;
	
	/** 绑定手机 */
	int BIND_MOBILE = 11;
}
