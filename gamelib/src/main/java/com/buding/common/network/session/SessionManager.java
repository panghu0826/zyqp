package com.buding.common.network.session;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.buding.common.model.Message;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;

import com.google.gson.GsonBuilder;
import com.google.protobuf.MessageLite;


/**
 * @name session管理类
 * 1.session是与channel(socket连接,此处为长连接)唯一对应的
 * 2.session的id目前设计的是自增长,当然也可以用netty4.3的特性 channel.id(),也是唯一的
 * 3.session中存储了连接信息(channel),玩家信息(id,状态)
 *
 * 用户在连接上服务器后,将用户放入匿名列表(准确定义为:未获取到游戏id的socket连接)中
 * 用户在登录游戏后,将用户放入在线列表(key为游戏id,vaule为对应的session)中
 * 用户与服务器失去连接时(scoket的keep_alive检测强关或者netty的心跳检测超时而关闭),清除该session,同时关闭连接
 * @author jaime qq_1094086610
 */
public abstract class SessionManager<T extends BaseSession> implements InitializingBean, SessionListener<T>, Runnable {
	public static final Logger log = LogManager.getLogger(SessionManager.class);

	// 已登录的用户列表,key为playerId
	private final ConcurrentHashMap<Integer, T> ONLINE_PLAYERID_IOSESSION_MAP = new ConcurrentHashMap<Integer, T>();

	// 未登录的用户列表,key为sessionId
	private final ConcurrentHashMap<Integer, T> ANONYMOUS_IOSESSION_MAP = new ConcurrentHashMap<Integer, T>();

	// 已关闭的SESSSION，如果不重连将被清除
	private final ConcurrentHashMap<Integer, T> SCHEDULE_REMOVE_IOSESSION_MAP = new ConcurrentHashMap<Integer, T>();

	private int maxOnlineCount = 0;

	private int minOnlineCount = 0;

//	SessionDataWriter<byte[]> sessionWriter = new UnCompressBinaryWriter();
//	public LogLevel logLevel = LogLevel.DEBUG;


	 //---------------------------------------------------定时执行清除已经失效的session,同时关闭对应的channel------------------------------------
	@Override
	public void afterPropertiesSet() throws Exception {
		ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
		pool.scheduleAtFixedRate(this, 10, 10, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		try {
			checkInvalidSession();
		} catch (Exception e) {
			log.error("CleanSessionError", e);
		}
	}

	public void checkInvalidSession() {
		for (int userid : SCHEDULE_REMOVE_IOSESSION_MAP.keySet()) {
			T session = SCHEDULE_REMOVE_IOSESSION_MAP.get(userid);
			if (session == null) {
				return;
			}
			if (session.isCanRemove() || session.channel == null) {
				cleanSession(session);
				this.SCHEDULE_REMOVE_IOSESSION_MAP.remove(userid);
			}
			if (session.channel != null && session.channel.isOpen()) {
				session.channel.close();
			}
		}
	}

	protected boolean cleanSession(T session) {
		if (session == ONLINE_PLAYERID_IOSESSION_MAP.get(session.userId)) {
			this.ONLINE_PLAYERID_IOSESSION_MAP.remove(session.userId);
		}

		if (session == ANONYMOUS_IOSESSION_MAP.get(session.sessionId)) {
			this.ANONYMOUS_IOSESSION_MAP.remove(session.sessionId);
		}
		return true;
	}

	//-----------------------------------------------------------------------------------------------------------------------------------



	//---------------------------------------------------登入成功,加入在线列表---------------------------------------------------------------


	public void put2OnlineList(int playerId, T session) {
		if (session == null) return;

		//绑定角色信息(id)
		session.setPlayerId(playerId);

//		Secretkey model = SecretKeyManager.secretMap.get(playerId);
//		if(model != null) {
//			session.setKey(model.getKey());
//			session.setSecretKey(model.getSecretKey());
//		}
		Integer sessionId = session.getSessionId();

		//移除上一状态的map内容
		if (ANONYMOUS_IOSESSION_MAP.containsKey(sessionId)) {
			ANONYMOUS_IOSESSION_MAP.remove(sessionId);
		}

		// 准备移除的会话再次上线的话不用在移除
		if (SCHEDULE_REMOVE_IOSESSION_MAP.contains(playerId)) {
			T oldSession = SCHEDULE_REMOVE_IOSESSION_MAP.get(playerId);
			if (session == oldSession) {
				SCHEDULE_REMOVE_IOSESSION_MAP.remove(playerId);
			}
		}

		//标记session状态
		session.sessionStatus = SessionStatus.VALID;

		//获取之前在线Map中的玩家session,若两个session不一样并且上个session的channel还开着,关掉它(玩家加入在线用户列表前关闭之前的Session)
		T orignSession = ONLINE_PLAYERID_IOSESSION_MAP.put(playerId, session);
		if (orignSession != null && orignSession != session && (orignSession.getChannel() != null && orignSession.getChannel().isOpen())) {
			orignSession.channel.close();
		}

		//更新下最大在线人数
		int onlineCount = getCurrentOnlineCount();
		if (this.maxOnlineCount < onlineCount) {
			this.maxOnlineCount = onlineCount;
		}

	}

	//-----------------------------------------------------------------------------------------------------------------------------------




	//---------------------------------------------------从在线列表中移除,比如登出------------------------------------------------------------

	public void removeFromOnlineList(Integer playerId) {
		T session = (T) ONLINE_PLAYERID_IOSESSION_MAP.remove(playerId);
		if (session == null) return;

		if (session.getChannel().isActive()) {
			ANONYMOUS_IOSESSION_MAP.put(session.getSessionId(), session);
		}

		int onlineCount = getCurrentOnlineCount();
		if (this.minOnlineCount > onlineCount) {
			this.minOnlineCount = onlineCount;
		}
	}

	//-----------------------------------------------------------------------------------------------------------------------------------



	//---------------------------------------------------加入匿名用户列表-------------------------------------------------------------------

	public void put2AnonymousList(T session) {
		if (session != null) {
			ANONYMOUS_IOSESSION_MAP.put(session.getSessionId(), session);
		}
	}

	//-----------------------------------------------------------------------------------------------------------------------------------


	//---------------------------------------------------从匿名用户列表删除-----------------------------------------------------------------

	public void removeFromAnonymousList(int sessionId) {
		if (ANONYMOUS_IOSESSION_MAP.containsKey(sessionId)) {
			ANONYMOUS_IOSESSION_MAP.remove(sessionId);
		}
	}

	//-----------------------------------------------------------------------------------------------------------------------------------


	public void schedule2Remove(T session) {
		if (session == null) return;

		int userId = session.userId;
		if(userId == 0) {
			removeFromAnonymousList(session.sessionId);
			return;
		}

		session.sessionStatus = SessionStatus.INVALID;
		session.planRemoveTime = System.currentTimeMillis();
		SCHEDULE_REMOVE_IOSESSION_MAP.put(userId, session);
	}

	//---------------------------------------------------发送消息------------------------------------------------------------------------

	/**
	 * 发消息基类1
	 * @param session
	 * @param msg
	 */
	public void write(T session, Message msg) {
		if (session == null) return;

		if (session.channel == null || session.channel.isOpen() == false) return;

		if (session.channel.isOpen()) {
			if (msg.getData() != null) session.channel.writeAndFlush(msg);
		} else {
			session.channel.close();
		}
	}

	/**
	 * 发消息基类2
	 * @param session
	 * @param buffer
	 */
	public void write(T session, byte[] buffer) {
		if (session == null) return;

		if (session.channel == null || session.channel.isOpen() == false) return;

//		log.info(String.format("发送数据[playerId:%d, 大小:%d]", new Object[]{playerId, buffer.length}));

		if (session.channel.isOpen() /*&& StringUtils.isNotBlank(session.getSecretKey())*/) {
			if (buffer != null) session.channel.writeAndFlush(new Message(buffer));
		} else {
			session.channel.close();
		}
	}

	public void write(Integer playerId, MessageLite msg) {
		T session = getIoSession(playerId);
		write(session, msg.toByteArray());
	}

	public void write(Collection<Integer> playerIdList, MessageLite msg) {
		if ((playerIdList == null) || (playerIdList.isEmpty())) {
			return;
		}

		byte[] bytes = msg.toByteArray();
		if (bytes == null) {
			return;
		}

		for (Iterator<Integer> i = playerIdList.iterator(); i.hasNext();) {
			// long playerId = ((Long) i.next()).longValue();
			Integer playerId = i.next();
			T session = getIoSession(playerId);
			if (session != null) {
				write(session, bytes);
			}
		}
	}

	//-----------------------------------------------------------------------------------------------------------------------------


	public Set<Integer> getOnlinePlayerIdList() {
		Set<Integer> onLinePlayerIdList = new HashSet<Integer>();
		Set<Integer> onlinePlayerIds = ONLINE_PLAYERID_IOSESSION_MAP.keySet();
		if ((onlinePlayerIds != null) && (!onlinePlayerIds.isEmpty())) {
			onLinePlayerIdList.addAll(onlinePlayerIds);
		}
		return onLinePlayerIdList;
	}

	public int getCurrentOnlineCount() {
		return ONLINE_PLAYERID_IOSESSION_MAP.size();
	}

	public T getIoSession(int playerId) {
		return (T) ONLINE_PLAYERID_IOSESSION_MAP.get(Integer.valueOf(playerId));
	}

	public int getPlayerId(T session) {
		return session.getPlayerId();
	}

	public String getRunThreads() {
		ThreadGroup group = Thread.currentThread().getThreadGroup();  
		ThreadGroup topGroup = group;  
		// 遍历线程组树，获取根线程组  
		while (group != null) {  
		    topGroup = group;  
		    group = group.getParent();  
		}  
		// 激活的线程数加倍  
		int estimatedSize = topGroup.activeCount() * 2;  
		Thread[] slackList = new Thread[estimatedSize];  
		// 获取根线程组的所有线程  
		int actualSize = topGroup.enumerate(slackList);  
		// copy into a list that is the exact size  
		Thread[] list = new Thread[actualSize];  
		System.arraycopy(slackList, 0, list, 0, actualSize);  
		List<String> nameList = new ArrayList<String>();
		System.out.println("Thread list size == " + list.length);  
		for (Thread thread : list) {  
		    nameList.add(thread.getName());  
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Thread list size == " + list.length);
		sb.append("\r\n");
		sb.append(new GsonBuilder().setPrettyPrinting().create().toJson(nameList));
		log.info(sb.toString());
		return sb.toString();
	}

//	public void writeTextWebSocketFrame(Integer playerId, String msg) {
//		T session = getIoSession(playerId);
//
//		writeTextWebSocketFrame(session, msg);
//	}
//
//	public void writeTextWebSocketFrame(T session, String msg) {
//		if (session.channel == null || session.channel.isOpen() == false) {
//			return;
//		}
//
//		if (logLevel.isDebugEnable() || session.logLevel.isDebugEnable()) {
//			log.info("发送网络信息:SessionId:{},Content:{}", session.getSessionId(), msg);
//		}
//
//		try {
//			sessionWriter.write(session, msg);
//		} catch (Exception e) {
//			log.error("SocketWriteError", e);
//		}
//	}
//
//	public void writeTextWebSocketFrame(Integer playerId, String key, Object content) {
//		JSONObject json = new JSONObject();
//		json.put(key, new Gson().toJson(content));
//		String txt = new Gson().toJson(json);
//		writeTextWebSocketFrame(playerId, txt);
//	}
//
//	public void writePbMsg2WebSocket(Integer playerId, String key, MessageLite msg) {
//		ByteBuf writeBytes = new PooledByteBufAllocator().buffer().writeBytes(msg.toByteArray());
//		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(writeBytes);
//
//		T session = getIoSession(playerId);
//		if (session.channel == null || session.channel.isOpen() == false) {
//			return;
//		}
//
//		session.channel.writeAndFlush(frame);
//
//		if (logLevel.isDebugEnable() || session.logLevel.isDebugEnable()) {
//			log.info("发送网络信息:PlayerId:{},Content:{}", playerId, msg.toString());
//		}
//	}
//
//	public void writeTextWebSocketFrame(T session, String key, Object content) {
//		JSONObject json = new JSONObject();
//		json.put(key, new Gson().toJson(content));
//		String txt = new Gson().toJson(json);
//		writeTextWebSocketFrame(session, txt);
//	}

	@Override
	public void sessionInvalided(T session) {
		removeFromAnonymousList(session.getSessionId());
		removeFromOnlineList(session.getSessionId());
	}

	public List<T> getAnonymousSessions() {
		return (List<T>) ANONYMOUS_IOSESSION_MAP.values();
		// return new ArrayList(ANONYMOUS_IOSESSION_MAP.values());
	}

	public T getAnonymousSession(int sessionId) {
		return ANONYMOUS_IOSESSION_MAP.get(sessionId);
	}

	public void writeAllOnline(MessageLite msg) {
		write(ONLINE_PLAYERID_IOSESSION_MAP.keySet(), msg);
	}

	public String getRemoteIp(T session) {
		if (session == null) {
			return "";
		}

		String remoteIp = (String) session.getAttribute(SessionType.REMOTE_HOST_KEY);
		if (StringUtils.isNotBlank(remoteIp)) {
			return remoteIp;
		}
		try {
			remoteIp = ((InetSocketAddress) session.getChannel().remoteAddress()).getAddress().getHostAddress();
			if (StringUtils.isBlank(remoteIp)) {
				remoteIp = ((InetSocketAddress) session.getChannel().localAddress()).getAddress().getHostAddress();
			}
			session.setAttribute(SessionType.REMOTE_HOST_KEY, remoteIp);
		} catch (Exception e) {
			remoteIp = null;
		}
		return StringUtils.defaultIfBlank(remoteIp, "");
	}

	public boolean isOnline(Integer playerId) {
		return ONLINE_PLAYERID_IOSESSION_MAP.containsKey(playerId);
	}

	public int getMaxOnlineCount() {
		return this.maxOnlineCount;
	}

	public void setMaxOnlineCount(int maxOnlineCount) {
		this.maxOnlineCount = maxOnlineCount;
	}

	public int getMinOnlineCount() {
		return this.minOnlineCount;
	}

	public void setMinOnlineCount(int minOnlineCount) {
		this.minOnlineCount = minOnlineCount;
	}

	public void resetOnlineUserCount() {
		int onlineCount = getCurrentOnlineCount();
		this.maxOnlineCount = onlineCount;
		this.minOnlineCount = onlineCount;
	}
}
