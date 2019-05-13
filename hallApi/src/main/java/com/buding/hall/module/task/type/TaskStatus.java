package com.buding.hall.module.task.type;

public enum TaskStatus {
	/**0 进行中 */
	PROGRESS,
	
	/**1 完成(未领取奖励) */
	COMPLETED,
	
	/**2 完成(已经领取奖励) */
	FINISHED,
	
	/**3 已关闭 */
	CLOSE;
}
