package com.buding.common.db.cache;

public abstract interface CachedService {
	public abstract Object put2EntityCache(String paramString, Object paramObject);

	public abstract Object put2EntityCache(String paramString, Object paramObject, long paramLong);

	public abstract Object getFromEntityCache(String paramString);

	public abstract void removeFromEntityCache(String paramString);

	public abstract void put2CommonCache(String paramString, Object paramObject);

	public abstract void put2CommonCache(String paramString, Object paramObject, long paramLong);

	public abstract void put2CommonHashCache(String paramString1, String paramString2, Object paramObject);

	public abstract Object put2CommonHashCacheIfAbsent(String paramString1, String paramString2, Object paramObject);

	public abstract void put2CommonHashCache(String paramString1, String paramString2, Object paramObject, long paramLong);

	public abstract Object getFromCommonCache(String paramString);

	public abstract Object getFromCommonCache(String paramString1, String paramString2);

	public abstract void removeFromCommonCache(String paramString);

	public abstract void removeFromCommonHashCache(String paramString1, String paramString2);
}