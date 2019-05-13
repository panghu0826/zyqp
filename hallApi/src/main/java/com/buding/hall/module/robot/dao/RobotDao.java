package com.buding.hall.module.robot.dao;

import java.util.List;

import com.buding.db.model.RobotSetting;


public interface RobotDao {
	public List<RobotSetting> loadRobotSettingList();
	public void update(RobotSetting setting);
	public RobotSetting getByMatchId(String matchId);
}
