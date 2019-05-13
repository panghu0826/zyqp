package com.buding.common.db.executor;

public interface DbService {

	public void submitUpdate2Queue(Object... paramArrayOfT);

	public void updateEntityIntime(Object... paramArrayOfT);

	public void updateEntityIntime(DbCallback paramDbCallback, Object... paramArrayOfT);
	
	public void flush();
}