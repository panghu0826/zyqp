package com.buding.common.logger;

public enum LogLevel {
	DEBUG(1), INFO(2), WARN(3), ERROR(4);

	private int level;
	public static LogLevel defLevel = LogLevel.INFO;

	private LogLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean isDebugEnable() {
		return this.getLevel() >= DEBUG.getLevel();
	}
	
	public boolean isInfoEnable() {
		return this.getLevel() >= INFO.getLevel();
	}
	
	public boolean isWarnEnable() {
		return this.getLevel() >= WARN.getLevel();
	}
	
	public boolean isErrorEnable() {
		return this.getLevel() >= ERROR.getLevel();
	}

}
