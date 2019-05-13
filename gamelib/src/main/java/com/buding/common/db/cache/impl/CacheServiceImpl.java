package com.buding.common.db.cache.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.buding.common.db.cache.CachedService;
import com.buding.common.db.model.CacheObject;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class CacheServiceImpl implements CachedService {  

	@Autowired(required = false)
	@Qualifier("dbcache.max_capacity_of_entity_cache")
	private Integer entityCacheSize = Integer.valueOf(500000);

	@Autowired(required = false)
	@Qualifier("dbcache.max_capacity_of_common_cache")
	private Integer commonCacheSize = Integer.valueOf(500000);

	private ConcurrentLinkedHashMap<String, Object> COMMON_CACHE = null;

	private static final Logger LOGGER = LogManager.getLogger(CacheServiceImpl.class);

	private ConcurrentLinkedHashMap<String, CacheObject> ENTITY_CACHE = null;

	@PostConstruct
	protected void init() {
		this.COMMON_CACHE = new ConcurrentLinkedHashMap.Builder<String, Object>().maximumWeightedCapacity(this.commonCacheSize.intValue()).build();
		this.ENTITY_CACHE = new ConcurrentLinkedHashMap.Builder<String, CacheObject>().maximumWeightedCapacity(this.entityCacheSize.intValue())
				.build();
	}

	public Object put2EntityCache(String key, Object object) {
		return put2EntityCache(key, object, -1L);
	}

	public Object put2EntityCache(String key, Object object, long timeToLive) {
		if ((key == null) || (object == null)) {
			return object;
		}

		if (timeToLive > 0L)
			this.ENTITY_CACHE.put(key, CacheObject.valueOf(object, timeToLive));
		else {
			this.ENTITY_CACHE.put(key, CacheObject.valueOf(object));
		}
		return getFromEntityCache(key);
	}

	public Object getFromEntityCache(String key) {
		CacheObject cacheObject = (CacheObject) this.ENTITY_CACHE.get(key);
		if (cacheObject == null) {
			return null;
		}

		if (!cacheObject.isValidate()) {
			this.ENTITY_CACHE.remove(key);
			return null;
		}

		return cacheObject.getEntity();
	}

	public void removeFromEntityCache(String key) {
		if (this.ENTITY_CACHE.containsKey(key))
			this.ENTITY_CACHE.remove(key);
	}

	public void put2CommonCache(String key, Object object) {
		put2CommonCache(key, object, -1L);
	}

	public void put2CommonCache(String key, Object object, long timeToLive) {
		if (object == null) {
			return;
		}

		if (timeToLive > 0L)
			this.COMMON_CACHE.put(key, CacheObject.valueOf(object, timeToLive));
		else
			this.COMMON_CACHE.put(key, object);
	}

	public void put2CommonHashCache(String hashKey, String subKey, Object object) {
		put2CommonHashCache(hashKey, subKey, object, -1L);
	}

	public Object put2CommonHashCacheIfAbsent(String hashKey, String subKey, Object object) {
		return put2CommonHashCacheIfAbsent(hashKey, subKey, object, -1L);
	}

	@SuppressWarnings("unchecked")
	public Object put2CommonHashCacheIfAbsent(String hashKey, String subKey, Object object, long timeToLive) {
		if (object == null) {
			return object;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("put2HashCache [hashKey: [{}], subKey: [{}], timeToLive: [{} ms]",
					new Object[] { hashKey, subKey, Long.valueOf(timeToLive) });
		}

		ConcurrentMap<String, Object> cache = null;
		if (this.COMMON_CACHE.containsKey(hashKey)) {
			Object currCache = this.COMMON_CACHE.get(hashKey);
			if ((currCache instanceof Map)) {
				cache = (ConcurrentMap<String, Object>) currCache;
				;
			}
		}

		if (cache == null) {
			cache = new ConcurrentHashMap<String, Object>(1);
			this.COMMON_CACHE.put(hashKey, cache);
		}

		if (timeToLive > 0L) {
			return cache.putIfAbsent(subKey, CacheObject.valueOf(object, timeToLive));
		}
		return cache.putIfAbsent(subKey, object);
	}

	@SuppressWarnings("unchecked")
	public void put2CommonHashCache(String hashKey, String subKey, Object object, long timeToLive) {
		if (object == null) {
			return;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("put2HashCache [hashKey: [{}], subKey: [{}], timeToLive: [{} ms]",
					new Object[] { hashKey, subKey, Long.valueOf(timeToLive) });
		}

		Map<String, Object> cache = null;
		if (this.COMMON_CACHE.containsKey(hashKey)) {
			Object currCache = this.COMMON_CACHE.get(hashKey);
			if ((currCache instanceof Map)) {
				cache = (Map<String, Object>) currCache;
			}
		}

		if (cache == null) {
			cache = new ConcurrentHashMap<String, Object>(1);
			this.COMMON_CACHE.put(hashKey, cache);
		}

		if (timeToLive > 0L)
			cache.put(subKey, CacheObject.valueOf(object, timeToLive));
		else
			cache.put(subKey, object);
	}

	public Object getFromCommonCache(String key) {
		LOGGER.debug("getFromCache-Key:[{}]", key);
		if (!this.COMMON_CACHE.containsKey(key)) {
			return null;
		}

		Object cacheObj = this.COMMON_CACHE.get(key);
		if ((cacheObj instanceof CacheObject)) {
			CacheObject co = (CacheObject) cacheObj;
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("CacheObject- [createTime: [{}] , ttl: [{}] , isValidate: [{}] ]",
						new Object[] { Long.valueOf(co.getCreateTime()), Long.valueOf(co.getTtl()), Boolean.valueOf(co.isValidate()) });
			}

			if (co.isValidate()) {
				return co.getEntity();
			}
			this.COMMON_CACHE.remove(key);
			return null;
		}

		return cacheObj;
	}

	@SuppressWarnings("unchecked")
	public Object getFromCommonCache(String hashKey, String subKey) {
		LOGGER.debug("hashKey: [{}] , subKey: [{}]", hashKey, subKey);
		if (!this.COMMON_CACHE.containsKey(hashKey)) {
			return null;
		}

		Object cacheObject = this.COMMON_CACHE.get(hashKey);
		if (cacheObject == null) {
			return null;
		}

		if ((cacheObject instanceof Map)) {
			Map<String, Object> cache = (Map<String, Object>) cacheObject;
			if (!cache.containsKey(subKey)) {
				return null;
			}

			Object subObject = cache.get(subKey);
			if (subObject == null) {
				return null;
			}

			if ((subObject instanceof CacheObject)) {
				CacheObject co = (CacheObject) subObject;
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("createTime: [{}], ttl: [{}} , isValidate: [{}]",
							new Object[] { Long.valueOf(co.getCreateTime()), Long.valueOf(co.getTtl()), Boolean.valueOf(co.isValidate()) });
				}

				if (co.isValidate()) {
					return co.getEntity();
				}
				cache.remove(subKey);
				return null;
			}

			return subObject;
		}
		return null;
	}

	public void removeFromCommonCache(String key) {
		this.COMMON_CACHE.remove(key);
	}

	@SuppressWarnings("rawtypes")
	public void removeFromCommonHashCache(String hashKey, String subKey) {
		Object cache = this.COMMON_CACHE.get(hashKey);
		if ((cache == null) || (!(cache instanceof Map))) {
			return;
		}

		Map map = (Map) cache;
		if (map.containsKey(subKey))
			map.remove(subKey);
	}
}