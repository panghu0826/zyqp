package com.buding.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.buding.common.db.dao.CommonDao;
import com.buding.common.db.executor.DbCallback;
import com.buding.common.db.executor.DbService;
import com.buding.common.db.model.BaseModel;
import com.buding.common.server.BaseServerComponent;
import com.buding.common.thread.NamedThreadFactory;
import com.buding.common.util.CollectionUtils;
import com.buding.common.util.ConcurrentHashSet;

//@Component
public class DbServiceImpl extends BaseServerComponent implements DbService, ApplicationListener<ContextClosedEvent> {
	private static ExecutorService DB_POOL_SERVICE;
	private static final Logger LOGGER = LogManager.getLogger(DbServiceImpl.class);

	//定时从map里面取数据放入queue, 再定时从queue中取出放入DB_POOL_SERVICE
	private LinkedBlockingQueue<Runnable> DB_SAVER_QUEUE = new LinkedBlockingQueue<Runnable>();

	//一开始提交的数据放入map里面去重
	private volatile ConcurrentMap<Class<?>, ConcurrentHashSet<?>> DB_OBJECT_MAP = new ConcurrentHashMap<Class<?>, ConcurrentHashSet<?>>();

	@Autowired(required = false)
	@Qualifier("dbcache.db_pool_capacity")
	private Integer dbPoolSize;

	@Autowired(required = false)
	@Qualifier("dbcache.db_pool_max_capacity")
	private Integer dbPoolMaxSize;

	@Autowired(required = false)
	@Qualifier("dbcache.db_pool_keep_alive_time")
	private Integer keepAliveTime;

	@Autowired(required = false)
	@Qualifier("dbcache.dbcache.max_block_time_of_entity_cache")
	private Integer entityBlockTime;

	@Autowired
	private CommonDao commonDao;
	private final ReentrantLock takeLock;
	private final Condition notEmpty;
	public final Runnable CUSTOMER_TASK;
	public final Runnable HANDLER_CACHED_OBJ_TASK;
	private static final int MAX_RETRY_COUNT = 5;

	public DbServiceImpl() {
		this.dbPoolSize = Integer.valueOf(2);

		this.dbPoolMaxSize = Integer.valueOf(16);

		this.keepAliveTime = Integer.valueOf(600);

		this.entityBlockTime = Integer.valueOf(2000);

		this.takeLock = new ReentrantLock();
		this.notEmpty = this.takeLock.newCondition();

		this.CUSTOMER_TASK = new Runnable() {
			public void run() {
				while (true)
					try {
						Runnable task = (Runnable) DB_SAVER_QUEUE.take();
						if (task != null) {
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("submit task.. QUEUE_SIZE:[{}] ", Integer.valueOf(DB_SAVER_QUEUE.size()));
							}
							DB_POOL_SERVICE.submit(task);
						}
					} catch (Exception ex) {
						LOGGER.error("Error: {}", ex);
					}

			}
		};
		this.HANDLER_CACHED_OBJ_TASK = new Runnable() {
			public void run() {
				while (true)
					try {
						if (entityBlockTime.intValue() > 0) {
							takeLock.lockInterruptibly();
							try {
								notEmpty.await(entityBlockTime.intValue(), TimeUnit.MILLISECONDS);
							} finally {
								takeLock.unlock();
							}
						}

						submitCachedDbObject();
					} catch (Exception ex) {
						LOGGER.error("Error: " + ex.getMessage());
					}
			}
		};
	}

	@PostConstruct
	void initialize() {
		ThreadGroup threadGroup = new ThreadGroup("缓存模块");
		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "入库线程池");
		DB_POOL_SERVICE = new ThreadPoolExecutor(this.dbPoolSize.intValue(), this.dbPoolMaxSize.intValue(), this.keepAliveTime.intValue(),
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookRunnable()));

		LOGGER.debug("Initialize DB Daemon Thread...");
		String threadName = "数据库入库Daemon线程";
		ThreadGroup group = new ThreadGroup(threadName);
		NamedThreadFactory factory = new NamedThreadFactory(group, threadName);
		Thread thread = factory.newThread(this.CUSTOMER_TASK);
		thread.setDaemon(true);
		thread.start();

		Thread pollCachedObjThread = factory.newThread(this.HANDLER_CACHED_OBJ_TASK);
		pollCachedObjThread.setDaemon(true);
		pollCachedObjThread.start();

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookRunnable()));
	}

	private final void submitCachedDbObject() {
		if (DB_OBJECT_MAP.isEmpty()) {
			return;
		}

		ConcurrentMap<Class<?>, ConcurrentHashSet<?>> objSet = DB_OBJECT_MAP;
		DB_OBJECT_MAP = new ConcurrentHashMap<Class<?>, ConcurrentHashSet<?>>(100);
		
		// Iterator iterator = objSet.keySet().iterator();
		// while (iterator.hasNext()) {
		// Class key = (Class) iterator.next();
		//
		// ConcurrentHashSet set = (ConcurrentHashSet) objSet.get(key);
		// if ((set != null) && (set.size() > 0))
		// DB_SAVER_QUEUE.add(createTask(null, new EntityCache(set)));
		// }

		for (ConcurrentHashSet<?> c : objSet.values()) {
			if (c != null && c.size() > 0) {
				DB_SAVER_QUEUE.add(createTask(null, new EntityCache(c)));
			}
		}

	}

	private final void updateIntimeCachedDbObject() {
		if (DB_OBJECT_MAP.isEmpty()) {
			return;
		}

		// ConcurrentMap objSet = new ConcurrentHashMap();
		// synchronized (DB_OBJECT_MAP) {
		// objSet.putAll(DB_OBJECT_MAP);
		// DB_OBJECT_MAP.clear();
		// }
		//
		// int subCount = 100;
		// for (ConcurrentHashSet concurrentHashSet : objSet.values())
		// if ((concurrentHashSet != null) && (!concurrentHashSet.isEmpty())) {
		// int maxSize = concurrentHashSet.size();
		// List list = new ArrayList(concurrentHashSet);
		// int maxCount = maxSize % subCount == 0 ? maxSize / subCount : maxSize
		// / subCount + 1;
		// for (int index = 0; index < maxCount; index++)
		// updateEntityIntime(new List[] { CollectionUtils.subListCopy(list,
		// subCount * index, subCount) });
		// }

		ConcurrentMap<Class<?>, ConcurrentHashSet<?>> objSet = DB_OBJECT_MAP;
		DB_OBJECT_MAP = new ConcurrentHashMap<Class<?>, ConcurrentHashSet<?>>(100);

		int subCount = 100;
		int maxSize;
		int maxCount;
		for (ConcurrentHashSet<?> c : objSet.values()) {
			if (c != null && !c.isEmpty()) {
				maxSize = c.size();
				maxCount = maxSize % subCount == 0 ? maxSize / subCount : maxSize / subCount + 1;
				for (int i = 0; i < maxCount; i++) {
					updateEntityIntime(CollectionUtils.subListCopy((List) c, subCount * i, subCount));
				}
			}
		}

	}

	private Runnable createTask(final DbCallback callback, final EntityCache entityCache) {
		return new Runnable() {
			public void run() {
				handleTask(callback, entityCache);
			}
		};
	}

	private void handleTask(DbCallback callback, EntityCache entityCache) {
		Collection entities = entityCache.getEntities();
		if ((entities != null) && (!entities.isEmpty())) {
			try {
				this.commonDao.update(entities);
			} catch (Exception ex) {
				LOGGER.error("执行入库时产生异常", ex);

				// if (EntityCache.access$904(entityCache) < MAX_RETRY_COUNT) {
				// add2Queue(callback, entityCache);
				// }
				if (entityCache.retryCount < MAX_RETRY_COUNT) {
					add2Queue(callback, entityCache);
				}

				return;
			}
		}

		if (callback != null)
			try {
				callback.doAfter();
			} catch (Exception ex) {
				LOGGER.error("执行入库后回调时产生异常", ex);
			}
	}

	public void submitUpdate2Queue(Object... entities) {
		if (entities.length <= 0) {
			return;
		}

		if (this.entityBlockTime.intValue() <= 0) {
			add2Queue(null, new EntityCache(entities));
			return;
		}

		for (Object entity : entities)
			if ((entity instanceof BaseModel))
				put2ObjectMap(entity);
			else if ((entity instanceof Collection))
				put2ObjectMap((Collection) entity);
	}

	private <T> void put2ObjectMap(T[] entities) {
		for (Object e : entities)
			put2ObjectMap(e);
	}

	private <T> void put2ObjectMap(Collection<T> entities) {
//		for (Iterator i$ = entities.iterator(); i$.hasNext();) {
//			Object e = i$.next();
//			put2ObjectMap(e);
//		}
		
		for(T e : entities) {
			put2ObjectMap(e);
		}
	}

	private <T> void put2ObjectMap(T entity) {
		Class clazz = entity.getClass();
		ConcurrentHashSet objs = DB_OBJECT_MAP.get(clazz);
		if (objs == null) {
			DB_OBJECT_MAP.putIfAbsent(clazz, new ConcurrentHashSet());
			objs = (ConcurrentHashSet) DB_OBJECT_MAP.get(clazz);
		}
		objs.add(entity);
	}
	
	@Override
	public void flush() {
		while (DB_POOL_SERVICE != null) {
			if (DB_POOL_SERVICE.isShutdown()) {
				break;
			}
			if (!DB_OBJECT_MAP.isEmpty()) {//将缓存数据冲入DB
				updateIntimeCachedDbObject();
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				LOGGER.error("{}", e);
			}

			if (DB_SAVER_QUEUE.isEmpty())
				try {
					DB_POOL_SERVICE.shutdown();
					DB_POOL_SERVICE.awaitTermination(3000L, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					LOGGER.error("{}", e);
				}
		}
	}

	private void add2Queue(DbCallback callback, EntityCache entityCache) {
		DB_SAVER_QUEUE.add(createTask(null, entityCache));
	}

	public void updateEntityIntime(Object... entities) {
		if (entities.length > 0)
			handleTask(null, new EntityCache(entities));
	}

	public void updateEntityIntime(DbCallback callback, Object... entities) {
		handleTask(callback, new EntityCache(entities));
	}

	public void onApplicationEvent(ContextClosedEvent event) {
		submitCachedDbObject();
	}

	private class ShutdownHookRunnable implements Runnable {
		private ShutdownHookRunnable() {
		}

		public void run() {
			flush();
		}
	}

	public static class EntityCache<T> {
		private Collection<T> entities;
		private int retryCount;

		public Collection<T> getEntities() {
			return this.entities;
		}

		public int getRetryCount() {
			return this.retryCount;
		}

		public EntityCache(T... entities) {
			this.entities = new ArrayList<T>(entities.length);
			for (T entity : entities)
				if ((entity instanceof BaseModel))
					this.entities.add(entity);
				else if ((entity instanceof Collection))
					this.entities.addAll((Collection<T>)entity);
				else
					for (T ent : (T[]) entity) {
						this.entities.add(ent);
					}
		}

		public EntityCache(Collection<T> entities) {
			this.entities = new ArrayList<T>(entities);
		}
	}

	@Override
	public String getName() {
		return "dbservice";
	}

	@Override
	public void stop() throws Exception {
		this.flush();
		super.stop();
	}

	@Override
	public String getStatusDesc() {
		StringBuilder sb = new StringBuilder();
		sb.append("DB_OBJECT_MAP(cache):" + DB_OBJECT_MAP.size()+"\r\n");
		sb.append("DB_SAVER_QUEUE(updating):" + DB_SAVER_QUEUE.size());
		return sb.toString();
	}
	
}