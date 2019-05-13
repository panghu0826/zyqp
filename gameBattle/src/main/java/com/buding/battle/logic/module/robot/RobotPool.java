package com.buding.battle.logic.module.robot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.buding.db.model.RobotSetting;

public class RobotPool {
	private RobotSetting setting;
	
	private transient RobotGenerator robotGenerator;
	private int currentId;
	private int minId;
	private int maxId;
	
	public ConcurrentMap<Integer, Robot> busyMap = new ConcurrentHashMap<Integer, Robot>();
	public ConcurrentMap<Integer, Robot> freeMap = new ConcurrentHashMap<Integer, Robot>();
	
	public RobotPool(RobotSetting s, RobotGenerator robotGenerator) {
		this.setting = s;
		this.robotGenerator = robotGenerator;
		setting.setRobotPlaying(0);
		setting.setRobotWaiting(0);

		String range = setting.getIdRange();
		minId = Integer.valueOf(range.split("-")[0]);
		maxId = Integer.valueOf(range.split("-")[1]);
		currentId = minId;
	}
	
	public synchronized void add(Robot robot) {	
		robot.pool = this;
		freeMap.put(robot.playerId, robot);
		updateSetting();
	}
	
	public synchronized Robot borrow() {
		if(setting.getTotalRobot() <= setting.getRobotPlaying()) {
			return null; //不能多借
		}
		
		Robot robot = null;
		if(freeMap.isEmpty()) {
			//不够再产生
			robot = robotGenerator.generate(setting, this);
		} else {
			//从空闲池里面取
			int ind = (int)(System.currentTimeMillis()%freeMap.size());
			List<Integer> list = new ArrayList<Integer>();
			list.addAll(freeMap.keySet());
			int key = list.get(ind);
			robot = freeMap.get(key);
		}
		
		if(robot != null) {
			freeMap.remove(robot.playerId);
			busyMap.put(robot.playerId, robot);
			updateSetting();
		}
		
		return robot;
	}

	private void updateSetting() {
		setting.setRobotWaiting(freeMap.size());
		setting.setRobotPlaying(busyMap.size());
	}
	
	public synchronized void ret(Robot robot) {
		busyMap.remove(robot.playerId);
		freeMap.put(robot.playerId, robot);
		updateSetting();
	}
	
	public int getNextRobotId() {
		if(currentId > maxId) {
			return -1;
		}
		currentId++;
		if(busyMap.containsKey(currentId) || freeMap.containsKey(currentId)) {
			return getNextRobotId();
		}
		
		return currentId;
	}

	public int getMinId() {
		return minId;
	}

	public void setMinId(int minId) {
		this.minId = minId;
	}

	public int getMaxId() {
		return maxId;
	}

	public void setMaxId(int maxId) {
		this.maxId = maxId;
	}
}
