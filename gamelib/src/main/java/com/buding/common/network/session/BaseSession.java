package com.buding.common.network.session;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.buding.common.logger.LogLevel;

public abstract class BaseSession {
	private static AtomicInteger idGenerator = new AtomicInteger(1); 
	public int sessionId;
	public long initTime;
	public long lastActiveTime;
	public long planRemoveTime;
	public int userId;	
//	public long key;
//	public String secretKey;
	ConcurrentMap<String, Object> attributeMap =  new ConcurrentHashMap<String, Object>();
	public SessionStatus sessionStatus = SessionStatus.VALID;
	public transient Channel channel;
	public LogLevel logLevel = LogLevel.defLevel;
	
	public BaseSession() {
		this.sessionId = idGenerator.getAndIncrement();
	}
	
	public int getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public long getInitTime() {
		return initTime;
	}

	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}

	public long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public int getPlayerId() {
		return userId;
	}

	public void setPlayerId(int playerId) {
		this.userId = playerId;
	}

//	public String getSecretKey() {
//		return secretKey;
//	}
//
//	public void setSecretKey(String secretKey) {
//		this.secretKey = secretKey;
//	}
//
//	public long getKey() {
//		return key;
//	}
//
//	public void setKey(long key) {
//		this.key = key;
//	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String key) {
		if(attributeMap.containsKey(key)) {
			return (T)attributeMap.get(key);
		}
		return null;
	}
	
	public void setAttributeIfAbsent(String key, Object val) {
		attributeMap.putIfAbsent(key, val);
	}
	
	public void setAttribute(String key, Object val) {
		attributeMap.put(key, val);
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public long getPut2CloseTime() {
		return planRemoveTime;
	}

	public void setPut2CloseTime(long put2CloseTime) {
		this.planRemoveTime = put2CloseTime;
	}

	public abstract boolean isCanRemove();
}
