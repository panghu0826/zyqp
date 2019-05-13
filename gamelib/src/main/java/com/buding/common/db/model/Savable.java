package com.buding.common.db.model;

public abstract class Savable extends BaseModel<Long> {
	public abstract String getUpdateSql();
	public abstract String getInsertSql();
	
}
