package com.buding.common.cache;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface CacheClient {
	public void set(String key, Object val);
	public <T> T get(String key, Class<T> cls);
	public void del(String key);
	public void hset(String key, String field, Object val);
	public <T> T hget(String key, String field, Class<T> cls);
	public void hdel(String key, String field);
}
