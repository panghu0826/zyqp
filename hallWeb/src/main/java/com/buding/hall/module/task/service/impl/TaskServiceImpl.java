package com.buding.hall.module.task.service.impl;

import com.buding.db.model.UserTask;
import com.buding.hall.module.task.dao.TaskDao;
import com.buding.hall.module.task.service.TaskService;
import com.buding.hall.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TaskServiceImpl implements TaskService{

//    private Logger logger = LogManager.getLogger(getClass());

    @Autowired
    TaskDao taskDao;

    @Autowired
    UserService userService;


    @Override
    public long insert(UserTask task) {
        return taskDao.insert(task);
    }

    @Override
    public void update(UserTask task) {
        taskDao.update(task);
    }

    @Override
    public UserTask get(long id) {
        return taskDao.get(id);
    }

    @Override
    public List<UserTask> getUnderingTask(int taskType, int userId) {
        return taskDao.getUnderingTask(taskType,userId);
    }

    @Override
    public UserTask getLatestUserTask(int userId, String taskId) {
        return taskDao.getLatestUserTask(userId,taskId);
    }

    @Override
    public UserTask getLatestUserTask(int userId, String taskId, int day) {
        return taskDao.getLatestUserTask(userId,taskId,day);
    }

    @Override
    public List<UserTask> getUserTaskList(int userId, String taskId, int day) {
        return taskDao.getUserTaskList(userId,taskId,day);
    }

    @Override
    public List<UserTask> getUserUnderingTask(int userId) {
        return taskDao.getUserUnderingTask(userId);
    }
}
