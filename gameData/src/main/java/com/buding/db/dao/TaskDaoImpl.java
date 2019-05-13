package com.buding.db.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.UserTask;
import com.buding.hall.module.task.dao.TaskDao;

public class TaskDaoImpl extends CachedServiceAdpter implements TaskDao {
	@Autowired
	DbService dbService;

	@Override
	public long insert(UserTask task) {
		this.commonDao.save(task);
		return task.getId();
	}

	@Override
	public void update(UserTask model) {
		this.put2EntityCache(model);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(model);
		} else {
			this.dbService.submitUpdate2Queue(model);
		}
	}

	@Override
	public List<UserTask> getUnderingTask(int taskType, int userId) {
		List<UserTask> list = this.getList("select * from user_task where task_type = ? and user_id = ? and finish = 0 and close = 0", UserTask.class, taskType, userId);
		return list;
	}

	@Override
	public UserTask getLatestUserTask(int userId, String taskId) {
		UserTask one = this.getOne("select * from user_task where task_id = ? and user_id = ?  order by id desc", UserTask.class, taskId, userId);
		return one;		
	}

	@Override
	public UserTask getLatestUserTask(int userId, String taskId, int day) {
		UserTask one = this.getOne("select * from user_task where task_id = ? and user_id = ? and day = ? order by id desc ", UserTask.class, taskId, userId, day);
		return one;	
	}

	@Override
	public List<UserTask> getUserTaskList(int userId, String taskId, int day) {
		return this.getList("select * from user_task where user_id = ? and task_id = ? and day = ?", UserTask.class, userId, taskId, day);
	}

	@Override
	public List<UserTask> getUserUnderingTask(int userId) {
		return this.getList("select * from user_task where user_id = ? and award = 0 and close = 0 ", UserTask.class, userId);
	}

	@Override
	public UserTask get(long id) {
		return this.get(id, UserTask.class);
	}

}
