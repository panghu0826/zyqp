package com.buding.hall.config.task;

import java.util.List;

import com.buding.common.conf.ValRequired;
import com.buding.hall.config.ItemPkg;

/**
 * 任务配置文件
 * @author Administrator
 *
 */
public class TaskConf {
	@ValRequired
	public String taskId; //任务的配置id
	
	@ValRequired
	public Integer taskType; //任务的类型, 参加TaskType.java
	
	@ValRequired
	public List<ItemPkg> awardItems; //奖励的道具
	
	@ValRequired
	public Integer amount;//做多少次完成任务，任务完成后不能再做，除非任务被重置(resetTime设置重置时间)
	
	@ValRequired
	public Integer resetTime; //重置任务时间，-1代表不重置
	
	@ValRequired
	public Boolean autoAward; //是否自动奖励, false需要手动领奖
	
	public TaskConf nextTask; //下一个任务
	public TaskConf preTask; //上一个任务
	public String preTaskId; //前置任务的id
	
	public List<Integer> clientType;
	
	@ValRequired
	public Integer taskGroup;//1系统任务 2每日任务
	
	@ValRequired
	public String taskImg;
	
	@ValRequired
	public String title;
}
