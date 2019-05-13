package com.buding.common.cache;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.serialize.Serializer;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class RedisCacheClient implements CacheClient {
	@Autowired
	RedisClient redisClient;
	
	Serializer serializer;

	@Override
	public void set(String key, Object val) {
		try {
			byte data[] = serializer.serialize(val);
			redisClient.set(key, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object get(String key, Class cls) {
		try {
			byte data[] = redisClient.get(key.getBytes("utf8"));
			return serializer.deserialize(data, cls);			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void hset(String key, String field, Object val) {
		try {
			byte data[] = serializer.serialize(val);
			redisClient.hset(key.getBytes("utf8"), field.getBytes("utf8"), data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object hget(String key, String field, Class cls) {
		try {
			byte data[] = redisClient.hget(key.getBytes("UTF8"), field.getBytes("UTF8"));
			return serializer.deserialize(data, cls);			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void del(String key) {
		try {
			redisClient.del(key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void hdel(String key, String field) {
		try {
			redisClient.hdel(key, field);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
	
}
