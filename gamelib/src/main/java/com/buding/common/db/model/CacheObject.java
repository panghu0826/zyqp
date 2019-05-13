package com.buding.common.db.model;

public class CacheObject {
	private static final long ONE_DAY_MILISECONDS = 86400000L;
	private Object entity;
	private long ttl = 86400000L;

	private long createTime = System.currentTimeMillis();

	private long expireTime = this.createTime + this.ttl;

	public static CacheObject valueOf(Object entity) {
		CacheObject entityCacheObject = new CacheObject();
		entityCacheObject.entity = entity;
		entityCacheObject.ttl = ONE_DAY_MILISECONDS;
		entityCacheObject.createTime = System.currentTimeMillis();
		entityCacheObject.expireTime = (entityCacheObject.createTime + entityCacheObject.ttl);
		return entityCacheObject;
	}

	public static CacheObject valueOf(Object entity, long timeToLive) {
		CacheObject entityCacheObject = new CacheObject();
		entityCacheObject.entity = entity;
		entityCacheObject.ttl = timeToLive;
		entityCacheObject.createTime = System.currentTimeMillis();
		entityCacheObject.expireTime = (entityCacheObject.createTime + entityCacheObject.ttl);
		return entityCacheObject;
	}

	public boolean isValidate() {
		return this.expireTime >= System.currentTimeMillis();
	}

	public Object getEntity() {
		return this.entity;
	}

	public long getTtl() {
		return this.ttl;
	}

	public long getCreateTime() {
		return this.createTime;
	}

	public long getExpireTime() {
		return this.expireTime;
	}
}
