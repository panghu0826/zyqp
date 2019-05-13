package com.buding.hall.module.task.dao;

import java.util.List;

import com.buding.db.model.UserTask;

public interface TaskDao {
	/**
	 * 插入任务
	 * @param task
	 * @return
	 */
	public long insert(UserTask task);
	
	/**
	 * 更新任务
	 * @param task
	 */
	public void update(UserTask task);
	
	public UserTask get(long id);
	
	/**
	 * 
	 * @param taskType
	 * @param userId
	 * @return
	 */
	public List<UserTask> getUnderingTask(int taskType, int userId);
	public UserTask getLatestUserTask(int userId, String taskId);
	public UserTask getLatestUserTask(int userId, String taskId, int day);
	public List<UserTask> getUserTaskList(int userId, String taskId, int day);
	public List<UserTask> getUserUnderingTask(int userId);
}
