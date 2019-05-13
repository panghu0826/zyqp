package com.buding.battle.logic.module.robot;

import com.buding.db.model.RobotSetting;


public interface RobotGenerator {
	public Robot generate(RobotSetting setting, RobotPool pool);
}
