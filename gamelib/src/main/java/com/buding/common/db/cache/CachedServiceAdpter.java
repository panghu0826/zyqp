package com.buding.common.db.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.dao.CommonDao;
import com.buding.common.db.model.BaseModel;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public abstract class CachedServiceAdpter {

	@Autowired(required = true)
	protected CommonDao commonDao;
	
	@Autowired
	protected CachedService cachedService; 
	private final Logger LOGGER = LogManager.getLogger(getClass());
	private static final ConcurrentLinkedHashMap.Builder<String, Object> BUILDER = new ConcurrentLinkedHashMap.Builder<String, Object>();
	private static final ConcurrentLinkedHashMap<String, Object> OBJECT_MAPS = BUILDER.maximumWeightedCapacity(1000).build();

	private static Object getLockObject(String key) {
		Object lock = OBJECT_MAPS.get(key);
		if (lock != null) {
			return lock;
		}
		OBJECT_MAPS.putIfAbsent(key, new Object());
		return OBJECT_MAPS.get(key);
	}

	protected <PK extends Comparable<PK>> String getEntityIdKey(PK id, Class<?> entityClazz) {
		return entityClazz.getName() + "_" + id;
	}

	public <T extends BaseModel<PK>, PK extends Comparable<PK>> T get(PK id, Class<T> clazz) {
		return get(id, clazz, true);
	}
	
	public <T extends BaseModel<PK>, PK extends Comparable<PK>> List<T> getList(String sql, Class<T> clazz, Object ...args) {
		List<T> list = this.commonDao.selectList(sql, clazz, args);
		if(list.isEmpty()) {
			return list;
		}
		List<T> ret = new ArrayList<T>();
		for(T t : list) {
			t = get(t.getId(), clazz);
			if(t != null) {
				ret.add(t);
			}
		}
		return ret;
	}
	
	public <T extends BaseModel<PK>, PK extends Comparable<PK>> T getOne(String sql, Class<T> clazz, Object ...args) {
		List<T> list = this.commonDao.selectList(sql, clazz, args);
		if(list.isEmpty()) {
			return null;
		}
		
		for(T t : list) {
			t = get(t.getId(), clazz);
			if(t != null) {
				return t;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T extends BaseModel<PK>, PK extends Comparable<PK>> T getEntityFromCache(PK id, Class<T> entityClazz) {
		String key = getEntityIdKey(id, entityClazz);
		return (T) this.cachedService.getFromEntityCache(key);
	}

	@SuppressWarnings("unchecked")
	protected <T, PK extends Comparable<PK>> T get(PK id, Class<T> clazz, boolean fromCache) {
		if (id == null) {
			return null;
		}

		String key = getEntityIdKey(id, clazz);
		if (!fromCache) {
			this.cachedService.removeFromEntityCache(key);
		} else {
			T entity = (T) this.cachedService.getFromEntityCache(key);
			if (entity != null) {
				return entity;
			}
		}

		Object lockObject = getLockObject(key);
		try {
			synchronized (lockObject) {
				T entity = (T) this.cachedService.getFromEntityCache(key);
				if (entity != null) {
					return entity;
				}

				entity = (T) getEntityFromDB((Serializable) id, clazz);
				this.cachedService.put2EntityCache(key, entity);
				return (T) this.cachedService.getFromEntityCache(key);
			}
		} catch (Exception e) {
			this.LOGGER.error("{}", e);
		}
		return null;
	}

	public <T extends BaseModel<PK>, PK extends Comparable<PK>> void removeEntityFromCache(PK id, Class<T> clazz) {
		this.cachedService.removeFromEntityCache(getEntityIdKey(id, clazz));
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseModel<PK>, PK extends Comparable<PK>> T put2EntityCache(PK id, Class<?> clazz, T entiy) {
		return (T) this.cachedService.put2EntityCache(getEntityIdKey(id, clazz), entiy);
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseModel<PK>, PK extends Comparable<PK>> List<T> put2EntityCache(T... entiys) {
		if ((entiys != null) && (entiys.length > 0)) {
			List<T> result = new ArrayList<T>();
			for (T entiy : entiys) {
				// result.add((T)this.cachedService.put2EntityCache(getEntityIdKey(entiy.getId(),
				// entiy.getClass()), entiy));
				result.add(put2EntityCache(entiy.getId(), entiy.getClass(), entiy));
			}
			return result;
		}
		return new ArrayList<T>(0);
	}

	public <T extends BaseModel<PK>, PK extends Comparable<PK>> List<T> put2EntityCache(Collection<T> entiys) {
		if ((entiys != null) && (entiys.size() > 0)) {
			List<T> result = new ArrayList<T>();
			for (T entiy : entiys) {
				result.add(put2EntityCache(entiy.getId(), entiy.getClass(), entiy));
			}
			return result;
		}
		return new ArrayList<T>(0);
	}

	public <T extends BaseModel<PK>, PK extends Comparable<PK>> void removeEntityFromCache(Collection<PK> idList, Class<T> clazz) {
		if ((idList != null) && (!idList.isEmpty())) {
			for (PK id : idList) {
				this.cachedService.removeFromEntityCache(getEntityIdKey(id, clazz));
			}
		}
	}

	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		return this.commonDao.get(id, clazz);
	}

	protected <T extends BaseModel<PK>, PK extends Comparable<PK>> List<T> getEntityFromIdList(Collection<PK> idList, Class<T> entityClazz) {
		List<T> entityList = new ArrayList<T>();
		if ((idList == null) || (idList.isEmpty())) {
			return entityList;
		}

		for (PK entityId : idList) {
			T entity = get(entityId, entityClazz);
			if (entity != null) {
				entityList.add(entity);
			}
		}
		return entityList;
	}
}