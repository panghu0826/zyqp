package com.buding.battle.logic.event;

public class TimerEvent {
	//触发时间
	public long triggerTime;
	
	//timer的id
	public int timerId;
	
	//开始设置的时间
	public long setTime;
	
	//是否已停止
	public boolean killed;
}
